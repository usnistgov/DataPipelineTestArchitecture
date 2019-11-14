package deprecated;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * Creates the connection to the Mazak test bed, retrieves the XML data and turns it into
 * DocumentBuilder object.
 */
final class DocumentConnector {

    private static final String INITIAL_URL = "http://mtconnect.mazakcorp.com:5609/current";
    private static final String SEQUENCE_URL = "http://mtconnect.mazakcorp.com:5609/sample?from=";
    private String currentURL;
    private URL url;
    private InputStream is;
    private Document doc;

    /*
     * Initial connection is to the most recently updated mtconnect document.
     */
    DocumentConnector() throws Exception {
        this(INITIAL_URL);
    }

    /*
     * Creates a new connection to a new url with an entire URL passed through as a String.
     */
    DocumentConnector(String urlString) throws Exception {
        this.currentURL = urlString;
        this.url = new URL(this.currentURL);
    }

    /*
     * Creates a new connection to a new url with a sequence number passed through.
     */
    DocumentConnector(int sequenceNumber) throws Exception {
        this.currentURL = SEQUENCE_URL + sequenceNumber;
        this.url = new URL(this.currentURL);
    }

    /*
     * Establishes connection to the page and puts the connection into an input stream.
     */
    void createConnection() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/xml");
        this.is = connection.getInputStream();
    }

    /*
     * Grabs the XML data from the page we are connected to and puts it into a Document object.
     */
    void createDocument() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        this.doc = db.parse(is);
        doc.normalizeDocument();
    }

    /**************************************/
    /* GETTERS AND SETTERS */
    /**************************************/

    Document getDocument(){
        return doc;
    }

}
