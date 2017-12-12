package nederhof.ocr.hiero.admin;

import java.awt.geom.*;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import nederhof.util.*;
import nederhof.util.xml.*;

public class NonHiero {

	// Symbols to be recognized in OCR that are not hieroglyphs.
	private static Vector<String> extras = null;
	// Boxes.
	private static Vector<String> boxes = null;
	
	public static Vector<String> getExtras() {
		if (extras == null)
			readExtras();
		return extras;
	}
	public static Vector<String> getBoxes() {
		if (extras == null)
			readExtras();
		return boxes;
	}

	// Extra signs, to be read only once.
    private static void readExtras() {
        final String extraFile = "data/ortho/extra_signs.xml";
        extras = new Vector<String>();
        boxes = new Vector<String>();
        try {
            InputStream in = FileAux.addressToStream(extraFile);
            DocumentBuilder parser = SimpleXmlParser.construct(false, false);
            Document doc = parser.parse(in);
            NodeList boxNodes = doc.getElementsByTagName("box");
            for (int i = 0; i < boxNodes.getLength(); i++) {
                Element extra = (Element) boxNodes.item(i);
                String name = extra.getAttribute("name");
                extras.add(name);
                boxes.add(name);
            }
            NodeList extraNodes = doc.getElementsByTagName("extra");
            for (int i = 0; i < extraNodes.getLength(); i++) {
                Element extra = (Element) extraNodes.item(i);
                String name = extra.getAttribute("name");
                extras.add(name);
            }
            in.close();
        } catch (Exception e) {
            System.err.println("Could not read: " + extraFile);
            System.err.println(e.getMessage());
        }
    }

	// Hieroglyphs that look like circle for line numbers.
	public static final String[] circleLike = 
	{ 
		"N5",
		"O50",
		"S21",
		"X6"
	};

	// Is name a non-hieroglyph ? Then does not start with capital letter.
	public static boolean isExtra(String name) {
		return !Character.isUpperCase(name.charAt(0));
	}

	// Is box?
	public static boolean isBox(String name) {
		return getBoxes().contains(name);
	}

	// Height of extra.
	// Give (approximate) height of non-hieroglyph.
	public static float height(String name) {
		if (name.equals("shade"))
			return 1f;
		else if (name.equals("unk"))
			return 0.7f;
		else if (name.matches("circle.*"))
			return 0.5f;
		else if (name.matches("^[0-9][0-9]*$"))
			return 0.4f;
		else if (name.equals("openbr") || name.equals("closebr"))
			return 1.0f;
		else
			return 0.3f;
	}

	// Is part of line number?
	public static boolean inLineNumber(String part) {
		return part.matches("^circle.*$") ||
			part.matches("^[0-9]$");
	}

	// Put circles and digits together into line number.
	public static String assembleLineNumber(Vector<String> parts) {
		// digits:
		String s1 = "";
		String s2 = "";
		String s3 = "";
		int prefLen = "circle".length();
		for (String part : parts) {
			if (part.matches("^circle[1-9][0-9_]?$"))
				s1 = "" + part.charAt(prefLen);
			if (part.matches("^circle[1-9_][0-9]$")) 
				s2 = "" + part.charAt(prefLen+1);
		}
		for (String part : parts) 
			if (part.matches("^[0-9]$")) {
				if (s1.equals(""))
					s1 = part;
				else if (s2.equals(""))
					s2 = part;
				else
					s3 = part;
			}
		return s1 + s2 + s3;
	}

	/////////////////////////////////////////////////////////
	// Modifiers.

	// Allowable modifiers of glyphs.
	public static final String[] modifiers = 
	{ 
		"[rotate=90]", 
		"[rotate=180]", 
		"[rotate=270]", 
		"[mirror]",
		"[part]"
	};
	// Is modifier (excluding empty).
	public static boolean isMod(String s) {
		for (int i = 0; i < modifiers.length; i++)
			if (modifiers[i].equals(s))
				return true;
		return false;
	}

	// Is there modifier in name?
	public static boolean hasMod(String s) {
		return s.indexOf('[') >= 0;
	}

	// Translate external (short) form of modifiers to internal (long).
	public static String modShortToLong(String s) {
		s = s.replaceAll("\\[90\\]", "[rotate=90]");
		s = s.replaceAll("\\[180\\]", "[rotate=180]");
		s = s.replaceAll("\\[270\\]", "[rotate=270]");
		s = s.replaceAll("\\[m\\]", "[mirror]");
		s = s.replaceAll("\\[p\\]", "[part]");
		return s;
	}
	// Converse.
	public static String modLongToShort(String s) {
		if (!NonHiero.isExtra(s)) {
			s = s.replaceAll("rotate=", "");
			s = s.replaceAll("mirror", "m");
			s = s.replaceAll("part", "p");
		}
		return s;
	}

}
