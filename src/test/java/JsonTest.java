
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nyble.App;
import com.nyble.api.messages.ConsumerInfoResponse;
import com.nyble.api.messages.ConsumerInfoResponseAdapter;
import com.nyble.main.ComparatorUtils;
import com.nyble.topics.Names;
import com.nyble.topics.consumerAttributes.ConsumerAttributesKey;
import com.nyble.topics.consumerAttributes.ConsumerAttributesValue;
import com.nyble.utils.StringConverter;
import junit.framework.TestCase;
import org.apache.kafka.clients.producer.ProducerRecord;

public class JsonTest extends TestCase {

    public void test_jsonObj(){
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ConsumerInfoResponse.class, new ConsumerInfoResponseAdapter())
                .create();

        String json = "{\"error\": true}";
        ConsumerInfoResponse rsp = gson.fromJson(json, ConsumerInfoResponse.class);

        assertTrue(rsp.hasError);
    }

    public void test_jsonArr(){
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ConsumerInfoResponse.class, new ConsumerInfoResponseAdapter())
                .create();

        String json = "[{\"firstname\":\"gigi\", \"lastname\":\"duru\"}, " +
                "{\"firstname\":\"ion\", \"lastname\":\"ionescu\"}]";
        ConsumerInfoResponse rsp = gson.fromJson(json, ConsumerInfoResponse.class);

        assertFalse(rsp.hasError);
        assertEquals(2, rsp.consumers.length);
        assertEquals("gigi", rsp.consumers[0].getFirstname());
        assertEquals("duru", rsp.consumers[0].getLastname());
        assertEquals("ion", rsp.consumers[1].getFirstname());
        assertEquals("ionescu", rsp.consumers[1].getLastname());
    }

    public void test_cornerCaseName(){
        String firstName = new StringConverter("DANIE;A").trim().nullIf("").get();
        firstName = new ComparatorUtils(firstName).convertToNull().toUpper().replaceDiacritics()
                .removeExtraCharsAndDuplicates().removeWhiteSpaces()
                .getText();

        String lastName = new StringConverter("TOMA").trim().nullIf("").get();
        lastName = new ComparatorUtils(lastName).convertToNull().toUpper().replaceDiacritics()
                .removeExtraCharsAndDuplicates().removeWhiteSpaces()
                .getText();

        String expected = "DANIE;ATOMA";
        String actual = ComparatorUtils.getFullName(firstName, lastName);
        System.out.println(actual);
        assertEquals(expected, actual);

        String value = new StringConverter(actual).coalesce("").get();
        ConsumerAttributesValue cav = new ConsumerAttributesValue(0 + "", 0 + "", "fullName",
                value, 0+"", 0+"");
        String attributeJson = cav.toJson();
        System.out.println(attributeJson);
    }

}