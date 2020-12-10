
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nyble.api.messages.ConsumerInfoResponse;
import com.nyble.api.messages.ConsumerInfoResponseAdapter;
import junit.framework.TestCase;

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

}