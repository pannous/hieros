/***************************************************************************/
/*                                                                         */
/*  ParsingHelper.java                                                     */
/*                                                                         */
/*  Copyright (c) 2009 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of PhilologEG, and may only be */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Helper for parsing files.

package nederhof.interlinear.egyptian;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.interlinear.*;
import nederhof.interlinear.labels.*;
import nederhof.util.xml.*;

public class ParsingHelper {

    // One from: hi, al, tr, lx, note.
    private String type;

    // Is the output in lite format?
    // For the lite format we abbreviate
    // <pos id="3"/> -> <@3> or <@"3">
    // <coord id="23"/> -> <23> or <"23">
    // <pre id="23"/> -> <23^pre> or <"23"^pre>
    // <post id="23"/> -> <23^post> or <"23"^post>
    // The form with " and " is to be used if 
    // * id contains white-space, @-sign, ^-sign, or if
    // * id does not contain any digit.
    // <prec id1="23" id2="24"/> is abbreviated to
    // <> 23 24 or <> "23" "24"
    // <prec id1="23" type="after" id2="24"/> is abbreviated to
    // << 23 24 or << "23" "24"
    private boolean lite = false;

    // Create helper for type.
    private ParsingHelper(String type, boolean lite) {
	this.type = type;
	this.lite = lite;
    }
    private ParsingHelper(String type) {
	this(type, false);
    }

    // Analyse part of XML file.
    private Vector getParts(Element elem) throws IOException {
	Vector items = new Vector();
	NodeList children = elem.getChildNodes();
	for (int i = 0; i < children.getLength(); i++) {
	    Node child = children.item(i);
	    if ((child instanceof CharacterData) && isTypeForData()) {
		CharacterData data = (CharacterData) child;
		String dataString = data.getData();
		dataString = dataString.replaceAll("\\s\\s*", " ");
		if (i == 0)
		    dataString = dataString.replaceAll("^\\s*", "");
		if (i == children.getLength() - 1)
		    dataString = dataString.replaceAll("\\s*$", "");
		if (type.equals("hi") && !dataString.matches("\\s*")) 
		    items.add(new HiPart(dataString, false));
		else if (type.equals("al"))
		    items.addAll(lowerUpperAlParts(dataString, type.equals("note")));
		else if (type.equals("tr"))
		    items.add(new NoPart(dataString));
		else if (type.equals("note"))
		    items.add(new NoPart(dataString));
	    } else if (child instanceof Element) {
		Element childElem = (Element) child;
		String tagName = childElem.getTagName();
		if (tagName.equals("hi") && isTypeForHi()) 
		    items.add(
			    new HiPart(data(childElem), type.equals("note")));
		else if (tagName.equals("al") && isTypeForAl()) 
		    items.addAll(lowerUpperAlParts(data(childElem), type.equals("note")));
		else if (tagName.equals("no") && isTypeForNo()) 
		    items.add(new NoPart(data(childElem)));
		else if (tagName.equals("i") && isTypeForI()) 
		    items.add(new IPart(data(childElem)));
		else if (tagName.equals("lx") && isTypeForLx())
		    items.add(lx(childElem));
		else if (tagName.equals("etc") && isTypeForEtc())
		    items.add(new EtcPart());
		else if (tagName.equals("a") && isTypeForLink()) {
		    String href = getValue(childElem.getAttributeNode("href"));
		    String text = data(childElem);
		    items.add(new LinkPart(text, href));
		} else if (tagName.equals("coord") && isTypeForCoord()) {
		    String id = getValue(childElem.getAttributeNode("id"));
		    items.add(new CoordPart(id));
		} else if (tagName.equals("pre") && isTypeForPrePost()) {
		    String id = getValue(childElem.getAttributeNode("id"));
		    items.add(new PrePart(id));
		} else if (tagName.equals("post") && isTypeForPrePost()) {
		    String id = getValue(childElem.getAttributeNode("id"));
		    items.add(new PostPart(id));
		} else if (tagName.equals("pos") && isTypeForCoord()) {
		    int symbol = getIntValue(childElem.getAttributeNode("symbol"));
		    String id = getValue(childElem.getAttributeNode("id"));
		    items.add(new PosPart(symbol, id));
		} else if (tagName.equals("note") && isTypeForNote()) {
		    Vector note = parseNote(childElem);
		    int symbol = getIntValue(childElem.getAttributeNode("symbol"));
		    items.add(new NotePart(note, symbol));
		}
	    }
	}
	return items;
    }

    // Parse hieroglyphic.
    public static Vector parseHi(Element el) throws IOException {
	ParsingHelper helper = new ParsingHelper("hi");
	return helper.getParts(el);
    }

    // Parse transliteration.
    public static Vector parseAl(Element el) throws IOException {
	ParsingHelper helper = new ParsingHelper("al");
	return helper.getParts(el);
    }

    // Parse translation.
    public static Vector parseTr(Element el) throws IOException {
	ParsingHelper helper = new ParsingHelper("tr");
	return helper.getParts(el);
    }

    // Parse lexical entry.
    public static Vector parseLx(Element el) throws IOException {
	ParsingHelper helper = new ParsingHelper("lx");
	return helper.getParts(el);
    }

    // Parse note.
    public static Vector parseNote(Element el) throws IOException {
	ParsingHelper helper = new ParsingHelper("note");
	return helper.getParts(el);
    }

    //////////////////////////////////
    // Transliteration.

    // For string of transliteration, split up into AlParts for alternating
    // lower and upper case.
    public static Vector lowerUpperAlParts(String s, boolean note) {
        Vector parts = new Vector();
	Vector rawParts = TransHelper.lowerUpperParts(s);
	for (int i = 0; i < rawParts.size(); i++) {
	    Object[] pair = (Object[]) rawParts.get(i);
	    String kind = (String) pair[0];
	    String info = (String) pair[1];
	    if (kind.equals("translower"))
		parts.add(new AlPart(info, false, note));
	    else
		parts.add(new AlPart(info, true, note));
	}
	return parts;
    }

    //////////////////////////////////
    // Writing.

    private String writeParts(Vector parts) throws IOException {
	parts = mergeTrans(parts);
	StringBuffer buf = new StringBuffer();
	for (int i = 0; i < parts.size(); i++) {
	    ResourcePart part = (ResourcePart) parts.get(i);
	    if (part instanceof HiPart) {
		HiPart hi = (HiPart) part;
		String info = hi.hi;
		info = XmlAux.escape(info);
		info = makeLiteSafe(info);
		if (type.equals("hi")) 
		    buf.append(info);
		else
		    buf.append("<hi>" + info + "</hi>");
	    } else if (part instanceof AlPart) {
		AlPart al = (AlPart) part;
		String info = al.str();
		info = XmlAux.escape(info);
		info = makeLiteSafe(info);
		if (type.equals("al"))
		    buf.append(info);
		else
		    buf.append("<al>" + info + "</al>");
	    } else if (part instanceof NoPart) {
		NoPart no = (NoPart) part;
		String info = no.str();
		info = XmlAux.escape(info);
		info = makeLiteSafe(info);
		if (type.equals("tr") || type.equals("note"))
		    buf.append(info);
		else 
		    buf.append("<no>" + info + "</no>");
	    } else if (part instanceof IPart) {
		IPart it = (IPart) part;
		String info = it.str();
		info = XmlAux.escape(info);
		info = makeLiteSafe(info);
		buf.append("<i>" + info + "</i>");
	    } else if (part instanceof LxPart) {
		LxPart lx = (LxPart) part;
		buf.append("<lx" +
			lexAttr("texthi", lx.texthi) +
			lexAttr("textal", lx.textal) +
			lexAttr("texttr", lx.texttr) +
			lexAttr("textfo", lx.textfo) +
			lexAttr("cite", lx.cite) +
			lexAttr("href", lx.href) +
			lexAttr("keyhi", lx.keyhi) +
			lexAttr("keyal", lx.keyal) +
			lexAttr("keytr", lx.keytr) +
			lexAttr("keyfo", lx.keyfo) +
			lexAttr("dicthi", lx.dicthi) +
			lexAttr("dictal", lx.dictal) +
			lexAttr("dicttr", lx.dicttr) +
			lexAttr("dictfo", lx.dictfo) +
			"\n/> ");
	    } else if (part instanceof EtcPart) {
		buf.append("<etc/>");
	    } else if (part instanceof LinkPart) {
		LinkPart link = (LinkPart) part;
		String href = XmlAux.escape(link.ref);
		String string = XmlAux.escape(link.string);
		buf.append("<a href=\"" + href + "\">" + string + "</a>");
	    } else if (part instanceof CoordPart) {
		CoordPart coord = (CoordPart) part;
		String id = XmlAux.escape(coord.id);
		if (lite) {
		    if (LiteHelper.idRequiresLongTagEncoding(id))
			buf.append("<\"" + id + "\">");
		    else
			buf.append("<" + id + ">");
		} else 
		    buf.append("<coord id=\"" + id + "\"/>");
	    } else if (part instanceof PrePart) {
		PrePart pre = (PrePart) part;
		String id = XmlAux.escape(pre.id);
		if (lite) {
		    if (LiteHelper.idRequiresLongTagEncoding(id))
			buf.append("<\"" + id + "\"^pre>");
		    else
			buf.append("<" + id + "^pre>");
		} else
		    buf.append("<pre id=\"" + id + "\"/>");
	    } else if (part instanceof PostPart) {
		PostPart post = (PostPart) part;
		String id = XmlAux.escape(post.id);
		if (lite) {
		    if (LiteHelper.idRequiresLongTagEncoding(id))
			buf.append("<\"" + id + "\"^post>");
		    else
			buf.append("<" + id + "^post>");
		} else
		    buf.append("<post id=\"" + id + "\"/>");
	    } else if (part instanceof PosPart) {
		PosPart pos = (PosPart) part;
		int symbol = pos.symbol;
		String id = XmlAux.escape(pos.id);
		if (lite && symbol < 0) {
		    if (lite && LiteHelper.idRequiresLongTagEncoding(id))
			buf.append("<@\"" + id + "\">");
		    else
			buf.append("<@" + id + ">");
		} else
		    buf.append("<pos" +
			    (symbol >= 0 ? " symbol=\"" + symbol + "\"" : "") +
			    " id=\"" + id + "\"/>");
	    } else if (part instanceof NotePart) {
		NotePart note = (NotePart) part;
		String text = writePartsNote(note.text());
		int symbol = note.symbol();
		buf.append("<note" +
			(symbol >= 0 ? " symbol=\"" + symbol + "\"" : "") + ">" + 
			text + "</note>");
	    }
	}
	return buf.toString();
    }

    // The separators , and ; and : in lite format could lead to clashes with
    // such characters in contents if these follow spaces. That is why 
    // spaces are removed before these operators.
    private String makeLiteSafe(String s) {
	if (lite) 
	    return s.replaceAll("\\s*([,;:])", "$1");
	else
	    return s;
    }

    private static String lexAttr(String name, String value) {
	return value.matches("\\s*") ? "" :
	    "\n" + name + "=\"" + XmlAux.escape(value) + "\"";
    }

    // Write hieroglyphic.
    public static String writePartsHi(Vector items, boolean lite) throws IOException {
	ParsingHelper helper = new ParsingHelper("hi", lite);
	return XmlAux.breakLines(helper.writeParts(items));
    }
    public static String writePartsHi(Vector items) throws IOException {
	return writePartsHi(items, false);
    }

    // Write transliteration.
    public static String writePartsAl(Vector items, boolean lite) throws IOException {
	ParsingHelper helper = new ParsingHelper("al", lite);
	return XmlAux.breakLines(helper.writeParts(items));
    }
    public static String writePartsAl(Vector items) throws IOException {
	return writePartsAl(items, false);
    }

    // Write translation.
    public static String writePartsTr(Vector items, boolean lite) throws IOException {
	ParsingHelper helper = new ParsingHelper("tr", lite);
	return XmlAux.breakLines(helper.writeParts(items));
    }
    public static String writePartsTr(Vector items) throws IOException {
	return writePartsTr(items, false);
    }

    // Write lexical entry.
    public static String writePartsLx(Vector items, boolean lite) throws IOException {
	ParsingHelper helper = new ParsingHelper("lx", lite);
	return XmlAux.breakLines(helper.writeParts(items));
    }
    public static String writePartsLx(Vector items) throws IOException {
	return writePartsLx(items, false);
    }

    // Write note.
    public static String writePartsNote(Vector items) throws IOException {
	ParsingHelper helper = new ParsingHelper("note");
	return helper.writeParts(items);
    }

    //////////////////////////////////
    // Auxiliaries.

    // Get data immediately below child.
    private String data(Element elem) throws IOException {
        String data = "";
        NodeList children = elem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof CharacterData) {
                CharacterData d = (CharacterData) child;
                data += d.getData();
            }
        }
        return data;
    }

    // Get lexical part immediately below child.");
    private LxPart lx(Element elem) {
	LxInfo info = new LxInfo(elem);
	return info.getLxPart();
    }

    // Get attribute value, but "" if attribute is null.
    private static String getValue(Attr attr) {
        return getValue(attr, "");
    }

    // Get attribute, or default value if null.
    private static String getValue(Attr attr, String defaultVal) {
        return attr == null ? defaultVal : attr.getValue();
    }

    // Get attribute value, but "-1" if attribute is null or wrong format.
    private static int getIntValue(Attr attr) {
	if (attr != null)
	    try {
		return Integer.parseInt(attr.getValue());
	    } catch (NumberFormatException e) {
		// ignore
	    }
	return -1;
    }

    // Is type where we can have data?
    private boolean isTypeForData() {
	return
	    type.equals("hi") ||
	    type.equals("al") ||
	    type.equals("tr") ||
	    type.equals("note");
    }

    // Is type where we can have hieroglyphic?
    private boolean isTypeForHi() {
	return 
	    type.equals("tr") ||
	    type.equals("note");
    }

    // Is type where we can have transliteration?
    private boolean isTypeForAl() {
	return 
	    type.equals("tr") ||
	    type.equals("note");
    }

    // Is type where we can have normal text?
    private boolean isTypeForNo() {
	return 
	    type.equals("al");
    }

    // Is type where we can have italic?
    private boolean isTypeForI() {
	return 
	    type.equals("note");
    }

    // Is type where we can have lexical item?
    private boolean isTypeForLx() {
	return 
	    type.equals("lx");
    }

    // Is type where we can have etc?
    private boolean isTypeForEtc() {
	return 
	    !type.equals("note");
    }

    // Is type where we can have link?
    private boolean isTypeForLink() {
	return 
	    type.equals("note");
    }

    // Is type where we can have coordinate?
    private boolean isTypeForCoord() {
	return 
	    !type.equals("note");
    }

    // Is type where we can have pre or post?
    private boolean isTypeForPrePost() {
	return 
	    type.equals("al") ||
	    type.equals("tr") ||
	    type.equals("lx");
    }

    // Is type where we can have note?
    private boolean isTypeForNote() {
	return 
	    type.equals("hi") ||
	    type.equals("al") ||
	    type.equals("tr");
    }

    ////////////////////////////////////////////////
    // Conversion between ResourceParts and to elements 
    // in styled editor.

    // Converting ResourceParts to elements for editing.
    // The posses and notes preceding hieroglyphic and
    // buffered and bundeled with hieroglyphic.
    public static Vector toEdit(Vector parts) {
	Vector posBuffer = new Vector();
	Vector noteBuffer = new Vector();
	Vector editParts = new Vector(parts.size());
	for (int i = 0; i < parts.size(); i++) {
	    ResourcePart part = (ResourcePart) parts.get(i);
	    if (part instanceof HiPart) {
		HiPart hi = (HiPart) part;
		String info = hi.hi;
		editParts.add(new Object[] {"hiero", 
			new Object[] {info, posBuffer, noteBuffer}});
		posBuffer = new Vector();
		noteBuffer = new Vector();
	    } else if (part instanceof AlPart) {
		AlPart al = (AlPart) part;
		String info = al.str();
		if (al.upper)
		    editParts.add(new Object[] {"transupper", info});
		else
		    editParts.add(new Object[] {"translower", info});
	    } else if (part instanceof NoPart) {
		NoPart no = (NoPart) part;
		String info = no.str();
		editParts.add(new Object[] {"plain", info});
	    } else if (part instanceof IPart) {
		IPart it = (IPart) part;
		String info = it.str();
		editParts.add(new Object[] {"italic", info});
	    } else if (part instanceof LxPart) {
		LxPart lx = (LxPart) part;
		LxInfo info = new LxInfo(lx);
		editParts.add(new Object[] {"lx", info});
	    } else if (part instanceof EtcPart) {
		editParts.add(new Object[] {"etc", null});
	    } else if (part instanceof LinkPart) {
		LinkPart link = (LinkPart) part;
		String ref = link.ref;
		String string = link.string;
		editParts.add(new Object[] {"link",
			new String[] {ref, string}});
	    } else if (part instanceof CoordPart) {
		CoordPart coord = (CoordPart) part;
		String info = coord.id;
		editParts.add(new Object[] {"coord", info});
	    } else if (part instanceof PrePart) {
		PrePart pre = (PrePart) part;
		String info = pre.id;
		editParts.add(new Object[] {"pre", info});
	    } else if (part instanceof PostPart) {
		PostPart post = (PostPart) part;
		String info = post.id;
		editParts.add(new Object[] {"post", info});
	    } else if (part instanceof PosPart) {
		PosPart pos = (PosPart) part;
		int symbol = pos.symbol;
		String id = pos.id;
		if (symbol < 0) 
		    editParts.add(new Object[] {"pos", id});
		else
		    posBuffer.add(pos);
	    } else if (part instanceof NotePart) {
		NotePart note = (NotePart) part;
		Vector text = note.text();
		int symbol = note.symbol();
		if (symbol < 0) {
		    Vector editText = toEdit(text);
		    editParts.add(new Object[] {"note", editText});
		} else 
		    noteBuffer.add(note);
	    } 
	}
	return editParts;
    }

    // Conversion back.
    public static Vector fromEdit(Vector parts) {
	return fromEdit(parts, false);
    }
    public static Vector fromEdit(Vector parts, boolean isNote) {
	Vector resourceParts = new Vector(parts.size());
	for (int i = 0; i < parts.size(); i++) {
	    Object[] pair = (Object[]) parts.get(i);
	    String type = (String) pair[0];
	    if (type.equals("hiero")) {
		Object[] triple = (Object[]) pair[1];
		String info = (String) triple[0];
		Vector posses = (Vector) triple[1];
		Vector notes = (Vector) triple[2];
		HiPart hi = new HiPart(info, isNote);
		resourceParts.addAll(posses);
		resourceParts.addAll(notes);
		resourceParts.add(hi);
	    } else if (type.equals("transupper")) {
		String info = (String) pair[1];
		AlPart al = new AlPart(info, true, isNote);
		resourceParts.add(al);
	    } else if (type.equals("translower")) {
		String info = (String) pair[1];
		AlPart al = new AlPart(info, false, isNote);
		resourceParts.add(al);
	    } else if (type.equals("plain")) {
		String info = (String) pair[1];
		NoPart no = new NoPart(info);
		no.setFootnote(isNote);
		resourceParts.add(no);
	    } else if (type.equals("italic")) {
		String info = (String) pair[1];
		IPart it = new IPart(info);
		it.setFootnote(isNote);
		resourceParts.add(it);
	    } else if (type.equals("lx")) {
		LxInfo info = (LxInfo) pair[1];
		LxPart lx = info.getLxPart();
		resourceParts.add(lx);
	    } else if (type.equals("etc")) {
		EtcPart etc = new EtcPart();
		resourceParts.add(etc);
	    } else if (type.equals("link")) {
		String[] stringRef = (String[]) pair[1];
		String ref = stringRef[0];
		String string = stringRef[1];
		LinkPart link = new LinkPart(string, ref);
		resourceParts.add(link);
	    } else if (type.equals("coord")) {
		String info = (String) pair[1];
		if (!info.matches("\\s*")) {
		    CoordPart coord = new CoordPart(info);
		    resourceParts.add(coord);
		}
	    } else if (type.equals("pre")) {
		String info = (String) pair[1];
		if (!info.matches("\\s*")) {
		    PrePart pre = new PrePart(info);
		    resourceParts.add(pre);
		}
	    } else if (type.equals("post")) {
		String info = (String) pair[1];
		if (!info.matches("\\s*")) {
		    PrePart post = new PrePart(info);
		    resourceParts.add(post);
		}
	    } else if (type.equals("pos")) {
		String id = (String) pair[1];
		PosPart pos = new PosPart(-1, id);
		resourceParts.add(pos);
	    } else if (type.equals("note")) {
		Vector editText = (Vector) pair[1];
		Vector text = fromEdit(editText, true);
		NotePart note = new NotePart(text, -1);
		resourceParts.add(note);
	    }
	}
	return resourceParts;
    }

    // Merge split lower and upper case transliteration styles, with ^ for
    // upper case.
    private static Vector mergeTrans(Vector parts) {
        Vector merged = new Vector();
        String buffer = "";
	boolean note = false;
        for (int i = 0; i < parts.size(); i++) {
	    ResourcePart part = (ResourcePart) parts.get(i);
	    if (part instanceof AlPart) {
		AlPart al = (AlPart) part;
		note = al.isFootnote();
		if (al.upper) 
		    for (int j = 0; j < al.str().length(); j++) {
			char c = al.str().charAt(j);
			if (Character.isWhitespace(c))
			    buffer += c;
			else
			    buffer += "^" + c;
		    }
		else 
		    buffer += al.str();
            } else {
                if (!buffer.equals("")) {
                    merged.add(new AlPart(buffer, false, note));
                    buffer = "";
                }
                merged.add(part);
            }
        }
        if (!buffer.equals(""))
	    merged.add(new AlPart(buffer, false, note));
        return merged;
    }


    /////////////////////////////////////////
    // precedence.

    // Get list of named positions in tier.
    public static Vector getPositions(Vector tier) {
	Vector positions = new Vector();
	for (int i = 0; i < tier.size(); i++) {
	    ResourcePart part = (ResourcePart) tier.get(i);
	    if (part instanceof PosPart) {
		PosPart pos = (PosPart) part;
		String id = pos.id;
		positions.add(id);
	    }
	}
	return positions;
    }

    // Analyse element that is precedence.
    public static void parsePrecedence(Element el, PosPrecedence precedence) {
	String id1 = getValue(el.getAttributeNode("id1"));
	String type1 = getValue(el.getAttributeNode("type1"), "start");
	String id2 = getValue(el.getAttributeNode("id2"));
	precedence.add(id1, type1, id2, "start");
    }

}
