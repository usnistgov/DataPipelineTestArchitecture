package KafkaApp;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

/*
 * Deprecated. Keeping here for now in order to eventually convert some of this data into the
 * KafkaConnector and App class. Uses a CountDownLatch to allow safe exiting of the program.
 */

class KafkaRunnable implements Runnable {

    private CountDownLatch latch;
    private KafkaConsumer<String,String> consumer;
    private KafkaProducer<String,String> producer;
    private final Logger logger = LoggerFactory.getLogger(KafkaRunnable.class.getName());
    private String topicProducer;

    KafkaRunnable(KafkaConsumer<String,String> consumer, KafkaProducer<String,String> producer, CountDownLatch latch, String topicProducer) {
        this.consumer = consumer;
        this.producer = producer;
        this.latch = latch;
        this.topicProducer = topicProducer;
    }

    @Override
    public void run() {
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    String xmlString = record.value();
                    JSONObject obj = XML.toJSONObject(xmlString);
                    String jStr = obj.toString();
                    ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(topicProducer, jStr);
                    System.out.println(jStr);
                    producer.send(producerRecord);
                }
                producer.flush();
            }
        } catch (WakeupException e) {
            logger.info("Received shutdown signal.");
        } finally {
            consumer.close();
            producer.close();
            latch.countDown();
        }
    }

    void shutdown() {
        consumer.wakeup();
    }

}
