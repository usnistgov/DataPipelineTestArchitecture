package gov.nist.mtconnect;

import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MTConnectSourceTaskTest {
  @Test
  public void test() {
      MTConnectSourceTask sourceTask = new MTConnectSourceTask();

      try {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("device_url", "http://mtconnect.mazakcorp.com:5609");
        properties.put("last_sequence", "-1");
        properties.put("topic_config", "5609XML");
        sourceTask.start(properties);

      }catch (){
          System.out.println("error in start: ");
      }


      try {
          sourceTask.poll();
      } catch (InterruptedException e) {
          e.printStackTrace();
      }
  }
  }