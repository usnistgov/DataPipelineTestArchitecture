import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

class FileReader {

    private File folder = new File("C:\\Users\\djh8\\IdeaProjects\\testing_xml\\files\\");

    FileReader() {

    }

    void readFromFile() throws Exception {
        int count = 0;
        for(File fileEntry : folder.listFiles()){
            Document doc = createDocumentFromFile(fileEntry);
            Node node = doc.getDocumentElement();
            TestDriver test = new TestDriver(node);
            test.printDataConsole();
            count++;
            if(count % 100 == 0)
                System.out.println("Files read: " + count);
        }
    }

    private Document createDocumentFromFile(File fileEntry) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(fileEntry);
        doc.normalizeDocument();
        return doc;
    }
}
