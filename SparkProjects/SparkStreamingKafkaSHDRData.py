
"""
 Consumes SHDR data messages from VMC-3Axis_SHDR topic in Kafka and parses them using JSON schema to extract key, values, timestamps.
 
 Usage: SparkStreamingKafkaSHDRData.py <bootstrap-servers> <subscribe-type> <topics>
   <bootstrap-servers> The Kafka "bootstrap.servers" configuration. A
   comma-separated list of host:port.
   <subscribe-type> There are three kinds of type, i.e. 'assign', 'subscribe',
   'subscribePattern'.
   |- <assign> Specific TopicPartitions to consume. Json string
   |  {"topicA":[0,1],"topicB":[2,4]}.
   |- <subscribe> The topic list to subscribe. A comma-separated list of
   |  topics.
   |- <subscribePattern> The pattern used to subscribe to topic(s).
   |  Java regex string.
   |- Only one of "assign, "subscribe" or "subscribePattern" options can be
   |  specified for Kafka source.
   <topics> Different value format depends on the value of 'subscribe-type'.

 Running our spark program to process SHDR data

    `$ ./bin/spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.0.0-preview2,org.apache.spark:spark-token-provider-kafka-0-10_2.12:3.0.0-preview2 \
        ../DataPipelineTestArchitecture/SparkStreamingKafkaSHDRData.py localhost:9092 subscribe VMC-3Axis_SHDR
"""
from __future__ import print_function

import sys

from pyspark.sql import SparkSession
from pyspark.sql.functions import split 
from pyspark.sql.functions import from_json, col

from pyspark.sql.types import *
from pyspark.sql.functions import *      # for window() function



if __name__ == "__main__":
    if len(sys.argv) != 4:
        print("""
        Usage: SparkStreamingKafkaSHDRData.py <bootstrap-servers> <subscribe-type> <topics>
        """, file=sys.stderr)
        sys.exit(-1)

    bootstrapServers = sys.argv[1]
    subscribeType = sys.argv[2]
    topics = sys.argv[3]

    # Start the Spark session
    spark = SparkSession\
        .builder\
        .appName("StructuredKafkaWordCount")\
        .getOrCreate()

    # Define the input data json schema needed to interpret the "value" in kafka stream
    # note: "schema", "payload" here must match the names within the value data. otherwise it won't work
    # this is probably not ideal as it is hardcoded. Maybe we can load a small batch of data and first infer schema
    jsonSchema = StructType().add("schema", StringType())\
                             .add("payload", StringType())
    print("\njsonSchema")
    print(jsonSchema)

    # Create DataSet representing the stream of input lines from kafka
    lines = spark\
        .readStream\
        .format("kafka")\
        .option("kafka.bootstrap.servers", bootstrapServers)\
        .option("kafka.batch.size",10)\
        .option(subscribeType, topics)\
        .load()

    print("\nschema of lines")
    lines.printSchema()

    # Create new dataframe using JSON schema to parse key and value
    parsedlines = lines.select( \
      from_json(col("key").cast("string"),jsonSchema).alias("parsedkey"), \
      from_json(col("value").cast("string"),jsonSchema).alias("parsedvalue"), \
      col("topic"), \
      col('offset'), \
      col('timestamp') )

    print("\nschema of parsedlines")
    parsedlines.printSchema() 

    # extract the payload from parsedkey and rename it to "key"
    parsedData = parsedlines.select("parsedkey.payload","parsedvalue","topic","offset","timestamp")
    parsedData = parsedData.withColumnRenamed("payload", "key")

    # extract the payload from parsedvalue and rename it to "value"
    parsedData2 = parsedData.select("key","parsedvalue.payload","topic","offset","timestamp")
    parsedData = parsedData2.withColumnRenamed("payload", "values")

    # Now split the lines in "value" to extract sensor_timestamp and actual value
    split_col = split(parsedData['values'], '\|')
    parsedData = parsedData.withColumn('sensor_timestamp', split_col.getItem(0))
    parsedData = parsedData.withColumn('value', split_col.getItem(2))
    # parsedData = parsedData.drop('value') # drop the original value column now

    # Reorganize dataframe 
    parsedData = parsedData.select('key','value','sensor_timestamp','timestamp','offset','topic' )

    print("\nschema of parsedData")
    parsedData.printSchema()    

    # break out into separate dataframes - need to redo in a better way for it to automatically breakout based on new keys
    Xcom_DF = parsedData.filter(parsedData['key'] == "Xcom")
    print("\nschema of Xcom_DF")
    Xcom_DF.printSchema()    

    Ycom_DF = parsedData.filter(parsedData['key'] == "Ycom")
    Xact_DF = parsedData.filter(parsedData['key'] == "Xact")
    Yact_DF = parsedData.filter(parsedData['key'] == "Yact")
    path_feedrate_DF = parsedData.filter(parsedData['key'] == "path_feedrate")
    line_DF = parsedData.filter(parsedData['key'] == "line")
    block_DF = parsedData.filter(parsedData['key'] == "block")


    # Start running the query that prints the running counts to the console
    query = parsedData\
        .writeStream\
        .outputMode('append')\
        .format('console')\
        .option('truncate', 'false')\
        .start()


    # Write stream to a kafka topic 
    # so far, can only get it to print "lines" , does not work on any other dataframe...
    query_Ycom = parsedData\
        .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)") \
        .writeStream\
        .format('kafka')\
        .option("kafka.bootstrap.servers", "localhost:9092")\
        .option('truncate', 'false')\
        .option("topic", "VMC-3Axis_Ycom")\
        .option("checkpointLocation", "/home/tim/DataPipelineTestArchitecture/checkpoint")\
        .outputMode('append')\
        .start()


    query.awaitTermination()


