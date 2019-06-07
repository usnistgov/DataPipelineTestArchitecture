package com.github.djharten.test_xml;

import org.w3c.dom.Node;

public class Main {

    /*
     * Establishes an initial connection at http://mtconnect.mazakcorp.com:5609/current, then times various tests.
     */
    public static void main(String[] args) throws Exception {
        Node node = createDocumentConnector();
        NodeProcessor processor = new NodeProcessor(node);
        //printDataConsole(processor);
        long start = System.nanoTime();
        parseToCurrent(processor);
        //traverseSequenceList(processor);
        long end = System.nanoTime();
        long dif = (end - start);
        System.out.println(dif);
    }

    /*
     * Default method that establishes a new connection at http://mtconnect.mazakcorp.com:5609/current,
     * takes the XML page from that site and turns it into a normalized Document. We then return the root
     * node of that XML tree.
     */
    private static Node createDocumentConnector() throws Exception {
        DocumentConnector doc = new DocumentConnector();
        doc.createConnection();
        doc.createDocument();
        return doc.getDocument().getDocumentElement();
    }

    /*
     * Creates a document with a specified URL. This is called after we grab the initial page from
     * http://mtconnect.mazakcorp.com:5609/current, each subsequent call to create a connection is
     * at http://mtconnect.mazakcorp.com:5609/sample?from=xxx, where xxx = the next sequence number.
     * We then return the root node of that XML tree.
     */
    private static Node createDocumentConnector(String url) throws Exception {
        DocumentConnector doc = new DocumentConnector(url);
        doc.createConnection();
        doc.createDocument();
        return doc.getDocument().getDocumentElement();
    }

    /*
     * This option prints out all of the parsed XML data in the console. Mainly used for testing purposes.
     */
    private static void printDataConsole(NodeProcessor processor){
        processor.getRootData();
        processor.parseTree(processor.getRootNode());
        processor.setDashes();
    }

    /*
     * Starting at the current XML page, we parse through the document and find the first and last sequence, then
     * go back to the first sequence + 10(a buffer to grab the page in time, since it can update 3-5 times / second),
     * and grab, then parse all of the deltas up until we get to the end sequence, updating the end sequence after
     * each iteration. We then have all the available data. Used to time how long it will take us to make the
     * connection, grab the XML data, and parse through the data strictly in Java.
     */
    private static void parseToCurrent(NodeProcessor processor) throws Exception {
        printDataConsole(processor);
        int firstSequence = processor.getFirstSequence();
        while (processor.getSequenceDifference(firstSequence) != 0) {
            String newURL = "http://mtconnect.mazakcorp.com:5609/sample?from=" + (firstSequence + 10);
            Node node = createDocumentConnector(newURL);
            processor.setRootNode(node);
            //processor.setPrintData(false);
            printDataConsole(processor);
            if (firstSequence % 1000 == 0)
                System.out.println(firstSequence + " " + processor.getLastSequence() + " dif: " + processor.getSequenceDifference(firstSequence));
            firstSequence++;
        }
    }

    /*
     * Starting at the current XML page, we parse through the document and find the first and last sequence, then
     * go back to the first sequence + 10(a buffer to grab the page in time, since it can update 3-5 times / second),
     * and continue making connections and grabbing each XML page up to the end sequence until we reach the last one.
     * Used to time how long it will take us to make the connection and grab the XML data strictly in Java.
     * Does NOT parse through the data.
     *
     * Note: This also does not update the endSequence value because that would involve parsing the data, however
     * these testing speeds can be compared against ones that do parse when the machine is turned off and not
     * updating(roughly 130k pages).
     */
    private static void traverseSequenceList(NodeProcessor processor) throws Exception {
        //processor.setPrintData(false);
        printDataConsole(processor);
        int firstSequence = processor.getFirstSequence();
        while(processor.getSequenceDifference(firstSequence) != 0){
            String newURL = "http://mtconnect.mazakcorp.com:5609/sample?from=" + (firstSequence + 10);
            Node node = createDocumentConnector(newURL);
            if (firstSequence % 1000 == 0)
                System.out.println(firstSequence + " " + processor.getLastSequence() + " dif: " + processor.getSequenceDifference(firstSequence));
            firstSequence++;
        }
    }

}