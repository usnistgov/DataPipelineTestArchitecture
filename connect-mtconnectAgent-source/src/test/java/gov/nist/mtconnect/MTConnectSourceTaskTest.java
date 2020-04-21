package gov.nist.mtconnect;

import org.apache.kafka.connect.source.SourceRecord;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

public class MTConnectSourceTaskTest {
    @Test
    public void test() {
        basicTest();
        testBadSequence();
        testNotMTConnectAddress();
        testDeviceSpecificURL();
    }

    @Test
    public void basicTest() {
        MTConnectSourceTask sourceTask = new MTConnectSourceTask();
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("agent_url", "http://mtconnect.mazakcorp.com:5612");
        properties.put("next_sequence", "-1");
        properties.put("topic_config", "5609XML");
        properties.put("request_interval", "10000");
        sourceTask.start(properties);

        try {
            sourceTask.poll();
            sleep(1000);
            sourceTask.poll();
            sourceTask.poll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void testBadSequence() {
        MTConnectSourceTask sourceTask = new MTConnectSourceTask();
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("agent_url", "http://mtconnect.mazakcorp.com:5609");
        properties.put("next_sequence", "1000");
        properties.put("topic_config", "5609XML");
        properties.put("request_interval", "10000");
        sourceTask.start(properties);

        try {
            sourceTask.poll();
            sleep(1000);
            sourceTask.poll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void testNotMTConnectAddress() {
        MTConnectSourceTask sourceTask = new MTConnectSourceTask();
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("agent_url", "http://mtconnect.mazakcorp.com");
        properties.put("next_sequence", "-1");
        properties.put("topic_config", "5609XML");
        properties.put("request_interval", "10000");
        sourceTask.start(properties);

        try {
            sourceTask.poll();
            sleep(1000);
            sourceTask.poll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void testDeviceSpecificURL() {
        MTConnectSourceTask sourceTask = new MTConnectSourceTask();
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("agent_url", "http://mtconnect.mazakcorp.com:5612");
        properties.put("device_path", "path=//Device[@name=\"Mazak\"]");
        properties.put("next_sequence", "-1");
        properties.put("topic_config", "5609XML");
        properties.put("request_interval", "10000");
        sourceTask.start(properties);

        try {
            sourceTask.poll();
            sleep(1000);
            List<SourceRecord> sourceRecord = sourceTask.poll();
            System.out.println(sourceRecord.toString());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}