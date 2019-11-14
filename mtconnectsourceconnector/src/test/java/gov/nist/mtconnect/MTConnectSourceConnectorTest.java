package gov.nist.mtconnect;

import org.apache.kafka.connect.source.SourceRecord;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

public class MTConnectSourceConnectorTest {
  public static final String AGENT_URL = MTConnectSourceTask.AGENT_URL;
  public static final String DEVICE_PATH = MTConnectSourceTask.DEVICE_PATH;
  public static final String TOPIC_CONFIG = MTConnectSourceTask.TOPIC_CONFIG;

  public static final String TEST_AGENT_URL = "http://mtconnect.mazakcorp.com:5612";
  public static final String TEST_AGENT_MULTIPLE_URLS = "http://mtconnect.mazakcorp.com:5612;http://mtconnect.mazakcorp.com:5609";
  public static final String TEST_DEVICE_PATH = "path=//Device[@name=\"Mazak\"]";
  public static final String TEST_DEVICE_MULTIPLE_PATHS = "path=//Device[@name=\"Mazak\"];path=//Device[@name=\"MFMS10-MC1\"]";
  public static final String TEST_TOPIC_CONFIG = "M80104K162N_XML";
  public static final String TEST_MULTIPLE_TOPICS = "M80104K162N_XML; MAZAK-M77KP290337_XML";


  @Test
  public void test() {
    Map<String,String> parsedConfigs = new HashMap<>();
    parsedConfigs.put(AGENT_URL, TEST_AGENT_URL);
    parsedConfigs.put(DEVICE_PATH, TEST_DEVICE_PATH);
    parsedConfigs.put(TOPIC_CONFIG, TEST_TOPIC_CONFIG);

    MTConnectSourceConnector connector = new MTConnectSourceConnector();
    connector.start(parsedConfigs);
    connector.taskConfigs(2);
  }

  @Test
  public void testMultipleURLs() {
    Map<String,String> parsedConfigs = new HashMap<>();
    parsedConfigs.put(AGENT_URL, TEST_AGENT_MULTIPLE_URLS);
    parsedConfigs.put(DEVICE_PATH, TEST_DEVICE_MULTIPLE_PATHS);
    parsedConfigs.put(TOPIC_CONFIG, TEST_MULTIPLE_TOPICS);

    MTConnectSourceConnector connector = new MTConnectSourceConnector();
    connector.start(parsedConfigs);
    List<Map<String, String>> taskConfigs = connector.taskConfigs(1);
    printTaskConfig(taskConfigs);

  }
  @Test
  public void testNullPath(){
    Map<String,String> parsedConfigs = new HashMap<>();
    parsedConfigs.put(AGENT_URL, TEST_AGENT_MULTIPLE_URLS);
    parsedConfigs.put(DEVICE_PATH, TEST_DEVICE_PATH+";");
    parsedConfigs.put(TOPIC_CONFIG, TEST_MULTIPLE_TOPICS);

    MTConnectSourceConnector connector = new MTConnectSourceConnector();
    connector.start(parsedConfigs);
    List<Map<String, String>> taskConfigs = connector.taskConfigs(2);
    printTaskConfig(taskConfigs);

  }

  @Test
  public void testWithTaskClass() throws InterruptedException {
    Map<String,String> parsedConfigs = new HashMap<>();
    parsedConfigs.put(AGENT_URL, TEST_AGENT_MULTIPLE_URLS);
    parsedConfigs.put(DEVICE_PATH, TEST_DEVICE_MULTIPLE_PATHS);
    parsedConfigs.put(TOPIC_CONFIG, TEST_MULTIPLE_TOPICS);

    MTConnectSourceConnector connector = new MTConnectSourceConnector();
    connector.start(parsedConfigs);
    List<Map<String, String>> taskConfigs = connector.taskConfigs(2);
    printTaskConfig(taskConfigs);

    ArrayList<MTConnectSourceTask> taskList = new ArrayList<MTConnectSourceTask>();

    for (int i =0; i<=1; i++) {
      taskList.add(new MTConnectSourceTask());
      taskList.get(i).start(taskConfigs.get(i));
    }

    for (int i =0; i<=1; i++) {
      List<SourceRecord> sourceRecord = taskList.get(i).poll();
      System.out.println(sourceRecord.toString());
    }
    sleep(1000);
    for (int i =0; i<=1; i++) {
      List<SourceRecord> sourceRecord = taskList.get(i).poll();
      System.out.println(sourceRecord.toString());
    }


    while(true){
      for (int i =0; i<=1; i++) {
        List<SourceRecord> sourceRecord = taskList.get(i).poll();
        System.out.println(sourceRecord.toString());
      }
      //sleep(1000);
    }


  }

  public void printTaskConfig(List<Map<String, String>> taskConfigs){
    System.out.println("Configurations:");
    for( int i = 0; i < taskConfigs.size(); i++){
      System.out.println(taskConfigs.get(i).toString());
    }
  }
}
