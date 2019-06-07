package com.github.djharten.test_xml;

import org.w3c.dom.Node;

public class Main {

    /*
     * Create a document using the mazakorp.mtconnect probe page and return it as a normalized document,
     * grab the root node of the XML tree, then parse through the tree, grabbing and printing all data.
     */
    public static void main(String[] args) throws Exception {
        Node node = createDocumentConnector();
        NodeProcessor processor = new NodeProcessor(node);
        //printDataConsole(processor);
        long start = System.nanoTime();
        parseToCurrent(processor);
        long end = System.nanoTime();
        long dif = (end - start) / 1_000_000_000;
        System.out.println(dif);
    }

    private static Node createDocumentConnector() throws Exception {
        DocumentConnector doc = new DocumentConnector();
        doc.createConnection();
        doc.createDocument();
        return doc.getDocument().getDocumentElement();
    }

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

    private static void parseToCurrent(NodeProcessor processor) throws Exception {
        printDataConsole(processor);
        int startSequence = processor.getFirstSequence() + 10;
        int dif = -1;
        while (dif != 0) {
            String newURL = "http://mtconnect.mazakcorp.com:5609/sample?from=" + startSequence;
            Node node = createDocumentConnector(newURL);
            processor = new NodeProcessor(node);
            printDataConsole(processor);
            int endSequence = processor.getLastSequence();
            dif = Math.abs(startSequence - endSequence);
            if (startSequence % 1000 == 0)
                System.out.println(startSequence + " " + endSequence + " dif: " + dif);
            if (dif != 0)
                startSequence++;
        }
    }

}