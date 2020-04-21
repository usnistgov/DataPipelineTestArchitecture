package main.java;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/*
 * Creates an MQTT client that can retrieve data and publish it to a topic.
 */
class MqttPublisher {

    /*
     * Set your topic, broker and clientId accordingly.
     *      topic : The topic you want to publish to in MQTT
     *      broker : The address for MQTT
     *      clientId : This client's unique client id.
     */
    private String topic;
    private static final int qos = 2;
    private MqttClient client;

    /*
     * Establish new MqttClient
     */
    MqttPublisher(String broker, String clientId, String topic) throws Exception {
        this.topic = topic;
        this.client = new MqttClient(broker, clientId, new MemoryPersistence());
    }

    /*
     * Establish an open connection to Mosquitto via the MqttClient
     */
    void createConnection() {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        try {
            this.client.connect(connOpts);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /*
     * Takes a String of XML data, converts it to a byte array, and publishes it to
     * the Mosquitto client.
     */
    void publishMessage(byte [] responseDocumentAsByte) {
        MqttMessage message = new MqttMessage(responseDocumentAsByte);
        message.setQos(qos);
        try {
            client.publish(topic, message);
            System.out.println(responseDocumentAsByte);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /*
     * Transforms the message to a String to be used for publishing.
     */
    String createMessage (Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        /*
         * Allows us to ignore the XML header declaration that comes with the XML page
         * but is not part of the MTConnect standard.
         */
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString();
    }

    /*
     * Disconnects the MqttPublisher from the Mosquitto client.
     */
    void disconnectPublisher() {
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}