package com.nyble.main;

import com.nyble.api.ConsumersAPI;
import com.nyble.api.messages.ConsumerInfoResponse;
import com.nyble.api.messages.GetConsumerInfoRequest;
import com.nyble.models.consumer.Consumer;
import com.nyble.models.consumer.ConsumerFlag;
import com.nyble.topics.Names;
import com.nyble.topics.consumerAttributes.ConsumerAttributesKey;
import com.nyble.topics.consumerAttributes.ConsumerAttributesValue;
import com.nyble.utils.StringConverter;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.nyble.main.AppSystem.*;

public class ConsumerController {

    final static Logger logger = LoggerFactory.getLogger(ConsumerController.class);
    final static Set<String> attributeToColumnMap = new HashSet<>(Arrays.asList("fullName", "email", "phone", "location",
            "birthDate", "flags", "declaredBrand"));
    private int systemId;
    private int consumerId;

    public ConsumerController(int systemId, int consumerId) {
        this.systemId = systemId;
        this.consumerId = consumerId;
    }

    public void updateConsumer(ConsumersAPI consumersAPI) throws NoSuchAlgorithmException {
        Map<String, String> consumerAttributes = getConsumer(consumersAPI);
            String now = new Date().getTime() + "";
            for (String att : attributeToColumnMap) {
                String value = new StringConverter(consumerAttributes.get(att)).coalesce("").get();
                ConsumerAttributesKey cak = new ConsumerAttributesKey(systemId, consumerId);
                ConsumerAttributesValue cav = new ConsumerAttributesValue(systemId + "", consumerId + "", att,
                        value, now, now);
                App.producerManager.getProducer().send(new ProducerRecord<>(Names.CONSUMER_ATTRIBUTES_TOPIC,
                        cak.toJson(), cav.toJson()));
            }
    }

    public Map<String, String> getConsumer(ConsumersAPI consumersAPI)
            throws NoSuchAlgorithmException {

        Map<String, String> rez = new HashMap<>();
        Properties system;
        if(systemId == Names.RMC_SYSTEM_ID){
            system = rmc;
        }else if (systemId == Names.RRP_SYSTEM_ID){
            system = rrp;
        }else{
            throw new RuntimeException("Unknown system");
        }
        GetConsumerInfoRequest request = new GetConsumerInfoRequest(consumerId+"",
                system.getProperty(CRM_KEY), system.getProperty(EXTERNAL_SYSTEM_ID));
        ConsumerInfoResponse rsp;
        try{
            rsp = consumersAPI.getConsumers(new URI(system.getProperty(BASE_URL)), request);
        }catch(Exception e){
            logger.error("API ERROR: req = {} \nrsp = {}", request.toString(), e.getMessage());
            throw new RuntimeException("API ERROR");
        }

        if(rsp.hasError){
            logger.warn("Consumer system {} id {} has error, message: {}", systemId, consumerId, rsp.raw);
            return rez;
        }

        ConsumerInfoResponse.ConsumerInfo consumer = rsp.consumers[0];

        String firstName = new StringConverter(consumer.getFirstname()).trim().nullIf("").get();
        firstName = new ComparatorUtils(firstName).convertToNull().toUpper().replaceDiacritics()
                .removeExtraCharsAndDuplicates().removeWhiteSpaces()
                .getText();

        String lastName = new StringConverter(consumer.getLastname()).trim().nullIf("").get();
        lastName = new ComparatorUtils(lastName).convertToNull().toUpper().replaceDiacritics()
                .removeExtraCharsAndDuplicates().removeWhiteSpaces()
                .getText();

        String phone = new StringConverter(consumer.getPhone()).trim().nullIf("").get();
        phone = new ComparatorUtils(phone).convertToNull().removeWhiteSpaces().formatAsPhone().getText();

        String email = new StringConverter(consumer.getEmail()).trim().nullIf("").get();
        email = new ComparatorUtils(email).convertToNull().toUpper()
                .removeWhiteSpaces()
                .getText();

        String city = new StringConverter(consumer.getCity()).trim().nullIf("").get();
        city = new ComparatorUtils(city).convertToNull().toUpper().replaceDiacritics()
                .removeExtraCharsAndDuplicates().removeWhiteSpaces()
                .getText();
        String county = new StringConverter(consumer.getCounty()).trim().nullIf("").get();
        county = new ComparatorUtils(county).convertToNull().toUpper().replaceDiacritics()
                .removeExtraCharsAndDuplicates().removeWhiteSpaces()
                .getText();
        String street = new StringConverter(consumer.getStreet_address()).trim().nullIf("").get();
        street = new ComparatorUtils(street).convertToNull().toUpper().replaceDiacritics()
                .removeExtraCharsAndDuplicates().removeWhiteSpaces()
                .getText();

        String birthDate = new StringConverter(consumer.getBirthdate()).trim().nullIf("").get();
        birthDate = new ComparatorUtils(birthDate).convertToNull().getText();


        rez.put("flags", calculateNewFlags(consumer));
        rez.put("declaredBrand", consumer.getDeclaredProduct());
        rez.put("fullName", ComparatorUtils.getFullName(firstName, lastName));
        rez.put("location", hash(ComparatorUtils.getLocation(county, city, street)));
        rez.put("phone", hash(phone));
        rez.put("email", hash(email));
        rez.put("birthDate", hash(birthDate));

        rez.put("location_clear", ComparatorUtils.getLocation(county, city, street));
        rez.put("phone_clear", phone);
        rez.put("email_clear", email);
        rez.put("birthDate_clear", birthDate);
        return rez;
    }

    private String calculateNewFlags(ConsumerInfoResponse.ConsumerInfo consumer) {
        Consumer aux = new Consumer();
        /*
        OPT_IN_SMS(0),
    OPT_IN_EMAIL(1),
    OPT_IN_POSTALADDRESS(2),
    OPT_IN_MARKET_ANALYSIS(3),
    OPT_IN(4),
    IS_ACTIVE(5),
    IS_PHONE_VALID(6),
    IS_EMAIL_VALID(7),
    WEB_USER(8),
    WEB_ACCOUNT_BANNED(9),
    GDPR_APPROVAL(10);
         */
        if(consumer.getOptInSms()) aux.setFlag(ConsumerFlag.OPT_IN_SMS);
        if(consumer.getOptInEmail()) aux.setFlag(ConsumerFlag.OPT_IN_EMAIL);
        if(consumer.getOptInPostalAddress()) aux.setFlag(ConsumerFlag.OPT_IN_POSTALADDRESS);
        if(consumer.getOptInMarketAnalysis()) aux.setFlag(ConsumerFlag.OPT_IN_MARKET_ANALYSIS);
        if(consumer.getOptIn()) aux.setFlag(ConsumerFlag.OPT_IN);
        if(consumer.isActive()) aux.setFlag(ConsumerFlag.IS_ACTIVE);
        if(consumer.isPhoneValid()) aux.setFlag(ConsumerFlag.IS_PHONE_VALID);
        if(consumer.isEmailValid()) aux.setFlag(ConsumerFlag.IS_EMAIL_VALID);
        if(consumer.isWebUser()) aux.setFlag(ConsumerFlag.WEB_USER);
        if(consumer.getWebAccountBanned()) aux.setFlag(ConsumerFlag.WEB_ACCOUNT_BANNED);
        if(consumer.getGdprApproval()) aux.setFlag(ConsumerFlag.GDPR_APPROVAL);
        if(consumer.smsConfirmed()) aux.setFlag(ConsumerFlag.SMS_CONFIRMED);
        if(consumer.emailConfirmed()) aux.setFlag(ConsumerFlag.EMAIL_CONFIRMED);

        return aux.getValue("flags");
    }

    private String hash(String value) throws NoSuchAlgorithmException {
        if(value == null){return null;}
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
        return hexify(hash);
    }

    private String hexify(byte[] hash){
        StringBuilder sb = new StringBuilder();
        for (byte b: hash) {
            int unsigned = (int) b & 0xff;
            String hex = Integer.toHexString(unsigned);
            if (hex.length() % 2 == 1) {
                hex = "0" + hex;
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
