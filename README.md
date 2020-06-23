# Creating the Data Pipeline Architecture on Ubuntu

## Under-construction!
- This will become a primer on how-to set up the pipeline, with more extensive documentation coming later.
- Much of the guide will be geared toward dev environments, future guidance on multi-node, distributed implementations
  
## 0) Software Requirements
The following software versions were used for this implementation:
- Ubuntu 18.04
- Apache Kafka 2.5.0: https://www.apache.org/dyn/closer.cgi?path=/kafka/2.5.0/kafka_2.12-2.5.0.tgz
- Apache Spark 3.0.0 pre-built for Hadoop 3.2: https://www.apache.org/dyn/closer.lua/spark/spark-3.0.0/spark-3.0.0-bin-hadoop3.2.tgz
- Apache Hadoop 3.2.1: https://www.apache.org/dyn/closer.cgi/hadoop/common/hadoop-3.2.1/hadoop-3.2.1.tar.gz
- Eclipse Mosquitto 1.6.10: https://mosquitto.org/download/ (https://mosquitto.org/files/source/mosquitto-1.6.10.tar.gz)
- Python 3.6.10: https://www.python.org/downloads/release/python-3610/
  - Python 3.7 messed up ubuntu 18.04
- Java OpenJdk 11: https://openjdk.java.net/projects/jdk/11/
  - Kafka is tested for 14, but Spark is only tested for up to 11.
- Data Pipeline files: - `git clone https://github.com/usnistgov/DataPipelineTestArchitecture`

## 1) Configure the environmental variables
- Complete the `../DataPipelineTestArchitecture/config/pipeline-env.sh` file with absolute paths
- Move the environmental variables file to bash profile
  - `sudo cp ./DataPipelineTestArchitecture/config/pipeline-env.sh /etc/profile.d`
  - make it immediately useful: `source /etc/profile.d/pipeline-env.sh`
  - in the future this can be modifed by `sudo nano /etc/profile.d/pipeline-env.sh`
- Go to $KAFKA/config and open "connect-distributed.properties".
  - e.g. `nano $KAFKA/config/connect-distributed.properties`
  - At the very bottom you'll see "#plugin.path= ...". Remove the # (uncommenting it) and replace with `plugin.path=$KafkaConnectors`.
    - This is telling Kafka connect to look in this folder for connectors.

## 2) Start Kafka
- `sudo bash $DATAPIPELINE/RunKafka.sh`
  - Before you run it for the first time: `sudo chmod -x $DATAPIPELINE/RunKafka.sh`
  - While you're doing that, do it for the shutdown script too: `sudo chmod -x $DATAPIPELINE/KillKafka.sh`

## 3) Edit the connectors config files
- In `$Kafka/config`, open `connect-mtconnect-source.properties`
  - `nano $KafkaConfig/connect-mtconnect-source.json`
- Edit the agent url, path information, and destination topic (multiple agents can be added, separated by semicolons)
  - The example agent is from the Mazak testbed. For example:
  - `agent_url = http://mtconnect.mazakcorp.com:5612`
  - `device_path = path=//Device[@name=\"Mazak\"]` (notice the escape character \")
  - `topic_config = M80104K162N_XML`
- Note: If path is empty, the connector will grab the whole response document
- Note2: I've been naming the topics, by the deviceID plus the data format; more guidance on naming topics coming in the future

## 5) Start MTConnect Agent connector
- This connector will collect the MTConnect XML Response document and store it in the specified topic
- Add a topic with topic name corresponding to the `connect-mtconnect-source.properties` file
  - `bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic M80104K162N_XML`
- Start the mtconnect connector, by running `bin/connect-standalone.sh config/connect-standalone.properties config/connect-mtconnect-source.properties`
- watch your data stream into kafka for hours on end
  - `bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic M80104K162N_XML --from-beginning`
- Use `ctrl+c` to kill the process
  - Note: it'll run your battery down if you leave it running for too long
  
## 6) Start MTConnect Adapter connector
- This connector will collect the raw SHDR output from an MTConnect adapter
- This step assumes that you have an adapter to test with, or have installed the adapter simulator found here:
  - http://mtcup.org/wiki/Installing_C%2B%2B_Agent_on_Ubuntu
  - Once you do, you can start the adapter:
    - via systemctl: `sudo systemctl start mtc_adapter`
    - or `/usr/bin/ruby /etc/mtconnect/adapter/run_scenario.rb -l /etc/mtconnect/adapter/VMC-3Axis-Log.txt`
    - First is easier, but the second approach allows you to swap out the log file more easily
- Edit the `connect-mtconnectTCP-source.properties` file
- Add the topic
- Start the connector: `bin/connect-standalone.sh config/connect-standalone.properties config/connect-mtconnectTCP-source.properties`
- **Note**: if you want to run both connectors at the same time, you need to start them at the same time (using the same the Connect instance)
  - For example: `bin/connect-standalone.sh config/connect-standalone.properties config/connect-mtconnect-source.properties config/connect-mtconnectTCP-source.properties`
- watch your data stream into kafka for hours on end
  - `bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic VMC-3Axis_SHDR --from-beginning
- As always, use `ctrl+c` to kill the process

## 7) Managing the connectors via the REST API
- Kafka Connect connectors can be managed via a REST API. Some of these steps will look familiar, but we'll be using the `connect-distributed` script and the curl statements will be POSTing .json files instead of .properties files.
  - Documentation can be found here: https://kafka.apache.org/documentation/#connect_rest
- Go to ./Kafka/kafka_2.12-2.5.0/config and open "connect-distributed.properties".
  - At the very bottom you'll see "#plugin.path= ...". Remove the # (uncommenting it) and put the full path of the "connectors" folder.    
  - For example: `plugin.path = /home/tim/Kafka/kafka_2.12-2.5.0/connectors`.
- Copy the .json files from `./DataPipelineTestArchitecture/config` to `./Kafka/kafka_2.12-2.5.0/config`
  - `cp ./DataPipelineTestArchitecture/config/connect-mtconnect-source.json ./Kafka/kafka_2.12-2.5.0/config`
  - `cp ./DataPipelineTestArchitecture/config/connect-mtconnectAdapter-source.json ./Kafka/kafka_2.12-2.5.0/config`
- With **Zookeeper** and **Kafka** already running, open another terminal tab and start distributed connect
  - `bin/connect-distributed.sh config/connect-distributed.properties`
- The REST API will be available by default at `http://localhost:8083`
- To start a connector:
  - `curl -d @config/connect-mtconnect-source.json -H "Content-Type: application/json" -X POST http://localhost:8083/connectors`
  - You can see what has been processed: `bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic M80104K162N_XML --from-beginning`
- Other helpful curl commands:
  - Pause a connector: `curl -X PUT http://localhost:8083/connectors/mtconnect-source-connector/pause`
  - Delete a *paused* connector: `curl -X DELETE http://localhost:8083/connectors/mtconnect-source-connector`
  - See what connectors are running: `curl -X GET http://localhost:8083/connectors`
  - Note1: You can only delete a connector that has been paused
  - Note2: The name of the connector is provided in the .json file, not the name of the .json file  
  
## 8) MQTT: Installing Eclipse Mosquitto, connecting it to Kafka, and connecting it to a sensor.
- Next on the to-do list (writing the documentation)


## 9) Set up Spark and Run Streaming Application

- Download Spark from: https://spark.apache.org/downloads.html
  - I downloaded `spark-2.4.5-bin-hadoop2.7`

- Spark needs JDK version 8. So first check if you have that installed with the following command:
  - $ `/usr/libexec/java_home -V`
  - This should list all versions of JDK
  - Or you can specifically check if version 8 is available via:
  - $ `/usr/libexec/java_home -v 1.8`
  - If it's not available, download from: https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html

- Now we need to make sure that JDK 8 is set as the default one:
  - $ `java -version`
  - If this does not give version 8, we need to update the default one to be version 8. Use following command
  - $ export JAVA_HOME=`/usr/libexec/java_home -v 1.8`  - this will only modify it for that terminal session
  - To permanently change though, update your bash profile and add that command at the top
  - $ `nano ~/.bash_profile` 
  - Save and close your bash_profile
  - Now run this command, which keeps the variable persistant when you open a new bash window 
  - $ `source ~/.bash_profile` 


- Go to the spark-2.4.5-bin-hadoop2.7/conf folder and modify the log4j.properties.template file 
  - Replace all instances of `"INFO"` with `"ERROR"` and save it as .properties file
  - This is to prevent it from printing a bunch of INFO logs everytime you run any script
  
- Download the SparkStreamingKafkaSHDRData.py file
  - In line 143, edit the path to your checkpoint directory. Simply create a new empty folder and provide path to it.
  
- Deploy the Spark Streaming Application
  - With steps 4, 5 and 6 done (kafka, zookeeper, MTConnect Agent Adapter Simulator, and MTConnect Adapter connector all running), in a new terminal tab/window, use spark-submit to launch the spark streaming application. We need to add the `Kafka 0.10+ Source for Structured Streaming` and `Kafka 0.10+ Token Provider for Streaming` dependencies via packages as below 
  - `cd your_path_to_spark_directory/spark-3.0.0-preview2-bin-hadoop2.7`
  - `./bin/spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.0.0-preview2,org.apache.spark:spark-token-provider-kafka-0-10_2.12:3.0.0-preview2 /your_path_to_where_application_is_saved/SparkStreamingKafkaSHDRData.py localhost:9092 subscribe VMC-3Axis_SHDR`
  - * Note: you must provide the correct version of your Spark when you use packages to add kafka dependencies. For me, it was `3.0.0-preview2`
  
- Consume data written to kafka topic by the Spark application
  - Open a new terminal window and cd to your kafka directory
  - `bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic VMC-3Axis_Ycom --from-beginning`

