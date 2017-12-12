package nederhof.util.xml;

import java.io.*;

public class HtmlFileWriter extends PrintWriter {

    // Open file for writing HTML.
    // Write header.
    // Make sure UTF-8 is used.
    public HtmlFileWriter(File file) throws IOException {
	super(new OutputStreamWriter(
		    new FileOutputStream(file), "UTF-8"));
	println("<html>");
    }

    // Make footer before closing.
    public void close() {
	println("</html>");
	super.close();
    }

}
