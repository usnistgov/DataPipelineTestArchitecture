package gov.nist.mtconnect;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;
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
import java.util.Map;
import java.net.HttpURLConnection;

public class MTConnectSourceTask extends SourceTask {
  //static final Logger log = LoggerFactory.getLogger(MTConnectSourceTask.class);

  private static final String DEVICE_URL = "http://mtconnect.mazakcorp.com:5609"; // should be input from config
  private String INITIAL_URL = this.DEVICE_URL+"/current";
  private String SEQUENCE_URL = this.DEVICE_URL+"/sample?from=";
  private String currentURL;
  private InputStream responseDocument;
  private Document xmlResponseDocument;
  private int nextSequence = -1;

  @Override
  public String version() {
    return VersionUtil.getVersion();
  }

  @Override
  public void start(Map<String, String> map) {
    //TODO: Do things here that are required to start your task. This could be open a connection to a database, etc.
  }

  @Override
  public List<SourceRecord> poll() throws InterruptedException {
        HttpURLConnection urlConnection = null;
        URL url = null;
        // Query the last known NextSequence Number
        // if nextSequence == -1, get Current
      try {
          if (this.nextSequence == -1) {
              url = new URL(this.INITIAL_URL);
          } else { //else append nextSequence to http
              url = new URL(this.SEQUENCE_URL + nextSequence);
          }
      }
        catch (MalformedURLException e) {
          e.printStackTrace();
      }

        try {
          urlConnection = (HttpURLConnection) url.openConnection();
          urlConnection.setRequestMethod("GET");
          urlConnection.setRequestProperty("Accept", "application/xml");
          int responseCode = urlConnection.getResponseCode();
          if (responseCode == HttpURLConnection.HTTP_OK){
              this.responseDocument = urlConnection.getInputStream();
          }
        } catch (IOException e) {
          e.printStackTrace();
        }

      DocumentBuilderFactory domFact = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = null;
      DOMSource domSource = null;
      StringWriter writer = new StringWriter();
      StreamResult result = new StreamResult(writer);

      try {
          builder = domFact.newDocumentBuilder();
          this.xmlResponseDocument = builder.parse(this.responseDocument);
          domSource = new DOMSource(this.xmlResponseDocument);
          TransformerFactory tf = TransformerFactory.newInstance();
          Transformer transformer = tf.newTransformer();
          transformer.transform(domSource, result);

      } catch (ParserConfigurationException | TransformerException | SAXException | IOException e) {
          e.printStackTrace();
      }

      Element root = this.xmlResponseDocument.getDocumentElement();
      String xmlResponseDocumentType = root.getNodeName();
      System.out.println("Response Document Type: " + xmlResponseDocumentType);

    // If Response is MTConnectStreams (responseDocument.firstNode.nodeType == "MTConnectStreams")
      if (xmlResponseDocumentType.equals("MTConnectStreams")){
          System.out.println("Getting sequence numbers");
          int[] sequenceNumbers = getCurrentSequenceNumbers(this.xmlResponseDocument);
          int lastSequence = sequenceNumbers[1];
          System.out.println("Last Sequence Number: " + Integer.toString(lastSequence));
          System.out.println("XML IN String format is: \n" + writer.toString());
      }
        // Put response document into SourceRecord
        // Store the Next Sequence Number

    // Else If Response is MTConnect Error
      if (xmlResponseDocumentType.equals("MTConnectError")){
          System.out.println("Response Document Type: " + xmlResponseDocumentType);

      }
      // log error
      // write -1 to nextSequence
    // Else (didn't get an MTConnect response at all)
    //TODO: Create SourceRecord objects that will be sent the kafka cluster.
      List<SourceRecord> sourceRecord = null;
      return sourceRecord;
   }

  @Override
  public void stop() {
    //TODO: Do whatever is required to stop your task.
  }


    private static int[] getCurrentSequenceNumbers(Document doc) {
        int[] sequenceNums = new int[2];
        NodeList nodes = doc.getElementsByTagName("Header");
        Node node = nodes.item(0);
        Element element = (Element) node;
        //NamedNodeMap map = element.getAttributes();
        //for(int i = 0; i < map.getLength(); i++) {
        //    Node attr = map.item(i);
        //    System.out.println(attr.getNodeValue() + " " + attr.getNodeName());
        //}
        sequenceNums[0] = Integer.parseInt(element.getAttribute("firstSequence"));
        sequenceNums[1] = Integer.parseInt(element.getAttribute("lastSequence"));
        return sequenceNums;
    }
}