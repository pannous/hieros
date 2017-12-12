/***************************************************************************/
/*                                                                         */
/*  ResourcePrecedence.java                                                */
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

// Alignment between positions in different resources.

package nederhof.interlinear.labels;

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.interlinear.*;
import nederhof.interlinear.frame.*;
import nederhof.util.*;
import nederhof.util.xml.*;

public class ResourcePrecedence extends TextResource {

    // Allowable extensions for file.
    private static Vector extensions = new Vector();
    static {
        extensions.add("xml");
    }

    // The width of the widest label used in editing of
    // properties.
    private static final int nameWidth =
        (new PropertyEditor.BoldLabel("file")).getPreferredSize().width + 5;

    // Constructor for initial creation.
    private ResourcePrecedence() {
        propertyNames = new String[] {
            "created",
            "modified",
            "resource1",
            "resource2",
            "precedence1",
            "precedence2",
        };
        setTierNames(null);
        initProperty("precedence1", new InterPosPrecedence());
        initProperty("precedence2", new InterPosPrecedence());
    }

    // Make from file.
    public ResourcePrecedence(String location) throws IOException {
        this();
        this.location = location;
        read();
        detectEditable();
    }

    // Make from parsed XML file.
    public ResourcePrecedence(String location, Document doc) throws IOException {
        this();
        this.location = location;
        read(doc);
        detectEditable();
    }

    // Make fresh file. Fail if already exists.
    // Write empty XML content to it.
    public static ResourcePrecedence make(File file) throws IOException {
        if (file.exists())
            throw new IOException("File already exists: " + file);
        else if (!FileAux.hasExtension(file.getName(), "xml"))
            throw new IOException("Resource file name should end on .xml");

        ResourcePrecedence map = new ResourcePrecedence();
        map.location = file.getPath();
        map.editable = true;
        map.initProperty("created", getDate());
        map.write(file);
        return map;
    }

    ///////////////////////////////////////////////
    // Editor.

    protected PropertiesEditor makeEditor(EditChainElement parent) {
        Vector editors = new Vector();
        editors.add(
                new FileLocationEditor(this, nameWidth, extensions));
	return new PropertiesEditor(this, editors, parent);
    }

    //////////////////////////////////////////////
    // Properties.

    // Set resources.
    public void setResources(TextResource resource1, TextResource resource2) {
	initProperty("resource1", resource1);
	initProperty("resource2", resource2);
    }

    // Get resources.
    public TextResource getResource1() {
	return (TextResource) getProperty("resource1");
    }
    public TextResource getResource2() {
	return (TextResource) getProperty("resource2");
    }

    // Name.
    public String getName() {
	return "alignment";
    }

    // Add precedence link forward, backward.
    public void addForward(String id1, int offset1, String type1,
	    String id2, int offset2, String type2) {
        InterPosPrecedence precedence = 
	    (InterPosPrecedence) getProperty("precedence1");
	precedence.add(id1, offset1, type1, id2, offset2, type2);
	makeModified();
    }
    public void addBackward(String id1, int offset1, String type1,
	    String id2, int offset2, String type2) {
        InterPosPrecedence precedence = 
	    (InterPosPrecedence) getProperty("precedence2");
	precedence.add(id1, offset1, type1, id2, offset2, type2);
	makeModified();
    }

    // Remove precedence links in carthesian product of the two
    // sets.
    public void removeForward(TreeSet positions1, TreeSet positions2) {
        InterPosPrecedence precedence = 
	    (InterPosPrecedence) getProperty("precedence1");
	precedence.remove(positions1, positions2);
	makeModified();
    }
    public void removeBackward(TreeSet positions1, TreeSet positions2) {
        InterPosPrecedence precedence = 
	    (InterPosPrecedence) getProperty("precedence2");
	precedence.remove(positions1, positions2);
	makeModified();
    }

    //////////////////////////////////
    // Reading.

    // Parser of corpus files.
    private static DocumentBuilder parser = 
	SimpleXmlParser.construct(false, false);

    // Read from XML file.
    private void read() throws IOException {
	InputStream in = FileAux.addressToStream(location);
        try {
            Document doc = parser.parse(in);
            read(doc);
        } catch (SAXException e) {
            throw new IOException(e.getMessage());
        }
	in.close();
    }

    // Read from parsed XML file.
    private void read(Document doc) throws IOException {
        Element top = doc.getDocumentElement();
        if (!top.getTagName().equals("precedence"))
             throw new IOException("File is not precedence");

        retrieveString(top, "created");
        retrieveString(top, "modified");

        retrievePrecedence(doc);
    }

    // Retrieve value from element and put it in resource.
    private void retrieveString(Element top, String prop) {
        String val = getValue(top.getAttributeNode(prop));
        initProperty(prop, val);
    }

    // Get alignment from document.
    private void retrievePrecedence(Document doc) throws IOException {
        InterPosPrecedence precedence1 = (InterPosPrecedence) getProperty("precedence1");
        InterPosPrecedence precedence2 = (InterPosPrecedence) getProperty("precedence2");
	NodeList list = doc.getElementsByTagName("prec1");
	for (int i = 0; i < list.getLength(); i++) {
	    Node item = list.item(i);
	    if (item instanceof Element) {
		Element elem = (Element) item;
		String id1 = getValue(elem.getAttributeNode("id1"));
		String id2 = getValue(elem.getAttributeNode("id2"));
		int offset1 = getInt(elem.getAttributeNode("offset1"));
		int offset2 = getInt(elem.getAttributeNode("offset2"));
		String type1 = getValue(elem.getAttributeNode("type1"), 
			"start");
		precedence1.add(id1, offset1, type1, id2, offset2, "start");
	    }
	}
	list = doc.getElementsByTagName("prec2");
	for (int i = 0; i < list.getLength(); i++) {
	    Node item = list.item(i);
	    if (item instanceof Element) {
		Element elem = (Element) item;
		String id1 = getValue(elem.getAttributeNode("id1"));
		String id2 = getValue(elem.getAttributeNode("id2"));
		int offset1 = getInt(elem.getAttributeNode("offset1"));
		int offset2 = getInt(elem.getAttributeNode("offset2"));
		String type1 = getValue(elem.getAttributeNode("type1"), 
			"start");
		precedence2.add(id1, offset1, type1, id2, offset2, "start");
	    }
	}
    }

    // Get attribute value, but "" if attribute is null.
    private static String getValue(Attr attr) {
        return getValue(attr, "");
    }

    // Get attribute, or default value if null.
    private static String getValue(Attr attr, String defaultVal) {
	return attr == null ? defaultVal : attr.getValue();
    }

    // Get attribute value, or 0 if failure.
    private static int getInt(Attr attr) {
	String s = getValue(attr);
	if (s.equals(""))
	    return 0;
	try {
	    return Integer.parseInt(s);
	} catch (NumberFormatException e) {
	    return 0;
	}
    }

    //////////////////////////////////
    // Writing.

    protected void write(PrintWriter out) throws IOException {
	writeXml(out);
    }

    protected void writeXml(PrintWriter out) throws IOException {
        writeHeader(out);
        writePrecedence(out);
        writeFooter(out);
    }

    // Write start of XML index file.
    private void writeHeader(PrintWriter out) throws IOException {
	out.println(XmlFileWriter.header);
        out.println("<precedence\n" +
                "  created=\"" + getEscapedProperty("created") + "\"\n" +
                "  modified=\"" + getEscapedProperty("modified") + "\">");
    }

    // Write end of XML index file.
    private void writeFooter(PrintWriter out) throws IOException {
        out.println("</precedence>");
    }

    // Write map.
    private void writePrecedence(PrintWriter out) throws IOException {
        InterPosPrecedence precedence1 = (InterPosPrecedence) getProperty("precedence1");
	for (Iterator it1 = precedence1.iterator(); it1.hasNext(); ) {
	    OffsetLink link = (OffsetLink) it1.next();
	    writePrecedenceLink(out, "prec1", link);
	}
        InterPosPrecedence precedence2 = (InterPosPrecedence) getProperty("precedence2");
	for (Iterator it2 = precedence2.iterator(); it2.hasNext(); ) {
	    OffsetLink link = (OffsetLink) it2.next();
	    writePrecedenceLink(out, "prec2", link);
	}
    }

    // Write precedence link.
    private static void writePrecedenceLink(PrintWriter out, 
	    String type, OffsetLink link) throws IOException {
	out.println("<" + type + 
		" id1=\"" + XmlAux.escape(link.id1) + "\"" +
		(link.offset1 == 0 ? "" : 
		    " offset1=\"" + link.offset1 + "\"") +
		(link.type1.equals("start") ? "" : 
		    " type1=\"" + link.type1 + "\"") +
		" id2=\"" + XmlAux.escape(link.id2) + "\"" +
		(link.offset2 == 0 ? "" : 
		    " offset2=\"" + link.offset2 + "\"") +
		(link.type2.equals("start") ? "" : 
		    " type2=\"" + link.type2 + "\"") +
		"/>");
    }

    //////////////////////////////////
    // Preamble.

    // Panel with preamble.
    public Component preamble() {
	return new JPanel(); // should not be used.
    }

    ///////////////////////////////////////////////////////
    // Tiers.

    // No tiers themselves are added, but precedence relations between tiers.
    public void addTiers(Vector tiers, TreeMap resourceIdToPositions,
            RenderParameters params) {
	String location1 = getResource1().getLocation();
	String location2 = getResource2().getLocation();
        InterPosPrecedence precedence1 = (InterPosPrecedence) getProperty("precedence1");
        InterPosPrecedence precedence2 = (InterPosPrecedence) getProperty("precedence2");
	LinkHelper.alignResourcePrecedence(tiers, 
		location1, location2,
		precedence1, resourceIdToPositions);
	LinkHelper.alignResourcePrecedence(tiers, 
		location2, location1,
		precedence2, resourceIdToPositions);
    }

}
