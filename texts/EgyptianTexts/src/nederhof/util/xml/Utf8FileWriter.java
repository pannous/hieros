package nederhof.util.xml;

import java.io.*;

public class Utf8FileWriter extends PrintWriter {

    // Open file for writing XML.
    // Write header.
    // Make sure UTF-8 is used.
    public Utf8FileWriter(File file) throws IOException {
	super(new OutputStreamWriter(
		    new FileOutputStream(file), "UTF-8"));
    }

}
