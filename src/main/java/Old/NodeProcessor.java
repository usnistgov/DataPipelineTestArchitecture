import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class NodeProcessor {

    private Node root;
    private static String dashes = "";
    private static int firstSequence;
    private int lastSequence;
    private boolean printData;

    NodeProcessor(Node root){
        this.root = root;
        this.printData = true;
    }

    /*
     * First we check if the nodeName is equal to "#text". If it is, this is actually an empty child node
     * that was created due to unnecessary whitespace in the XML document(can validate the XML doc with
     * https://www.xmlvalidation.com/ to see). We also check if the node has no attributes. If either of these
     * cases are true we return, otherwise we grab each key-value pair in the element and print it out.
     */
    private void listNodeData(Node node) {
        String nodeName = node.getNodeName();
        if(nodeName.equals("#text") || node.getAttributes() == null )
            return;
        StringBuilder data = processNodeData(node);
        if(this.printData){
            System.out.println(dashes + nodeName);
            System.out.print(data);
        }
    }

    private StringBuilder processNodeData(Node node) {
        StringBuilder sb = new StringBuilder();
        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attr = (Attr) attributes.item(i);
            String attrName = attr.getNodeName();
            String attrVal = attr.getNodeValue();
            checkSequence(attrName, attrVal);
            if(this.printData)
                sb.append(dashes).append("----[Attr]: ").append(attrName).append(" , [Val]: ").append(attrVal).append("\n");
        }
        return sb;
    }

    private void checkSequence(String name, String val){
        if(name.equals("firstSequence"))
            firstSequence = Integer.parseInt(val);
        else if(name.equals("lastSequence"))
            this.lastSequence = Integer.parseInt(val);
    }

    /*
     * Checks if our node is null, if so we have exhausted that branch of the tree and return. Otherwise,
     * create a list of the current node's children. Go through the tree, grabbing each child, printing out
     * their data, and working through the tree until the list is exhausted. After each branch is exhausted
     * we check the tree's last node to see if their is text content.
     */
    void parseTree(Node node) {
        if (node == null)
            return;

        NodeList nList = node.getChildNodes();
        for (int i = 0; i < nList.getLength(); i++) {
            Node child = nList.item(i);
            listNodeData(child);
            if (child.hasChildNodes()) {
                dashes += "--";
                parseTree(child);
            }
            checkLastChildForText(child);
        }
        if(dashes.length() != 0)
            dashes = dashes.substring(0, dashes.length() - 2);
    }

    /*
     * Checks a leaf node in a tree for text content data in the node. Fixes whitespace issues. If
     * anyone says I wrote this I will vehemently deny it...please don't tell people how I live.
     */
    private void checkLastChildForText(Node node) {
        if(node.getNodeType() == Node.TEXT_NODE) {
            String textEle = node.getTextContent().replace("\n", "").replace("\r", "").replace(" ", "");
           if(textEle.length() > 0 && this.printData)
               System.out.println(dashes + "----[Contents]: " + textEle);
        }
    }

    /**************************************/
    /* GETTERS AND SETTERS */
    /**************************************/
    Node getRootNode() {
        return this.root;
    }

    void getRootData() {
        listNodeData(root);
    }

    void setDashes(){
        dashes = "";
    }

    int getFirstSequence(){
        return firstSequence;
    }

    int getLastSequence() {
        return lastSequence;
    }

    int getSequenceDifference(int first){
        return Math.abs(lastSequence - first);
    }

    void setPrintData(boolean bool){
        this.printData = bool;
    }
}
