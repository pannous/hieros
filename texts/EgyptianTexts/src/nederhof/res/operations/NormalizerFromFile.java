package nederhof.res.operations;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.res.*;
import nederhof.util.*;
import nederhof.util.xml.*;

// Transforming by applying substitutions according to XML file.
// The mappings implemented in subclasses.
public abstract class NormalizerFromFile extends ResNormalizer {

	protected ResComposer composer = new ResComposer();

	// Constructor, taking XML file containing mapping.
	public NormalizerFromFile(String url) {
		loadMapping(url);
	}

	protected ResTopgroup normalize(ResNamedglyph glyph) {
		glyph = (ResNamedglyph) super.normalize(glyph);
		String mapped = getMapping().get(glyph.name);
		return mapped == null ? glyph : composer.makeTopgroup(mapped);
	}

	// Load mapping from XML file.
	public void loadMapping(String url) {
		if (getMapping() != null)
			return;
		initializeMapping();
		try {
			DocumentBuilder parser = SimpleXmlParser.construct(false, false);
			InputStream in = FileAux.addressToStream(url);
			Document doc = parser.parse(in);
			NodeList transforms = doc.getElementsByTagName("transform");
			for (int i = 0; i < transforms.getLength(); i++) {
				Element transform = (Element) transforms.item(i);
				String fromName = transform.getAttribute("from");
				String toExpr = transform.getAttribute("to");
				getMapping().put(fromName, toExpr);
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Could not read: " + url);
			System.err.println(e.getMessage());
		}
	}

	// Subclass to implement mapping.
	protected abstract void initializeMapping();
	protected abstract TreeMap<String,String> getMapping();

}
