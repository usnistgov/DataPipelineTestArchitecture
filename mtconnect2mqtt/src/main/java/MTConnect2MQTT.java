package main.java;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.junit.Test;

/*
 * This gets all of the data from the Mazak Testbed and publishes it into MQTT. It establishes
 * a connection to the website, goes through all needed sequence numbers via GET requests, and
 * takes the XML data, publishing it to MQTT.
 */
public class MTConnect2MQTT {
    private static final String agentURL = "http://mtconnect.mazakcorp.com:5609";
    private static final String devicePath = "path=//Device[@name=\"MFMS10-MC1\"]";
    private static final String topic = "MAZAK-M77KP290337";
    private Integer nextSequence = -1;

    private static final String broker = "tcp://test.mosquitto.org:1883";
    private static final String clientId = "mqtt-1";
    @Test
    public void start() throws Exception {

        /*
         * Establish a new MqttPublisher to open the connection to Mosquitto.
         */
        MqttPublisher publisher = new MqttPublisher(broker, clientId, topic);

        while(true) {
            Document xmlResponseDocument = this.getXMLResponseDocument();

            if (xmlResponseDocument != null){
                String xmlResponseDocumentType = xmlResponseDocument.getDocumentElement().getNodeName();
                System.out.println("Response Document Type: " + xmlResponseDocumentType);
                if (xmlResponseDocumentType.equals("MTConnectStreams")){
                    // Store the Next Sequence Number
                    this.nextSequence = getNextSequenceNumber(xmlResponseDocument);
                    publisher.createConnection();
                    publisher.publishMessage(this.DOMtoByteArray(xmlResponseDocument).toByteArray());
                    publisher.disconnectPublisher();
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
        }
    }


    private int getNextSequenceNumber(Document doc) {
        NodeList nodes = doc.getElementsByTagName("Header");
        Node node = nodes.item(0);
        Element element = (Element) node;

        return Integer.parseInt(element.getAttribute("nextSequence"));
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

    private ByteArrayOutputStream DOMtoByteArray(Document xmlResponseDocument){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(xmlResponseDocument), new StreamResult(bos));
        }
        catch( TransformerException e){
            e.printStackTrace();
        }

        return bos;
    }
}
