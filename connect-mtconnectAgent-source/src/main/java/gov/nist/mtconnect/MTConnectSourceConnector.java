package gov.nist.mtconnect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MTConnectSourceConnector extends SourceConnector {
  private static Logger log = LoggerFactory.getLogger(MTConnectSourceConnector.class);
  private MTConnectSourceConnectorConfig configProperties;

  private static final String AGENT_URL = MTConnectSourceTask.AGENT_URL;
  private static final String DEVICE_PATH = MTConnectSourceTask.DEVICE_PATH;
  private static final String TOPIC_CONFIG = MTConnectSourceTask.TOPIC_CONFIG;

  @Override
  public String version() {
    return VersionUtil.getVersion();
  }
  @Override
  public void start(Map<String, String> props) {
    log.info("Starting up MTConnect Source Connector");
    configProperties = new MTConnectSourceConnectorConfig(props);

    //TODO: Add things you need to do to setup your connector.
  }

  @Override
  public Class<? extends Task> taskClass() {
    //TODO: Return your task implementation.
    return MTConnectSourceTask.class;
  }

  @Override
  public List<Map<String, String>> taskConfigs(int maxTasks) {
    //maxTasks shouldn't be greater than the number of Devices that need to be queried
    //Need to add functionality for when maxTasks is less than number of Devices
    List<Map<String, String>> configs = new ArrayList<>();
    //TODO: Define the individual task configurations that will be executed.
    // phrase.split(";");
    String agentURLs = configProperties.getString(AGENT_URL);
    String[]splitAgentURLs = agentURLs.split(";");
    String devicePaths = configProperties.getString(DEVICE_PATH);
    String[]splitDevicePaths = devicePaths.split(";",-1);
    String topics = configProperties.getString(TOPIC_CONFIG);
    String[]splitTopics = topics.split(";");

    for (int i=0; i < splitAgentURLs.length; i++) {
      Map<String, String> taskConfig = new HashMap<>();
      taskConfig.put(AGENT_URL, splitAgentURLs[i]);
      taskConfig.put(DEVICE_PATH, splitDevicePaths[i]);
      taskConfig.put(TOPIC_CONFIG, splitTopics[i]);
      configs.add(taskConfig);
    }
    /*
    System.out.println("Configurations:");
    for( int i = 0; i < configs.size(); i++){
        System.out.println(configs.get(i).toString());
    }*/

    return configs;
  }

  @Override
  public void stop() {
    //TODO: Do things that are necessary to stop your connector.
  }

  @Override
  public ConfigDef config() {
    return MTConnectSourceConnectorConfig.conf();
  }
}
