#! /bin/bash

curl -d @connectors/connect-mqtt-source.json -H "Content-Type: application/json" -X POST http://localhost:8083/connectors
sleep 1.5
curl -d @connectors/connect-mongodb-sink.json -H "Content-Type: application/json" -X POST http://localhost:8083/connectors
sleep 1.5
curl -d @connectors/connect-elasticsearch-sink.json -H "Content-Type: application/json" -X POST http://localhost:8083/connectors
sleep 1.5
sudo docker run --net=datapipelinetestarchitecture_default --rm confluentinc/cp-kafka:5.1.0 kafka-topics --zookeeper zookeeper:2181 --topic mqtt-to-kafka --create --partitions 3 --replication-factor 1 exec bash
sleep 6
sudo docker run --net=datapipelinetestarchitecture_default --rm confluentinc/cp-kafka:5.1.0 kafka-topics --zookeeper zookeeper:2181 --topic json_topic --create --partitions 3 --replication-factor 1
sleep 6
gnome-terminal -- bash -c "sudo docker run --net=datapipelinetestarchitecture_default --rm confluentinc/cp-kafka:5.1.0 kafka-console-consumer --bootstrap-server kafka:9092 --topic mqtt-to-kafka; exec bash"
sleep 1
gnome-terminal -- bash -c "sudo docker run --net=datapipelinetestarchitecture_default --rm confluentinc/cp-kafka:5.1.0 kafka-console-consumer --bootstrap-server kafka:9092 --topic json_topic; exec bash"
