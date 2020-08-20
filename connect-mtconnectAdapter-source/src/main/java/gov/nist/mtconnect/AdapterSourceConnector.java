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

public class AdapterSourceConnector extends SourceConnector {
  public static final String IP_ADDRESS = AdapterSourceTask.IP_ADDRESS;
  public static final String PORT = AdapterSourceTask.PORT;
  public static final String TOPIC_CONFIG = AdapterSourceTask.TOPIC_CONFIG;
  public static final String LINGER_MS = AdapterSourceTask.LINGER_MS;
  public static final String BATCH_SIZE = AdapterSourceTask.BATCH_SIZE;
  public static final String MAX_CONNECTION_ATTEMPTS = AdapterSourceTask.MAX_CONNECTION_ATTEMPTS;
  public static final String TIMEOUT = AdapterSourceTask.TIMEOUT;

  private static Logger log = LoggerFactory.getLogger(AdapterSourceConnector.class);
  private AdapterSourceConnectorConfig config;

  @Override
  public String version() {
    return VersionUtil.getVersion();
  }

  @Override
  public void start(Map<String, String> props) {
    log.info("Starting up MTConnect Source Connector");
    config = new AdapterSourceConnectorConfig(props);

    //TODO: Add things you need to do to setup your connector.
  }

  @Override
  public Class<? extends Task> taskClass() {
    //TODO: Return your task implementation.
    return AdapterSourceTask.class;
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
      taskConfig.put(MAX_CONNECTION_ATTEMPTS,config.getString(MAX_CONNECTION_ATTEMPTS));
      taskConfig.put(TIMEOUT, config.getString(TIMEOUT));
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
    return AdapterSourceConnectorConfig.conf();
  }
}
