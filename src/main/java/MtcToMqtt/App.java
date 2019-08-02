package MtcToMqtt;

import org.w3c.dom.*;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Scanner;

/*
 * This gets all of the data from the Mazak Testbed and publishes it into MQTT. It establishes
 * a connection to the website, goes through all needed sequence numbers via GET requests, and
 * takes the XML data, publishing it to MQTT.
 */
public class App {

    public static void main(String[] args) throws Exception {

        /*
         * Get the most recent sequence number we have searched through and continue getting+
         * the "sample" page from "mtconnect.mazakcorp.com:5609/sample?from=" based on the last
         * value we have saved. Then create the initial document based on that sequence number.
         * If we do not have a current sequence number saved, that means that we had not yet
         * searched this Mazak machine, and we need to grab the initial page with all information
         * from "mtconnect.mazakcorp.com:5609/current".
         */
        System.out.println("Establishing document connection.");
        int sequenceNumber = getSequenceNumber();
        Document doc = connectInitialDocument(sequenceNumber);

        /*
         * Grab the current set of sequence numbers from the initial document we created.
         * We need the first and last sequence to know the range of values we are going
         * to be searching through.
         */
        System.out.println("Getting sequence numbers");
        int[] sequenceNumbers = getCurrentSequenceNumbers(doc);
        int firstSequence = sequenceNumbers[0] + 500;
        int lastSequence = sequenceNumbers[1];

        /*
         * Establish a new MqttPublisher to open the connection to Mosquitto.
         */
        MqttPublisher publisher = new MqttPublisher();
        publisher.createConnection();

        /*
         * Check what our saved sequenceNumber is. If the value is -1 we have not searched
         * the machine at all, so we send the "current" to Mosquitto. If the saved sequenceNumber
         * is higher than the firstSequence we have already searched part of the available list, so
         * we set our firstSequence value to be the current sequenceNumber.
         */
        if(sequenceNumber == -1) {
            String xmlString = publisher.createMessage(doc);
            publisher.publishMessage(xmlString);
            firstSequence++;
        } else if(sequenceNumber > firstSequence) {
            firstSequence = sequenceNumber;
        }

        /*
         * Run the driver to collect all the data, then update the sequence numbers accordingly.
         * We then disconnect and reconnect the MqttPublisher in order to avoid timeout or lag
         * issues.
         */
        while(true) {
            runDriver(firstSequence, lastSequence, publisher);
            sequenceNumbers = getCurrentSequenceNumbers(doc);
            firstSequence = lastSequence;
            lastSequence = sequenceNumbers[1];
            publisher.disconnectPublisher();
            publisher.createConnection();
        }

    }

    /*
     * Goes to the saved text file with the most recently saved value and returns it. If there
     * is no saved file returns -1.
     */
    private static int getSequenceNumber() throws FileNotFoundException {
        File file = new File("src/main/sequence-track.txt");
        Scanner sc = new Scanner(file);
        int x = -1;
        if (sc.hasNextInt())
            x = sc.nextInt();
        return x;
    }

    /*
     * Create the first Document as we start the program. This checks if we have a saved file
     * number, and creates the correct document accordingly.
     */
    private static Document connectInitialDocument(int sequenceNumber) throws Exception {
        DocumentConnector connection;
        if(sequenceNumber == -1)
            connection = new DocumentConnector();
        else
            connection = new DocumentConnector(sequenceNumber);
        connection.createConnection();
        connection.createDocument();
        return connection.getDocument();
    }

    /*
     * From the initial Document, finds the first and last sequence numbers and returns them.
     */
    private static int[] getCurrentSequenceNumbers(Document doc) {
        int[] sequenceNums = new int[2];
        NodeList nodes = doc.getElementsByTagName("Header");
        Node node = nodes.item(0);
        Element element = (Element) node;
        NamedNodeMap map = element.getAttributes();
        for(int i = 0; i < map.getLength(); i++) {
            Node attr = map.item(i);
            System.out.println(attr.getNodeValue() + " " + attr.getNodeName());
        }
        sequenceNums[0] = Integer.parseInt(element.getAttribute("firstSequence"));
        sequenceNums[1] = Integer.parseInt(element.getAttribute("lastSequence"));
        return sequenceNums;
    }

    /*
     * If we are caught up on the most recent Mazak page, we do nothing. Otherwise we go through
     * every sequence number and send that page's data through Mosquitto. Saves the last sequence
     * number once we are complete. Then sleep for 1 hour.
     */
    private static void runDriver(int firstSequence, int lastSequence, MqttPublisher publisher) throws Exception {
        if(lastSequence == firstSequence) {
            System.out.println("No new documents to grab. Sleeping for 1 hour.");
        } else {
            System.out.println("Beginning data stream from Mazak to MQTT");
            while(lastSequence > firstSequence) {
                DocumentConnector connection = new DocumentConnector(firstSequence);
                connection.createConnection();
                connection.createDocument();
                Document doc = connection.getDocument();
                //publishDriver(doc, publisher);
                String xmlString = publisher.createMessage(doc);
                publisher.publishMessage(xmlString);
                firstSequence++;
                System.out.println(firstSequence + " " + lastSequence);
            }
            saveSequenceNumber(firstSequence);
        }
        try {
            Thread.sleep(1000 * 60 * 60);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
     * Saves the last sequence number once we have collected all the documents.
     */
    private static void saveSequenceNumber(int sequenceNumber) {
        File file = new File("src/main/sequence-track.txt");
        try {
            FileWriter fWriter = new FileWriter(file.getAbsoluteFile(), false);
            BufferedWriter writer = new BufferedWriter(fWriter);
            writer.write(sequenceNumber);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
