// Lexical annotation hieroglyphic.

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
import nederhof.interlinear.egyptian.lex.*;
import nederhof.interlinear.egyptian.pdf.*;
import nederhof.interlinear.frame.*;
import nederhof.interlinear.frame.pdf.*;
import nederhof.interlinear.labels.*;
import nederhof.res.*;
import nederhof.util.*;
import nederhof.util.xml.*;

public class EgyptianLexico extends TextResource {

    // Allowable extensions for file.
    private static Vector extensions = new Vector();
    static {
        extensions.add("xml");
    }

    // Overrides superclass.
    protected boolean allowableName(String name) {
        return FileAux.hasExtension(name, "xml");
    }

    // The width of the widest label used in editing of
    // properties.
    private static int nameWidth() {
        return
            (new PropertyEditor.BoldLabel("labelname")).getPreferredSize().width + 5;
    }

    // Constructor for initial creation.
    public EgyptianLexico() {
        propertyNames = new String[] {
            "creator",
            "name",
            "labelname",
            "created",
            "modified",
            "version",
	    "language",
            "header",
            "bibliography"
        };
        setTierNames(new String[] {
            "lexical"
            });
    }

    // Make from file.
    public EgyptianLexico(String location) throws IOException {
        this();
        this.location = location;
        read();
        detectEditable();
    }

    // Make from parsed XML file.
    public EgyptianLexico(String location, Document doc) throws IOException {
        this();
        this.location = location;
        read(doc);
        detectEditable();
    }

    // Make fresh resource. Fail if already exists.
    // Write empty XML content to it.
    public static EgyptianLexico make(File file) throws IOException {
        if (file.exists())
            throw new IOException("File already exists: " + file);
        else if (!FileAux.hasExtension(file.getName(), "xml"))
            throw new IOException("Resource file name should end on .xml");

        EgyptianLexico resource = new EgyptianLexico();
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
                new EuropeanLanguageEditor(this, "language", nameWidth(),
                    "(which modern language used in translation, if any)"));
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

    // Read from XML file.
    protected void read() throws IOException {
        InputStream in = FileAux.addressToStream(location);
        if (FileAux.hasExtension(location, "xml")) {
            try {
                Document doc = parser.parse(in);
                read(doc);
            } catch (SAXException e) {
                throw new IOException(e.getMessage());
            }
        } 
        in.close();
    }

    //////////////////////////////////
    // Reading from XML.

    // Read from parsed XML file.
    private void read(Document doc) throws IOException {
        Element top = doc.getDocumentElement();
        if (!top.getTagName().equals("lex"))
             throw new IOException("File is not Ancient Egyptian lexical annotation");

        retrieveString(top, "creator");
        retrieveString(top, "name");
        retrieveString(top, "labelname");
        retrieveString(top, "created");
        retrieveString(top, "modified");
        retrieveString(top, "version");
        retrieveString(top, "language");

        retrieveHeader(doc);
        retrieveBibliography(doc);
        retrieveUse(doc);
        retrieveFragments(doc);
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
	int tierNo = 0; // the only tier
	String name = tierName(tierNo);
	String mode = (String) modes.get(name);
	if (isValidMode(mode))
	    setMode(tierNo, mode);
	else
	    setMode(tierNo, SHOWN);
    }

    // Retrieve unused fragments.
    private void retrieveFragments(Document doc) throws IOException {
	Vector<String> fragments = new Vector<String>();
	NodeList list = doc.getElementsByTagName("fragment");
        for (int i = 0; i < list.getLength(); i++) {
	    Node node = list.item(i);
	    if (node instanceof Element) {
		String fragment = "";
		NodeList childNodes = node.getChildNodes();
		for (int j = 0; j < childNodes.getLength(); j++) {
		    Node childNode = childNodes.item(j);
		    if (childNode instanceof org.w3c.dom.Text)
			fragment += childNode.getNodeValue();
		}
		fragments.add(fragment);
	    }
	}
	initProperty("fragments", fragments);
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
	String id = getValue(elem.getAttributeNode("id"));
        NodeList children = elem.getChildNodes();
        Vector[] tiers = new Vector[nTiers()];
	Vector tier = new Vector();
	String texthi = "";
	String textal = "";
	String texttr = "";
	String textfo = "";
	String cite = "";
	String href = "";
	String keyhi = "";
	String keyal = "";
	String keytr = "";
	String keyfo = "";
	String dicthi = "";
	String dictal = "";
	String dicttr = "";
	String dictfo = "";
        for (int j = 0; j < children.getLength(); j++) {
            Node child = children.item(j);
            if (child instanceof Element) {
                Element childElem = (Element) child;
                String name = childElem.getTagName();
                if (name.equals("texthi")) {
		    texthi = normHiero(retrieveText(childElem));
                } else if (name.equals("textal")) {
                    textal = normText(retrieveText(childElem));
                } else if (name.equals("texttr")) {
                    texttr = normText(retrieveText(childElem));
                } else if (name.equals("textfo")) {
                    textfo = normText(retrieveText(childElem));
                } else if (name.equals("cite")) {
                    cite = normText(retrieveText(childElem));
                } else if (name.equals("href")) {
                    href = normText(retrieveText(childElem));
		} else if (name.equals("keyhi")) {
		    keyhi = normHiero(retrieveText(childElem));
                } else if (name.equals("keyal")) {
                    keyal = normText(retrieveText(childElem));
                } else if (name.equals("keytr")) {
                    keytr = normText(retrieveText(childElem));
                } else if (name.equals("keyfo")) {
                    keyfo = normText(retrieveText(childElem));
		} else if (name.equals("dicthi")) {
		    dicthi = normHiero(retrieveText(childElem));
                } else if (name.equals("dictal")) {
                    dictal = normText(retrieveText(childElem));
                } else if (name.equals("dicttr")) {
                    dicttr = normText(retrieveText(childElem));
                } else if (name.equals("dictfo")) {
                    dictfo = normText(retrieveText(childElem));
                }
            }
        }
	tier.add(new LxPart(texthi, textal, texttr, textfo,
		    cite, href, keyhi, keyal, keytr, keyfo,
		    dicthi, dictal, dicttr, dictfo, id));
	tiers[0] = tier;
        TextPhrase phrase = new TextPhrase(this, tiers);
        addPhrase(phrase);
    }

    // Get text from element.
    private String retrieveText(Element elem) throws IOException {
	String text = "";
	NodeList children = elem.getChildNodes();
	for (int i = 0; i < children.getLength(); i++) {
	    Node child = children.item(i);
	    if (child instanceof CharacterData) {
		CharacterData data = (CharacterData) child;
		text += data.getData();
	    }
	}
	return text;
    }

    // Normalize it.
    private String normText(String s) {
	s = s.replaceAll("\\s\\s*", " ");
	s = s.replaceAll("^ ", "");
	s = s.replaceAll(" $", "");
	return s;
    }
    // Normalize hieroglyphic.
    private String normHiero(String s) {
	s = s.replaceAll("^\\s*", "");
	s = s.replaceAll("\\s*$", "");
	return s;
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

    ///////////////////////////
    // Writing.

    protected void write(PrintWriter out) throws IOException {
        if (FileAux.hasExtension(location, "xml"))
            writeXml(out);
    }

    ///////////////////////////
    // Writing of XML.

    // Write.
    protected void writeXml(PrintWriter out) throws IOException {
        writeHeader(out);
        writeHeading(out);
        writeBibliography(out);
        writeMode(out);
        writeFragments(out);
        writePhrases(out);
        writeFooter(out);
    }

    // Write start of XML index file.
    protected void writeHeader(PrintWriter out) throws IOException {
        out.println(XmlFileWriter.header);
        out.println("<lex\n" +
                "  creator=\"" + getEscapedProperty("creator") + "\"\n" +
                "  name=\"" + getEscapedProperty("name") + "\"\n" +
                "  labelname=\"" + getEscapedProperty("labelname") + "\"\n" +
                "  created=\"" + getEscapedProperty("created") + "\"\n" +
                "  modified=\"" + getEscapedProperty("modified") + "\"\n" +
                "  version=\"" + getEscapedProperty("version") + "\"\n" +
                "  language=\"" + getEscapedProperty("language") + "\"\n" +
                ">");
    }

    // Write end of XML index file.
    private void writeFooter(PrintWriter out) throws IOException {
        out.println("</lex>");
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
	int tierNo = 0; // the only tier
	out.println("<tier name=\"" + tierName(tierNo) + "\" " +
		"mode=\"" + getMode(tierNo) + "\"/>");
    }

    // Write unprocessed fragments.
    private void writeFragments(PrintWriter out) throws IOException {
	Vector<String> fragments = (Vector<String>) getProperty("fragments");
	if (fragments != null)
	    for (String fragment : fragments)
		out.println("<fragment>" + fragment + "</fragment>\n");
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
	// how much text on single line
	int maxLine = 50;
	int tierNo = 0; // the only tier
	Vector<ResourcePart> tier = phrase.getTier(tierNo);
	for (int k = 0; k < tier.size(); k++) {
	    LxPart lx = (LxPart) tier.get(k);
	    String texthi = XmlAux.escape(lx.texthi);
	    String textal = XmlAux.escape(lx.textal);
	    String texttr = XmlAux.escape(lx.texttr);
	    String textfo = XmlAux.escape(lx.textfo);
	    String cite = XmlAux.escape(lx.cite);
	    String href = XmlAux.escape(lx.href);
	    String keyhi = XmlAux.escape(lx.keyhi);
	    String keyal = XmlAux.escape(lx.keyal);
	    String keytr = XmlAux.escape(lx.keytr);
	    String keyfo = XmlAux.escape(lx.keyfo);
	    String dicthi = XmlAux.escape(lx.dicthi);
	    String dictal = XmlAux.escape(lx.dictal);
	    String dicttr = XmlAux.escape(lx.dicttr);
	    String dictfo = XmlAux.escape(lx.dictfo);
	    String id = lx.id;
	    out.println("<segment id=\"" + id + "\">");
	    if (!texthi.matches("\\s*"))
		out.println("<texthi>" + texthi + "</texthi>");
	    if (!textal.matches("\\s*"))
		out.println("<textal>" + textal + "</textal>");
	    if (!texttr.matches("\\s*"))
		out.println("<texttr>" + texttr + "</texttr>");
	    if (!textfo.matches("\\s*"))
		out.println("<textfo>" + textfo + "</textfo>");
	    if (!cite.matches("\\s*"))
		out.println("<cite>" + cite + "</cite>");
	    if (!href.matches("\\s*"))
		out.println("<href>" + href + "</href>");
	    if (!keyhi.matches("\\s*"))
		out.println("<keyhi>" + keyhi + "</keyhi>");
	    if (!keyal.matches("\\s*"))
		out.println("<keyal>" + keyal + "</keyal>");
	    if (!keytr.matches("\\s*"))
		out.println("<keytr>" + keytr + "</keytr>");
	    if (!keyfo.matches("\\s*"))
		out.println("<keyfo>" + keyfo + "</keyfo>");
	    if (!dicthi.matches("\\s*"))
		out.println("<dicthi>" + dicthi + "</dicthi>");
	    if (!dictal.matches("\\s*"))
		out.println("<dictal>" + dictal + "</dictal>");
	    if (!dicttr.matches("\\s*"))
		out.println("<dicttr>" + dicttr + "</dicttr>");
	    if (!dictfo.matches("\\s*"))
		out.println("<dictfo>" + dictfo + "</dictfo>");
	    out.println("</segment>");
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
	String scheme = "none";
	TreeMap<Integer,Vector<int[]>> phraseToPositions = new TreeMap<Integer,Vector<int[]>>();
	int tierNo = 0; // the only tier
	if (!getMode(tierNo).equals(IGNORED) &&
		(!isEmptyTier(tierNo) || edit)) {
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
	    for (int j = 0; j < nPhrases(); j++) {
		Vector<ResourcePart> resourceParts = getPhrase(j).getTier(tierNo);
		constructor.select(resourceParts, j);
	    }
	    if (nPhrases() == 0 && edit)
		constructor.selectEmpty();
	    Tier tier = new Tier(tiers.size(), getMode(tierNo),
		    constructor.parts());
	    tiers.add(tier);
	    tierNums.add(new Integer(tierNo));
	    labels.add(label);
	    versions.add(version);
	    phraseStarts.add(phraseStart);
	}
    }

    ///////////////////////////////////////////////////
    // Editing of phrases.

    // Overrides superclass.
    public TreeSet<LabelOffset> positionIdOffset(int phraseNum, int tierNum, int pos,
	    boolean mayCreate, boolean all, WrappedBool changed) {
	TreeSet<LabelOffset> singleton = new TreeSet<LabelOffset>();
	TextPhrase phrase = getPhrase(phraseNum);
	int tierNo = 0; // the only tier
	Vector<ResourcePart> t = phrase.getTier(tierNo);
	for (int k = 0; k < t.size(); k++) {
	    LxPart lx = (LxPart) t.get(k);
	    if (!lx.id.equals(""))
		singleton.add(new LabelOffset(lx.id, 0));
	}
	return singleton;

    }

    public String positionId(Vector<ResourcePart> tier, int pos, boolean mayCreate) {
	if (0 <= pos && pos < nPhrases()) {
            TextPhrase phrase = getPhrase(pos);
	    int tierNo = 0; // the only tier
	    Vector<ResourcePart> t = phrase.getTier(tierNo);
	    for (int k = 0; k < t.size(); k++) {
		LxPart lx = (LxPart) t.get(k);
		if (lx.id.equals(""))
		    return null;
		else
		    return lx.id;
	    }
	}
	return null;
    }
    public TreeSet<LabelOffset> positionIdOffset(Vector<ResourcePart> tier, int pos,
	    boolean mayCreate, WrappedBool changed) {
	TreeSet<LabelOffset> singleton = new TreeSet<LabelOffset>();
	if (0 <= pos && pos < nPhrases()) {
            TextPhrase phrase = getPhrase(pos);
	    int tierNo = 0; // the only tier
	    Vector<ResourcePart> t = phrase.getTier(tierNo);
	    for (int k = 0; k < t.size(); k++) {
		LxPart lx = (LxPart) t.get(k);
		if (!lx.id.equals(""))
		    singleton.add(new LabelOffset(lx.id, 0));
	    }
	}
	return singleton;
    }

    public Vector getEditors(TextPhrase phrase) {
	return null;
    }

    public ResourceEditor getEditor(int currentPhrase) {
	return new LexicalAnnotator(this, currentPhrase);
    }

    // Get empty phrase consisting of single ortho part.
    public TextPhrase emptyPhrase() {
	int tierNo = 0; // the only tier
	LxPart part = new LxPart(freshId());
	Vector tier = new Vector();
	tier.add(part);
	Vector[] tiers = new Vector[nTiers()];
	tiers[tierNo] = tier;
	TextPhrase empty = new TextPhrase(this, tiers);
	return empty;
    }
    private String freshId() {
        int id = 0;
        while (ids.contains("" + id))
            id++;
        return "" + id;
    }

    // Join two phrases. Return null if not possible.
    public TextPhrase joinPhrases(TextPhrase phrase1, TextPhrase phrase2) {
	return null;
    }

}
