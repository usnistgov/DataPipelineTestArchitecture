package MtcToMqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/*
 * Streams all incoming data from a topic we are subscribed to. NOTE: Untested as of 08/02/2019.
 */
public class StreamApp implements MqttCallback {

    public static void main(String[] args) throws Exception{

        //String broker = "tcp://localhost:1883";
        String broker = "tcp://test.mosquitto.org";
        String clientId = "mqtt-to-kafka";
        String topic = "test-topic";
        MqttSubscriber sub = new MqttSubscriber(broker, clientId);

        sub.createConnection();
        sub.subscribe(topic);

    }

    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        System.out.println(mqttMessage);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
