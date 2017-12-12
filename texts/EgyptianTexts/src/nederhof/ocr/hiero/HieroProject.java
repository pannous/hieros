package nederhof.ocr.hiero;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.ocr.*;

public class HieroProject extends Project {

	// Make project from directory.
	public HieroProject(File location) throws IOException {
		super(location);
	}

	protected void processFormat(Element formatEl,
			Vector<LineFormat> formatted) throws IOException {
		String type = formatEl.getAttribute("type");
		String val = formatEl.getAttribute("val");
		if (type.equals("res")) {
			ResFormat format = new ResFormat(val);
			processNote(formatEl, format);
			formatted.add(format);
		}
		else if (type.equals("num"))
			formatted.add(new NumFormat(val));
	}

    // Process notes for RES element.
    protected void processNote(Element formatEl, ResFormat format)
                throws IOException {
        NodeList children = formatEl.getElementsByTagName("note");
        for (int i = 0; i < children.getLength(); i++) {
            Element noteEl = (Element) children.item(i);
            int sym = strToInt(noteEl.getAttribute("symbol"));
            String text = noteEl.getAttribute("text");
            format.setNote(sym, text);
        }
    }

	protected void getFormat(Document doc, Element lineEl,
			Vector<LineFormat> formatted) throws IOException {
		for (LineFormat form : formatted) {
			Element formatEl = doc.createElement("format");
			formatEl.setAttribute("val", form.getVal());
			if (form instanceof ResFormat) {
				ResFormat resForm = (ResFormat) form;
				formatEl.setAttribute("type", "res");
				getNotes(doc, formatEl, resForm);
			} else {
				formatEl.setAttribute("type", "num");
			}
			lineEl.appendChild(formatEl);
		}
	}

    protected void getNotes(Document doc, Element formatEl, ResFormat resForm) {
        for (Map.Entry<Integer,String> pair : resForm.getNotes().entrySet())
            if (pair.getValue() != null && !pair.getValue().matches("\\s*")) {
                Element noteEl = doc.createElement("note");
                noteEl.setAttribute("symbol", "" + pair.getKey());
                noteEl.setAttribute("text", pair.getValue());
                formatEl.appendChild(noteEl);
            }
    }

}
