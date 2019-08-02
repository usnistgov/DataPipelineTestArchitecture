package KafkaApp;


import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Collections;
import java.util.Properties;

/*
 * Creates both the Kafka consumer and Kafka producer used to consume from XML topic and
 * produce to JSON topic. Sets all of the properties for both of them, and can turn the
 * consumer on.
 */
public class KafkaConnector {
    /*
     * Set your server, groupId and topic information accordingly.
     *      bootstrapServer : The address/port for Kafka
     *      groupIdConsumer : The ID of the consumer group.
     *      topicConsumer : The XML topic that MQTT is writing to in Kafka(located in the MQTT connector file).
     *      groupIdProducer : The ID of the producer group.
     *      topicProducer : The JSON topic that Kafka writes the new JSON string to.
     */
    private final Logger logger = LoggerFactory.getLogger(Consumer.class.getName());
    private Properties consumerProperties;
    private Properties producerProperties;
    private KafkaConsumer<String, String> consumer;
    private KafkaProducer<String, String> producer;
    private final String bootstrapServer ="127.0.0.1:29092";
    private final String groupIdConsumer = "test-group";
    private final String topicConsumer = "mqtt-to-kafka";
    private final String groupIdProducer = "json-group";
    private final String topicProducer = "json_topic";

    /*
     * Set all the properties for the producer and consumer.
            Consumer:
                BOOTSTRAP_SERVERS_CONFIG : The Kafka server.
                KEY_DESERIALIZER_CLASS_CONFIG : Deserializes the incoming message's key from bytes into its proper format.
                VALUE_DESERIALIZER_CLASS_CONFIG : Deserializes the incoming message's value from bytes into its proper format.
                GROUP_ID_CONFIG : Sets the consumer's group ID.
                AUTO_OFFSET_RESET_CONFIG : Sets where our offset is located on the topic.
            Producer:
                BOOTSTRAP_SERVERS_CONFIG : The Kafka server.
                KEY_DESERIALIZER_CLASS_CONFIG : Serializes the incoming message's key into bytes.
                VALUE_DESERIALIZER_CLASS_CONFIG : Serializes the incoming message's value into bytes.
     */
    KafkaConnector() {
        this.consumerProperties = new Properties();
        this.consumerProperties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        this.consumerProperties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        this.consumerProperties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        this.consumerProperties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupIdConsumer);
        this.consumerProperties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        this.consumer = new KafkaConsumer<String, String>(consumerProperties);

        this.producerProperties = new Properties();
        this.producerProperties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        this.producerProperties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        this.producerProperties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        this.producer = new KafkaProducer<String, String>(producerProperties);
    }

    Consumer consume(){
        this.consumer.subscribe(Collections.singletonList(topicConsumer));
        return this.consumer;
    }

    /**************************************/
    /* GETTERS AND SETTERS */
    /**************************************/

    public String getTopicProducer() {
        return this.topicProducer;
    }

    public KafkaConsumer<String, String> getConsumer() {
        return this.consumer;
    }

    public KafkaProducer<String, String> getProducer() {
        return this.producer;
    }

}
