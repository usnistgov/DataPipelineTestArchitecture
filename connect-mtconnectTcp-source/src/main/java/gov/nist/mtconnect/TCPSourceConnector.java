package gov.nist.mtconnect;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TCPSourceConnector extends SourceConnector {
  public static final String IP_ADDRESS = TCPSourceTask.IP_ADDRESS;
  public static final String PORT = TCPSourceTask.PORT;
  public static final String TOPIC_CONFIG = TCPSourceTask.TOPIC_CONFIG;
  public static final String LINGER_MS = TCPSourceTask.LINGER_MS;
  public static final String BATCH_SIZE = TCPSourceTask.BATCH_SIZE;
  public static final String SPLIT_SHDR = TCPSourceTask.SPLIT_SHDR;
  private static Logger log = LoggerFactory.getLogger(TCPSourceConnector.class);
  private TCPSourceConnectorConfig config;

  @Override
  public String version() {
    return VersionUtil.getVersion();
  }

  @Override
  public void start(Map<String, String> props) {
    log.info("Starting up MTConnect Source Connector");
    config = new TCPSourceConnectorConfig(props);

    //TODO: Add things you need to do to setup your connector.
  }

  @Override
  public Class<? extends Task> taskClass() {
    //TODO: Return your task implementation.
    return TCPSourceTask.class;
  }

  @Override
  public List<Map<String, String>> taskConfigs(int maxTasks) {

    List<Map<String, String>> configs = new ArrayList<>();
    // Define the individual task configurations that will be executed.
    // phrase.split(";");
    String ipAddresses = config.getString(IP_ADDRESS);
    String[]splitIPAddresses = ipAddresses.split(";");
    String ports = config.getString(PORT);
    String[]splitPorts = ports.split(";",-1);
    String topics = config.getString(TOPIC_CONFIG);
    String[]splitTopics = topics.split(";");

    for (int i=0; i < splitIPAddresses.length; i++) {
      Map<String, String> taskConfig = new HashMap<>();
      taskConfig.put(IP_ADDRESS, splitIPAddresses[i]);
      taskConfig.put(PORT, splitPorts[i]);
      taskConfig.put(TOPIC_CONFIG, splitTopics[i]);
      taskConfig.put(LINGER_MS, config.getString(LINGER_MS));
      taskConfig.put(BATCH_SIZE, config.getString(BATCH_SIZE));
      taskConfig.put(SPLIT_SHDR, config.getString(SPLIT_SHDR));
      configs.add(taskConfig);
    }

    return configs;

  }

  @Override
  public void stop() {
    //TODO: Do things that are necessary to stop your connector.
  }

  @Override
  public ConfigDef config() {
    return TCPSourceConnectorConfig.conf();
  }
}
