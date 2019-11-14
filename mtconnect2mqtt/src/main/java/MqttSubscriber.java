package main.java;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/*
 * Subscribes to an MQTT topic. NOTE: Untested as of 08/02/2019.
 */
public class MqttSubscriber {

    private MqttClient client;

    MqttSubscriber(String broker, String clientId) throws Exception {
        this.client = new MqttClient(broker, clientId, new MemoryPersistence());
    }

    void createConnection() {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        try {
            this.client.connect(connOpts);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    void subscribe(String topic) throws Exception {
        this.client.subscribe(topic);
    }



}
