package gov.nist.mtconnect;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.errors.ConnectException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterSourceConnectorTest {
  public static final String IP_ADDRESS = AdapterSourceTask.IP_ADDRESS;
  public static final String PORT = AdapterSourceTask.PORT;
  public static final String TOPIC_CONFIG = AdapterSourceTask.TOPIC_CONFIG;
  public static final String LINGER_MS = AdapterSourceTask.LINGER_MS;
  public static final String BATCH_SIZE = AdapterSourceTask.BATCH_SIZE;
  public static final String MAX_CONNECTION_ATTEMPTS = AdapterSourceTask.MAX_CONNECTION_ATTEMPTS;
  public static final String TIMEOUT = AdapterSourceTask.TIMEOUT;

  public static final String TEST_IP_ADDRESS = "127.0.0.1";
  public static final String TEST_PORT = "7878";
  public static final String TEST_TOPIC_CONFIG = "VMC-3Axis_SHDR";
  public static final String TEST_LINGER_MS = "1000";
  public static final String TEST_BATCH_SIZE = "10";
  public static final String TEST_CONNECTION_ATTEMPTS = "3";
  public static final String TEST_TIMEOUT = "60000";

  @Test
  public void testInitialConfigs() {
    Map<String,String> parsedConfigs = new HashMap<>();
    parsedConfigs.put(IP_ADDRESS, TEST_IP_ADDRESS);
    parsedConfigs.put(PORT, TEST_PORT);
    parsedConfigs.put(TOPIC_CONFIG, TEST_TOPIC_CONFIG);
    parsedConfigs.put(LINGER_MS, TEST_LINGER_MS);
    parsedConfigs.put(BATCH_SIZE, TEST_BATCH_SIZE);


    AdapterSourceConnector connector = new AdapterSourceConnector();
    connector.start(parsedConfigs);
    List<Map<String, String>> configs = connector.taskConfigs(2);
    System.out.println(configs.get(0));
  }
  @Test
  public void testWithTasks() throws InterruptedException {
    Map<String,String> parsedConfigs = new HashMap<>();
    parsedConfigs.put(IP_ADDRESS, TEST_IP_ADDRESS);
    parsedConfigs.put(PORT, TEST_PORT);
    parsedConfigs.put(TOPIC_CONFIG, TEST_TOPIC_CONFIG);
    parsedConfigs.put(LINGER_MS, TEST_LINGER_MS);
    parsedConfigs.put(BATCH_SIZE, TEST_BATCH_SIZE);
    parsedConfigs.put(MAX_CONNECTION_ATTEMPTS, TEST_CONNECTION_ATTEMPTS);
    parsedConfigs.put(TIMEOUT, TEST_TIMEOUT);

    MockAdapterService mockAdapterService = new MockAdapterService();
    new Thread(mockAdapterService).start();

    AdapterSourceConnector connector = new AdapterSourceConnector();
    connector.start(parsedConfigs);
    List<Map<String, String>> configs = connector.taskConfigs(2);

    AdapterSourceTask task = new AdapterSourceTask();
    task.start(configs.get(0));
    List<SourceRecord> records = task.poll();
    task.stop();

    for (int i =0 ; i < records.size(); i++){
      System.out.println(records.get(i));
    }
  }
  @Test (expected = ConnectException.class)
  public void testGracefulFail() throws InterruptedException {
    Map<String,String> parsedConfigs = new HashMap<>();
    parsedConfigs.put(IP_ADDRESS, TEST_IP_ADDRESS);
    parsedConfigs.put(PORT, TEST_PORT);
    parsedConfigs.put(TOPIC_CONFIG, TEST_TOPIC_CONFIG);
    parsedConfigs.put(LINGER_MS, TEST_LINGER_MS);
    parsedConfigs.put(BATCH_SIZE, TEST_BATCH_SIZE);
    parsedConfigs.put(MAX_CONNECTION_ATTEMPTS, "4");
    parsedConfigs.put(TIMEOUT, TEST_TIMEOUT);

    MockAdapterService mockAdapterService = new MockAdapterService();
    new Thread(mockAdapterService).start();

    AdapterSourceConnector connector = new AdapterSourceConnector();
    connector.start(parsedConfigs);
    List<Map<String, String>> configs = connector.taskConfigs(2);

    AdapterSourceTask task = new AdapterSourceTask();
    task.start(configs.get(0));
    while(true) {
      List<SourceRecord> records = task.poll();
      if (!records.isEmpty()){System.out.println(records.get(0));}
      //else {task.start(configs.get(0));}
    }
  }
}
