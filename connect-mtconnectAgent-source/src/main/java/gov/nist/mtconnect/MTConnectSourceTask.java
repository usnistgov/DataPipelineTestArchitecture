package gov.nist.mtconnect;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;



import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class MTConnectSourceTask extends SourceTask {
  static final Logger log = LoggerFactory.getLogger(MTConnectSourceTask.class);

  public static final String AGENT_URL = "agent_url";
  public static final String DEVICE_PATH = "device_path";
  public static final String NEXT_SEQUENCE = "next_sequence";
  public static final String TOPIC_CONFIG = "topic_config";

  private String agentURL; // should be input from config
  private String devicePath;
  private String topic;
  private Integer nextSequence = -1;

  @Override
  public String version() {
    return VersionUtil.getVersion();
  }

  @Override
  public void start(Map<String, String> props) {
    //TODO: Do things here that are required to start your task. This could be open a connection to a database, etc.
      agentURL = props.get(AGENT_URL);
      devicePath = props.get(DEVICE_PATH);
      //nextSequence = Integer.parseInt(props.get(NEXT_SEQUENCE));
      topic = props.get(TOPIC_CONFIG);

      log.debug("Trying to get persistedMap.");

      Map<String, Object> persistedMap = null;
      if (context != null && context.offsetStorageReader() != null) {
          // Lookup the offset by the device url (the key is stored as a singletonMap)
          persistedMap = context.offsetStorageReader().offset(Collections.singletonMap(AGENT_URL, agentURL));
      }
      //log.info("The persistedMap is {}", persistedMap);
      if (persistedMap != null) {
          this.nextSequence = (Integer) persistedMap.get(NEXT_SEQUENCE);
      }

  }

  @Override
  public List<SourceRecord> poll() throws InterruptedException {
      Document xmlResponseDocument = this.getXMLResponseDocument();
      List<SourceRecord> records = Collections.emptyList();

      if (xmlResponseDocument != null){
          String xmlResponseDocumentType = xmlResponseDocument.getDocumentElement().getNodeName();
          System.out.println("Response Document Type: " + xmlResponseDocumentType);
          if (xmlResponseDocumentType.equals("MTConnectStreams")){
              // Put response document into SourceRecord
              // Store the Next Sequence Number
              this.nextSequence = getNextSequenceNumber(xmlResponseDocument);
              //System.out.println("Next Sequence Number: " + this.nextSequence);
              //System.out.println("XML IN String format is: \n" + this.DOMtoStringWriter(xmlResponseDocument).toString());
              records = this.getResponseDocumentAsSourceRecords(xmlResponseDocument);
          }
          else if (xmlResponseDocumentType.equals("MTConnectError")){
              // Else If Response is MTConnect Error
              this.nextSequence = -1;
              System.out.println("Response Document Type: " + xmlResponseDocumentType);
          }
      }
      else {
          // Else (didn't get an MTConnect response at all)
          this.nextSequence = -1;
          System.out.println("No Valid Response Document");
      }

      return records;
  }

    @Override
  public void stop() {
    //TODO: Do whatever is required to stop your task.
  }


    private Document getXMLResponseDocument() {
        HttpURLConnection urlConnection;
        String urlString;
        URL url;
        InputStream responseStream = null;
        // Query the last known NextSequence Number
        // if nextSequence == -1, get Current
        try {
            if (this.nextSequence == -1) {
                urlString = this.agentURL + "/current?";
            }
            else if(this.nextSequence != -1){
                urlString = this.agentURL +"/sample?from=" + this.nextSequence;
            }
            else { //else append nextSequence to http
                urlString = this.agentURL + "/current?";
            }

            if (this.devicePath != null){
                urlString += "&"+this.devicePath;
            }
            System.out.println(urlString);
            url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Accept", "application/xml");

            //Check the url connection
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                //If okay, get inputstream from connection
                responseStream = urlConnection.getInputStream();
                // parse stream if it's not null
                if (responseStream != null){return this.parseStream(responseStream);}
                else {
                    System.out.println("Not Returning MTConnect (HTTP Okay though?)");
                    return null;
                }
            } else {
                System.out.println("Not Returning MTConnect (HTTP Connection Issue)");
                return null;
            }
        } catch (MalformedURLException | ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace(); //url.Connection.getResponseCode
        }

        return null;
    }

    private List<SourceRecord> getResponseDocumentAsSourceRecords(Document xmlResponseDocument) {
      ArrayList<SourceRecord> records = new ArrayList<>();
      Map<String, String> sourcePartition = new HashMap<String, String>();
        sourcePartition.put(AGENT_URL, this.agentURL);
        sourcePartition.put(DEVICE_PATH, this.devicePath);
      Map<String, Integer> sourceOffset = Collections.singletonMap(NEXT_SEQUENCE, nextSequence);
      String value = this.DOMtoStringWriter(xmlResponseDocument).toString();
      records.add(new SourceRecord(sourcePartition, sourceOffset, topic, null, value));

      return records;
    }

    private static int getNextSequenceNumber(Document doc) {
        NodeList nodes = doc.getElementsByTagName("Header");
        Node node = nodes.item(0);
        Element element = (Element) node;

        return Integer.parseInt(element.getAttribute("nextSequence"));
    }

    private Document parseStream(InputStream responseStream) {

        try {
            Document xmlResponseDocument = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(responseStream);
            return xmlResponseDocument;

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private StringWriter DOMtoStringWriter(Document xmlResponseDocument){
        StringWriter writer = new StringWriter();
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(xmlResponseDocument), new StreamResult(writer));
        }
        catch( TransformerException e){
            e.printStackTrace();
        }

        return writer;
    }


}