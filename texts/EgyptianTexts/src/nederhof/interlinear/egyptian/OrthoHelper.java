// Helper for parsing and printing orthography.

package nederhof.interlinear.egyptian;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.interlinear.egyptian.ortho.*;
import nederhof.util.xml.*;

public class OrthoHelper {

    //////////////////////////////////
    // Reading.

    // Read vector of OrthoElems from XML.
    public static Vector<OrthoElem> parseOrtho(Element elem) 
			throws IOException {
	Vector<OrthoElem> orthos = new Vector();
	NodeList children = elem.getChildNodes();
	for (int i = 0; i < children.getLength(); i++) {
	    Node child = children.item(i);
	    if (child instanceof Element) {
		Element childElem = (Element) child;
		String name = childElem.getTagName();
		String arg = "";
		String val = "";
		Vector<String> args = OrthoElem.argNames(name);
		for (String a : args) {
		    String v = getValue(childElem.getAttributeNode(a));
		    if (!v.equals("")) {
			arg = a;
			val = v;
		    }
		}
		Vector signRanges = new Vector();
		Vector letterRanges = new Vector();
		NodeList rangeChildren = childElem.getChildNodes();
		for (int j = 0; j < rangeChildren.getLength(); j++) {
		    Node range = rangeChildren.item(j);
		    if (range instanceof Element) {
			Element rangeElem = (Element) range;
			parseRange(rangeElem, signRanges, letterRanges);
		    }
		}
		int[][] signRangeArray = vectorToArray(signRanges);
		int[][] letterRangeArray = vectorToArray(letterRanges);
		OrthoElem ortho = OrthoElem.makeOrtho(name, arg, val, 
			signRangeArray, letterRangeArray);
		if (ortho != null)
		    orthos.add(ortho);
	    }
	}
	return orthos;
    }

    // Parse ranges.
    private static void parseRange(Element elem, 
	    Vector signRanges, Vector letterRanges) throws IOException {
	try {
	    String name = elem.getTagName();
	    Attr posAttr = elem.getAttributeNode("pos");
	    Attr lenAttr = elem.getAttributeNode("len");
	    String posStr = getValue(posAttr);
	    String lenStr = getValue(lenAttr);
	    int pos = Integer.parseInt(posStr);
	    int len = 1;
	    if (!lenStr.equals(""))
		len = Integer.parseInt(lenStr);
	    if (name.equals("signs"))
		signRanges.add(new int[] {pos, len});
	    else if (name.equals("letters"))
		letterRanges.add(new int[] {pos, len});
	} catch (NumberFormatException e) {
	    throw new IOException(e.getMessage());
	}
    }

    // Get attribute value or "" if none.
    private static String getValue(Attr attr) {
	return attr == null ? "" : attr.getValue();
    }

    // Turn vector into array.
    private static int[][] vectorToArray(Vector rangeVector) {
	int[][] ranges = new int[rangeVector.size()][];
	for (int i = 0; i < rangeVector.size(); i++)
	    ranges[i] = (int[]) rangeVector.get(i);
	return ranges;
    }

    //////////////////////////////////
    // Writing.

    // Write vector of OrthoElems.
    public static String writeOrtho(Vector<OrthoElem> orthos) {
	orthos = new Vector<OrthoElem>(orthos);
	sort(orthos);
	StringBuffer buf = new StringBuffer();
	for (int i = 0; i < orthos.size(); i++) {
	    OrthoElem ortho = orthos.get(i);
	    buf.append("<" + ortho.name());
	    if (ortho.argName() != null) {
		String val = ortho.argValue();
		if (!val.equals("")) {
		    buf.append(" " + ortho.argName() + "=");
		    buf.append("\"" + XmlAux.escape(val) + "\"");
		}
	    }
	    buf.append(">\n");
	    int[][] signs = ortho.signRanges();
	    int[][] letters = ortho.letterRanges();
	    writeRanges(buf, signs, "signs");
	    writeRanges(buf, letters, "letters");
	    buf.append("</" + ortho.name() + ">\n");
	}
	return buf.toString();
    }

    // Write ranges.
    private static void writeRanges(StringBuffer buf, int[][] ranges, String name) {
	if (ranges != null) {
	    buf.append("  ");
	    for (int i = 0; i < ranges.length; i++) {
		buf.append("<" + name);
		int[] range = ranges[i];
		buf.append(" pos=\"" + range[0] + "\"");
		if (range[1] > 1)
		    buf.append(" len=\"" + range[1] + "\"");
		buf.append("/>");
	    }
	    buf.append("\n");
	}
    }

    ////////////////////////////////////////
    // Editing.

    // Move positions of hieroglyphs and letters.
    public static Vector move(Vector orthos, int signDist, int letterDist) {
	Vector moved = new Vector();
	for (int i = 0; i < orthos.size(); i++) {
	    OrthoElem ortho = (OrthoElem) orthos.get(i);
	    String name = ortho.name();
	    String arg = ortho.argName();
	    String val = ortho.argValue();
	    int[][] signs = ortho.signRanges();
	    int[][] letters = ortho.letterRanges();
	    if (signs != null)
		for (int j = 0; j < signs.length; j++) 
		    signs[j][0] += signDist;
	    if (letters != null)
		for (int j = 0; j < letters.length; j++) 
		    letters[j][0] += letterDist;
	    OrthoElem cloned = OrthoElem.makeOrtho(name, arg, val, signs, letters);
	    moved.add(cloned);
	}
	return moved;
    }

    ////////////////////////////////////////////
    // Sorting.

    public static void sort(Vector<OrthoElem> orthos) {
	Collections.sort(orthos, new SortOrthosBySignOrder());
    }

    /**
     * The comparator class enable the sorting of the collection of ortho
     * elements. The order depends on the first sign the function links to.
     */
    private static class SortOrthosBySignOrder implements Comparator<OrthoElem> {
        public int compare(OrthoElem o1, OrthoElem o2) {
            int[] signs1 = o1.signs();
            int[] signs2 = o2.signs();
            int lowest1 = signs1 == null ? -1 : signs1[0];
            int lowest2 = signs2 == null ? -1 : signs2[0];
            return (lowest1 - lowest2);
        }
    }

}

