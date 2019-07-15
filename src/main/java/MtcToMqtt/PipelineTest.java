package MtcToMqtt;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class PipelineTest {

    private static final File TEST_FILE = new File("src/main/test-doc.xml");

    public static void main(String[] args) throws Exception {
        MqttPublisher publisher = new MqttPublisher();
        publisher.createConnection();
        Document doc = createDocumentFromFile();
        publishTestDocument(publisher, doc);
        System.exit(0);
    }

    private static Document createDocumentFromFile() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(TEST_FILE);
        doc.normalizeDocument();
        return doc;
    }

    private static void publishTestDocument(MqttPublisher publisher, Document doc) throws Exception {
        String xmlMessage = publisher.createMessage(doc);
        publisher.publishMessage(xmlMessage);
        publisher.disconnectPublisher();
    }

}