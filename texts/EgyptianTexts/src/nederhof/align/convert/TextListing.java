/***************************************************************************/
/*                                                                         */
/*  TextListing.java                                                       */
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

package nederhof.align.convert;

// Reads list of texts in XML format, and produces sorted index.
// Also turns light files into xml files.

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.align.*;
import nederhof.util.*;
import nederhof.util.xml.*;

public class TextListing {

    // Build XML validating parser.
    private static DocumentBuilder constructParser() {
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
	    parser.setErrorHandler(new SimpleXmlErrorHandler(true));
	} catch (ParserConfigurationException e) {
	    System.err.println(e.getMessage());
	    System.exit(-1);
	}
	return parser;
    }

    // Default place where source files are stored.
    private static final String sourceBaseDefault = "textsrc";
    // Default place where xml files are to be stored.
    private static final String xmlBaseDefault = "textxml";

    // Convert XML listing of texts to sorted index.
    // If no arguments provided, take default arguments.
    public static void main(String[] args) {
	File sourceDir = new File(sourceBaseDefault);
	File xmlDir = new File(xmlBaseDefault);
	File pdfDir = null;
	if (args.length == 2 || args.length == 3) {
	    sourceDir = new File(args[0]);
	    xmlDir = new File(args[1]);
	    if (!sourceDir.isDirectory() || !xmlDir.isDirectory()) {
		System.err.println("Arguments to TextListing should be directories");
		System.exit(-1);
	    }
	    if (args.length == 3) {
		pdfDir = new File(args[2]);
		if (!pdfDir.isDirectory()) {
		    System.err.println("Arguments to TextListing should be directories");
		    System.exit(-1);
		}
	    }
	} else if (args.length != 0) 
	    System.err.println("Warning: TextListing expects 0 or 2 arguments");
	copyFileDir(sourceDir, xmlDir, "AELalign.0.3.dtd");
	copyFileDir(sourceDir, xmlDir, "ISOlat1.ent");
	copyFileDir(sourceDir, xmlDir, "selectlist.dtd");
	TreeMap index = readList(sourceDir, xmlDir, pdfDir);
	printXMLIndex(new File(xmlDir, "index.xml"), index);
	if (pdfDir != null)
	    printHTMLIndex(new File(pdfDir, "index.html"), index);
	System.exit(0);
    }

    // Read XML file with listing of texts.
    // Produce hierarchical index.
    private static TreeMap readList(File sourceDir, File xmlDir, File pdfDir) {
	DocumentBuilder parser = constructParser();
	TreeMap index = new TreeMap();
	try {
	    Document doc = parser.parse(new File(sourceDir, "listing.xml"));
	    processDoc(sourceDir, doc, xmlDir, pdfDir, index);
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	} catch (SAXException e) {
	    System.err.println(e.getMessage());
	}
	return index;
    }

    // Process texts in document.
    private static void processDoc(File sourceDir, Document doc, File xmlDir, File pdfDir, 
	    TreeMap index) {
	Element list = doc.getDocumentElement();
	NodeList children = list.getChildNodes();
	int numChildren = children.getLength();
	for (int i = 0; i < numChildren; i++) {
	    Node child = children.item(i);
	    if (child instanceof Element) {
		Element elem = (Element) child;
		processText(sourceDir, elem, xmlDir, pdfDir, index);
	    }
	}
    }

    // Process one text.
    private static void processText(File sourceDir, Element txt, File xmlDir, File pdfDir, 
	    TreeMap index) {
	String textName = txt.getAttributeNode("name").getValue();
	textName = ISOlatEntities.stringToEntities(textName);
	String textKey = (txt.hasAttribute("key") ?
		txt.getAttributeNode("key").getValue() : null);
	INode node = getNewNode(textName, textKey, "", "", textName, index);
	NodeList children = txt.getChildNodes();
	int numChildren = children.getLength();
	for (int i = 0; i < numChildren; i++) {
	    Node child = children.item(i);
	    if (child instanceof Element) {
		Element elem = (Element) child;
		String tagName = elem.getTagName();
		if (tagName.equals("file")) {
		    String fileName = elem.getAttributeNode("name").getValue();
		    fileName = convertFile(sourceDir, fileName, xmlDir);
		    if (fileName != null)
			node.files.addLast(fileName);
		} else if (tagName.equals("alt")) {
		    if (node.pdfFile == null)
			node.pdfFile = writePdf(node.files, xmlDir, pdfDir, textName);
		    processAlt(elem, textName, node.files, node.pdfFile, index);
		} else if (tagName.equals("collect")) {
		    if (node.pdfFile == null)
			node.pdfFile = writePdf(node.files, xmlDir, pdfDir, textName);
		    processColl(elem, index, textName, textKey, node.files, node.pdfFile);
		}
	    }
	}
	if (node.pdfFile == null)
	    node.pdfFile = writePdf(node.files, xmlDir, pdfDir, textName);
    }

    // Process alternative name.
    private static void processAlt(Element alt, String mainName, LinkedList files, String pdfFile, 
	    TreeMap index) {
	String textName = alt.getAttributeNode("name").getValue();
	textName = ISOlatEntities.stringToEntities(textName);
	String textKey = (alt.hasAttribute("key") ?
		alt.getAttributeNode("key").getValue() : null);
	INode node = getNewNode(textName, textKey, 
		textName + "; see: ", textName + "; ", mainName, index);
	node.files.addAll(files);
	node.pdfFile = pdfFile;
    }

    // Process one collection.
    private static void processColl(Element coll, TreeMap tree, 
	    String name, String key, LinkedList files, String pdfFile) {
	String tagName = coll.getTagName();
	String textName = coll.getAttributeNode("name").getValue();
	textName = ISOlatEntities.stringToEntities(textName);
	String textKey = (coll.hasAttribute("key") ?
		coll.getAttributeNode("key").getValue() : null);
	NodeList children = coll.getChildNodes();
	int numChildren = children.getLength();
	if (numChildren > 0) {
	    INode node = getNode(textName, textKey, null, null, textName, tree);
	    for (int i = 0; i < numChildren; i++) {
		Node child = children.item(i);
		if (child instanceof Element) {
		    Element elem = (Element) child;
		    processColl(elem, node.children, name, key, files, pdfFile);
		}
	    }
	} else if (tagName.equals("collect") || tagName.equals("subcollect")) {
	    INode node = getNode(textName, textKey, null, null, textName, tree);
	    INode child = getNewNode(name, key, "", "", name, node.children);
	    child.files.addAll(files);
	    child.pdfFile = pdfFile;
	} else {
	    INode node = getNewNode(textName, textKey, 
		    textName + "; see: ", textName + "; ", name, tree);
	    node.files.addAll(files);
	    node.pdfFile = pdfFile;
	}
    }

    // Get node in hierarchy.
    private static INode getNode(String name, String keyname, String labelXML, 
	    String labelHTML, String textName, TreeMap ind) {
	KeyString key = getSortingKey(name, keyname);
	if (ind.containsKey(key))
	    return (INode) ind.get(key);
	else {
	    INode node = new INode();
	    node.labelXML = labelXML;
	    node.labelHTML = labelHTML;
	    node.name = textName;
	    ind.put(key, node);
	    return node;
	}
    }

    // As before, but report error if node already exists.
    private static INode getNewNode(String name, String keyname, String labelXML, 
	    String labelHTML, String textName, TreeMap ind) {
	KeyString key = getSortingKey(name, keyname);
	if (ind.containsKey(key)) {
	    System.err.println("Doubly defined: " + name);
	    System.exit(-1);
	    return null;
	} else {
	    INode node = new INode();
	    node.labelXML = labelXML;
	    node.labelHTML = labelHTML;
	    node.name = textName;
	    ind.put(key, node);
	    return node;
	}
    }

    // Print hierarchy as XML file.
    private static void printXMLIndex(File outFile, TreeMap index) {
	try {
	    PrintWriter out =
		new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
	    printXMLIndex(out, index);
	    out.close();
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	}
    }

    // As above, but in HTML.
    private static void printHTMLIndex(File outFile, TreeMap index) {
	try {
	    PrintWriter out =
		new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
	    printHTMLIndex(out, index);
	    out.close();
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	}
    }

    // Print XML file.
    private static void printXMLIndex(PrintWriter out, TreeMap index) 
	throws IOException {
	    out.println("<?xml version=\"1.0\"?>");
	    out.println("<!DOCTYPE selectlist SYSTEM \"selectlist.dtd\">");
	    out.println("<selectlist>");
	    printXMLTexts(out, index);
	    out.println("</selectlist>");
    }

    // As above, but in HTML.
    private static void printHTMLIndex(PrintWriter out, TreeMap index) 
	throws IOException {
	    out.println("<html>");
	    out.println("<ul>");
	    printHTMLTexts(out, index);
	    out.println("</ul>");
	    out.println("</html>");
    }

    // Print listing.
    private static void printXMLTexts(PrintWriter out, TreeMap ind) {
	Collection nodes = ind.values();
	Iterator iter = nodes.iterator();
	while (iter.hasNext()) {
	    INode node = (INode) iter.next();
	    if (node.files.isEmpty()) {
		if (node.children.isEmpty()) {
		    System.err.println("Leaf has no files: " + node.name);
		}
		out.println("<internal label=\"" + node.name + "\">");
		printXMLTexts(out, node.children);
		out.println("</internal>");
	    } else {
		if (!node.children.isEmpty()) {
		    System.err.println("Non-leaf has files: " + node.name);
		}
		String label;
		if (node.labelXML == null)
		    label = node.name;
		else
		    label = node.labelXML + "&lt;font color=blue&gt;" + node.name + "&lt;/font&gt;";
		out.println("<leaf label=\"" + label + "\"\n\t" +
			"name=\"" + node.name + "\">");
		ListIterator listIter = node.files.listIterator();
		while (listIter.hasNext()) {
		    String file = (String) listIter.next();
		    out.println("<file name=\"" + file + "\"/>");
		}
		printXMLTexts(out, node.children);
		out.println("</leaf>");
	    } 
	}
    }

    // As above, but in HTML.
    private static void printHTMLTexts(PrintWriter out, TreeMap ind) {
	Collection nodes = ind.values();
	Iterator iter = nodes.iterator();
	while (iter.hasNext()) {
	    INode node = (INode) iter.next();
	    if (node.files.isEmpty()) {
		out.println("<li> " + node.name);
		out.println("<ul>");
		printHTMLTexts(out, node.children);
		out.println("</ul>");
		out.println("</li>");
	    } else {
		String label = (node.labelHTML == null ? "" : node.labelHTML);
		if (node.pdfFile == null)
		    out.println("<li> " + label + node.name + " </li>");
		else
		    out.println("<li> " + label + 
			"<a href=\"" + node.pdfFile + "\">" + node.name + "</a> </li>");
	    }
	}
    }

    // Node in hierarchy of index.
    private static class INode {
	public String labelXML;
	public String labelHTML;
	public String name;
	public LinkedList files;
	public String pdfFile;
	public TreeMap children;

	public INode() {
	    labelXML = null;
	    labelHTML = null;
	    name = "";
	    files = new LinkedList();
	    pdfFile = null;
	    children = new TreeMap();
	}
    }

    // Strings to be compared with ignore case.
    private static class KeyString implements Comparable {
	public String string;
	public KeyString(String str) {
	    string = str;
	}
	public boolean equals(Object o) {
	    KeyString other = (KeyString) o;
	    return string.equalsIgnoreCase(other.string);
	}
	public int compareTo(Object o) {
	    KeyString other = (KeyString) o;
	    return string.compareToIgnoreCase(other.string);
	}
    }

    // Get key for sorting. Try to obtain up to 2 numbers from string. Place these
    // first, padded with zeros, place actual string afterwards.
    // If there is key, and that does not start with number, do no extraction
    // of numbers.
    private static KeyString getSortingKey(String name, String key) {
	String str = (key == null ? name : key);
	int num1 = 0;
	int num2 = 0;
	if (key == null || 
		(key.length() > 0 && Character.isDigit(key.charAt(0)))) {
	    int fromPos = skipNonDigits(str, 0);
	    int toPos = skipDigits(str, fromPos);
	    if (toPos >= 0) {
		String sub = str.substring(fromPos, toPos);
		num1 = Integer.parseInt(sub);
	    } else {
		num1 = 0;
		toPos = fromPos;
	    }
	    fromPos = skipNonDigits(str, toPos);
	    toPos = skipDigits(str, fromPos);
	    if (toPos >= 0) {
		String sub = str.substring(fromPos, toPos);
		num2 = Integer.parseInt(sub);
	    } else 
		num2 = 0;
	}
	String key1 = Integer.toString(num1);
	String key2 = Integer.toString(num2);
	key1 = padding(key1);
	key2 = padding(key2);
	str = key1 + key2 + str;
	return new KeyString(str);
    }

    // Skip zero or more nondigits.
    private static int skipNonDigits(String str, int pos) {
	while (pos < str.length() && !Character.isDigit(str.charAt(pos)))
	    pos++;
	return pos;
    }

    // Skip one or more digits. Return -1 if no digits.
    private static int skipDigits(String str, int pos) {
	if (pos < str.length() && Character.isDigit(str.charAt(pos)))
	    pos++;
	else
	    return -1;
	while (pos < str.length() && Character.isDigit(str.charAt(pos)))
	    pos++;
	return pos;
    }

    // Do padding with zeros to make str (representing number)
    // long enough.
    private static String padding(String str) {
	while (str.length() < 8)
	    str = "0" + str;
	return str;
    }

    // Convert light file to xml file, or copy source xml file to other directory,
    // but only if target xml file does not yet exist,
    // or is older than input file.
    // Return base name (i.e. without .xml), or null if cannot be produced.
    private static String convertFile(File sourceDir, String name, File xmlDir) {
	File source = new File(sourceDir, name);
	if (!source.exists()) {
	    System.err.println("does not exist: " + source.getAbsolutePath());
	    return null;
	}
	String xmlEnding = ".xml";
	String lightEnding = ".light";
	String base;
	if (name.endsWith(xmlEnding)) {
	    base = name.substring(0, name.length()-xmlEnding.length());
	    File xml = new File(xmlDir, name);
	    if (!xmlDir.equals(sourceDir) &&
		    (!xml.exists() || source.lastModified() >= xml.lastModified())) {
		String sourceFull = source.getAbsolutePath();
		String xmlFull = xml.getAbsolutePath();
		REStoREScode.encodeRES(sourceFull, xmlFull);
	    }
	} else if (name.endsWith(lightEnding)) {
	    base = name.substring(0, name.length()-lightEnding.length());
	    File xml = new File(xmlDir, base + xmlEnding);
	    if (!xml.exists() || source.lastModified() >= xml.lastModified()) {
		AlightAlign.convertFile(source, xml);
		String xmlFull = xml.getAbsolutePath();
		REStoREScode.encodeRES(xmlFull, xmlFull);
	    }
	} else {
	    System.err.println("no or unknown extension: " + name);
	    base = null;
	}
	return base;
    }

    // Write formatted text to PDF file. Return name of that file, which is
    // derived from first source file.
    // We check whether new PDF file is really necessary (not already exists
    // or some input file is newer).
    private static String writePdf(LinkedList files, File xmlDir, File pdfDir, String header) {
	if (files.isEmpty() || pdfDir == null)
	    return null;
	String fileName = (String) files.getFirst();
	File target = new File(pdfDir, fileName + ".pdf");
	long agePdf = (target.exists() ? target.lastModified() : 0L);
	boolean newNeeded = (target.exists() ? false : true);
	String[] fullFiles = new String[files.size()];
	for (int i = 0; i < files.size(); i++) {
	    String base = (String) files.get(i);
	    File source = new File(xmlDir, base + ".xml");
	    if (source.lastModified() > agePdf)
		newNeeded = true;
	    fullFiles[i] = source.getAbsolutePath();
	}
	if (newNeeded) {
	    String fullFileName = new File(pdfDir, fileName).getAbsolutePath();
	    Align.writePdf(fullFiles, fullFileName, header);
	}
	return fileName + ".pdf";
    }

    // Copy file from one directory to another.
    private static void copyFileDir(File inDir, File outDir, String name) {
	File inFile = new File(inDir, name);
	File outFile = new File(outDir, name);
	try {
	    FileAux.copyFile(inFile, outFile);
	} catch (FileNotFoundException e) {
	    System.err.println(e.getMessage());
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	}
    }

}
