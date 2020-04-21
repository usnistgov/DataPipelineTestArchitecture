# Creating the Data Pipeline Architecture on Ubuntu

## This is on my to-do list!
- This is under construction since the old readme was outdated
- This will become a primer on how-to set up the pipeline, with more extensive documentation coming later.
- Much of the guide will be geared toward dev environments, future guidance on multi-node, distributed implementations
- This was all developed and tested on Ubuntu 18.04.
- For the most part, we'll follow the Quick Start guide: https://kafka.apache.org/quickstart

## 1) Download Apache Kafka and this repository
- https://kafka.apache.org/quickstart#quickstart_download
- `git clone https://github.com/usnistgov/DataPipelineTestArchitecture`

## 2) Do some stuff to the directories
- In the folder where you have Kafka (e.g. ./Kafka/kafka_2.12-2.5.0), create a folder called "connectors"
- Copy `mtconnect-source-connector-1.0-SNAPSHOT.jar` from the DataPipelineTestArchitecture/connectors folder, and put it in the "./Kafka/kafka_2.12-2.5.0/connectors" folder
  - For example `cp ./DataPipelineTestArchitecture/connectors/mtconnect-source-connector-1.0-SNAPSHOT.jar ../Kafka/kafka_2.12-2.5.0/connectors`
- Copy `connect-mtconnectTcp-source-1.0-SNAPSHOT.jar` too
- Go to ./Kafka/kafka_2.12-2.5.0/config and open "connect-standalone.properties".
  - At the very bottom you'll see "#plugin.path= ...". Remove the # (uncommenting it) and put the full path of the "connectors" folder.     - For example: "plugin.path = /home/time/Kafka/kafka_2.12-2.5.0/connectors". This is telling Kafka connect to look in this folder for connectors.
- Copy the .properties files in DataPipelineTestArchitecture/config to ./Kafka/kafka_2.12-2.5.0/config
  - For example `cp ./DataPipelineTestArchitecture/config/connect-mtconnect-source.properties ../Kafka/kafka_2.12-2.5.0/config`

## 3) Edit the .properties files


## 4) Start Kafka
- Start a Zookeeper instance: `bin/zookeeper-server-start.sh config/zookeeper.properties`
- Start a Kafka instance: `bin/kafka-server-start.sh config/server.properties`

## 5) Start connectors
- Add a topic per the quick start but with topic name of "M80104K...." as it's written in the .properties file
- run `bin/connect-standalone.sh config/connect-standalone.properties config/connect-mtconnect-source.properties` which should start the mtconnect connector.
