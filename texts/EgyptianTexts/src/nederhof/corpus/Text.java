/***************************************************************************/
/*                                                                         */
/*  Text.java                                                              */
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

// Text in linguistic corpus.

package nederhof.corpus;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.swing.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.util.*;
import nederhof.util.xml.*;

public class Text {

    // The file where text is found. This is URI.
    private String location;

    // The file summing resources and other properties of text
    // are only parsed when needed.
    // Up to that moment, this is false, and all
    // following variables are empty.
    private boolean analyzed = false;

    // Is location editable?
    private boolean editable = false;

    // A short description of the available resources,
    // e.g. modern languages for translations.
    private String description = "";

    // Mapping from language to main name.
    private TreeMap mainName = new TreeMap();

    // Mapping from language to TreeSet of other names.
    private TreeMap names = new TreeMap();

    // Places of text within collections.
    private Vector<CollectionItem> collections = new Vector<CollectionItem>();

    // The available resources for this text, as file names.
    private Vector<String> resources = new Vector<String>();

    // The precedence files between resources, as file names.
    private Vector<String[]> precedences = new Vector<String[]>();

    // Where should automatic alignment take place?
    private Vector<String[]> autoaligns = new Vector<String[]>();

    // Make text, without reading file. Location is URI.
    public Text(String location) {
	this.location = location;
    }

    // Make empty text.
    private Text() {
    }

    // Make fresh text. Fail if already exists.
    // Write empty XML content to it.
    public static Text makeText(File file) throws IOException {
	if (file.exists())
	    throw new IOException("File already exists: " + file);
	else if (!FileAux.hasExtension(file.getName(), "xml"))
	    throw new IOException("Text file name should end on .xml");

	Text text = new Text();
	text.location = file.getPath();
	text.save(file);
	return text;
    }

    //////////////////////////////////
    // Reading.

    // Parser of text files.
    private static DocumentBuilder parser = 
	SimpleXmlParser.construct(false, false);

    // Analyze text.
    public void analyze() {
	try {
	    InputStream in = FileAux.addressToStream(location);
	    Document doc = parser.parse(in);
	    processDoc(doc);
	    in.close();
	} catch (SAXException e) {
	    JOptionPane.showMessageDialog(null,
		    "Could not analyze text " + location + ": " + e.getMessage(),
		    "Reading error", JOptionPane.ERROR_MESSAGE);
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(null,
		    "Could not analyze text " + location + ": " + e.getMessage(),
		    "Reading error", JOptionPane.ERROR_MESSAGE);
	}
	detectEditable();
	analyzed = true;
    }

    private void processDoc(Document doc) throws IOException {
	Element top = doc.getDocumentElement();
	if (!top.getTagName().equals("text"))
	    throw new IOException("File is not text");

	// description
	description = getValue(top.getAttributeNode("description"));

	// names
	NodeList list = doc.getElementsByTagName("primary");
	for (int i = 0; i < list.getLength(); i++) {
	    Node item = list.item(i);
	    if (item instanceof Element) {
		Element elem = (Element) item;
		String language = getValue(elem.getAttributeNode("language"));
		String name = getValue(elem.getAttributeNode("name"));
		mainName.put(language, name);
	    }
	}
	list = doc.getElementsByTagName("secondary");
	for (int i = 0; i < list.getLength(); i++) {
	    Node item = list.item(i);
	    if (item instanceof Element) {
		Element elem = (Element) item;
		String language = getValue(elem.getAttributeNode("language"));
		String name = getValue(elem.getAttributeNode("name"));
		if (names.get(language) == null)
		    names.put(language, new TreeSet());
		TreeSet languageNames = (TreeSet) names.get(language);
		languageNames.add(name);
	    }
	}

	// collections
	list = doc.getElementsByTagName("collection");
	for (int i = 0; i < list.getLength(); i++) {
	    Node item = list.item(i);
	    if (item instanceof Element) {
		Element elem = (Element) item;
		collections.add(new CollectionItem(elem));
	    }
	}

	// resources
	list = doc.getElementsByTagName("resource");
	for (int i = 0; i < list.getLength(); i++) {
	    Node item = list.item(i);
	    if (item instanceof Element) {
		Element elem = (Element) item;
		String loc = getValue(elem.getAttributeNode("location"));
		resources.add(resolve(loc));
	    }
	}

	// precedence files
	list = doc.getElementsByTagName("precedence");
	for (int i = 0; i < list.getLength(); i++) {
	    Node item = list.item(i);
	    if (item instanceof Element) {
		Element elem = (Element) item;
		String loc = getValue(elem.getAttributeNode("location"));
		String loc1 = getValue(elem.getAttributeNode("resource1"));
		String loc2 = getValue(elem.getAttributeNode("resource2"));
		String locResolve = resolve(loc);
		String loc1Resolve = resolve(loc1);
		String loc2Resolve = resolve(loc2);
		precedences.add(new String[] {locResolve, loc1Resolve, loc2Resolve});
	    }
	}

	// Automatic alignment
	list = doc.getElementsByTagName("autoalign");
	for (int i = 0; i < list.getLength(); i++) {
	    Node item = list.item(i);
	    if (item instanceof Element) {
		Element elem = (Element) item;
		String loc1 = getValue(elem.getAttributeNode("location1"));
		String tier1 = getValue(elem.getAttributeNode("tier1"));
		String loc2 = getValue(elem.getAttributeNode("location2"));
		String tier2 = getValue(elem.getAttributeNode("tier2"));
		String loc1Resolve = resolve(loc1);
		String loc2Resolve = resolve(loc2);
		autoaligns.add(new String[] {loc1Resolve, tier1, loc2Resolve, tier2});
	    }
	}
    }

    // Get attribute value, but "" if attribute is null.
    private static String getValue(Attr attr) {
        return attr == null ? "" : attr.getValue();
    }

    // Only once, analyse location whether editable.
    private void detectEditable() {
        editable = false;
        try {
            if (!location.startsWith("jar")) {
                File file = new File(location);
                editable = file.canWrite();
            }
        } catch (SecurityException e) {
            // ignore
        }
    }

    ////////////////////////////////////////// 
    // Writing.

    // Write to file.
    // First write to temporary, before overwriting original file.
    public void save() throws IOException {
	if (!analyzed)
	    analyze();
	File temp = new File(location + "~");
	save(temp);
	save(new File(location));
	temp.delete();
    }

    // Write to file.
    private void save(File outFile) throws IOException {
	PrintWriter out = new XmlFileWriter(outFile);
	writeHeader(out);
	writeNames(out);
	writeCollections(out);
	writeResources(out);
	writePrecedences(out);
	writeAutoaligns(out);
	writeFooter(out);
	out.close();
    }

    // Write start of XML index file.
    private void writeHeader(PrintWriter out) throws IOException {
	out.println("<text description=\"" + XmlAux.escape(description) + "\">");
    }

    // Write end of XML index file.
    private void writeFooter(PrintWriter out) throws IOException {
	out.println("</text>");
    }

    // Write names.
    private void writeNames(PrintWriter out) throws IOException {
	Iterator it = mainName.keySet().iterator();
	while (it.hasNext()) {
	    String lang = (String) it.next();
	    String name = (String) mainName.get(lang);
	    out.println("<primary language=\"" + lang + "\" " +
		    "name=\"" + XmlAux.escape(name) + "\"/>");
	}

	it = names.keySet().iterator();
	while (it.hasNext()) {
	    String lang = (String) it.next();
	    TreeSet languageNames = (TreeSet) names.get(lang);
	    Iterator it2 = languageNames.iterator();
	    while (it2.hasNext()) {
		String name = (String) it2.next();
		out.println("<secondary language=\"" + lang + "\" " +
			"name=\"" + XmlAux.escape(name) + "\"/>");
	    }
	}
    }

    // Write collections.
    private void writeCollections(PrintWriter out) throws IOException {
	for (int i = 0; i < collections.size(); i++) {
	    CollectionItem collection = collections.get(i);
	    collection.print(out);
	}
    }

    // Write resources.
    private void writeResources(PrintWriter out) throws IOException {
	for (int i = 0; i < resources.size(); i++) {
	    String resource = resources.get(i);
	    out.println("<resource location=\"" + 
		    XmlAux.escape(relative(resource)) + "\"/>");
	}
    }

    // Write precedences.
    private void writePrecedences(PrintWriter out) throws IOException {
	for (int i = 0; i < precedences.size(); i++) {
	    String[] precedence = precedences.get(i);
	    out.println("<precedence\n" +
		    "  location=\"" + XmlAux.escape(relative(precedence[0])) + "\"\n" +
		    "  resource1=\"" + XmlAux.escape(relative(precedence[1])) + "\"\n" +
		    "  resource2=\"" + XmlAux.escape(relative(precedence[2])) + "\"/>");
	}
    }

    // Write autoaligns.
    private void writeAutoaligns(PrintWriter out) throws IOException {
	for (int i = 0; i < autoaligns.size(); i++) {
	    String[] autoalign = autoaligns.get(i);
	    out.println("<autoalign\n" +
		    "  location1=\"" + XmlAux.escape(relative(autoalign[0])) + "\"\n" +
		    "  tier1=\"" + XmlAux.escape(autoalign[1]) + "\"\n" +
		    "  location2=\"" + XmlAux.escape(relative(autoalign[2])) + "\"\n" +
		    "  tier2=\"" + XmlAux.escape(autoalign[3]) + "\"/>");
	}
    }

    //////////////////////////////
    // Getters/setters.

    public String getLocation() {
	return location;
    }

    public void setLocation(String location) {
	this.location = location;
    }

    public boolean isEditable() {
	if (!analyzed)
	    analyze();
	return editable;
    }

    public String getDescription() {
	if (!analyzed)
	    analyze();
	return description;
    }

    public void setDescription(String description) {
	if (!analyzed)
	    analyze();
	this.description = description;
    }

    public TreeMap getMainName() {
	if (!analyzed)
	    analyze();
	return mainName;
    }

    public void setMainName(TreeMap mainName) {
	if (!analyzed)
	    analyze();
	this.mainName = mainName;
    }

    public TreeMap getNames() {
	if (!analyzed)
	    analyze();
	return names;
    }

    public TreeSet getNames(String lang) {
	if (!analyzed)
	    analyze();
	TreeSet otherNames = (TreeSet) names.get(lang);
	if (otherNames == null)
	    return new TreeSet();
	else
	    return otherNames;
    }

    public void setNames(TreeMap names) {
	if (!analyzed)
	    analyze();
	this.names = names;
    }

    public Vector<CollectionItem> getCollections() {
	if (!analyzed)
	    analyze();
	return collections;
    }

    public void setCollections(Vector<CollectionItem> collections) {
	if (!analyzed)
	    analyze();
	this.collections = collections;
    }

    // Get file addresses of resources.
    public Vector<String> getResources() {
	if (!analyzed)
	    analyze();
	return (Vector<String>) resources.clone();
    }

    // Make sure file names are relative to text file.
    public void setResources(Vector<String> resources) {
	if (!analyzed)
	    analyze();
	this.resources = resources;
    }

    // Get precendences, each as triple of file name, and that of both resources.
    public Vector<String[]> getPrecedences() {
	if (!analyzed)
	    analyze();
	return (Vector<String[]>) precedences.clone();
    }

    // Make sure file names are relative to text file.
    public void setPrecedences(Vector<String[]> precedences) {
	if (!analyzed)
	    analyze();
	this.precedences = precedences;
    }

    // Get autoalign, each as 4-tuple of file name, tier name, 
    // file name tier name.
    public Vector<String[]> getAutoaligns() {
	if (!analyzed)
	    analyze();
	return (Vector<String[]>) autoaligns.clone();
    }

    // Make sure file names are relative to text file.
    public void setAutoaligns(Vector<String[]> autoaligns) {
	if (!analyzed)
	    analyze();
	this.autoaligns = autoaligns;
    }

    // Get name by prioritized language. If not found, give any name.
    public String getName() {
	if (!analyzed)
	    analyze();

	for (int i = 0; i < Settings.languages.length; i++) 
	    if (mainName.get(Settings.languages[i]) != null)
		return (String) mainName.get(Settings.languages[i]);

	Iterator it = mainName.values().iterator();
	while (it.hasNext()) 
	    return (String) it.next();

	return "no name";
    }

    // Move text to different location.
    public void moveTo(File newLoc) throws IOException {
	File oldLoc = new File(location);
	if (newLoc.equals(oldLoc))
	    ; // ignore
	else if (newLoc.exists())
	    throw new IOException("Target file exists: " + newLoc.getPath());
	else if (!FileAux.hasExtension(newLoc.getName(), "xml"))
	    throw new IOException("Text file name should end on .xml");
	else
	    try {
		save(newLoc);
		location = newLoc.getPath();
		oldLoc.delete();
	    } catch (FileNotFoundException e) {
		throw new IOException(e.getMessage());
	    }
    }

    ////////////////////////////////////////////////////////
    // File auxiliaries.

    // File path of resource that is relative to directory of text
    // converted to path relative to current directory.
    private String resolve(String resourceLocation) {
	return FileAux.resolve(location, resourceLocation);
    }

    // File relative to text.
    private File relative(File file) {
        File textFile = new File(location);
        return FileAux.getRelativePath(file, textFile.getParentFile());
    }
    // Same, but for string.
    private String relative(String file) {
        return relative(new File(file)).getPath();
    }

    ////////////////////////////////////////// 
    // Testing.

    public static void main(String[] args) {
	try {
	    // Text t1 = makeText(new File("testfile.xml"));
	    Text t1 = new Text("testdir/testtext.xml");
	    // t1.analyze();
	    // t1.moveTo(new File("testdir/testtext.xml"));
	    t1.getResources();
	} catch (Exception e) {
	    System.out.println(e.getMessage());
	}
    }

}
