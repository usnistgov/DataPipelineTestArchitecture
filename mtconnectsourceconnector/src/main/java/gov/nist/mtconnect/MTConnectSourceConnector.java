package gov.nist.mtconnect;

import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MTConnectSourceConnector extends SourceConnector {
  private static Logger log = LoggerFactory.getLogger(MTConnectSourceConnector.class);
  private MTConnectSourceConnectorConfig config;

  @Override
  public String version() {
    return VersionUtil.getVersion();
  }
  @Override
  public void start(Map<String, String> map) {
    log.info("Starting up MTConnect Source Connector");
    config = new MTConnectSourceConnectorConfig(map);

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
    //TODO: Define the individual task configurations that will be executed.

    throw new UnsupportedOperationException("This has not been implemented.");
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
