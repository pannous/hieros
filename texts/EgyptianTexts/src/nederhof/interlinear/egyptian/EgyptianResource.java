/***************************************************************************/
/*                                                                         */
/*  EgyptianResource.java                                                  */
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

// An Eygptian resource.

package nederhof.interlinear.egyptian;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.corpus.*;
import nederhof.corpus.frame.*;
import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.pdf.*;
import nederhof.interlinear.frame.*;
import nederhof.interlinear.frame.pdf.*;
import nederhof.interlinear.labels.*;
import nederhof.util.*;
import nederhof.util.xml.*;

public class EgyptianResource extends TextResource {

    // Allowable extensions for file.
    private static Vector extensions = new Vector();
    static {
        extensions.add("xml");
        extensions.add("txt");
    }

    // Overrides superclass.
    protected boolean allowableName(String name) {
        return FileAux.hasExtension(name, "xml") ||
            FileAux.hasExtension(name, "txt");
    }

    // The width of the widest label used in editing of
    // properties.
    private static int nameWidth() {
        return
            (new PropertyEditor.BoldLabel("labelname")).getPreferredSize().width + 5;
    }

    // Constructor for initial creation.
    public EgyptianResource() {
        propertyNames = new String[] { 
            "creator",
            "name",
            "labelname",
            "created",
            "modified",
            "version",
            "scheme",
            "language",
            "upload",
            "email",
            "password",
            "header",
            "bibliography"
        };
        setTierNames(new String[] {
            "hieroglyphic",
            "transliteration",
            "translation",
            "lexical" 
            });
    }

    // Make from file.
    public EgyptianResource(String location) throws IOException {
        this();
        this.location = location;
        read();
        detectEditable();
    }

    // Make from parsed XML file.
    public EgyptianResource(String location, Document doc) throws IOException {
        this();
        this.location = location;
        read(doc);
        detectEditable();
    }

    // Make from plain text file.
    public EgyptianResource(String location, LineNumberReader reader) 
                throws IOException {
        this();
        this.location = location;
        readText(reader);
        detectEditable();
    }

    // Make fresh resource. Fail if already exists.
    // Write empty XML content to it.
    public static EgyptianResource make(File file) throws IOException {
        if (file.exists())
            throw new IOException("File already exists: " + file);
        else if (!FileAux.hasExtension(file.getName(), "xml") &&
                !FileAux.hasExtension(file.getName(), "txt"))
            throw new IOException("Resource file name should end on .xml or .txt");

        EgyptianResource resource = new EgyptianResource();
        resource.location = file.getPath();
        resource.editable = true;
        resource.initProperty("created", getDate());
        resource.write(file);
        return resource;
    }

    ///////////////////////////////////////////////
    // Editor.

    protected PropertiesEditor makeEditor(EditChainElement parent) {
        Vector editors = new Vector();
        editors.add(
                new FileLocationEditor(this, nameWidth(), extensions));
        editors.add(
                new TextFieldEditor(this, "creator", nameWidth(),
                    "(name of person who created electronic resource)"));
        editors.add(
                new TextFieldEditor(this, "name", nameWidth(),
                    "(e.g. family name of intellectual author)"));
        editors.add(
                new TextFieldEditor(this, "labelname", nameWidth(),
                    "(e.g. first few letters of name)"));
        editors.add(
                new TextFieldEditor(this, "version", nameWidth(),
                    "(manuscript version, e.g. B1 or R)"));
        editors.add(
                new TextFieldEditor(this, "scheme", nameWidth(),
                    "(line numbering scheme, e.g. Old or New)"));
        editors.add(
                new EuropeanLanguageEditor(this, "language", nameWidth(),
                    "(which modern language used in the translation, if any)"));
        editors.add(
                new StyledTextEditor(this, "header", new EgyptianParsEditPopup()));
        editors.add(
                new StyledTextEditor(this, "bibliography", new EgyptianParsEditPopup()));
        return new PropertiesEditor(this, editors, parent);
    }

    //////////////////////////////////////////////
    // Properties.

    // Name is extended with version if present.
    public String getName() {
        String version = getStringProperty("version");
        String language = getStringProperty("language");
        String languageName = EuropeanLanguages.getName(language);
        return super.getName() +
            (!version.matches("\\s*") ? " (" + version + ")" : "") +
            (languageName != null && !languageName.matches("\\s*") ?
             " - " + languageName : "");
    }

    // String describing creation.
    private String creation() {
        String creator = getStringProperty("creator");
        String created = getStringProperty("created");
        String modified = getStringProperty("modified");
        StringBuffer buf = new StringBuffer();
        if (!creator.matches("\\s*") ||
                !created.matches("\\s*") ||
                !modified.matches("\\s*")) {
            if (!creator.matches("\\s*") || !created.matches("\\s*")) {
                buf.append("Created");
                if (!created.matches("\\s*"))
                    buf.append(" on " + created);
                if (!creator.matches("\\s*"))
                    buf.append(" by " + creator);
                buf.append(". ");
            }
            if (!modified.matches("\\s*"))
                buf.append("Last modified " + modified + ".\n");
        }
        return buf.toString();
    }

    //////////////////////////////////
    // Reading.

    // Parser of corpus files.
    private static DocumentBuilder parser = 
        SimpleXmlParser.construct(false, false);

    // Read from XML file or text file.
    protected void read() throws IOException {
        InputStream in = FileAux.addressToStream(location);
        if (FileAux.hasExtension(location, "xml")) {
            try {
                Document doc = parser.parse(in);
                read(doc);
            } catch (SAXException e) {
                throw new IOException(e.getMessage());
            }
        } else if (FileAux.hasExtension(location, "txt")) {
            LineNumberReader reader = new LineNumberReader(
                    new InputStreamReader(in, "UTF-8"));
            readText(reader);
        }
        in.close();
    }

    //////////////////////////////////
    // Reading from XML.

    // Read from parsed XML file.
    private void read(Document doc) throws IOException {
        Element top = doc.getDocumentElement();
        if (!top.getTagName().equals("egyptian"))
             throw new IOException("File is not Ancient Egyptian resource");

        retrieveString(top, "creator");
        retrieveString(top, "name");
        retrieveString(top, "labelname");
        retrieveString(top, "created");
        retrieveString(top, "modified");
        retrieveString(top, "version");
        retrieveString(top, "scheme");
        retrieveString(top, "language");
        retrieveString(top, "upload");
        retrieveString(top, "email");
        retrieveString(top, "password");

        retrieveHeader(doc);
        retrieveBibliography(doc);
        retrieveUse(doc);
        retrievePhrases(doc);
    }

    // Retrieve value from element and put it in resource.
    private void retrieveString(Element top, String prop) {
        String val = getValue(top.getAttributeNode(prop));
        initProperty(prop, val);
    }

    // Get header from document. Assume there is one.
    private void retrieveHeader(Document doc) throws IOException {
        StyledHelper helper = egyptianStyledHelper();
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
        paragraphs = TransHelper.splitTransLowerUpper(paragraphs);
        initProperty("header", paragraphs);
    }

    // Get bibliography from document. Assume there is one.
    private void retrieveBibliography(Document doc) throws IOException {
        StyledHelper helper = egyptianStyledHelper();
        Vector paragraphs = new Vector();
        NodeList list = doc.getElementsByTagName("bibliography");
        if (list.getLength() > 0) {
            Node node = list.item(0);
            if (node instanceof Element) {
                Element elem = (Element) node;
                paragraphs = helper.getParagraphs(elem);
            }
        }
        paragraphs = TransHelper.splitTransLowerUpper(paragraphs);
        initProperty("bibliography", paragraphs);
    }

    // Retrieve use of tiers.
    // The useds and showns are for backward compatibility
    // and should not occur in new resources.
    private void retrieveUse(Document doc) throws IOException {
        TreeMap modes = new TreeMap();
        NodeList list = doc.getElementsByTagName("tier");
        for (int i = 0; i < list.getLength(); i++) {
            Node item = list.item(i);
            if (item instanceof Element) {
                Element elem = (Element) item;
                String name = getValue(elem.getAttributeNode("name"));
                String mode = getValue(elem.getAttributeNode("mode"));
                modes.put(name, mode);
            }
        }
        for (int i = 0; i < nTiers(); i++) {
            String name = tierName(i);
            String mode = (String) modes.get(name);
            if (isValidMode(mode))
                setMode(i, mode);
            else
                setMode(i, SHOWN);
        }
    }

    // Get phrases.
    private void retrievePhrases(Document doc) throws IOException {
        NodeList list = doc.getElementsByTagName("segment");
        for (int i = 0; i < list.getLength(); i++) {
            Node item = list.item(i);
            if (item instanceof Element) {
                Element elem = (Element) item;
                retrievePhrase(elem);
            }
        }
    }

    // Get phrase.
    private void retrievePhrase(Element elem) throws IOException {
        NodeList children = elem.getChildNodes();
        Vector<ResourcePart>[] tiers = new Vector[nTiers()];
        for (int k = 0; k < nTiers(); k++)
            tiers[k] = new Vector<ResourcePart>();
        for (int j = 0; j < children.getLength(); j++) {
            Node child = children.item(j);
            if (child instanceof Element) {
                Element tier = (Element) child;
                String tierName = tier.getTagName();
                if (tierName.equals("texthi")) {
                    tiers[0] = ParsingHelper.parseHi(tier);
                } else if (tierName.equals("textal")) {
                    tiers[1] = ParsingHelper.parseAl(tier);
                } else if (tierName.equals("texttr")) {
                    tiers[2] = ParsingHelper.parseTr(tier);
                } else if (tierName.equals("textlx")) {
                    tiers[3] = ParsingHelper.parseLx(tier);
                } else if (tierName.equals("prec")) {
                    ParsingHelper.parsePrecedence(tier, precedence);
                } 
            }
        }
        TextPhrase phrase = new TextPhrase(this, tiers);
        addPhrase(phrase);
    }

    // Get attribute value, but "" if attribute is null.
    private static String getValue(Attr attr) {
        return attr == null ? "" : attr.getValue();
    }

    // Styled helper with transliteration and hieroglyphic.
    private static StyledHelper egyptianStyledHelper() {
        StyledHelper helper = new StyledHelper();
        helper.extraTypes.add(new String[] {"al", "trans"}); 
        helper.extraTypes.add(new String[] {"hi", "hiero"});
        return helper;
    }

    ////////////////////////////
    // Reading of text.

    // Read from text file.
    private void readText(LineNumberReader in) throws IOException {
        String line = trimEnd(in.readLine());
        while (line != null && line.matches("[a-zA-Z]* *=.*")) {
            readTextProperty(line);
            line = trimEnd(in.readLine());
        }

        StringBuffer header = new StringBuffer();
        StringBuffer bibliography = new StringBuffer();
        header.append("<header>");
        bibliography.append("<bibliography>");

        StringBuffer par = new StringBuffer();
        while (line != null && !line.matches("\\s*###\\s*")) {
            if (line.matches("\\s*")) {
                if (par.toString().matches("\\s*<li>.*</li>\\s*")) {
                    header.append(par);
                    par = new StringBuffer();
                } else if (!par.toString().matches("\\s*")) {
                    header.append("<p>" + par + "</p>");
                    par = new StringBuffer();
                }
            } else 
                par.append(line);
            line = trimEnd(in.readLine()) + " ";
        }
        if (line != null)
            line = trimEnd(in.readLine());
        while (line != null && !line.matches("\\s*###\\s*")) {
            if (line.matches("\\s*")) {
                if (!par.toString().matches("\\s*")) {
                    bibliography.append("<li>" + par + "</li>");
                    par = new StringBuffer();
                }
            } else 
                par.append(line);
            line = trimEnd(in.readLine()) + " ";
        }
        header.append("</header>");
        bibliography.append("</bibliography>");
        readTextHeader(header.toString());
        readTextBibliography(bibliography.toString());
        readTextPhrases(in);
    }

    // Pattern of property assignment.
    private static final Pattern propertyPat =
        Pattern.compile("^([a-zA-Z]*) *= *(.*)");

    // Take line to be property.
    private void readTextProperty(String line) throws IOException {
        Matcher m = propertyPat.matcher(line);
        if (m.find()) {
            String prop = m.group(1);
            String val = m.group(2);
            for (int i = 0; i < nTiers(); i++) {
                String name = tierName(i);
                if (prop.equals(name) && isValidMode(val)) {
                    setMode(i, val);
                    return;
                }
            }
            initProperty(prop, val);
            return;
        }
        throw new IOException("Strange line in " + location + ":\n" + line);
    }

    // Analyse header from XML.
    private void readTextHeader(String str) throws IOException {
        StringReader reader = new StringReader(str);
        InputSource source = new InputSource(reader);
        try {
            Document doc = parser.parse(source);
            retrieveHeader(doc);
        } catch (SAXException e) {
            throw new IOException(e.getMessage() + "\nIn:\n" + str);
        }
        reader.close();
    }

    // Analyse bibliography from XML.
    private void readTextBibliography(String str) throws IOException {
        StringReader reader = new StringReader(str);
        InputSource source = new InputSource(reader);
        try {
            Document doc = parser.parse(source);
            retrieveBibliography(doc);
        } catch (SAXException e) {
            throw new IOException(e.getMessage() + "\nIn:\n" + str);
        }
        reader.close();
    }

    // Read phrases.
    private void readTextPhrases(LineNumberReader in) throws IOException {
        String[] phrase = LiteHelper.readPhrase(in);
        while (phrase != null) {
            String xml = 
                XmlFileWriter.header +
                "<segment>" +
                "<texthi>" + LiteHelper.expandTags(phrase[0]) + "</texthi>\n" +
                "<textal>" + LiteHelper.expandTags(phrase[1]) + "</textal>\n" +
                "<texttr>" + LiteHelper.expandTags(phrase[2]) + "</texttr>\n" +
                "<textlx>" + LiteHelper.expandTags(phrase[3]) + "</textlx>\n" +
                phrase[4] + // the precedences
                "</segment>";
            StringReader reader = new StringReader(xml);
            InputSource source = new InputSource(reader);
            try {
                Document doc = parser.parse(source);
                Element top = doc.getDocumentElement();
                retrievePhrase(top);
            } catch (IOException e) {
                throw new IOException(e.getMessage() + "\nIn:\n" + xml);
            } catch (SAXException e) {
                throw new IOException(e.getMessage() + "\nIn:\n" + xml);
            }
            reader.close();
            phrase = LiteHelper.readPhrase(in);
        }
    }

    // Remove spurious whitespace symbols at end of string.
    private static String trimEnd(String str) {
        if (str == null)
            return str;
        else
            return str.replaceFirst("\\s+$", "");
    }

    ///////////////////////////
    // Writing.

    protected void write(PrintWriter out) throws IOException {
        if (FileAux.hasExtension(location, "xml"))
            writeXml(out);
        else 
            writeText(out);
    }

    ///////////////////////////
    // Writing of XML.

    // Write.
    protected void writeXml(PrintWriter out) throws IOException {
        writeHeader(out);
        writeHeading(out);
        writeBibliography(out);
        writeMode(out);
        writePhrases(out);
        writeFooter(out);
    }
    // As above, but exclusive of upload information.
    protected void writeXmlExclusive(PrintWriter out) throws IOException {
        writeHeaderExclusive(out);
        writeHeading(out);
        writeBibliography(out);
        writeMode(out);
        writePhrases(out);
        writeFooter(out);
    }

    // Write start of XML index file.
    protected void writeHeader(PrintWriter out) throws IOException {
        out.println(XmlFileWriter.header);
        out.println("<egyptian\n" +
                "  creator=\"" + getEscapedProperty("creator") + "\"\n" +
                "  name=\"" + getEscapedProperty("name") + "\"\n" +
                "  labelname=\"" + getEscapedProperty("labelname") + "\"\n" +
                "  created=\"" + getEscapedProperty("created") + "\"\n" +
                "  modified=\"" + getEscapedProperty("modified") + "\"\n" +
                "  version=\"" + getEscapedProperty("version") + "\"\n" +
                "  scheme=\"" + getEscapedProperty("scheme") + "\"\n" +
                "  language=\"" + getEscapedProperty("language") + "\"\n" +
                "  upload=\"" + getEscapedProperty("upload") + "\"\n" +
                "  email=\"" + getEscapedProperty("email") + "\"\n" +
                "  password=\"" + getEscapedProperty("password") + "\"\n" +
                ">");
    }
    // As above, but exclusive of upload information.
    protected void writeHeaderExclusive(PrintWriter out) throws IOException {
        out.println(XmlFileWriter.header);
        out.println("<egyptian\n" +
                "  creator=\"" + getEscapedProperty("creator") + "\"\n" +
                "  name=\"" + getEscapedProperty("name") + "\"\n" +
                "  labelname=\"" + getEscapedProperty("labelname") + "\"\n" +
                "  created=\"" + getEscapedProperty("created") + "\"\n" +
                "  modified=\"" + getEscapedProperty("modified") + "\"\n" +
                "  version=\"" + getEscapedProperty("version") + "\"\n" +
                "  scheme=\"" + getEscapedProperty("scheme") + "\"\n" +
                "  language=\"" + getEscapedProperty("language") + "\"\n" +
                ">");
    }

    // Write end of XML index file.
    private void writeFooter(PrintWriter out) throws IOException {
        out.println("</egyptian>");
    }

    // Write heading.
    // For bullets, let there be list item.
    private void writeHeading(PrintWriter out) throws IOException {
        StyledHelper helper = egyptianStyledHelper();
        Vector header = (Vector) getProperty("header");
        out.println("<header>");
        if (header != null)
            for (int i = 0; i < header.size(); i++) {
                Vector par = (Vector) header.get(i);
                par = TransHelper.mergeTransLowerUpper(par);
                out.println(helper.writeParagraph(par));
            }
        out.println("</header>");
    }

    // Write bibliography
    private void writeBibliography(PrintWriter out) throws IOException {
        StyledHelper helper = egyptianStyledHelper();
        helper.itemPars = true;
        Vector bibl = (Vector) getProperty("bibliography");
        if (bibl != null && bibl.size() > 0) {
            out.println("<bibliography>");
            for (int i = 0; i < bibl.size(); i++) {
                Vector par = (Vector) bibl.get(i);
                par = TransHelper.mergeTransLowerUpper(par);
                out.println(helper.writeParagraph(par));
            }
            out.println("</bibliography>");
        }
    }

    // Write mode of tiers.
    private void writeMode(PrintWriter out) throws IOException {
        for (int i = 0; i < nTiers(); i++) 
            out.println("<tier name=\"" + tierName(i) + "\" " +
                    "mode=\"" + getMode(i) + "\"/>");
    }

    // Write phrases.
    private void writePhrases(PrintWriter out) throws IOException {
        boolean isFirst = true;
        for (int i = 0; i < nPhrases(); i++) {
            TextPhrase phrase = getPhrase(i);
            if (!phrase.tiersEmpty()) {
                if (!isFirst) 
                    out.println();
                else
                    isFirst = false;
                writePhrase(out, phrase);
            }
        }
    }

    // Write phrases.
    private void writePhrase(PrintWriter out, TextPhrase phrase) 
                throws IOException {
        out.println("<segment>");
        for (int j = 0; j < nTiers(); j++) 
            if (!phrase.isEmptyTier(j)) {
                Vector<ResourcePart> tier = phrase.getTier(j);
                switch (j) {
                    case 0:
                        out.println("<texthi>");
                        out.println(ParsingHelper.writePartsHi(tier));
                        out.println("</texthi>");
                        break;
                    case 1:
                        out.println("<textal>");
                        out.println(ParsingHelper.writePartsAl(tier));
                        out.println("</textal>");
                        break;
                    case 2:
                        out.println("<texttr>");
                        out.println(ParsingHelper.writePartsTr(tier));
                        out.println("</texttr>");
                        break;
                    case 3:
                        out.println("<textlx>");
                        out.println(ParsingHelper.writePartsLx(tier));
                        out.println("</textlx>");
                }
            }
        for (int j = 0; j < nTiers(); j++) {
            Vector<ResourcePart> tier = phrase.getTier(j);
            Vector positions = ParsingHelper.getPositions(tier);
            writePosPrecedence(out, positions);
        }
        out.println("</segment>");
    }

    // Write precedence for links between positions in phrase.
    private void writePosPrecedence(PrintWriter out, Vector positions) 
                throws IOException {
        for (int i = 0; i < positions.size(); i++) {
            String id1 = (String) positions.get(i);
            LinkedList links = precedence.getFrom(id1);
            for (Iterator it = links.iterator(); it.hasNext(); ) {
                Link link = (Link) it.next();
                String type1 = link.type1;
                String id2 = link.id2;
                if (id1.compareTo(id2) != 0) {
                    id1 = XmlAux.escape(id1);
                    id2 = XmlAux.escape(id2);
                    out.println("<prec id1=\"" + id1 + "\"" +
                            (!type1.equals("start") ?
                             " type1=\"" + type1 + "\"" : "") +
                            " id2=\"" + id2 + "\"/>");
                }
            }
        }
    }

    ///////////////////////////
    // Writing of text file.

    // Write.
    protected void writeText(PrintWriter out) throws IOException {
        writeTextHeader(out);
        out.println();
        writeTextHeading(out);
        out.println("###");
        out.println();
        writeTextBibliography(out);
        out.println("###");
        writeTextPhrases(out);
    }

    protected void writeTextHeader(PrintWriter out) throws IOException {
        out.println("creator = " + getStringProperty("creator"));
        out.println("name = " + getStringProperty("name"));
        out.println("labelname = " + getStringProperty("labelname"));
        out.println("created = " + getStringProperty("created"));
        out.println("modified = " + getStringProperty("modified"));
        out.println("version = " + getStringProperty("version"));
        out.println("scheme = " + getStringProperty("scheme"));
        out.println("language = " + getStringProperty("language"));
        out.println("upload = " + getStringProperty("upload"));
        out.println("email = " + getStringProperty("email"));
        out.println("password = " + getStringProperty("password"));
        for (int i = 0; i < nTiers(); i++) 
            out.println(tierName(i) + " = " + getMode(i));
    }

    private void writeTextHeading(PrintWriter out) throws IOException {
        StyledHelper helper = egyptianStyledHelper();
        helper.unmarkedPars = true;
        Vector header = (Vector) getProperty("header");
        if (header != null)
            for (int i = 0; i < header.size(); i++) {
                Vector par = (Vector) header.get(i);
                par = TransHelper.mergeTransLowerUpper(par);
                out.println(helper.writeParagraph(par));
            }
    }

    private void writeTextBibliography(PrintWriter out) throws IOException {
        StyledHelper helper = egyptianStyledHelper();
        helper.unmarkedPars = true;
        Vector bibl = (Vector) getProperty("bibliography");
        if (bibl != null && bibl.size() > 0) {
            for (int i = 0; i < bibl.size(); i++) {
                Vector par = (Vector) bibl.get(i);
                par = TransHelper.mergeTransLowerUpper(par);
                out.println(helper.writeParagraph(par));
            }
        }
    }

    private void writeTextPhrases(PrintWriter out) throws IOException {
        for (int i = 0; i < nPhrases(); i++) {
            TextPhrase phrase = getPhrase(i);
            if (!phrase.tiersEmpty()) 
                LiteHelper.writePhrase(out, 
                        phrase.getTier(0),
                        phrase.getTier(1),
                        phrase.getTier(2),
                        phrase.getTier(3));
            for (int j = 0; j < nTiers(); j++) {
                Vector tier = phrase.getTier(j);
                Vector positions = ParsingHelper.getPositions(tier);
                LiteHelper.writePosPrecedence(out, precedence, positions);
            }
        }
    }

    //////////////////////////////////
    // Preamble.

    // Preamble showing properties and such.
    public Component preamble() {
        JPanel preamble = new JPanel();
        preamble.setLayout(new BoxLayout(preamble, BoxLayout.Y_AXIS));
        preamble.setBackground(Color.WHITE);
        preamble.add(creationPanel());
        preamble.add(headerPanel());
        preamble.add(bibliographyPanel());
        return preamble;
    }

    // Part of preamble about creation.
    private Component creationPanel() {
        String creation = XmlAux.escape(creation());
        JLabel textPane = new PropertyEditor.ItalicLabel(creation);
        return textPane;
    }

    // Part of preamble containing header.
    private Component headerPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(Color.WHITE);
        TitledBorder title = 
            BorderFactory.createTitledBorder(
                    new LineBorder(Color.GRAY, 2), getName());
        panel.setBorder(
                BorderFactory.createCompoundBorder(title,
                    BorderFactory.createEmptyBorder(0,5,5,5)));
        StyledTextPane pane = new UneditableStyledPane("header");
        panel.add(pane);
        return panel;
    }

    // Part of preamble containing bibliography.
    private Component bibliographyPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(Color.WHITE);
        TitledBorder title = 
            BorderFactory.createTitledBorder(
                    new LineBorder(Color.GRAY, 2), "bibliography");
        panel.setBorder(
                BorderFactory.createCompoundBorder(title,
                    BorderFactory.createEmptyBorder(0,5,5,5)));
        StyledTextPane pane = new UneditableStyledPane("bibliography");
        panel.add(pane);
        return panel;
    }

    // Pane for hieroglyphic text.
    private class UneditableStyledPane extends StyledTextPane {
        public UneditableStyledPane(final String prop) {
            super(Settings.inputTextFontName,
                Settings.inputTextFontSize);
            setEditable(false);
            int style = Settings.translitFontStyle;
            float size = (float) Settings.translitFontSize;
            Font lower = TransHelper.translitLower(style, size);
            Font upper = TransHelper.translitUpper(style, size);
            addFont("TransLower", style, lower);
            addStyle("translower", "TransLower", style);
            addFont("TransUpper", style, upper);
            addStyle("transupper", "TransUpper", style);
            addComponent("hiero", new HieroViewGenerator());
            addComponent("link", new LinkViewGenerator());
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        Vector pars = (Vector) getProperty(prop);
                        if (pars == null)
                            pars = new Vector();
                        setParagraphs(pars);
                    }
            });
        }
    }

    ///////////////////////////////////////////////////////
    // PDF of tiers.

    // Title
    public Vector pdfPreamble(PdfRenderParameters params) {
        Vector pars = new Vector();
        pdfCreation(pars, params);
        pdfHeader(pars, params);
        pdfBibliography(pars, params);
        return pars;
    }

    private void pdfCreation(Vector pars, PdfRenderParameters params) {
        EgyptianTierPdfPart p = new PlainPdfPart(creation());
        p.setParams(params);
        Vector par = new Vector();
        par.add(p);
        pars.add(par);
    }

    private void pdfHeader(Vector pars, PdfRenderParameters params) {
        Vector header = (Vector) getProperty("header");
        if (header != null)
            for (int i = 0; i < header.size(); i++) {
                Vector par = (Vector) header.get(i);
                Vector parts = ParagraphPdfHelper.paragraphToParts(par, params);
                pars.add(parts);
            }
    }

    private void pdfBibliography(Vector pars, PdfRenderParameters params) {
         String bullet = StyledTextPane.itemStart + " ";
         Vector bibl = (Vector) getProperty("bibliography");
         if (bibl != null && bibl.size() > 0) {
             EgyptianTierPdfPart header = new Header3Part("Bibliography");
             header.setParams(params);
             Vector headerPar = new Vector();
             headerPar.add(header);
             pars.add(headerPar);
             for (int i = 0; i < bibl.size(); i++) {
                 Vector par = (Vector) bibl.get(i);
                 par = (Vector) par.clone();
                 par.add(0, new Object[] {"plain", bullet});
                 Vector parts = ParagraphPdfHelper.paragraphToParts(par, params);
                 pars.add(parts);
             }
         }
    }

    ///////////////////////////////////////////////////////
    // Tiers.

    // Keep ids occurring in tiers.
    private TreeSet<String> ids = new TreeSet<String>();

    // Add to existing tiers new tiers.
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
        String label = getStringProperty("labelname");
        String version = getStringProperty("version");
        String scheme = getStringProperty("scheme");
        TreeMap<Integer,Vector<int[]>> phraseToPositions = new TreeMap<Integer,Vector<int[]>>();
        for (int i = 0; i < nTiers(); i++) 
            if (!getMode(i).equals(IGNORED) &&
                    (!isEmptyTier(i) || edit)) {
                Vector<Integer> phraseStart = new Vector<Integer>();
                TierConstructor constructor = 
                    new TierConstructor(location,
                            tiers.size(), version, scheme,
                            labelToPositions, 
                            labelToPrePositions, 
                            labelToPostPositions, 
                            phraseToPositions,
                            phraseStart,
                            resourceIdToPositions,
                            ids,
                            params, pdf, edit);
                Vector parts = new Vector();
                for (int j = 0; j < nPhrases(); j++) {
                    Vector<ResourcePart> resourceParts = getPhrase(j).getTier(i);
                    constructor.select(resourceParts, j);
                }
                if (nPhrases() == 0 && edit) 
                    constructor.selectEmpty();
                Tier tier = new Tier(tiers.size(), getMode(i),
                        constructor.parts());
                tiers.add(tier);
                tierNums.add(new Integer(i));
                labels.add(label);
                versions.add(version);
                phraseStarts.add(phraseStart);
            }
        LinkHelper.alignPhrases(tiers, phraseToPositions);
        LinkHelper.alignResourcePrecedence(tiers, location, location, precedence,
                resourceIdToPositions);
    }

    ///////////////////////////////////////////////////
    // Editing of phrases.

    // Name of id for position in phrase of tier.
    // Return null if failed. If labelled position does not exist, create one.
    public String positionId(Vector<ResourcePart> tier, int pos, boolean mayCreate) {
        return PhraseEditHelper.positionId(tier, pos, ids, mayCreate);
    }

    // Name of id for position in phrase of tier, plus offset.
    // Return null if failed. If labelled position does not exist, create one.
    // Wrapped bool says whether label was created.
    public TreeSet<LabelOffset> positionIdOffset(Vector<ResourcePart> tier, int pos, 
            boolean mayCreate, WrappedBool changed) {
        return PhraseEditHelper.positionIdOffset(tier, pos, ids, mayCreate, changed);
    }

    // Get editors for the tiers, except those that are ignored.
    public Vector getEditors(TextPhrase phrase) {
        Vector editors = new Vector();
        for (int i = 0; i < nTiers(); i++) 
            if (!getMode(i).equals(IGNORED)) {
                String name = tierName(i);
                Vector tier = phrase.getTier(i);
                if (name.equals("hieroglyphic"))
                    editors.add(
                            new HiEditor(i, name, tier));
                else if (name.equals("transliteration"))
                    editors.add(
                            new AlEditor(i, name, tier));
                else if (name.equals("translation"))
                    editors.add(
                            new TrEditor(i, name, tier));
                else if (name.equals("lexical"))
                    editors.add(
                            new LxEditor(i, name, tier));
            }
        return editors;
    }

    // Join phrases.
    // Put spaces in between where appropriate.
    // Normalize hieroglyphic to join fragments.
    public TextPhrase joinPhrases(TextPhrase phrase1, TextPhrase phrase2) {
        for (int i = 0; i < nTiers(); i++) 
            if (!phrase1.isEmptyTier(i) && !phrase2.isEmptyTier(i) &&
                    (tierName(i).equals("transliteration") || tierName(i).equals("translation")))
                addTrailingSpace(phrase1.getTier(i));
        TextPhrase join = TextPhrase.join(phrase1, phrase2);
        for (int i = 0; i < nTiers(); i++)
            if (tierName(i).equals("hieroglyphic"))
                PhraseEditHelper.normalizeHieroTier(join.getTier(i));
        return join;
    }
    private void addTrailingSpace(Vector<ResourcePart> tier) {
        if (tier.size() > 0) {
            ResourcePart last = tier.lastElement();
            if (last instanceof StringPart)
                ((StringPart) last).string += " ";
        }
    }

    // Cut phrase in i-th tier into two parts at position.
    // Subclass to define appropriately.
    public void cutPhrase(int i,
            Vector original, int pos, Vector left, Vector right) {
        PhraseEditHelper.cutPhrase(original, pos, left, right,
                tierName(i).equals("hieroglyphic"));
    }

    /////////////////////////////////////////////////////// 
    // Uploading.

    // Upload.
    public String upload() throws IOException {
        String server = getStringProperty("upload");
        if (!server.matches("\\s*")) {
            Uploader uploader = new Uploader(server);
            String email = getStringProperty("email");
            String password = getStringProperty("password");
            String content = exclusiveString();
            TreeMap parameters = new TreeMap();
            if (!email.matches("\\s*")) 
                parameters.put("email", email);
            if (!password.matches("\\s*")) 
                parameters.put("password", password);
            parameters.put("type", "resource");
            parameters.put("content", content);
            return uploader.uploadSimple(parameters);
        } else
            return super.upload();
    }

    // Can be uploaded?
    public boolean uploadable() {
        return !getStringProperty("upload").matches("\\s*");
    }

}
