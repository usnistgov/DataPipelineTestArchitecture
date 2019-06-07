package com.github.djharten.test_xml;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

final class DocumentConnector {

    private static final String INITIAL_URL = "http://mtconnect.mazakcorp.com:5609/current";
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
     * Creates a new connection to a new url.
     */
    DocumentConnector(String urlString) throws Exception {
        this.currentURL = urlString;
        this.url = new URL(urlString);
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

    String getURL(){
        return currentURL;
    }

    void setURL(String url) {
        this.currentURL = url;
    }

    Document getDocument(){
        return doc;
    }

    void setDocument(Document doc) {
        this.doc = doc;
    }
}
