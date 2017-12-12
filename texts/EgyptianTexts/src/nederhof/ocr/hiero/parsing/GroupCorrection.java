package nederhof.ocr.hiero.parsing;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import nederhof.res.*;
import nederhof.util.*;
import nederhof.util.xml.*;

// Where OCR picks up group satisfying pattern, replace it by another pattern,
// and adding penalty.
public class GroupCorrection {

	// A correction is the pattern of an old group,
	// a new group (as string), and penalty to be added.
	public static class Correction {
		public String inPattern;
		public String outGroup;
		public int penalty;
		public Correction(String inPattern, String outGroup, int penalty) {
			this.inPattern = inPattern;
			this.outGroup = outGroup;
			this.penalty = penalty;
		}
		public boolean matches(String inString) {
			return inString.matches(inPattern);
		}
	}

	// Maps number of glyphs, to mapping from string representation of group,
	// to list of corrections (first that fits is taken).
	private static TreeMap<Integer,HashMap<String,LinkedList<Correction>>> correctionMapping = null;

	// Pattern of some arguments (optional).
	public static final String args = "(\\[[^\\[\\]]*\\])?";
	public static final String ops = "[^0-9].*";

	// Constructor.
	public GroupCorrection() {
		if (correctionMapping != null)
			return;
		correctionMapping = new TreeMap<Integer,HashMap<String,LinkedList<Correction>>>();
		readCorrections();
	}

	private static void readCorrections() {
		String correctionsFile = "data/ortho/group_corrections.xml";
		try {
			InputStream in = FileAux.addressToStream(correctionsFile);
			DocumentBuilder parser = SimpleXmlParser.construct(false, false);
			Document doc = parser.parse(in);
			NodeList corrections = doc.getElementsByTagName("correction");
			for (int i = 0; i < corrections.getLength(); i++) 
				readCorrection((Element) corrections.item(i));
			in.close();
		} catch (Exception e) {
			System.err.println("Error reading: " + correctionsFile);
			System.err.println(e.getMessage());
		}
	}

	private static void readCorrection(Element correction) throws IOException {
		String penaltyString = correction.getAttribute("penalty");
		int penalty = 0;
		try {
			penalty = Integer.parseInt(penaltyString);
		} catch (NumberFormatException e) {
			throw new IOException(e);
		}
		NodeList patterns = correction.getElementsByTagName("from");
		if (patterns.getLength() < 1)
			throw new IOException("Correction without from");
		String inPattern = readInPattern((Element) patterns.item(0));

		NodeList groups = correction.getElementsByTagName("to");
		if (groups.getLength() < 1)
			throw new IOException("Correction without to");
		String outGroup = readGroup((Element) groups.item(0));

		String[] names = getNames(inPattern);
		addCorrection(names, inPattern, outGroup, penalty);
	}

	private static String readInPattern(Element pattern) {
		StringBuffer pat = new StringBuffer();
		NodeList elems = pattern.getChildNodes();
		for (int i = 0; i < elems.getLength(); i++) {
			Node node = elems.item(i);
			if (node instanceof Element) {
				Element elem = (Element) node;
				String name = elem.getTagName();
				if (name.equals("args")) 
					pat.append(args);
				else if (name.equals("ops")) 
					pat.append(ops);
			} else if (node instanceof Text) {
				pat.append(Pattern.quote(node.getNodeValue()));
			}
		}
		return pat.toString();
	}

	private static String readGroup(Element group) {
		return group.getNodeValue();
	}

	private static final Pattern gardinerPat =
		Pattern.compile("([A-I]|[K-Z]|Aa|NL|NU)([0-9]+)([a-z]?)");

	private static String[] getNames(String s) {
		Pattern pat = gardinerPat;
		Matcher m = pat.matcher(s);
		Vector<String> names = new Vector<String>();
		while (m.find()) {
			names.add(m.group(0));
		}
		String[] namesAr = new String[names.size()];
		for (int i = 0; i < names.size(); i++)
			namesAr[i] = names.get(i);
		return namesAr;
	}

	// Add correction, consisting of individual glyphs, pattern, new pattern,
	// and panelty to be added.
	private static void addCorrection(String[] names, String inPattern, String outGroup, int penalty) 
			throws IOException {
		int num = names.length;
		if (correctionMapping.get(num) == null)
			correctionMapping.put(num, new HashMap<String,LinkedList<Correction>>());
		HashMap<String,LinkedList<Correction>> stringCorrections = 
			correctionMapping.get(num);
		if (names.length == 0)
			throw new IOException("No name");
		String composed = composeNames(names);
		if (stringCorrections.get(composed) == null)
			stringCorrections.put(composed, new LinkedList<Correction>());
		LinkedList<Correction> corrections = stringCorrections.get(composed);
		corrections.add(new Correction(inPattern, outGroup, penalty));
	}

	// Return the first correction, or null if there is none.
	public Correction correct(ResTopgroup group) {
		Vector<ResNamedglyph> glyphs = group.glyphs();
		int num = glyphs.size();
		if (correctionMapping.get(num) == null)
			return null;
		HashMap<String,LinkedList<Correction>> stringCorrections = 
			correctionMapping.get(num);
		String composed = composeGlyphNames(glyphs);
		if (stringCorrections.get(composed) == null)
			return null;
		LinkedList<Correction> corrections = stringCorrections.get(composed);
		for (Correction corr : corrections)
			if (corr.matches(group.toString()))
				return corr;
		return null;
	}

	// Compose names (one or more) into single string.
	private static String composeNames(String[] names) {
		String s = names[0];
		for (int i = 1; i < names.length; i++)
			s += "-" + names[i];
		return s;
	}
	private static String composeNames(Vector<String> names) {
		String s = names.get(0);
		for (int i = 1; i < names.size(); i++)
			s += "-" + names.get(i);
		return s;
	}
	private static String composeGlyphNames(Vector<ResNamedglyph> glyphs) {
		Vector<String> names = new Vector<String>();
		for (ResNamedglyph named : glyphs)
			names.add(named.name);
		return composeNames(names);
	}

	// For testing.
	public static void main(String[] args) {
		System.out.println("xyz".matches("xyz(\\[[^\\[\\]]*\\])?"));
		System.out.println("xyz[]".matches("xyz(\\[[^\\[\\]]*\\])?"));
		System.out.println("A1*A1[sep=5]".matches("A1\\*A1(\\[[^\\[\\]]*\\])?"));
		System.out.println("N35:[sep=0.5]N35".matches("N35:(\\[[^\\[\\]]*\\])?N35"));
		System.out.println("N35:[sep=0.5]N35:[sep=0.5]N35".matches("N35:[sep=0.5]N35:(\\[[^\\[\\]]*\\])?N35"));
		System.out.println("N35:[sep=0.5]N35:[sep=0.5]N35".matches("N35:\\[sep=0\\.5\\]N35:(\\[[^\\[\\]]*\\])?N35"));
		new GroupCorrection();
	}

}
