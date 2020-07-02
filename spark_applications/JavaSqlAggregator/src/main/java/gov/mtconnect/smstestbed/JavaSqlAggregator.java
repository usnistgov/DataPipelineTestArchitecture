package gov.mtconnect.smstestbed;

import java.util.*;

import org.apache.spark.streaming.api.java.*;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.*;
import org.apache.spark.streaming.kafka010.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.*;
import scala.Tuple2;



public class JavaSqlAggregator {
    public static void main() throws InterruptedException {
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", "localhost:9092");
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "use_a_separate_group_id_for_each_stream");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);

        String logFile = "YOUR_SPARK_HOME/README.md"; // Should be some file on your system

        Collection<String> topics = Arrays.asList("VMC-3Axis_SHDR");

        SparkConf conf = new SparkConf().setAppName("MTConnect Aggregator").setMaster("local");
        JavaStreamingContext streamingContext = new JavaStreamingContext(conf, new Duration(1000));


        JavaInputDStream<ConsumerRecord<String, String>> stream =
                KafkaUtils.createDirectStream(
                        streamingContext,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams)
                );

        stream.mapToPair(record -> new Tuple2<>(record.key(), record.value()));

       System.out.println("\n Schema of Records");
       stream.print();
    streamingContext.start();
    streamingContext.awaitTermination();
    }


}
