package gov.nist.mtconnect;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TCPSourceConnectorTest {
  public static final String IP_ADDRESS = TCPSourceTask.IP_ADDRESS;
  public static final String PORT = TCPSourceTask.PORT;
  public static final String TOPIC_CONFIG = TCPSourceTask.TOPIC_CONFIG;
  public static final String LINGER_MS = TCPSourceTask.LINGER_MS;
  public static final String BATCH_SIZE = TCPSourceTask.BATCH_SIZE;
  public static final String SPLIT_SHDR = TCPSourceTask.SPLIT_SHDR;

  public static final String TEST_IP_ADDRESS = "127.0.0.1";
  public static final String TEST_PORT = "7878";
  public static final String TEST_TOPIC_CONFIG = "VMC-3Axis_SHDR";
  public static final String TEST_LINGER_MS = "10000";
  public static final String TEST_BATCH_SIZE = "100";
  public static final String TEST_SPLIT_SHDR = "true";

  @Test
  public void testInitialConfigs() {
    Map<String,String> parsedConfigs = new HashMap<>();
    parsedConfigs.put(IP_ADDRESS, TEST_IP_ADDRESS);
    parsedConfigs.put(PORT, TEST_PORT);
    parsedConfigs.put(TOPIC_CONFIG, TEST_TOPIC_CONFIG);
    parsedConfigs.put(LINGER_MS, TEST_LINGER_MS);
    parsedConfigs.put(BATCH_SIZE, TEST_BATCH_SIZE);
    parsedConfigs.put(SPLIT_SHDR, TEST_SPLIT_SHDR);


    TCPSourceConnector connector = new TCPSourceConnector();
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
    parsedConfigs.put(SPLIT_SHDR, TEST_SPLIT_SHDR);


    TCPSourceConnector connector = new TCPSourceConnector();
    connector.start(parsedConfigs);
    List<Map<String, String>> configs = connector.taskConfigs(2);

    TCPSourceTask task = new TCPSourceTask();
    task.start(configs.get(0));
    task.poll();
    task.stop();
  }
}
