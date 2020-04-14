package gov.nist.mtconnect;

import org.apache.kafka.connect.source.SourceRecord;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;

import static java.lang.Thread.sleep;

public class ResponseDocumentDisaggregationTest {
  public static final String AGENT_URL = MTConnectSourceTask.AGENT_URL;
  public static final String DEVICE_PATH = MTConnectSourceTask.DEVICE_PATH;
  public static final String TOPIC_CONFIG = MTConnectSourceTask.TOPIC_CONFIG;

  public static final String TEST_AGENT_URL = "http://mtconnect.mazakcorp.com:5612";
  public static final String TEST_AGENT_MULTIPLE_URLS = "http://mtconnect.mazakcorp.com:5612;http://mtconnect.mazakcorp.com:5609";
  public static final String TEST_DEVICE_PATH = "path=//Device[@name=\"Mazak\"]";
  public static final String TEST_DEVICE_MULTIPLE_PATHS = "path=//Device[@name=\"Mazak\"];path=//Device[@name=\"MFMS10-MC1\"]";
  public static final String TEST_TOPIC_CONFIG = "M80104K162N_XML";
  public static final String TEST_MULTIPLE_TOPICS = "M80104K162N_XML; MAZAK-M77KP290337_XML";

  @Test
  public void test(){

    Document responseDocument = this.getXMLResponseDocument();
    ResponseDocumentDisaggregator disaggregator = new ResponseDocumentDisaggregator();
    HashMap<String, Document> documentHashMap = disaggregator.returnDataItem(responseDocument);
    Set keySet = documentHashMap.keySet();
    Iterator it = keySet.iterator();
    for (HashMap.Entry<String,Document> entry : documentHashMap.entrySet()) {
      String value = this.DOMtoStringWriter((Document) entry.getValue()).toString();
      System.out.println("Key: " + entry.getKey() +"; Value: " + value);
    }
  }

  @Test
  public void testFullPath() throws XPathExpressionException, ParserConfigurationException {

    Document responseDocument = this.getXMLResponseDocument();
    ResponseDocumentDisaggregator disaggregator = new ResponseDocumentDisaggregator();
    HashMap<String, Document> documentHashMap = disaggregator.returnFullPath(responseDocument);
    for (HashMap.Entry<String,Document> entry : documentHashMap.entrySet()) {
      String value = this.DOMtoStringWriter((Document) entry.getValue()).toString();
      System.out.println("Key: " + entry.getKey() +"; Value: " + value);
    }
  }

  private Document getXMLResponseDocument() {
    HttpURLConnection urlConnection;
    String urlString;
    URL url;
    InputStream responseStream = null;
    // Query the last known NextSequence Number
    // if nextSequence == -1, get Current
    try{
      urlString = this.TEST_AGENT_URL + "/current?";
      urlString += "&"+this.TEST_DEVICE_PATH;

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
        if (responseStream != null) {
          return this.parseStream(responseStream);
        }
      }
    } catch (MalformedURLException | ProtocolException e) {
      e.printStackTrace();
    } catch (IOException e){
      e.printStackTrace(); //url.Connection.getResponseCode
    }

    return null;
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
