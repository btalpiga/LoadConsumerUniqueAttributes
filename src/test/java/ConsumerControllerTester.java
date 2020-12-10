import com.nyble.api.ConsumersAPI;
import com.nyble.api.messages.ConsumerInfoResponse;
import com.nyble.main.ConsumerController;
import junit.framework.TestCase;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static org.mockito.Mockito.*;

public class ConsumerControllerTester extends TestCase {

    @Mock
    ConsumersAPI consumersAPI;

    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    public void test_getConsumer() throws URISyntaxException, NoSuchAlgorithmException {
        ConsumerInfoResponse rsp = new ConsumerInfoResponse();
        rsp.consumers = new ConsumerInfoResponse.ConsumerInfo[]{
                new ConsumerInfoResponse.ConsumerInfo()
        };
        rsp.consumers[0].setFirstname("mihai");
        rsp.consumers[0].setLastname("popescuu");
        rsp.consumers[0].setBirthdate("10-10-1990 12:12");
        rsp.consumers[0].setPhone("0741989547");
        rsp.consumers[0].setEmail("fadfdasdas@wrefevcaf.com");
        rsp.consumers[0].setStreet_address("str. grivitei");
        when(consumersAPI.getConsumers(any(), any())).thenReturn(rsp);

        ConsumerController cc = new ConsumerController(1, 1);
        Map<String, String> rez = cc.getConsumer(consumersAPI);

        assertEquals("MIHAIPOPESCU", rez.get("fullName"));
        assertEquals("0040741989547", rez.get("phone_clear"));
        assertEquals("FADFDASDAS@WREFEVCAF.COM", rez.get("email_clear"));
        assertEquals("STRGRIVITEI", rez.get("location_clear"));
        assertEquals("10-10-1990 12:12", rez.get("birthDate_clear"));
    }
}
