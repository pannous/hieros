/***************************************************************************/
/*                                                                         */
/*  SchemeMap.java                                                         */
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

// Mapping between numbering scheme, for one text version.

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

public class SchemeMap extends TextResource {

    // Allowable extensions for file.
    private static Vector extensions = new Vector();
    static {
        extensions.add("xml");
    }

    // The width of the widest label used in editing of
    // properties.
    private static final int nameWidth =
        (new PropertyEditor.BoldLabel("labelname")).getPreferredSize().width + 5;

    // Constructor for initial creation. 
    private SchemeMap() {
        propertyNames = new String[] {
            "creator",
            "name",
            "labelname",
            "created",
            "modified",
            "version",
            "scheme1",
            "scheme2",
            "header",
            "bibliography",
            "mapping"
        };
        setTierNames(null);
    }

    // Make from file.
    public SchemeMap(String location) throws IOException {
	this();
	this.location = location;
	read();
	detectEditable();
    }

    // Make from parsed XML file.
    public SchemeMap(String location, Document doc) throws IOException {
	this();
	this.location = location;
	read(doc);
	detectEditable();
    }

    // Make fresh map. Fail if already exists.
    // Write empty XML content to it.
    public static SchemeMap make(File file) throws IOException {
	if (file.exists())
	    throw new IOException("File already exists: " + file);
	else if (!FileAux.hasExtension(file.getName(), "xml"))
	    throw new IOException("Resource file name should end on .xml");

        SchemeMap map = new SchemeMap();
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
        editors.add(
                new TextFieldEditor(this, "creator", nameWidth,
                    "(name of person who created electronic resource)"));
        editors.add(
                new TextFieldEditor(this, "name", nameWidth,
                    "(e.g. family name of intellectual author)"));
        editors.add(
                new TextFieldEditor(this, "labelname", nameWidth,
                    "(e.g. first few letters of name)"));
        editors.add(
                new TextFieldEditor(this, "version", nameWidth,
                    "(manuscript version, e.g. B1 or R)"));
        editors.add(
                new TextFieldEditor(this, "scheme1", nameWidth,
                    "(first line numbering scheme, e.g. Old)"));
        editors.add(
                new TextFieldEditor(this, "scheme2", nameWidth,
                    "(second line numbering scheme, e.g. New)"));
        editors.add(
                new StyledTextEditor(this, "header", new SimpleEditPopup()));
        editors.add(
                new StyledTextEditor(this, "bibliography", new SimpleEditPopup()));
	editors.add(
		new SchemeMapEditor(this, "mapping"));
        return new PropertiesEditor(this, editors, parent);
    }

    ////////////////////////////////////////////// 
    // Properties.

    // Name is extended with version and schemes if present.
    public String getName() {
        String versionMaybe = getStringProperty("version");
	String version = 
	    !versionMaybe.matches("\\s*") ?
	     " - " + versionMaybe : null;
        String scheme1 = getStringProperty("scheme1");
        String scheme2 = getStringProperty("scheme2");
	String schemes = 
	    (!scheme1.matches("\\s*") && !scheme2.matches("\\s*")) ?
		" " + scheme1 + "/" + scheme2 : null;
        return super.getName() +
            (version != null ? version : "") +
            (schemes != null ? schemes : "");
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
	if (!top.getTagName().equals("schememap"))
	     throw new IOException("File is not scheme mapping");

	retrieveString(top, "creator");
	retrieveString(top, "name");
	retrieveString(top, "labelname");
	retrieveString(top, "created");
	retrieveString(top, "modified");
	retrieveString(top, "version");
	retrieveString(top, "scheme1");
	retrieveString(top, "scheme2");

	retrieveHeader(doc);
	retrieveBibliography(doc);
	retrieveMapping(doc);
    }

    // Retrieve value from element and put it in resource.
    private void retrieveString(Element top, String prop) {
	String val = getValue(top.getAttributeNode(prop));
        initProperty(prop, val);
    }

    // Get header from document. Assume there is one.
    private void retrieveHeader(Document doc) throws IOException {
	StyledHelper helper = new StyledHelper();
	helper.allowBullets = true;
	Vector paragraphs = new Vector();
	NodeList list = doc.getElementsByTagName("header");
	if (list.getLength() > 0) {
	    Node node = list.item(0);
	    if (node instanceof Element) {
		Element elem = (Element) node;
		paragraphs = helper.getParagraphs(elem);
	    }
	}
	initProperty("header", paragraphs);
    }

    // Get bibliography from document. Assume there is one.
    private void retrieveBibliography(Document doc) throws IOException {
	StyledHelper helper = new StyledHelper();
	Vector paragraphs = new Vector();
	NodeList list = doc.getElementsByTagName("bibliography");
	if (list.getLength() > 0) {
	    Node node = list.item(0);
	    if (node instanceof Element) {
		Element elem = (Element) node;
		paragraphs = helper.getParagraphs(elem);
	    }
	}
	initProperty("bibliography", paragraphs);
    }

    // Get mapping from document.
    private void retrieveMapping(Document doc) throws IOException {
	NodeList list = doc.getElementsByTagName("map");
	Vector mapping = new Vector(list.getLength());
	for (int i = 0; i < list.getLength(); i++) {
	    Node item = list.item(i);
	    if (item instanceof Element) {
		Element elem = (Element) item;
		String first = getValue(elem.getAttributeNode("first"));
		String second = getValue(elem.getAttributeNode("second"));
		mapping.add(new String[] {first, second});
	    }
	}
	initProperty("mapping", mapping);
    }

    // Get attribute value, but "" if attribute is null.
    private static String getValue(Attr attr) {
	return attr == null ? "" : attr.getValue();
    }

    //////////////////////////////////
    // Writing.

    protected void write(PrintWriter out) throws IOException {
	writeXml(out);
    }

    protected void writeXml(PrintWriter out) throws IOException {
        writeHeader(out);
	writeHeading(out);
	writeBibliography(out);
	writeMap(out);
        writeFooter(out);
    }

    // Write start of XML index file.
    private void writeHeader(PrintWriter out) throws IOException {
	out.println(XmlFileWriter.header);
        out.println("<schememap\n" +
		"  creator=\"" + getEscapedProperty("creator") + "\"\n" +
		"  name=\"" + getEscapedProperty("name") + "\"\n" +
		"  labelname=\"" + getEscapedProperty("labelname") + "\"\n" +
		"  created=\"" + getEscapedProperty("created") + "\"\n" +
		"  modified=\"" + getEscapedProperty("modified") + "\"\n" +
		"  version=\"" + getEscapedProperty("version") + "\"\n" +
		"  scheme1=\"" + getEscapedProperty("scheme1") + "\"\n" +
		"  scheme2=\"" + getEscapedProperty("scheme2") + "\">");
    }

    // Write end of XML index file.
    private void writeFooter(PrintWriter out) throws IOException {
        out.println("</schememap>");
    }

    // Write heading.
    // For bullets, let there be list item.
    private void writeHeading(PrintWriter out) throws IOException {
	StyledHelper helper = new StyledHelper();
	Vector header = (Vector) getProperty("header");
	out.println("<header>");
	if (header != null)
	    for (int i = 0; i < header.size(); i++) {
		Vector par = (Vector) header.get(i);
		out.println(helper.writeParagraph(par));
	    }
	out.println("</header>");
    }

    // Write bibliography
    private void writeBibliography(PrintWriter out) throws IOException {
	StyledHelper helper = new StyledHelper();
	helper.itemPars = true;
	Vector bibl = (Vector) getProperty("bibliography");
	if (bibl != null && bibl.size() > 0) {
	    out.println("<bibliography>");
	    for (int i = 0; i < bibl.size(); i++) {
		Vector par = (Vector) bibl.get(i);
		out.println(helper.writeParagraph(par));
	    }
	    out.println("</bibliography>");
	}
    }

    // Write map.
    private void writeMap(PrintWriter out) throws IOException {
	out.println("<mapping>");
	Vector mapping = (Vector) getProperty("mapping");
	if (mapping != null) 
	    for (int i = 0; i < mapping.size(); i++) {
		String[] pair = (String[]) mapping.get(i);
		out.println("<map first=\"" + 
			XmlAux.escape(pair[0]) + "\" " +
			"second=\"" + 
			XmlAux.escape(pair[1]) + "\"/>");
	    }
	out.println("</mapping>");
    }

    //////////////////////////////////
    // Preamble.

    // Panel with preamble.
    public Component preamble() {
	JTextPane text = new JTextPane() {
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);
                super.paintComponent(g2);
	    }
	};
	text.setEditable(false);
	text.setContentType("text/html");
	text.setText(html());
	return text;
    }

    // HTML view of contents.
    private String html() {
	StringBuffer buf = new StringBuffer();
	buf.append("<html>\n");
	htmlCreation(buf);
	htmlHeader(buf);
	htmlBibliography(buf);
	// htmlMapping(buf); // doesn't seem desirable 
	buf.append("</html>\n");
	return buf.toString();
    }

    // Write HTML creation info.
    private void htmlCreation(StringBuffer buf) {
	String creator = getEscapedProperty("creator");
	String created = getEscapedProperty("created");
	String modified = getEscapedProperty("modified");
	if (!creator.matches("\\s*") || 
		!created.matches("\\s*") || 
		!modified.matches("\\s*")) {
	    buf.append("<i>\n");
	    if (!creator.matches("\\s*") || !created.matches("\\s*")) {
		buf.append("Created");
		if (!created.matches("\\s*"))
		    buf.append(" on " + created);
		if (!creator.matches("\\s*"))
		    buf.append(" by " + creator);
		buf.append(".\n");
	    }
	    if (!modified.matches("\\s*"))
		buf.append("Last modified " + modified + ".\n");
	    buf.append("</i>\n");
	}
    }

    // Write HTML header.
    private void htmlHeader(StringBuffer buf) {
	String name = getName();
	buf.append("<h1>" + name + "</h1>\n");

	Vector header = (Vector) getProperty("header");
	if (header != null) {
	    StyledHelper helperHeader = new StyledHelper();
	    boolean inUlSeries = false;
	    for (int i = 0; i < header.size(); i++) {
		Vector par = (Vector) header.get(i);
		String parString = helperHeader.writeParagraph(par);
		if (parString.startsWith("<li>") && !inUlSeries) {
		    buf.append("<ul>");
		    inUlSeries = true;
		} else if (parString.startsWith("<p>") && inUlSeries) {
		    buf.append("</ul>");
		    inUlSeries = false;
		}
		buf.append(helperHeader.writeParagraph(par));
	    }
	    if (inUlSeries)
	        buf.append("</ul>");
	}
    }

    // Write HTML bibliography.
    private void htmlBibliography(StringBuffer buf) {
	Vector bibliography = (Vector) getProperty("bibliography");
	if (bibliography != null && bibliography.size() > 0) {
	    StyledHelper helperBibl = new StyledHelper();
	    helperBibl.itemPars = true;
	    buf.append("<h2>Bibliography</h2>\n");
	    buf.append("<ul>\n");
	    for (int i = 0; i < bibliography.size(); i++) {
		Vector par = (Vector) bibliography.get(i);
		buf.append(helperBibl.writeParagraph(par));
	    }
	    buf.append("</ul>\n");
	}
    }

    // Write HTML mapping.
    private void htmlMapping(StringBuffer buf) {
	Vector mapping = (Vector) getProperty("mapping");
	String version = getEscapedProperty("version");
	String scheme1 = getEscapedProperty("scheme1");
	String scheme2 = getEscapedProperty("scheme2");

	if (mapping != null) {
	    if (version.matches("\\s*"))
		buf.append("<h2>Mapping</h2>\n");
	    else
		buf.append("<h2>Version " + version + "</h2>\n");
	    buf.append("<table border=\"1\">\n");
	    buf.append("<tr><th>");
	    buf.append(scheme1);
	    buf.append("</th><th>");
	    buf.append(scheme2);
	    buf.append("</th></tr>\n");
	    for (int i = 0; i < mapping.size(); i++) {
		String[] pair = (String[]) mapping.get(i);
		buf.append("<tr><td>" + 
			XmlAux.escape(pair[0]) + "</td><td>" + 
			XmlAux.escape(pair[1]) + "</td></tr>\n");
	    }
	    buf.append("</table>");
	}
    }

    // Add new mappings.
    public void addTiers(Vector<Tier> tiers, Vector<Integer> tierNums, 
	    Vector<String> labels, Vector<String> versions,
	    Vector<Vector<Integer>> phraseStarts,
            TreeMap<VersionSchemeLabel,Vector<int[]>> labelToPositions,
            TreeMap<VersionSchemeLabel,Vector<int[]>> labelToPrePositions,
            TreeMap<VersionSchemeLabel,Vector<int[]>> labelToPostPositions,
            TreeMap<ResourceId,int[]> resourceIdToPositions,
	    TreeMap<VersionSchemeLabel,VersionSchemeLabel> schemeMappings,
            RenderParameters params, 
	    boolean pdf, boolean edit) {
	Vector mapping = (Vector) getProperty("mapping");
        String version = getStringProperty("version");
        String scheme1 = getStringProperty("scheme1");
        String scheme2 = getStringProperty("scheme2");
	for (int i = 0; i < mapping.size(); i++) {
	    String[] pair = (String[]) mapping.get(i);
	    VersionSchemeLabel label1 = new VersionSchemeLabel(version, scheme1, pair[0]);
	    VersionSchemeLabel label2 = new VersionSchemeLabel(version, scheme2, pair[1]);
	    if (schemeMappings.get(label2) == null)
		schemeMappings.put(label1, label2);
	    else 
		schemeMappings.put(label1, schemeMappings.get(label2));
	}
    }

}

