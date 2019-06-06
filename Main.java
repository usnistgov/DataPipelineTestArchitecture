package com.github.djharten.test_xml;

import org.w3c.dom.*;

public class Main {

    /*
     * Create a document using the mazakorp.mtconnect probe page and return it as a normalized document,
     * grab the root node of the XML tree, then parse through the tree, grabbing and printing all data.
     */
    public static void main(String[] args) throws Exception {
        //Node node = createDocumentConnector();
        //printDataConsole(node);
        long start = System.nanoTime();
        parseToCurrent();
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
    private static void printDataConsole(Node node){
        NodeProcessor processor = new NodeProcessor(node);
        processor.getRootData();
        processor.parseTree(node);
        processor.setDashes();
    }

    private static void parseToCurrent() throws Exception {
        Node node = createDocumentConnector();
        printDataConsole(node);
        int startSequence = NodeProcessor.getFirstSequence() + 500;
        boolean caughtUp = false;
        while(!caughtUp){
            NodeProcessor processor = new NodeProcessor(node);
            processor.getRootData();
            processor.parseTree(node);
            processor.setDashes();
            int endSequence = processor.nextSequence;
            int dif = Math.abs(endSequence - startSequence);
            if(startSequence % 1000 == 0)
                System.out.println(startSequence + " " + endSequence + " dif: " + dif);
            if(dif < 500)
                caughtUp = true;
            else {
                String newURL = "http://mtconnect.mazakcorp.com:5609/sample?from=" + startSequence;
                startSequence++;
                node = createDocumentConnector(newURL);
            }
        }
    }

}