# Getting started with Kafka on Mac OS
## The pre-requisites
1. **Make sure you have all the necessary software prior to installing Kafka.**
    - Open up a terminal and install brew with this command: ```/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"```
    - Check which version of Java you have by entering ```java -version```. Currently, Java 8 is needed to run Kafka. If you have a newer version of Java or don't have Java installed, you can use ```brew cask install caskroom/versions/java8``` to install the proper version of Java necessary to run Kafka.

## Installing Kafka
2. **Download and extract The Kafka Binaries**
	- Go to https://kafka.apache.org/downloads to download the Kafka binaries. Choose the download link that uses the most recent version of Scala(2.12 as of this writeup).
	- Move your tar file to the directory you wish to have Kafka installed in. In this example we move it to root with ```mv Downloads/kafka_your_version.tgz .``` and extract it with ```tar -xvf kafka_your_version.tgz```
	- Kafka is now installed on your system. Test out that everything went well by going into your Kafka directory with ```cd kafka_your_version``` and running ```bin/kafka-topcs.sh```. A general guide of commands should come up.

3. **Install Kafka commands using Brew**
    - Enter ```brew install kafka``` in your terminal. After this completes, you should be able to run Kafka commands from any location in your terminal. Try it by entering ```kafka-``` and hitting tab. If it was successful, you should get a list of all commands that begin with that, try using it by just entering ```kafka-topics``` and see if the general guide of commands you saw in the last step came up again. If so this was done successfully.
    
## Setting up your Zookeeper and Kafka environments
4. **Test that Zookeeper works then set your Zookeeper and Kakfa properties**
    - We now have all that is required to install to begin our first Kafka Broker, since the previous install will also give us Zookeeper which is necessary to run Kafka.
    - Begin by starting a Zookeeper server with ```zookeeper-server-start config/zookeeper.properties```. If this was successful, you should see ```INFO binding to port 0.0.0.0/0.0.0.0:2181 (org.apache.zookeeper.server.NIOServerCnxnFactory)``` come up in the last line of your terminal.
    - If that worked, close out of Zookeeper with ctrl+c, and open up the config/zookeeper.properties file with a text editor. Inside, there should be something that looks like this:
        ```bash
        # The number of milliseconds of each tick
        tickTime=2000
        # The number of ticks that the initial 
        # synchronization phase can take
        initLimit=10
        # The number of ticks that can pass between 
        # sending a request and getting an acknowledgement
        syncLimit=5
        # the directory where the snapshot is stored.
        # do not use /tmp for storage, /tmp here is just 
        # example sakes.
        dataDir=/tmp/zookeeper
        ```
        We want to change the ```dataDir=/tmp/zookeeper``` path. First, add a data directory in your main Kafka directory with ```mkdir data```, and inside that we will create a Zookeeper directory with ```mkdir data/zookeeper```. Then, change your dataDir to say ```dataDir=/Users/your_user_name/kafka_your_version/data/zookeeper```. 
    - We want to do the same thing now, but with Kafka. In your main Kafka directory use ```mkdir data/kafka```. Then we will open up and edit server.properties. Inside the file you should see a lot of different default settings. We're interested in 'log.dirs':
        ```bash
        ############################# Log Basics #############################

		# A comma seperated list of directories under which to store log files
		log.dirs=/tmp/kafka-logs
        ```
        Again we will change this to say ```log.dirs=/Users/your_user_name/kafka_your_version/data/kafka```.

## Running Kafka
5. **You should now be able to run Zookeeper and Kafka**
	- Check that everything is working properly by first running Zookeeper with ```zookeeper-server-start config/zookeeper.properties```. If that is still working, open up a NEW terminal window(The current Zookeeper one must stay opened to keep running), and use the command ```kafka-server-start config/server.properties```. If this worked properly you should see ```INFO [KafkaServer id=0] started``` at the bottom of your terminal.        

## Creating Topics
6. **Creating your first Topic**
    - At this point, you should have Zookeeper up and running in one terminal and Kafka in another. Now open up a third terminal, keeping both of those open. 
    - Create your first topic with ```kafka-topics --zookeeper 127.0.0.1:2181 --topic first_topic --create --partitions 3 --replication-factor 1```. This will create our topic, based on the following information:
        - ```kafka-topics``` : Since we are creating a topic, we need to be using this command.
        - ```--zookeeper 127.0.0.1:2181``` : In order to create a topic, we have to point at Zookeeper, and on every local machine Zookeeper runs on 127.0.0.1 at port 2181.
        - ```--topic first_topic --create``` This names our topic. "first_topic" is used, but it can be named anything, then we create it with ```--create```.
        - ```--partitions 3``` : We have to give Kafka the number of partitions that we want when we create a topic. 3 partitions is an arbitrary amount being used in this example.
        - ```replication-factor 1``` : This gives us a replication-factor of 1. An RF of 1 is generally a poor idea, however, when we create our first broker it must be this value, because since we only have one broker we cannot have a replication factor higher than the number of brokers we have.
    - If you entered this all in correctly, you should get an output of ```Created topic "first_topic```.
    - Double check that everything is correct with the command ```kafka-topics --zookeeper 127.0.0.1:2181 --list```. This gives us a list of all existing topics. Check that "first_topic" is in there.
    - To get more detailed information about a topic, use ```kafka-topics --zookeeper 127.0.0.1:2181 --topic first_topic --describe```.

## Creating Producers, Consumers, and Consumer Groups
 7. **Producing and Consuming**
     - Now that Kafka is properly setup and we have a topic created, you are able to produce and consume messages. To do this, enter ```kafka-console-producer --broker-list 127.0.0.1:9092 --topic first_topic```. If you get a '>' sign waiting for input that means it worked. Send a few lines of text.
     - Now in a new terminal window, enter ```kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic```. You shouldn't see any information pop up. That's because while we created messages in the producer, we are not pulling up the entire message history, only getting the new streaming messages the producer sends while the consumer is open. So go back to the producer message and send a few more lines of text. You should see it begin to print the data in the consumer terminal window.
     - Use Ctrl+C to quit the consumer terminal. Now let's get the entire topic's consumed history. Enter ```kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --from-beginning```. This should print out all the data you entered from the beginning. Note that this does not necessarily print out in order. Ordering is only guaranteed within partitions, and if you created 3 partitions you may not get this data in the order you input it.
     
 8. **Using Console Consumer Groups**
     - Now let's create a Consumer Group. Ctrl+C on your consumer terminal again, and enter ```kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-first-application```. This now creates the consumer group "my-first-application", and runs the consumer with the group.
     - Open another terminal window and enter the same consumer command, ```kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-first-application```. Start sending messages with the producer. You should see messages being randomly split between the two terminal windows. This is because they are part of the same group, and we have 3 partitions with 2 console consumers. If you add a third console consumer you should see the messages getting split up between the 3 of them.
     - Close one of the three consumers, and start sending messages again. You should see that Kafka knows to start splitting the messages between the two remaining consumers. This is showing what happens when a consumer goes down.
     - Let's create a second consumer group called "my-second-application", and read it from the beginning with ```kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-second-application --from-beginning```. You should see all the messages you have sent to the producer so far. Ctrl + C and then run the exact same command. This time nothing should come up. The reason for this is we specified the group. Since the offsets are committed in Kafka, it knows where we have read up to. It knows that "my-second-application" has read everything, and therefore there is nothing to read, even with ```--from-beginning```.
     - So, run "my-second-application" without the ```--from-beginning``` tag. Send a few messages from the producer and you'll see it's getting messages okay. Ctrl + C in the consumer console and produce a few more messages. Then run your consumer again. You should see the messages consumed that it missed out on while it was closed. It's now all caught up again.
     
9. **Using Kafka Consumer Groups**
   - Let's get a list of all console consumer groups we have. Enter ```kafka-consumer-groups --bootstrap-server 127.0.0.1:9092 --list```. You should see "my-first-application" and "my-second-application".
   - We can see the details of a topic. Use ```kafka-consumer-groups --bootstrap-server 127.0.0.1:9092 --describe --group my-second-application```. This will give you information on the topic(s), the partition(s), each partition's current offset, the log end offset, the lag, consumer id, host, and client id.
   - Since we read everything we produced in "my-second-application" previously, the "lag" should be 0 for each partition. Go ahead and get the details of "my-first-application". If you closed the terminal for this application and then produced more messages. You should see some values in the "lag" column. This is how far behind the application's offset is from the current end of data. Catch up to the rest of the data with ```kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-first-application```. You should get the missing messages. Check the application's details again and there should be 0 lag in each partition.
   - Take note of the Consumer ID. This is the location of the consumer that is consuming data. If there is something there, it means a consumer is currently active on that partition. This is a good detail to keep in mind.

## Changing Offset Values
10. **Resetting Offsets**
    - If you need to have your topic "replay" all of your data and reset your offsets, this can be done. There are multiple options, they are ```--to-datetime```, ```--by-period```, ```--to-earliest```, ```--to-latest```, ```shift-by```, ```--from-file```, and ```to-current```.
    - We'll use ```to-earliest``` in this example. It will take it all the way back to the beginning. We can reset a specific topic, or all topics. Enter ```kafka-consumer-groups --bootstrap-server 127.0.0.1:9092 --group my-first-application --reset-offsets -to-earliest --execute --topic first_topic```. If successful you should get the list of the topic(s), the partitions of the topic(s), and the new offset.
    - Let's run the consumer group again for "my-first-application". You should see all the data that we have produced being read into it now. Close the consumer group and check the details on it again. The current offset and log end offset should be the same, with a lag of 0.
    - Try changing the offset with the ```shift-by``` command. Specifically, use ```shift-by -2```. This will send the offsets of each partition back by 2. If you run the consumer again, you should get 6 messages printed, 2 for each partition.
    
## **General CLI examples**
- Running Zookeeper: ```zookeeper-server-start config/zookeeper.properties```
- Running Kafka: ```kafka-server-start config/server.properties```
- Create a topic: ```kafka-topics --zookeeper 127.0.0.1:2181 --topic first_topic --create --partitions 3 --replication-factor 1```	
- Delete a topic: ```kafka-topics --zookeeper 127.0.0.1:2181 --topic first_topic --delete```
- Produce message(s): ```kafka-console-producer --broker-list 127.0.0.1:9092 --topic first_topic```
- Specify 'acks' setting: ```kafka-console-producer --broker-list 127.0.0.1:9092 --topic first_topic --producer-property acks=all```
- Consume live message(s): ```kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic```
- Consume all messages: ```kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --from-beginning```
- Creating a Consumer Group: ```kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-first-application```
- Listing all Consumer Groups: ```kafka-consumer-groups --bootstrap-server 127.0.0.1:9092 --list```
- Getting details on a Consumer Group: ```kafka-consumer-groups --bootstrap-server 127.0.0.1:9092 --describe --group my-second-application```
- Reset Offsets: ```kafka-consumer-groups --bootstrap-server 127.0.0.1:9092 --group my-first-application --reset-offsets -to-earliest --execute --topic first_topic```
- Using a Producer with keys:
    ```bash
    kafka-console-producer --broker-list 127.0.0.1:9092 --topic first_topic --property parse.key=true --property key.separator=,
    > key,value
    > another key,another value
    ```
- Using a Consumer with keys: ```kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --from-beginning --property print.key=true --property key.separator=,```
    


## **General Kafka Information**
- Change new topic defaults: ```vim config/server.properties```. In here you can set the default number of partitions, replication factor, etc. of a new topic.

