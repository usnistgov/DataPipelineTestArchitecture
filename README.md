# Creating the Data Pipeline Architecture on Ubuntu

## 1) Clone this repository
- Grab the repository at https://github.com/usnistgov/DataPipelineTestArchitecture.git
- Create a "jars" directory inside this cloned repository.

## 2) Install Docker
- Follow the instructions here: https://docs.docker.com/install/linux/docker-ce/ubuntu/ (Setting this up with the "Install using the repository" is the easiest, and suggested way to do this).

## 3) Download the necessary JARs
- Download the necessary JAR files here:
    - https://www.confluent.io/connector/kafka-connect-mqtt/
    - https://www.confluent.io/connector/kafka-connect-mongodb-sink/
    - https://www.confluent.io/connector/kafka-connect-elasticsearch/ (Note: For Elasticsearch to work, the `vm.max_map_count` kernel settings need to be set to at least 262144 for use. You can do this with `sudo systcl -w vm.max_map_count=262144`).
 - Unzip the files in your "jars" directory where you cloned this repository.

## 4) Install the images via Docker
- Install docker compose with `sudo apt install docker-compose`.
- Run `sudo docker-compose up` from inside the clone repo's directory. You should be in the same folder where the "docker-compose.yml" file is located.
- This will install all of the images for the different tools we are using, and will also create a local connection inside of Docker so they are all able to communicate with one another.

## 5) Creating topics, and connecting the Sources and Sinks
- We need to connect our MQTT source to Kafka,Kafka to our MongoDB sink, and Kafka to our Elasticsearch sink. We also need to create our topics in Kafka to produce to/consume from. We do that by using the "connect" files to connect the sources/sinks and terminal commands to create our topics.. There's a bash script to set them all up, use `chmod u+x kafka-setup.sh` then `./kafka-setup.sh`. This will also open new terminals that will then be listening to these topics so we can then check if everything is working properly.
- Now our sources and sinks should all be connected, and our data pipeline should be up and running. This can be checked by typing `curl localhost:8083/connectors/<Your source or sink name>/status | jq`(you may need to install jq with `sudo apt install jq` - it's just a JSON processor that prints JSON text out in a more readable, or "pretty" format). The name of the source or sink is located in the json file, labeled "name". So, for example, to check the status of the MongoDB sink you'd enter `curl localhost:8083/connectors/mongodb-sink/status | jq`.

## 6) Setup Java
- If you already use Java or have it set up, you can skip this step.
- Using Ubuntu you should already have Java installed, check with `java -version`, and check you have the JDK with `javac -version`. If you don't have them installed then you can download them with `sudo apt install default-jre` and `sudo apt-get install openjdk-11-jdk`.
- Download IntelliJ using either the Ubuntu Software Center or with `sudo snap install intellij-idea-community --classic`.
- Load IntelliJ. Select import project and import the "DataPipelineTestArchitecture" directory. Follow the steps, making sure to import from External using Maven, and if you need to setup your SDK it should be in "/usr/lib/jvm/java-11-openjdk-amd64". If it is not, use `whereis java` in the terminal to find where it is located.
- Upon loading into IntelliJ, go to "Settings -> Maven -> Importing" and check the box that says "Import Maven projects automatically". If you are asked to "Add as Maven Project" do that as well.
- Give IntelliJ a couple of minutes to install all of the dependencies.

## 7) Test that everything is working
- There is a test program and XML file available, named "PipelineTest.java". Go ahead and run the program. If everything is working correctly, the program should grab the data from the XML file, produce it to MQTT who sends it to Kafka, then both MongoDB and Elasticsearch should consume that data as JSON.
- If everything worked properly, you should see the data from the test file being consumed in both terminal windows. `mqtt-to-kafka` will be in XML format and `json_topic` will be in JSON.
- As long as that worked properly, then it should be in both MongoDB and Elasticsearch. We can look to be sure.
  - Open a new terminal and enter `sudo docker exec -it mongo mongo-db`. It will open the CLI for MongoDB. With `show dbs` we should see "test" as an option. Go in that database with `use test` and then `show collections` should give us "MyCollection". Finally, with `db.MyCollection.find()` we should see our test data populated in MongoDB. You can exit out of MongoDB with `exit`.
  - In the same terminal, go ahead and enter `curl localhost:9200/json_topic_index/_search?pretty`. This should show our test data now populated in Elasticsearch.
