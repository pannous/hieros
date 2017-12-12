package nederhof.util.xml;

import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import org.xml.sax.*;

// Simple pretty printer for Xml.
public class XmlPretty {

    public static void print(Document doc, File file) throws IOException {
	try {
	    TransformerFactory factory = SAXTransformerFactory.newInstance();
	    factory.setAttribute("indent-number", new Integer(4));
	    Transformer transformer = factory.newTransformer();
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    DOMSource docDom = new DOMSource(doc);
	    Result docResult = new StreamResult(file);
	    transformer.transform(docDom, docResult);
	} catch (Exception e) {
	    throw new IOException(e.getMessage());
	}
    }

}
