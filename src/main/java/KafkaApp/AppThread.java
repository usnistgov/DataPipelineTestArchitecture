package KafkaApp;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class AppThread {

    public static void main(String[] args) {
        new AppThread().run();
    }

    private AppThread() {

    }

    private void run() {
        Logger logger = LoggerFactory.getLogger(AppThread.class.getName());
        KafkaConnector connector = new KafkaConnector();
        connector.consume();
        KafkaConsumer<String, String> consumer = connector.getConsumer();
        KafkaProducer<String, String> producer = connector.getProducer();

        /*
         * Latch is used to deal with multiple threads.
         */
        CountDownLatch latch = new CountDownLatch(1);

        logger.info("Creating consumer and producer threads.");

        Runnable runnable = new KafkaRunnable(consumer, producer, latch, connector.getTopicProducer());
        Thread runnableThread = new Thread(runnable);
        runnableThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Caught shutdown hook.");
            ((KafkaRunnable) runnable).shutdown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("Application has exited");
        }
        ));

        try {
            latch.await();
        } catch(InterruptedException e) {
            logger.error("Application Interrupted");
        } finally {
            logger.info("application is closing.");
        }

    }
}