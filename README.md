# Creating the Data Pipeline Architecture on Ubuntu

## 1) Clone this repository
- Create a "jars" directory inside this cloned repository.

## 2) Install Docker
- Follow the instructions here: [https://docs.docker.com/install/linux/docker-ce/ubuntu/](https://docs.docker.com/install/linux/docker-ce/ubuntu/)

## 3) Download the necessary JARs
- Download the necessary JAR files here:
    - https://www.confluent.io/connector/kafka-connect-mqtt/
    - https://www.confluent.io/connector/kafka-connect-mongodb-sink/
    - https://www.confluent.io/connector/kafka-connect-elasticsearch/
 - Unzip the files in your "jars" directory where you cloned this repository.

## 4) Install the images via Docker
- Install docker compose with `sudo apt install docker-compose`.
- Run `sudo docker-compose up` from inside the clone repo's directory. You should be in the same folder where the "docker-compose.yml" file is located.
- This will install all of the images for the different tools we are using, and will also create a local connection inside of Docker so they are all able to communicate with one another.

## 5) Connect the Sources and Sinks
- We need to connect our MQTT source to Kafka,Kafka to our MongoDB sink, and Kafka to our Elasticsearch sink. We do that by using the "connect" files. Enter the following commands:
  - `curl -d @connect-mqtt-source.json -H "Content-Type: application/json" -X POST http://localhost:8083/connectors` 
  - `curl -d @connect-mongodb-sink.json -H "Content-Type: application/json" -X POST http://localhost:8083/connectors`
  - `curl -d @connect-elasticsearch-sink.json -H "Content-Type: application/json" -X POST http://localhost:8083/connectors`
- Now our sources and sinks should all be connected, and our data pipeline should be up and running.

## 6) Test that everything is working
- There is a test program and XML file available, named "PipelineTest.java". Go ahead and run the program. If everything is working correctly, the program should grab the data from the XML file, produce it to MQTT who sends it to Kafka, then both MongoDB and Elasticsearch should consume that data as JSON.
