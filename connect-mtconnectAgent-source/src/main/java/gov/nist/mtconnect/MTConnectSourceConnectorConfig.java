package gov.nist.mtconnect;

import com.sun.org.apache.regexp.internal.RE;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Importance;

import java.util.Map;


class MTConnectSourceConnectorConfig extends AbstractConfig {

  public static final String AGENT_URL_CONFIG = MTConnectSourceTask.AGENT_URL;
  private static final String AGENT_URL_DOC = "This is the URL (IP/Port) of the agent/device to be connected to.";

  public static final String DEVICE_PATH_CONFIG = MTConnectSourceTask.DEVICE_PATH;
  private static final String DEVICE_PATH_DOC = "If there are multiple devices on the same response document, this "
          + "helps to sort them out into different streams."
          + "Otherwise, they will be stored together in the same topic.";

  public static final String TOPIC_CONFIG = MTConnectSourceTask.TOPIC_CONFIG;
  private static final String TOPIC_DOC = "This is the topic that the data will be published to.";

  public static final String REQUEST_INTERVAL = MTConnectSourceTask.REQUEST_INTERVAL;
  private static final String REQUEST_INTERVAL_DOC = "This is the minimum amount of time between requests (in millis)";

  public static ConfigDef conf() {
    return new ConfigDef()
            .define(AGENT_URL_CONFIG, Type.STRING, Importance.HIGH, AGENT_URL_DOC)
            .define(DEVICE_PATH_CONFIG, Type.STRING, Importance.LOW, DEVICE_PATH_DOC)
            .define(TOPIC_CONFIG, Type.STRING, Importance.HIGH, TOPIC_DOC)
            .define(REQUEST_INTERVAL,Type.STRING, "0", Importance.LOW, REQUEST_INTERVAL_DOC);
  }

  public MTConnectSourceConnectorConfig(ConfigDef config, Map<String, String> parsedConfig) {
    super(config, parsedConfig);
  }

  public MTConnectSourceConnectorConfig(Map<String, String> parsedConfig) {
    this(conf(), parsedConfig);
  }
}