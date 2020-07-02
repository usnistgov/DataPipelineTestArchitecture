# Creating the Data Pipeline Architecture on Ubuntu

## Under-construction!
- This will become a primer on how-to set up the pipeline, with more extensive documentation coming later.
- Much of the guide will be geared toward dev environments, future guidance on multi-node, distributed implementations
  
## Software Requirements
The following software versions were used for this implementation:
- Ubuntu 18.04
- Apache Kafka 2.5.0: https://www.apache.org/dyn/closer.cgi?path=/kafka/2.5.0/kafka_2.12-2.5.0.tgz
- Apache Spark 3.0.0 pre-built for Hadoop 3.2: https://www.apache.org/dyn/closer.lua/spark/spark-3.0.0/spark-3.0.0-bin-hadoop3.2.tgz
- Apache Hadoop 3.2.1: https://www.apache.org/dyn/closer.cgi/hadoop/common/hadoop-3.2.1/hadoop-3.2.1.tar.gz
- Eclipse Mosquitto 1.6.10: https://mosquitto.org/download/
  - add mosquitto to repository list: `sudo apt-add-repository ppa:mosquitto-dev/mosquitto-ppa`
  - install mosquitto: `sudo apt-get install mosquitto`
- Python 3.6.10: https://www.python.org/downloads/release/python-3610/
  - Python 3.7 messed up ubuntu 18.04
- Java OpenJdk 11: https://openjdk.java.net/projects/jdk/11/
  - Kafka is tested for 14, but Spark is only tested for up to 11.
  - Can be set in the environmental variables file below or using update-alternatives
- Data Pipeline files: - `git clone https://github.com/usnistgov/DataPipelineTestArchitecture`

## 1) Configure the environmental variables
- Complete the `$PATH/DataPipelineTestArchitecture/config/pipeline-env.sh` file with absolute paths
- Move the environmental variables file to bash profile
  - `sudo cp $PATH/DataPipelineTestArchitecture/config/pipeline-env.sh /etc/profile.d`
  - make it immediately useful: `source /etc/profile.d/pipeline-env.sh`
  - in the future this can be modifed by `sudo nano /etc/profile.d/pipeline-env.sh`
- Go to $KAFKA/config and open "connect-distributed.properties".
  - e.g. `nano $KAFKA/config/connect-distributed.properties`
  - At the very bottom you'll see "#plugin.path= ...". Remove the # (uncommenting it) and replace with `plugin.path=$KafkaConnectors`.
    - This is telling Kafka connect to look in this folder for connectors.

## 2) Start Kafka
- `sudo bash -i $DATAPIPELINE/RunKafka.sh`
  - Before you run it for the first time: `sudo chmod -x $DATAPIPELINE/RunKafka.sh`
  - While you're doing that, do it for the shutdown script too: `sudo chmod -x $DATAPIPELINE/KillKafka.sh`
- Use `sudo bash -i $DATAPIPELINE/KillKafka.sh` to shut them down

## 3) Connecting to MTConnect data sources
- Kafka Connect connectors can be managed via a REST API, using the curl statements to POST .json config files.
   - Documentation can be found here: https://kafka.apache.org/documentation/#connect_rest
- The REST API will be available by default at `http://localhost:8083`
### 3a) Connecting to an MTConnect Agent
- This connector will collect the MTConnect XML Response document and store it in the specified topic
- In `$Kafka/config`, open `connect-mtconnect-source.json`
  - `nano $KafkaConfig/connect-mtconnect-source.json`
- Edit the agent url, path information, and destination topic (multiple agents can be added, separated by semicolons)
  - The example agent is from the Mazak testbed. For example:
  - `"agent_url" : "http://mtconnect.mazakcorp.com:5612"`
  - `"device_path" : "path=//Device[@name=\"Mazak\"]"` (notice the escape character \")
  - `"topic_config" : "M80104K162N_XML"`
  - Note: If path is empty, the connector will grab the whole response document
- Add a topic with topic name corresponding to the `connect-mtconnect-source.json` file
  - `$KAFKA/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic M80104K162N_XML`
- Start the mtconnect connector using the REST API
  - `curl -d @$KafkaConfig/connect-mtconnect-source.json -H "Content-Type: application/json" -X POST http://localhost:8083/connectors`
- watch your data stream into kafka for hours on end
  - `$KAFKA/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic M80104K162N_XML --from-beginning`
- *Note: * You'll need to create and configure a separate .json file for each set of agents you want to connect to
  - It may make sense to configure a separate .json for each agent (tbd how to manage the config files)
  

### 3b) Connecting to MTConnect Adapter
- This connector will collect the raw SHDR output from an MTConnect adapter
- This step assumes that you have an adapter to test with, or have installed the adapter simulator found here:
  - http://mtcup.org/wiki/Installing_C%2B%2B_Agent_on_Ubuntu
  - Once you do, you can start the adapter:
    - `/usr/bin/ruby /etc/mtconnect/adapter/run_scenario.rb -l /etc/mtconnect/adapter/VMC-3Axis-Log.txt`
- Edit the `$KafkaConfig/connect-mtconnectAdapter-source.json` file
  - `"ip_address": "127.0.0.1",` (this is localhost for the adapter simulation)
  - `"port": "7878",` (this is the default port for adapters)
  - `"topic_config": "VMC-3Axis_SHDR",`
- Add the topic
  - `$KAFKA/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic VMC-3Axis_SHDR`
- Start the connector: 
  - `curl -d @$KafkaConfig/connect-mtconnectAdapter-source.json -H "Content-Type: application/json" -X POST http://localhost:8083/connectors`
- watch your data stream into kafka for hours on end
  - `$KAFKA/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic VMC-3Axis_SHDR --from-beginning`

### 3x) Other helpful curl commands:
- See what connectors are running: `curl -X GET http://localhost:8083/connectors`
- Pause a connector: `curl -X PUT http://localhost:8083/connectors/mtconnect-source-connector/pause`
- Delete a *paused* connector: `curl -X DELETE http://localhost:8083/connectors/mtconnect-source-connector`
- Note1: You can only delete a connector that has been paused
- Note2: The name of the connector is provided in the .json file, not the name of the .json file  

 
## 5) MQTT: Installing Eclipse Mosquitto, connecting it to Kafka, and connecting it to a sensor.

### 5a) Set-up mosquitto.conf and start mosquitto

- `mosquitto [-c config file] [ -d | --daemon ] [-p port number] [-v]`
- **Note:** This probably should be added to the default start script
- **To Do:** mosquitto.conf file

### 5b) Add MQTT Connector to Kafka Connect


## 6) Set up Spark and Run Streaming Application
- (OPTIONAL) Go to the `$SPARK/conf` and modify the log4j.properties.template file 
  - Replace all instances of `"INFO"` with `"ERROR"` and save it as .properties file
  - This is to prevent it from printing a bunch of INFO logs everytime you run any script
  
- Edit the `SparkStreamingKafkaSHDRData.py` file
  - In line 143, edit the path to your checkpoint directory. Simply create a new empty folder and provide path to it.
  
- Deploy the Spark Streaming Application
  - Kafka, zookeeper, MTConnect Agent Adapter Simulator, and MTConnect Adapter connector need to be running
  - in a new terminal tab/window, use spark-submit to launch the spark streaming application 
    - `$SPARK/bin/spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.0.0,org.apache.spark:spark-token-provider-kafka-0-10_2.12:3.0.0 $DataPipeline/spark_applications/SparkStreamingKafkaSHDRData.py localhost:9092 subscribe VMC-3Axis_SHDR`
    - *Note:* you must provide the correct version of your Spark when you use packages to add kafka dependencies.
  
- Consume data written to kafka topic by the Spark application
  - Open a new terminal window:
  - `$KAFKA/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic VMC-3Axis_Ycom --from-beginning`

