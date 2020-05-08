#!/bin/bash

# script to start zookeeper, kafka, run the MTConnect adapter simulator (ruby script), 
# start the standalone connector, and finally see data in a consumer 

KAFKA_DIR=/Users/sar6/Documents/TimSprockProject/kafka_2.12-2.5.0 # change to your directory
ADAPTER_DIR=/Users/sar6/Documents/TimSprockProject/parts_processes-master # change to your directory

# Start a Zookeeper instance: 
echo ; echo Starting Zookeeper...
cd $KAFKA_DIR
bin/zookeeper-server-start.sh config/zookeeper.properties 1>ZookeeperOutput.txt 2>ZookeeperError.txt & # run this in the background, and put the stadout and stderror into files  
ZOOKEEPER_PID=$! # save the process ID
echo The Process ID of Zookeper instance is $ZOOKEEPER_PID # display process ID
echo You may find Zookeeper logs in ZookeeperOutput.txt and errors in ZookeeperError.txt ; echo    

# Start Kafka
sleep 5s # wait for zookeeper to start
echo Starting Kafka...
bin/kafka-server-start.sh config/server.properties 1>KafkaOutput.txt 2>KafkaError.txt & # run this in the background, and put the stadout and stderror into files   
KAFKA_PID=$! # save the process ID
echo The Process ID of Kafka instance is $KAFKA_PID # display process ID
echo You may find Kafka logs in KafkaOutput.txt and errors in KafkaError.txt ; echo  

# Start the adapter, after changing the directory to where the run_scenario.rb and VMC-3Axis-Log.txt files are
cd $ADAPTER_DIR
sleep 5s # wait for kafka to start
echo Staring MTConnect Adapter Simulator...
# /usr/bin/ruby ./run_scenario.rb -l ./VMC-3Axis-Log.txt # Use which ruby instead if you don't know path
`which ruby` ./run_scenario.rb -l ./VMC-3Axis-Log.txt 1>AdapterOutput.txt 2>AdapterErrors.txt & # run this in the background, and put the stadout and stderror into files  
ADAPTER_PID=$! # save the process ID
echo The Process ID of MTConnect Adapter Simulator instance is $ADAPTER_PID # display process ID
echo You may find Adapter logs in AdapterOutput.txt and errors in AdapterErrors.txt ; echo  

# Start the connector, after navigating to its directory 
cd $KAFKA_DIR
sleep 5s # wait for adapter to run
echo Starting the standalone connector...
bin/connect-standalone.sh config/connect-standalone.properties config/connect-mtconnectTCP-source.properties  1>ConnectorOutput.txt 2>ConnectorError.txt & # run this in the background, and put the stadout and stderror into files   
CONNECTOR_PID=$! # save the process ID
echo The Process ID of Connector instance is $CONNECTOR_PID # display process ID
echo You may find Connector logs in ConnectorOutput.txt and errors in ConnectorError.txt ; echo  

# write all process IDs to a text file so you can quit them later 
echo "CONNECTOR_PID" $CONNECTOR_PID > ProcessIDs.txt
echo "ADAPTER_PID" $ADAPTER_PID >> ProcessIDs.txt
echo "KAFKA_PID" $KAFKA_PID >> ProcessIDs.txt
echo "ZOOKEEPER_PID" $ZOOKEEPER_PID >> ProcessIDs.txt

# Watch your data stream into kafka for hours on end, in a new terminal tab:
cd $KAFKA_DIR
sleep 5s # wait for connector to start
echo Starting the consumer to display data in the topic...
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic VMC-3Axis_SHDR --from-beginning
