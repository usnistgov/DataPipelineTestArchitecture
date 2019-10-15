package gov.nist.mtconnect;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.w3c.dom.*;
import org.xml.sax.SAXException;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collections;
import java.net.HttpURLConnection;

public class MTConnectSourceTask extends SourceTask {
  //static final Logger log = LoggerFactory.getLogger(MTConnectSourceTask.class);

  private static final String DEVICE_URL = "device_url";
  private static final String LAST_SEQUENCE = "last_sequence";
  private static final String TOPIC_CONFIG = "topic_config";
  private String deviceURL = "http://mtconnect.mazakcorp.com:5609"; // should be input from config
  private String initialUrl = this.deviceURL+"/current";
  private String sequenceUrl = this.deviceURL+"/sample?from=";
  private String topic = deviceURL+"XML";
  private InputStream responseStream;
  private Document xmlResponseDocument;
  private Integer nextSequence = -1;
  private Integer lastSequence = -1;

  @Override
  public String version() {
    return VersionUtil.getVersion();
  }

  @Override
  public void start(Map<String, String> props) {
    //TODO: Do things here that are required to start your task. This could be open a connection to a database, etc.
      this.setupTaskConfig(props);

      //log.debug("Trying to get persistedMap.");
      /*
      Map<String, Object> persistedMap = null;
      if (context != null && context.offsetStorageReader() != null) {
          // Lookup the offset by the device url (the key is stored as a singletonMap)
          persistedMap = context.offsetStorageReader().offset(Collections.singletonMap(DEVICE_URL, deviceURL));
      }
      //log.info("The persistedMap is {}", persistedMap);
      if (persistedMap != null) {
          this.lastSequence = (Integer) persistedMap.get(LAST_SEQUENCE);
      }
      */
  }

  @Override
  public List<SourceRecord> poll() throws InterruptedException {
        HttpURLConnection urlConnection = null;
        URL url = null;
        // Query the last known NextSequence Number
        // if nextSequence == -1, get Current
      try {
          if (this.nextSequence == -1) {
              url = new URL(this.initialUrl);
          } else { //else append nextSequence to http
              url = new URL(this.sequenceUrl + nextSequence);
          }

          urlConnection = (HttpURLConnection) url.openConnection();
          urlConnection.setRequestMethod("GET");
          urlConnection.setRequestProperty("Accept", "application/xml");
          int responseCode = urlConnection.getResponseCode();
          if (responseCode == HttpURLConnection.HTTP_OK){
              responseStream = urlConnection.getInputStream();
          }
      }
        catch (IOException e) {
          e.printStackTrace();
      }

      xmlResponseDocument = this.parseStream(responseStream);
      Element root = xmlResponseDocument.getDocumentElement();
      String xmlResponseDocumentType = root.getNodeName();
      System.out.println("Response Document Type: " + xmlResponseDocumentType);

    // If Response is MTConnectStreams (responseDocument.firstNode.nodeType == "MTConnectStreams")
      if (xmlResponseDocumentType.equals("MTConnectStreams")){
          System.out.println("Getting sequence numbers");
          int[] sequenceNumbers = getCurrentSequenceNumbers(xmlResponseDocument);
          this.lastSequence = sequenceNumbers[1];
          System.out.println("Last Sequence Number: " + this.lastSequence);
          System.out.println("XML IN String format is: \n" + this.DOMtoStringWriter(xmlResponseDocument).toString());
          return getReponseDocumentAsSourceRecords(xmlResponseDocument);
      }
        // Put response document into SourceRecord
        // Store the Next Sequence Number

    // Else If Response is MTConnect Error
      else if (xmlResponseDocumentType.equals("MTConnectError")){
          this.lastSequence = -1;
          System.out.println("Response Document Type: " + xmlResponseDocumentType);
          return getReponseDocumentAsSourceRecords(null);
      }
    // Else (didn't get an MTConnect response at all)
      else {
          this.lastSequence = -1;
          System.out.println("No Response Document");
          return Collections.emptyList();
      }
  }

  @Override
  public void stop() {
    //TODO: Do whatever is required to stop your task.
  }

  private List<SourceRecord> getReponseDocumentAsSourceRecords(Document xmlResponseDocument) {
      ArrayList<SourceRecord> records = new ArrayList<>();
      Map<String, String> sourcePartition = Collections.singletonMap(DEVICE_URL, deviceURL);
      Map<String, Integer> sourceOffset = Collections.singletonMap(LAST_SEQUENCE, lastSequence);
      String value = this.DOMtoStringWriter(xmlResponseDocument).toString();
      records.add(new SourceRecord(sourcePartition, sourceOffset, topic, null, value));

      return records;
    }
    private void setupTaskConfig(Map<String, String> props) {
        this.deviceURL = props.get(DEVICE_URL);
        this.nextSequence = Integer.parseInt(props.get(LAST_SEQUENCE));
        this.topic = props.get(TOPIC_CONFIG);

    }
    private static int[] getCurrentSequenceNumbers(Document doc) {
        int[] sequenceNums = new int[2];
        NodeList nodes = doc.getElementsByTagName("Header");
        Node node = nodes.item(0);
        Element element = (Element) node;

        sequenceNums[0] = Integer.parseInt(element.getAttribute("firstSequence"));
        sequenceNums[1] = Integer.parseInt(element.getAttribute("lastSequence"));
        return sequenceNums;
    }

    private Document parseStream(InputStream responseStream) {
        DocumentBuilderFactory domFact = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;

        try {
            builder = domFact.newDocumentBuilder();
            xmlResponseDocument = builder.parse(responseStream);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return xmlResponseDocument;
    }

    private StringWriter DOMtoStringWriter(Document xmlResponseDocument){
        DOMSource domSource;
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);

        try {
            domSource = new DOMSource(xmlResponseDocument);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
        }
        catch( TransformerException e){
            e.printStackTrace();
        }

        return writer;
    }


}