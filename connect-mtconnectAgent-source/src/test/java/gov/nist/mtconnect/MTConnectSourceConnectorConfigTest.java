package gov.nist.mtconnect;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MTConnectSourceConnectorConfigTest {
  public static final String AGENT_URL = MTConnectSourceTask.AGENT_URL;
  public static final String DEVICE_PATH = MTConnectSourceTask.DEVICE_PATH;
  public static final String TOPIC_CONFIG = MTConnectSourceTask.TOPIC_CONFIG;
  @Test
  public void doc() {
    System.out.println(MTConnectSourceConnectorConfig.conf().toRst());
  }

  @Test
  public void test(){
    Map<String,String> parsedConfigs = new HashMap<>();
    parsedConfigs.put(AGENT_URL, "http://mtconnect.mazakcorp.com:5612");
    parsedConfigs.put(DEVICE_PATH, "path=//Device[@name=\"Mazak\"]");
    parsedConfigs.put(TOPIC_CONFIG, "M80104K162N_XML");

    MTConnectSourceConnectorConfig config = new MTConnectSourceConnectorConfig(parsedConfigs);
    System.out.println(config.values().toString());
  }
  @Test
  public void testMultipleURLs(){
    // NEED to handle situations where no path is provided, return empty string

    Map<String,String> parsedConfigs = new HashMap<>();
    parsedConfigs.put(AGENT_URL, "http://mtconnect.mazakcorp.com:5612;http://mtconnect.mazakcorp.com:5609");
    parsedConfigs.put(DEVICE_PATH, "path=//Device[@name=\"Mazak\"];");
    parsedConfigs.put(TOPIC_CONFIG, "M80104K162N_XML; MAZAK-M77KP290337_XML");

    MTConnectSourceConnectorConfig config = new MTConnectSourceConnectorConfig(parsedConfigs);
    System.out.println(config.values().toString());


  }
}