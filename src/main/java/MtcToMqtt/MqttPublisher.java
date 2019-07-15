package MtcToMqtt;

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

class MqttPublisher {

    /*
     * Set your topic, broker and clientId accordingly.
     */
    private static final String topic = "baeldung";
    private static final String broker = "tcp://localhost:1883";
    private static final String clientId = "connect-custom";
    private static final int qos = 2;
    private MqttClient client;

    /*
     * Establish new MqttClient
     */
    MqttPublisher() throws Exception {
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
    void publishMessage(String xml) {
        MqttMessage message = new MqttMessage(xml.getBytes());
        message.setQos(qos);
        try {
            client.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    String createMessage (Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
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