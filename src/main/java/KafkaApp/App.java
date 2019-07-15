package Kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONObject;
import org.json.XML;

import java.time.Duration;


public class App {

    public static void main(String[] args) {

        KafkaConnector connector = new KafkaConnector();
        connector.consume();
        KafkaConsumer<String, String> consumer = connector.getConsumer();
        KafkaProducer<String, String> producer = connector.getProducer();

        /*
         * Poll for new data. While there is new data being consumed, we take the value(the XML String),
         * convert it to a JSONObject, turn that into a String and publish it to a the Producer that
         * is listening for JSON data. The XML data was published to a separate topic. This allows our
         * sinks to listen to separate topics based on the format that works better for them(i.e. MongoDB
         * uses JSON, MySQL uses XML).
         */
        while(true){
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for(ConsumerRecord<String, String> record : records) {
                String xmlString = record.value();
                JSONObject obj = XML.toJSONObject(xmlString);
                String jStr = obj.toString();
                ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(connector.getTopicProducer(), jStr);
                producer.send(producerRecord);
                //logger.info("Key: " + record.key() + ", Value: " + record.value());
                //logger.info("Partition: " + record.partition() + ", Offset: " + record.offset());
            }
            producer.flush();
        }

    }

}
