package gov.nist.mtconnect;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.util.HashMap;

public class ResponseDocumentDisaggregator {

    public HashMap<String,Document> returnDataItem(Document input) throws XPathExpressionException, ParserConfigurationException {
        HashMap<String,Document> documentHashMap= new HashMap<>();
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        XPathExpression expr = xPath.compile("//*[parent::Samples|parent::Events|parent::Condition]");
        Object result = expr.evaluate(input, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); i++) {
            Element nodeAsElement = (Element) nodes.item(i);
            String dataItemId = nodeAsElement.getAttribute("dataItemId");
            Document newXmlDocument = dbf.newDocumentBuilder().newDocument();
            Node imported = newXmlDocument.importNode(nodes.item(i), true);
            newXmlDocument.appendChild(imported);
            documentHashMap.put(dataItemId, newXmlDocument);
        }

        return documentHashMap;
    }

    public HashMap<String,Document> returnFullPath(Document input) throws XPathExpressionException, ParserConfigurationException {

        HashMap<String,Document> documentHashMap= new HashMap<>();
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();


        XPathExpression expr = xPath.compile("//*[parent::Samples|parent::Events|parent::Condition]");
        Object result = expr.evaluate(input, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;


        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String dataItemId = ((Element) node).getAttribute("dataItemId");

            Document newXmlDocument = dbf.newDocumentBuilder().newDocument();
            newXmlDocument.appendChild(newXmlDocument.importNode(input.getDocumentElement(), true));
            expr = xPath.compile("//*[@dataItemId = \"" + dataItemId + "\"]");
            result = expr.evaluate(newXmlDocument, XPathConstants.NODESET);
            node = ((NodeList) result).item(0);

            Node sibling;
            while (node.getParentNode() != null) {
                while (node.getNextSibling() != null) {
                    sibling = node.getNextSibling();
                    node.getParentNode().removeChild(sibling);
                }
                while (node.getPreviousSibling() != null) {
                    sibling = node.getPreviousSibling();
                    node.getParentNode().removeChild(sibling);
                }
                node = node.getParentNode();
            }
            documentHashMap.put(dataItemId, newXmlDocument);
        }

        return documentHashMap;
    }
}
