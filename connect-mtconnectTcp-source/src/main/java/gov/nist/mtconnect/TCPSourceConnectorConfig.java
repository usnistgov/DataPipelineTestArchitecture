package gov.nist.mtconnect;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;

import java.util.Map;


public class  TCPSourceConnectorConfig extends AbstractConfig {

  public static final String IP_ADDRESS_CONFIG = TCPSourceTask.IP_ADDRESS;
  private static final String IP_ADDRESS_DOC = "This is a setting important to my connector.";

  public static final String PORT_CONFIG = TCPSourceTask.PORT;
  private static final String PORT_DOC = "This is a setting important to my connector.";

  public static final String TOPIC_CONFIG = TCPSourceTask.TOPIC_CONFIG;
  private static final String TOPIC_CONFIG_DOC = "This is a setting important to my connector.";

  public static final String BATCH_SIZE_CONFIG = TCPSourceTask.BATCH_SIZE;
  private static final String BATCH_SIZE_DOC = "This is a setting important to my connector.";

  public static final String LINGER_MS_CONFIG = TCPSourceTask.LINGER_MS;
  private static final String LINGER_MS_DOC = "This is a setting important to my connector.";

  public static final String SPLIT_SHDR_CONFIG = TCPSourceTask.SPLIT_SHDR;
  private static final String SPLIT_SHDR_DOC = "This is a setting important to my connector.";

  public TCPSourceConnectorConfig(ConfigDef config, Map<String, String> parsedConfig) {
    super(config, parsedConfig);
  }

  public TCPSourceConnectorConfig(Map<String, String> parsedConfig) {
    this(conf(), parsedConfig);
  }

  public static ConfigDef conf() {
    return new ConfigDef()
        .define(IP_ADDRESS_CONFIG, Type.STRING, Importance.HIGH, IP_ADDRESS_DOC)
        .define(PORT_CONFIG, Type.STRING, "7878", Importance.HIGH, PORT_DOC)
        .define(TOPIC_CONFIG, Type.STRING, Importance.HIGH, TOPIC_CONFIG_DOC)
        .define(BATCH_SIZE_CONFIG, Type.STRING, "1000", Importance.LOW, BATCH_SIZE_DOC)
        .define(LINGER_MS_CONFIG, Type.STRING, "10000", Importance.LOW, LINGER_MS_DOC)
        .define(SPLIT_SHDR_CONFIG, Type.STRING, "true", Importance.LOW, SPLIT_SHDR_DOC);
  }

}
