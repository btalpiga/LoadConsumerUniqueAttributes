package com.nyble;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.nyble.api.ConsumersAPI;
import com.nyble.api.messages.ConsumerInfoResponseAdapter;
import com.nyble.main.ConsumerController;
import com.nyble.managers.ProducerManager;
import com.nyble.topics.Names;
import com.nyble.util.DBUtil;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.httpclient.ApacheHttpClient;
import feign.slf4j.Slf4jLogger;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static com.nyble.main.AppSystem.*;

@SpringBootApplication
public class App {

    public static String KAFKA_CLUSTER_BOOTSTRAP_SERVERS = "10.100.1.17:9093";
    public static Properties producerProperties = new Properties();
    public static  ProducerManager producerManager;

    static void init(){
        producerProperties.put("bootstrap.servers", App.KAFKA_CLUSTER_BOOTSTRAP_SERVERS);
        producerProperties.put("acks", "all");
        producerProperties.put("retries", 5);
        producerProperties.put("batch.size", 16384);
        producerProperties.put("linger.ms", 1);
        producerProperties.put("buffer.memory", 33554432);
        producerProperties.put("key.serializer", StringSerializer.class.getName());
        producerProperties.put("value.serializer", StringSerializer.class.getName());

        producerManager = ProducerManager.getInstance(producerProperties);
    }

    static void cleanResources(ConfigurableApplicationContext ctx,  ScheduledExecutorService scheduler){
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            producerManager.getProducer().flush();
            producerManager.getProducer().close();
            if(ctx.isActive()){
                ctx.close();
            }
            scheduler.shutdown();
            try {
                scheduler.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }

    private static final Logger logger = LoggerFactory.getLogger(App.class);
    final static Slf4jLogger feignLogger = new Slf4jLogger(App.class);


//    public static void main(String[] args){
//        final ConsumersAPI consumersAPI = Feign.builder()
//                .client(new ApacheHttpClient())
//                .encoder(new GsonEncoder())
//                .decoder(new GsonDecoder(Collections.singleton(new ConsumerInfoResponseAdapter())))
//                .logger(feignLogger).logLevel(feign.Logger.Level.FULL)
//                .target(ConsumersAPI.class, rmc.getProperty(BASE_URL));
//
//        GetConsumerInfoRequest request = new GetConsumerInfoRequest("12170339",
//                rmc.getProperty(CRM_KEY), rmc.getProperty(EXTERNAL_SYSTEM_ID));
//        ConsumerInfoResponse rsp;
//        try{
//            rsp = consumersAPI.getConsumers(new URI(rmc.getProperty(BASE_URL)), request);
//            System.out.println(rsp.consumers[0].getGdprApproval());
//        }catch(Exception e){
//            logger.error("API ERROR: req = {} \nrsp = {}", request.toString(), e.getMessage());
//            throw new RuntimeException("API ERROR");
//        }
//    }

    public static void main(String[] args){

        init();
        final ConsumersAPI consumersAPI = Feign.builder()
                .client(new ApacheHttpClient())
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder(Collections.singleton(new ConsumerInfoResponseAdapter())))
                .logger(feignLogger).logLevel(feign.Logger.Level.BASIC)
                .target(ConsumersAPI.class, rmc.getProperty(BASE_URL));

        if(args.length == 1){
            File consumers = new File(args[0]);
            logger.info("Run from file: {}", args[0]);
            try {
                runFromFile(consumersAPI, consumers);
            } catch (IOException | NoSuchAlgorithmException e) {
                logger.error(e.getMessage(), e);
            }
        }else{
            ConfigurableApplicationContext ctx = SpringApplication.run(App.class, args);
            logger.info("Running at fixed rate, from 30 to 30 min");
            final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            final Runnable task = () -> {
                try {
                    getUpdatedConsumers(consumersAPI, Names.RMC_SYSTEM_ID);
                } catch (Exception e) {
                    logger.error("RMC updating error: "+e.getMessage(), e);
                }

                try{
                    getUpdatedConsumers(consumersAPI, Names.RRP_SYSTEM_ID);
                }catch (Exception e) {
                    logger.error("RRP updating error: "+e.getMessage(), e);
                }
            };

            /*
             * scheduleAtFixedRate(Runnable command, long initialDelay, long delay, TimeUnit unit):
             * Executes a periodic task after an initial delay, then repeat after every given period.
             * If any execution of this task takes longer than its period, then subsequent executions may start late,
             * but will not concurrently execute.
             */
            long initialDelay = 0;
            long constantDelay = 30;
            scheduler.scheduleAtFixedRate(task, initialDelay, constantDelay, TimeUnit.MINUTES);
            cleanResources(ctx, scheduler);
        }
    }

    public static void runFromFile(ConsumersAPI consumersAPI, File consumers)
            throws IOException, NoSuchAlgorithmException {
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(consumers))){
            String line;
            while((line = bufferedReader.readLine()) != null){
                String[] cells = line.split(",");
                int systemId = Integer.parseInt(cells[0]);
                int consumerId = Integer.parseInt(cells[1]);
                ConsumerController cc = new ConsumerController(systemId, consumerId);
                cc.updateConsumer(consumersAPI);
            }
        }
    }

    public static void getUpdatedConsumers(ConsumersAPI consumersAPI, int systemId)
            throws SQLException, NoSuchAlgorithmException {
        String lastRunAt = getLastRunTimestamp(systemId);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -10);
        String newLastRun = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime());
        final String query = String.format("select id from consumer " +
                "where date_update > '%s' and date_update<='%s'", lastRunAt, newLastRun);
        logger.debug("RMC Query: {}", query);
        Properties system;
        if(systemId == Names.RMC_SYSTEM_ID){system = rmc;}
        else if(systemId == Names.RRP_SYSTEM_ID){system = rrp;}
        else{ throw new RuntimeException("Unknown system");}

        logger.info("Start ingesting consumers on systemId {}, query: {}", systemId, query);
        int consumers = 0;
        try(Connection conn = DBUtil.getInstance().getConnection(system.getProperty(SOURCE_JDBC_CONN_NAME));
            Statement st = conn.createStatement()){
            st.setFetchSize(1000);

            ResultSet rs = st.executeQuery(query);
            while(rs.next()){
                consumers++;
                int consumerId = rs.getInt(1);
                ConsumerController cc = new ConsumerController(systemId, consumerId);
                cc.updateConsumer(consumersAPI);
            }
        }
        logger.info("Ingested {} consumers on systemId {}", consumers, systemId);
        updateLastRunTimestamp(systemId, newLastRun);
    }

    private static void updateLastRunTimestamp(int systemId, String newLastRun) throws SQLException {
        String key;
        if(systemId == Names.RMC_SYSTEM_ID){
            key = "POLL_CONSUMER_ATTRS_LAST_RUN_RMC";
        }else if(systemId == Names.RRP_SYSTEM_ID){
            key = "POLL_CONSUMER_ATTRS_LAST_RUN_RRP";
        }else{
            throw new RuntimeException("Unknown system id");
        }
        final String query = String.format("update config_parameters set value = '%s' " +
                "where key = '%s'", newLastRun, key);
        try(Connection conn = DBUtil.getInstance().getConnection("datawarehouse");
            Statement st = conn.createStatement()){

            st.executeUpdate(query);
        }
    }

    private static String getLastRunTimestamp(int systemId) throws SQLException {
        String key;
        if(systemId == Names.RMC_SYSTEM_ID){
            key = "POLL_CONSUMER_ATTRS_LAST_RUN_RMC";
        }else if(systemId == Names.RRP_SYSTEM_ID){
            key = "POLL_CONSUMER_ATTRS_LAST_RUN_RRP";
        }else{
            throw new RuntimeException("Unknown system id");
        }
        final String query = String.format("select value from config_parameters where key = '%s'",
                key);
        try(Connection conn = DBUtil.getInstance().getConnection("datawarehouse");
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query)){

            if(rs.next()){
                return rs.getString(1);
            }else{
                throw new RuntimeException("No last run time for systemId "+systemId);
            }
        }
    }


}
