package gov.nist.mtconnect;

import org.apache.kafka.connect.source.SourceRecord;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TCPSourceTaskTest {
  public static final String IP_ADDRESS = "ip_address";
  public static final String PORT = "port";
  public static final String TOPIC_CONFIG = "topic_config";
  public static final String LINGER_MS = "linger_ms";
  public static final String BATCH_SIZE = "batch_size";
  public static final String SPLIT_SHDR = "split_shdr";
  public static final String MAX_CONNECTION_ATTEMPTS = "max_connection_attempts";
  public static final String TIMEOUT = "timeout";

  @Test
  public void testWithProperties() throws InterruptedException{
    Map<String, String> properties = new HashMap<String, String>();
    properties.put(IP_ADDRESS, "localhost");
    properties.put(PORT, "7878");
    properties.put(BATCH_SIZE, "100");
    properties.put(LINGER_MS, "10000");
    properties.put(TOPIC_CONFIG, "VMC-3Axis_SHDR");
    properties.put(SPLIT_SHDR, "true");
    properties.put(MAX_CONNECTION_ATTEMPTS, "2");
    properties.put(TIMEOUT, "30000");
    TCPSourceTask task = new TCPSourceTask();
    task.start(properties);
    List<SourceRecord> output = task.poll();
    System.out.println(output.get(1).toString());
    task.stop();

  }
}