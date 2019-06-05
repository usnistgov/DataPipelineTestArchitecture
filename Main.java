package com.github.djharten.test_xml;

//import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class Main {

    /*
     * Global that keeps track of the level of the tree we are in. Just used for aesthetics.
     */
    private static String dashes = "";

    /*
     * Create a document using the mazakorp.mtconnect probe page and return it as a normalized document,
     * grab the root node of the XML tree, then parse through the tree, grabbing and printing all data.
     */
    public static void main(String[] args) throws Exception {
        Document doc = createDocument();
        Node node = doc.getDocumentElement();
        deviceRootData(node);
        parseTree(node);
    }

    /*
     * Open up an HTTP request connection to the mazakorp page, grab the XML data from the probe page, normalize it and
     * return it.
     */
    private static Document createDocument() throws Exception {
        //File xmlFile = new File("C:\\Users\\djh8\\IdeaProjects\\Testing\\src\\com\\github\\djharten\\test_xml\\probe.xml");
        URL url = new URL("http://mtconnect.mazakcorp.com:5609/probe");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/xml");
        InputStream is = connection.getInputStream();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(is);
        doc.normalizeDocument();
        return doc;
    }

    /*
     * First we check if the nodeName is equal to "#text". If it is, this is actually an empty child node
     * that was created due to unnecessary whitespace in the XML document(can validate the XML doc with
     * https://www.xmlvalidation.com/ to see). We also check if the node has no attributes. If either of these
     * cases are true we return, otherwise we grab each key-value pair in the element and print it out.
     */
    private static void listNodeData(Node node) {
        String nodeName = node.getNodeName();
        if(nodeName.equals("#text") || node.getAttributes() == null )
            return;

        System.out.println(dashes + nodeName);

        NamedNodeMap attributes = node.getAttributes();
        for(int i = 0; i < attributes.getLength(); i++){
            Attr attr = (Attr) attributes.item(i);
            String attrName = attr.getNodeName();
            String attrVal = attr.getNodeValue();
            System.out.println(dashes + "----[Attr]: " + attrName + ", [Val]: " + attrVal);
        }
    }

    /*
     * Helper method that gives us the data for the root of the tree.
     */
    private static void deviceRootData(Node node) {
        listNodeData(node);
        dashes += "--";
    }

    /*
     * Checks if our node is null, if so we have exhausted that branch of the tree and return. Otherwise,
     * create a list of the current node's children. Go through the tree, grabbing each child, printing out
     * their data, and working through the tree until the list is exhausted. After each branch is exhausted
     * we check the tree's last node to see if their is text content.
     */
    private static void parseTree(Node node) {
        if (node == null)
            return;

        NodeList nList = node.getChildNodes();

        for (int i = 0; i < nList.getLength(); i++) {
            Node child = nList.item(i);
            listNodeData(child);
            if(child.hasChildNodes()) {
                dashes += "--";
                parseTree(child);
            }
            checkLastChildForText(child);
        }
        dashes = dashes.substring(0, dashes.length() - 2);
    }

    /*
     * Checks a leaf node in a tree for text content data in the node. Fixes whitespace issues. If
     * anyone says I wrote this I will vehemently deny it...please don't tell people how I live.
     */
    private static void checkLastChildForText(Node node) {
        if(node.getNodeType() == Node.TEXT_NODE) {
            String textEle = node.getTextContent().replace("\n", "").replace("\r", "").replace(" ", "");
            if(textEle.length() > 0)
                System.out.println(dashes + "----[Contents]: " + textEle);
        }
    }

}
