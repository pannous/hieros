package nederhof.util.xml;

import java.io.*;

public class XmlFileWriter extends Utf8FileWriter {

    // Header we will use here.
    public static final String header = 
	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    // Open file for writing XML in UTF-8.
    // Write header.
    public XmlFileWriter(File file) throws IOException {
	super(file);
	println(header);
    }

}
