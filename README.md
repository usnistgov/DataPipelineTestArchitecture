# Creating the Data Pipeline Architecture on Ubuntu

## Under-construction!
- This will become a primer on how-to set up the pipeline, with more extensive documentation coming later.
- Much of the guide will be geared toward dev environments, future guidance on multi-node, distributed implementations
- For the most part, we'll follow the Quick Start guide: https://kafka.apache.org/quickstart
- This was all developed and tested on Ubuntu 18.04. The sample commands assume:
  - You have Kafka 2.5.0 downloaded (see #1) and it's unzipped to your home directory, e.g. `/home/tim/`
  - Same for the DataPipelineTestArchitecture
  - If you have a different version or it's stored in a different directory, hopefully you can adapt the commands below
  - The biggest issue that you'll most likely encounter is executing the commands in the wrong directory

## 1) Download Apache Kafka and this repository
- https://kafka.apache.org/quickstart#quickstart_download
- `git clone https://github.com/usnistgov/DataPipelineTestArchitecture`

## 2) Do some stuff to the directories
- In the folder where you have Kafka (e.g. ./Kafka/kafka_2.12-2.5.0), create a folder called "connectors"
  - `mkdir ./Kafka/kafka_2.12-2.5.0/connectors`
- Copy `mtconnect-source-connector-1.0-SNAPSHOT.jar` from the DataPipelineTestArchitecture/connectors folder, and put it in the "./Kafka/kafka_2.12-2.5.0/connectors" folder
  - For example `cp ./DataPipelineTestArchitecture/connectors/mtconnect-source-connector-1.0-SNAPSHOT.jar ../Kafka/kafka_2.12-2.5.0/connectors`
- Copy `connect-mtconnectTcp-source-1.0-SNAPSHOT.jar` too
- Go to ./Kafka/kafka_2.12-2.5.0/config and open "connect-standalone.properties".
  - At the very bottom you'll see "#plugin.path= ...". Remove the # (uncommenting it) and put the full path of the "connectors" folder.    
  - For example: `plugin.path = /home/tim/Kafka/kafka_2.12-2.5.0/connectors`. This is telling Kafka connect to look in this folder for connectors.
- Copy the .properties files in DataPipelineTestArchitecture/config to ./Kafka/kafka_2.12-2.5.0/config
  - For example `cp ./DataPipelineTestArchitecture/config/connect-mtconnect-source.properties ../Kafka/kafka_2.12-2.5.0/config`

## 3) Edit the .properties files
- In `./Kafka/kafka_2.12-2.5.0/config`, open `connect-mtconnect-source.properties`
- Edit the agent url, path information, and destination topic (multiple agents can be added, separated by semicolons)
  - The example agent is from the Mazak testbed. For example:
  - `agent_url = http://mtconnect.mazakcorp.com:5612`
  - `device_path = path=//Device[@name=\"Mazak\"]` (notice the escape character \")
  - `topic_config = M80104K162N_XML`
- Note: If path is empty, the connector will grab the whole response document
- Note2: I've been naming the topics, by the deviceID plus the data format; more guidance on naming topics coming in the future


## 4) Start Kafka
- You'll need two separate terminal tabs open, and working in the kafka directory
  - For example, `cd ./Kafka/kafka_2.12-2.5.0`
- Start a Zookeeper instance: `bin/zookeeper-server-start.sh config/zookeeper.properties`
- Start a Kafka instance: `bin/kafka-server-start.sh config/server.properties`
- Note: it may be worth creating systemctl scripts to handle this?

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

- 1. Download from: https://spark.apache.org/downloads.html
  - I downloaded spark-3.0.0-preview2-bin-hadoop2.7.tgz

- 2. Go to the spark-3.0.0-preview2-bin-hadoop2.7/conf folder and modify the log4j.properties.template file 
  - Replace all instances of `"INFO"` with `"ERROR"` and save it as .properties file
  - This is to prevent it from printing a bunch of INFO logs everytime you run any script
  
- 3. Deploy the Spark Streaming Application
  - With steps 4, 5 and 6 done (kafka, zookeeper, MTConnect Agent Adapter Simulator, and MTConnect Adapter connector all running), in a new terminal tab/window, use spark-submit to launch the spark streaming application. We need to add the `Kafka 0.10+ Source for Structured Streaming` dependency via packages as below
  
  $ `cd your_path_to_spark_directory/spark-3.0.0-preview2-bin-hadoop2.7
  $ `./bin/spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.0.0-preview2 /your_path_to_where_application_is_saved/SparkStreamingKafkaSHDRData.py localhost:9092 subscribe VMC-3Axis_SHDR
  
  * Note: you must provide the correct version of your Spark when you use packages to add kafka dependencies. For me, it was `3.0.0-preview2
  
- 4. Consume data written to kafka topic by the Spark application
  - Open a new terminal window and cd to your kafka directory
  $ `bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic VMC-3Axis_Ycom --from-beginning

