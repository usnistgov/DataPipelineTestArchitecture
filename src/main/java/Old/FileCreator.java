import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

class FileCreator {

        private DocumentConnector doc;
        private final String dir = "C:\\Users\\djh8\\IdeaProjects\\testing_xml\\files\\";


    FileCreator() throws Exception {
            this.doc = new DocumentConnector();
            this.doc.createConnection();
    }

    void writeToFile(int sequence) throws Exception {
        ReadableByteChannel readableByteChannel = Channels.newChannel(doc.getURLObject().openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(dir + sequence + ".xml");
        FileChannel fileChannel = fileOutputStream.getChannel();
        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        System.out.println("Created file: " + sequence);
    }


}
