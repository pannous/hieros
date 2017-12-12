package nederhof.res.operations;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.res.*;
import nederhof.util.*;
import nederhof.util.xml.*;

// Changing ResFragment by removing certain named glyphs.
// The names are from a file.

public abstract class NormalizerRemoveFromFile extends NormalizerRemoveSpecial {

    // Constructor, taking XML file containing set.
    public NormalizerRemoveFromFile(String url) {
	loadSet(url);
    }

    protected boolean isSpecial(ResNamedglyph glyph) {
	return getSet().contains(glyph.name);
    }

    protected ResTopgroup makeSpecial(ResSwitch after) {
	String name = getSet().isEmpty() ? "\"?\"" : getSet().first();
	ResNamedglyph named = new ResNamedglyph(name, after);
	return named;
    }

    // Load mapping from XML file.
    public void loadSet(String url) {
        if (getSet() != null)
            return;
        initializeSet();
        try {
            DocumentBuilder parser = SimpleXmlParser.construct(false, false);
            InputStream in = FileAux.addressToStream(url);
            Document doc = parser.parse(in);
            NodeList signs = doc.getElementsByTagName("sign");
            for (int i = 0; i < signs.getLength(); i++) {
                Element sign = (Element) signs.item(i);
                String name = sign.getAttribute("name");
                getSet().add(name);
            }
            in.close();
        } catch (Exception e) {
            System.err.println("Could not read: " + url);
            System.err.println(e.getMessage());
        }
    }

    // Subclass to implement.
    protected abstract void initializeSet();
    protected abstract TreeSet<String> getSet();
}
