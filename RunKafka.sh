#!/bin/bash

# script to start zookeeper, kafka, and the distributed connector
# can be used as a template to spin up distributed kafka nodes 

KAFKA_DIR=/home/tim/Kafka/kafka_2.12-2.4.1 # directory where kafka lives

# Start a Zookeeper instance: 
echo ; echo Starting Zookeeper...
cd $KAFKA_DIR # change to your directory
bin/zookeeper-server-start.sh config/zookeeper.properties 1>../ZookeeperOutput.txt 2>../ZookeeperError.txt & # run this in the background, and put the stadout and stderror into files  
ZOOKEEPER_PID=$! # save the process ID
echo The Process ID of Zookeper instance is $ZOOKEEPER_PID # display process ID
echo You may find Zookeeper logs in ZookeeperOutput.txt and errors in ZookeeperError.txt ; echo    

# Start Kafka
sleep 5s # wait for zookeeper to start
echo Starting Kafka...
bin/kafka-server-start.sh config/server.properties 1>../KafkaOutput.txt 2>../KafkaError.txt & # run this in the background, and put the stadout and stderror into files   
KAFKA_PID=$! # save the process ID.
echo The Process ID of Kafka instance is $KAFKA_PID # display process ID
echo You may find Kafka logs in KafkaOutput.txt and errors in KafkaError.txt ; echo  

# Start the connector, after navigating to its directory 
sleep 5s # wait for adapter to run
echo Starting the distributed connector...
bin/connect-distributed.sh config/connect-distributed.properties 1>../ConnectOutput.txt 2>../ConnectError.txt & # run this in the background, and put the stadout and stderror into files   
CONNECT_PID=$! # save the process ID
echo The Process ID of Connector instance is $CONNECT_PID # display process ID
echo You may find Connector logs in ConnectOutput.txt and errors in ConnectorError.txt ; echo  

# write all process IDs to a text file so you can quit them later 
echo "CONNECT_PID" $CONNECT_PID > ../ProcessIDs.txt
echo "KAFKA_PID" $KAFKA_PID >> ../ProcessIDs.txt
echo "ZOOKEEPER_PID" $ZOOKEEPER_PID >> ../ProcessIDs.txt

