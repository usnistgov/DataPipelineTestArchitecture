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

public class KafkaConnector {
    /*
     * Set your server, groupId and topic information accordingly.
     */
    private final Logger logger = LoggerFactory.getLogger(Consumer.class.getName());
    private Properties consumerProperties;
    private Properties producerProperties;
    private KafkaConsumer<String, String> consumer;
    private KafkaProducer<String, String> producer;
    private final String bootstrapServer ="127.0.0.1:29092";
    private final String groupIdConsumer = "my-first-application";
    private final String topicConsumer = "mqtt-to-kafka";
    private final String groupIdProducer = "json-group";
    private final String topicProducer = "json_topic";

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
