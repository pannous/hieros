/***************************************************************************/
/*                                                                         */
/*  XMLfiles.java                                                          */
/*                                                                         */
/*  Copyright (c) 2008 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Reads XML files in AELalign format.
// Constructs a system of streams from them.

package nederhof.align;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.List;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;

import nederhof.res.*;

public class XMLfiles {

    // List of XML files.
    private String[] args;

    // Constructor. Just store list of input files.
    public XMLfiles(String[] files) {
	args = files;
    }

    // System of streams.
    private StreamSystem streams;

    // Every read file has unique number.
    private int fileNum;

    // Main routine. For each argument in command line, incorporate document.
    // If an argument does not end in .xml , then the input file is that name
    // plus extension .xml . Otherwise, the argument denotes input file.
    public StreamSystem alignSystem() {
	constructParser();
	streams = new StreamSystem();
	for (fileNum = 0; fileNum < args.length; fileNum++)
	    includeFile(args[fileNum]);
	streams.reprocess();
	return streams;
    }

    // To become XML parser.
    private static DocumentBuilder parser;

    // Build XML validating parser.
    private static void constructParser() {
	parser = getParser();
    }

    public static DocumentBuilder getParser() {
	DocumentBuilder parser = null;
	try {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setCoalescing(false);
	    factory.setExpandEntityReferences(true);
	    factory.setIgnoringComments(true);
	    factory.setIgnoringElementContentWhitespace(true); 
	    factory.setNamespaceAware(false);
	    factory.setValidating(true);
	    parser = factory.newDocumentBuilder();
	    parser.setEntityResolver(new XMLResolver());
	    parser.setErrorHandler(new SimpleErrorHandler());
	} catch (ParserConfigurationException e) {
	    System.err.println(e.getMessage());
	    System.exit(-1);
	}
	return parser;
    }

    // Allow DTD to be found.
    private static class XMLResolver extends DefaultHandler {
	public InputSource resolveEntity(String publicId, String systemId) {
	    return new InputSource(systemId);
	}
    }

    // Name of XML file to be read.
    private String inName;

    // Read XML file.
    private void includeFile(String name) {
	String ending = ".xml";
	if (name.endsWith(ending))
	    name = name.substring(0, name.length()-ending.length());
	inName = name + ".xml";
	try {
	    Document doc = parser.parse(inName);
	    includeDocument(doc, name);
	} catch (IOException e) {
	    System.err.println("In " + inName);
	    System.err.println(e.getMessage());
	} catch (SAXException e) {
	}
    }

    // Information from XML file.
    private Element created;
    private String nameStr;
    private String encodingStr;
    private Element header;
    private Element bibl;

    // Process XML file.
    private void includeDocument(Document doc, String fileName) {
	Element resource = doc.getDocumentElement();
	nameStr = "";
	encodingStr = "";
	NodeList children = resource.getChildNodes();
	int numChildren = children.getLength();
	for (int i = 0; i < numChildren; i++) {
	    Node child = children.item(i);
	    if (child instanceof Element) {
		Element elem = (Element) child;
		String tagName = elem.getTagName();
		if (tagName.equals("created")) {
		    includeCreated(elem);
		} else if (tagName.equals("header")) {
		    includeHeader(elem);
		} else if (tagName.equals("bibliography")) {
		    includeBibl(elem);
		} else if (tagName.equals("body")) {
		    includeBody(elem);
		}
	    }
	}
	streams.addFile(fileName, created, nameStr, encodingStr, header, bibl);
    }

    // Process created part of XML file.
    private void includeCreated(Element created) {
	this.created = created;
    }

    // Process header part of XML file.
    private void includeHeader(Element header) {
	this.header = header;
	nameStr = header.getAttributeNode("name").getValue();
	encodingStr = header.getAttributeNode("encoding").getValue();
    }

    // Process bibliography part of XML file.
    private void includeBibl(Element bibl) {
	this.bibl = bibl;
    }

    // Current version name and scheme.
    private String name;
    private String scheme;

    // Process body of XML file.
    private void includeBody(Element body) {
	name = "";
	scheme = "";
	NodeList children = body.getChildNodes();
	int numChildren = children.getLength();
	for (int i = 0; i < numChildren; i++) {
	    Node child = children.item(i);
	    if (child instanceof Element) {
		Element elem = (Element) child;
		String tagName = elem.getTagName();
		if (tagName.equals("version")) 
		    includeVersion(elem);
		else if (tagName.equals("equateversion")) 
		    includeEquateVersion(elem);
		else if (tagName.equals("equatepos"))
		    includeEquatePos(elem);
		else if (tagName.equals("phrase"))
		    includePhrase(elem);
	    }
	}
    }

    // Process version.
    private void includeVersion(Element elem) {
	name = elem.getAttributeNode("name").getValue();
	scheme = elem.getAttributeNode("scheme").getValue();
    }

    // Process equateversion.
    private void includeEquateVersion(Element elem) {
	String name2 = elem.getAttributeNode("name2").getValue();
	String scheme2 = elem.getAttributeNode("scheme2").getValue();
	streams.addEqualVersion(name, scheme, name2, scheme2);
    }

    // Process equatepos.
    private void includeEquatePos(Element elem) {
	String tag1 = elem.getAttributeNode("pos1").getValue();
	String name2 = elem.getAttributeNode("name2").getValue();
	String scheme2 = elem.getAttributeNode("scheme2").getValue();
	String tag2 = elem.getAttributeNode("pos2").getValue();
	streams.addEqualPos(fileNum, name, scheme, tag1, name2, scheme2, tag2);
    }

    // Process phrase.
    private void includePhrase(Element phrase) {
	Pos phrasePos = new Pos(fileNum);
	NodeList children = phrase.getChildNodes();
	int numChildren = children.getLength();
	for (int i = 0; i < numChildren; i++) {
	    Node child = children.item(i);
	    if (child instanceof Element) {
		Element elem = (Element) child;
		String tagName = elem.getTagName();
		if (tagName.equals("texthi")) 
		    includeText(elem, RenderContext.HIERO_FONT, phrasePos);
		else if (tagName.equals("textal")) 
		    includeText(elem, RenderContext.EGYPT_FONT, phrasePos);
		else if (tagName.equals("texttr")) 
		    includeText(elem, RenderContext.LATIN_FONT, phrasePos);
		else if (tagName.equals("textlx")) 
		    includeText(elem, RenderContext.LX, phrasePos);
	    }
	}
    }

    // Process texthi/al/tr/lx. Start with phrasal coordinate.
    private void includeText(Element text, int streamType, Pos phrasePos) {
	LinkedList stream = streams.getStream(fileNum, name, scheme, streamType);
	Point point = new EmptyPoint(streamType, phrasePos, false);
	stream.addLast(point);
	includeTextPart(text, stream, streamType, false);
    }

    // Process part of texthi/al/tr/lx.
    private void includeTextPart(Element part, LinkedList stream, int textType, 
	    boolean inFootnote) {
	NodeList children = part.getChildNodes();
	int numChildren = children.getLength();
	for (int i = 0; i < numChildren; i++) {
	    Node child = children.item(i);
	    if (child instanceof Element) {
		Element childEl = (Element) child;
		String tagName = childEl.getTagName();
		if (tagName.equals("i"))
		    includeTextPart(childEl, stream, getItalic(inFootnote), inFootnote);
		else if (tagName.equals("hi"))
		    includeTextPart(childEl, stream, getHiero(inFootnote), inFootnote);
		else if (tagName.equals("al"))
		    includeTextPart(childEl, stream, getEgypt(inFootnote), inFootnote);
		else if (tagName.equals("no"))
		    includeTextPart(childEl, stream, getLatin(inFootnote), inFootnote);
		else if (tagName.equals("tr")) {
		    Marker open = new Marker(textType, "open");
		    stream.addLast(open);
		    includeTextPart(childEl, stream, getLatin(inFootnote), inFootnote);
		    Marker close = new Marker(textType, "close");
		    stream.addLast(close);
		} else if (tagName.equals("note")) {
		    Note elem = new Note(textType);
		    includeTextPart(childEl, elem.getStream(), getLatin(true), true);
		    stream.addLast(elem);
		} else if (tagName.equals("etc")) {
		    Marker elem = new Marker(textType, "etc");
		    stream.addLast(elem);
		} else if (tagName.equals("coord")) {
		    String tag = childEl.getAttributeNode("pos").getValue();
		    Pos pos = new Pos(name, scheme, tag, fileNum);
		    if (childEl.getChildNodes().getLength() == 0) {
			Point point = new EmptyPoint(textType, pos, false);
			stream.addLast(point);
		    } else {
			Point pointOpen = new OpenPoint(textType, pos, false);
			Point pointClose = new ClosePoint(textType, pos, false);
			stream.addLast(pointOpen);
			includeTextPart(childEl, stream, textType, inFootnote);
			stream.addLast(pointClose);
		    }
		} else if (tagName.equals("align")) {
		    String name2 = childEl.getAttributeNode("name").getValue();
		    String scheme2 = childEl.getAttributeNode("scheme").getValue();
		    String tag = childEl.getAttributeNode("pos").getValue();
		    if (childEl.getChildNodes().getLength() == 0) {
			Pos pos = new Pos(name2, scheme2, tag, fileNum);
			Point point = new EmptyPoint(textType, pos, true);
			stream.addLast(point);
		    } else {
			Pos pos = new Pos(name2, scheme2, tag, fileNum);
			Point pointOpen = new OpenPoint(textType, pos, true);
			Point pointClose = new ClosePoint(textType, pos, true);
			stream.addLast(pointOpen);
			includeTextPart(childEl, stream, textType, inFootnote);
			stream.addLast(pointClose);
		    }
		} else if (tagName.equals("markup")) {
		    String type = safeGetVal(childEl, "type");
		    String markup = getString(childEl);
		    Markup elem = new Markup(textType, type, markup);
		    stream.addLast(elem);
		} else if (tagName.equals("lx")) {
		    Lx elem = getLx(childEl);
		    stream.addLast(elem);
		} else if (tagName.equals("a")) {
		    NamedNodeMap attrs = childEl.getAttributes();
		    int numAttrs = attrs.getLength();
		    String href = "";
		    for (int j = 0; j < numAttrs; j++) {
			Node childChild = attrs.item(j);
			if (childChild instanceof Attr) {
			    Attr attr = (Attr) childChild;
			    if (attr.getName().equals("href"))
				href = attr.getValue();
			}
		    }
		    NodeList childChilds = childEl.getChildNodes();
		    int numChildChilds = childChilds.getLength();
		    for (int j = 0; j < numChildChilds; j++) {
			Node childChild = childChilds.item(j);
			if (childChild instanceof CharacterData) {
			    CharacterData childData = (CharacterData) childChild;
			    String txt = childData.getData();
			    txt = txt.replaceAll("\\s+", " ");
			    stream.addLast(
				    new Link(RenderContext.FOOT_LATIN_FONT, txt, href));
			}
		    }
		}
	    } else if (child instanceof CharacterData) {
		CharacterData childData = (CharacterData) child;
		String text = childData.getData();
		if (textType == RenderContext.HIERO_FONT ||
			textType == RenderContext.FOOT_HIERO_FONT) {
		    HieroElem elem = new HieroElem(getHiero(inFootnote), text);
		    stream.addLast(elem);
		} else {
		    TextElem elem = new TextElem(textType, text);
		    stream.addLast(elem);
		}
	    } else if (child instanceof EntityReference) {
		// Superfluous now that entity references are expanded.
		String text = "&" + child.getNodeName() + ";";
		TextElem elem = new TextElem(textType, text);
		stream.addLast(elem);
	    }
	}
    }

    // Get text type, depending on whether in footnote.
    private static int getLatin(boolean inFootnote) {
	if (inFootnote)
	    return RenderContext.FOOT_LATIN_FONT;
	else
	    return RenderContext.LATIN_FONT;
    }
    private static int getEgypt(boolean inFootnote) {
	if (inFootnote)
	    return RenderContext.FOOT_EGYPT_FONT;
	else
	    return RenderContext.EGYPT_FONT;
    }
    private static int getHiero(boolean inFootnote) {
	if (inFootnote)
	    return RenderContext.FOOT_HIERO_FONT;
	else
	    return RenderContext.HIERO_FONT;
    }
    private static int getItalic(boolean inFootnote) {
	if (inFootnote)
	    return RenderContext.FOOT_ITALIC_FONT;
	else
	    return RenderContext.ITALIC_FONT;
    }

    // Get attributes from lx element.
    private Lx getLx(Element lx) {
	String texthi = safeGetVal(lx, "texthi");
	String textal = safeGetVal(lx, "textal");
	String texttr = safeGetVal(lx, "texttr");
	String textfo = safeGetVal(lx, "textfo");
	String cite = safeGetVal(lx, "cite");
	String href = safeGetVal(lx, "href");
	String keyhi = safeGetVal(lx, "keyhi");
	String keyal = safeGetVal(lx, "keyal");
	String keytr = safeGetVal(lx, "keytr");
	String keyfo = safeGetVal(lx, "keyfo");
	String dicthi = safeGetVal(lx, "dicthi");
	String dictal = safeGetVal(lx, "dictal");
	String dicttr = safeGetVal(lx, "dicttr");
	String dictfo = safeGetVal(lx, "dictfo");
	return new Lx(texthi, textal, texttr, textfo, cite, href,
		keyhi, keyal, keytr, keyfo,
		dicthi, dictal, dicttr, dictfo);
    }

    // Get string value from AttributeNode, but return null if node is null;
    private static String safeGetVal(Element lx, String attrName) {
	if (lx.hasAttribute(attrName))
	    return lx.getAttributeNode(attrName).getValue();
	else
	    return null;
    }

    // Transform part of XML document into string representation.
    // Look at tag names to decide whitespace layout.
    public static String getString(Element el) {
	if (el == null)
	    return "";
	String tagName = el.getTagName();
	String top = "<" + tagName;
	NamedNodeMap attrs = el.getAttributes();
	int numAttrs = attrs.getLength();
	for (int i = 0; i < numAttrs; i++) {
	    Node child = attrs.item(i);
	    if (child instanceof Attr) {
		Attr attr = (Attr) child;
		top += " " + attr.getName() + "=\"" + attr.getValue() + "\"";
	    }
	}
	top += ">";
	String middle = "";
	NodeList children = el.getChildNodes();
	int numChildren = children.getLength();
	for (int i = 0; i < numChildren; i++) {
	    Node child = children.item(i);
	    if (child instanceof Element) {
		Element childEl = (Element) child;
		middle += getString(childEl);
	    } else if (child instanceof CharacterData) {
		CharacterData childData = (CharacterData) child;
		String txt = childData.getData();
		// There are limits to what we can do with pure HTML.
		if (tagName.equals("hi"))
		    middle += "<b>" + txt + "</b>";
		else if (tagName.equals("al"))
		    middle += "<i>" + txt + "</i>";
		else
		    middle += txt;
	    } else if (child instanceof EntityReference) {
		// Cannot happen now that entity references are expanded.
	    }
	}
	String bottom = "</" + tagName + ">";
	if (spaciousTag(tagName))
	    return top + middle + bottom + "\n";
	else if (tagName.equals("created") ||
		tagName.equals("header") ||
		tagName.equals("bibliography") ||
		tagName.equals("hi") ||
		tagName.equals("al") || 
		tagName.equals("no") ||
		tagName.equals("note") ||
		tagName.equals("markup"))
	    return middle;
	else if (tagName.equals("tr"))
	    return "\"" + middle + "\"";
	else
	    return top + middle + bottom;
    }

    // XML tags where line breaks are to be inserted.
    private static boolean spaciousTag(String tag) {
	return
	    tag.equals("p") || tag.equals("ul") || tag.equals("li");
    }

    ///////////////////////////////////////////////////////////////////////
    // Preamble, represented as Vector. Each element is paragraph.

    // Start of preambles for several resources.
    public static PreamblePars getPreambleHead(String name) {
	PreamblePars pars = new PreamblePars();
	pars.add(new Header(RenderContext.HEADER1_FONT, 
		    name));
	pars.makeFinished();
	return pars;
    }

    // Add one preamble to paragraphs.
    public static PreamblePars getPreamble(String name, Element created,
	    Element header, Element bibl) {
	PreamblePars pars = new PreamblePars();
	addPreamble(pars, name, created, header, bibl);
	pars.normalize();
	return pars;
    }

    // Make the vector out of XML elements.
    public static void addPreamble(PreamblePars pars,
	    String name, Element created, Element header, Element bibl) {
	pars.add(new Header(RenderContext.HEADER2_FONT, name));
	pars.makeFinished();
	int font = RenderContext.NORMAL_FONT;
	if (header != null) 
	    getPreamble(header, pars, font);
	if (bibl != null) {
	    pars.add(new Header(RenderContext.HEADER3_FONT, 
			"Bibliography"));
	    pars.makeFinished();
	    getPreamble(bibl, pars, font);
	}
	if (created != null) {
	    getPreamble(created, pars, font);
	    pars.makeFinished();
	}
    }

    // Add paragraphs from Element.
    public static void add(PreamblePars pars, Element elem) {
	getPreamble(elem, pars, RenderContext.NORMAL_FONT);
    }

    // Turn part of preamble in XML into paragraphs of text.
    private static void getPreamble(Element el, PreamblePars pars, int font) {
	String tagName = el.getTagName();
	NodeList children = el.getChildNodes();
	int numChildren = children.getLength();
	NamedNodeMap attrs = el.getAttributes();
	int numAttrs = attrs.getLength();
	if (tagName.equals("p")) {
	    pars.makeFinished();
	} else if (tagName.equals("ul")) {
	    pars.makeFinished();
	} else if (tagName.equals("li")) {
	    pars.makeFinished();
	    pars.add(new Bullet(font));
	} else if (tagName.equals("i")) {
	    font = RenderContext.ITALIC_FONT;
	} else if (tagName.equals("a")) {
	    String href = "";
	    for (int i = 0; i < numAttrs; i++) {
		Node child = attrs.item(i);
		if (child instanceof Attr) {
		    Attr attr = (Attr) child;
		    if (attr.getName().equals("href"))
			href = attr.getValue();
		}
	    }
	    for (int i = 0; i < numChildren; i++) {
		Node child = children.item(i);
		if (child instanceof CharacterData) {
		    CharacterData childData = (CharacterData) child;
		    String txt = childData.getData();
		    txt = txt.replaceAll("\\s+", " ");
		    pars.add(new Link(font, txt, href));
		}
	    }
	    return;
	} else if (tagName.equals("hi")) {
	    font = RenderContext.HIERO_FONT;
	    for (int i = 0; i < numChildren; i++) {
		Node child = children.item(i);
		if (child instanceof Element) {
		    Element childEl = (Element) child;
		    getPreamble(childEl, pars, font);
		} else if (child instanceof CharacterData) {
		    CharacterData childData = (CharacterData) child;
		    String txt = childData.getData();
		    pars.add(new HieroElem(RenderContext.HIERO_FONT, txt));
		}
	    }
	    return;
	} else if (tagName.equals("al")) {
	    font = RenderContext.EGYPT_FONT;
	} else if (tagName.equals("tr")) {
	    font = RenderContext.NORMAL_FONT;
	    pars.add(new TextElem(font, "\""));
	} else if (tagName.equals("no")) {
	    font = RenderContext.NORMAL_FONT;
	}
	for (int i = 0; i < numChildren; i++) {
	    Node child = children.item(i);
	    if (child instanceof Element) {
		Element childEl = (Element) child;
		getPreamble(childEl, pars, font);
	    } else if (child instanceof CharacterData) {
		CharacterData childData = (CharacterData) child;
		pars.add(new TextElem(font, childData.getData()));
	    }
	}
	if (tagName.equals("li")) {
	    pars.makeFinished();
	} else if (tagName.equals("p")) {
	    pars.makeFinished();
	} else if (tagName.equals("tr")) {
	    pars.add(new TextElem(font, "\""));
	}
    }

    ///////////////////////////////////////////////////////////////////////

    // For any parse error in XML document, report and continue with next document.
    private static class SimpleErrorHandler implements ErrorHandler {
	public void error(SAXParseException e) 
	    throws SAXException {
		if (e.getSystemId() != null)
		    System.err.println("In " + e.getSystemId());
		else if (e.getPublicId() != null)
		    System.err.println("In " + e.getPublicId());
		System.err.println("line " + e.getLineNumber() + 
			": " + e.getMessage());
		throw e;
	    }

	public void fatalError(SAXParseException e) 
	    throws SAXException {
		error(e);
	    }

	public void warning(SAXParseException e) 
	    throws SAXException {
		error(e);
	    }
    }
}
