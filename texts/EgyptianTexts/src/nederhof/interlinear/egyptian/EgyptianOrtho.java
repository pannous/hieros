// Orthographic annotation hieroglyphic.

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
import nederhof.interlinear.egyptian.ortho.*;
import nederhof.interlinear.egyptian.pdf.*;
import nederhof.interlinear.frame.*;
import nederhof.interlinear.frame.pdf.*;
import nederhof.interlinear.labels.*;
import nederhof.res.*;
import nederhof.util.*;
import nederhof.util.xml.*;

public class EgyptianOrtho extends TextResource {

    // Allowable extensions for file.
    private static Vector<String> extensions = new Vector<String>();
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
    public EgyptianOrtho() {
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
            "orthographic"
            });
    }

    // Make from file.
    public EgyptianOrtho(String location) throws IOException {
        this();
        this.location = location;
        read();
        detectEditable();
    }

    // Make from parsed XML file.
    public EgyptianOrtho(String location, Document doc) throws IOException {
        this();
        this.location = location;
        read(doc);
        detectEditable();
    }

    // Make fresh resource. Fail if already exists.
    // Write empty XML content to it.
    public static EgyptianOrtho make(File file) throws IOException {
        if (file.exists())
            throw new IOException("File already exists: " + file);
        else if (!FileAux.hasExtension(file.getName(), "xml"))
            throw new IOException("Resource file name should end on .xml");

        EgyptianOrtho resource = new EgyptianOrtho();
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
                    "(which modern language used in descriptions, if any)"));
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
        if (!top.getTagName().equals("ortho"))
             throw new IOException("File is not Ancient Egyptian orthographic annotation");

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
	Vector<OrthoElem> textortho = new Vector<OrthoElem>();
        for (int j = 0; j < children.getLength(); j++) {
            Node child = children.item(j);
            if (child instanceof Element) {
                Element childElem = (Element) child;
                String name = childElem.getTagName();
                if (name.equals("texthi")) {
		    texthi = retrieveText(childElem);
		    texthi = texthi.replaceAll("^\\s*", "");
		    texthi = texthi.replaceAll("\\s*$", "");
                } else if (name.equals("textal")) {
                    textal = retrieveText(childElem);
		    textal = textal.replaceAll("\\s\\s*", " ");
		    textal = textal.replaceAll("^ ", "");
		    textal = textal.replaceAll(" $", "");
                } else if (name.equals("textortho")) {
		    textortho = OrthoHelper.parseOrtho(childElem);
                }
            }
        }
	tier.add(new OrthoPart(texthi, textal, textortho, id));
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
        writePhrases(out);
        writeFooter(out);
    }

    // Write start of XML index file.
    protected void writeHeader(PrintWriter out) throws IOException {
        out.println(XmlFileWriter.header);
        out.println("<ortho\n" +
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
        out.println("</ortho>");
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
	Vector tier = phrase.getTier(tierNo);
	for (int k = 0; k < tier.size(); k++) {
	    OrthoPart ortho = (OrthoPart) tier.get(k);
	    String hi = XmlAux.escape(ortho.texthi);
	    String al = XmlAux.escape(ortho.textal);
	    String id = ortho.id;
	    out.println("<segment id=\"" + id + "\">");
	    if (hi.length() > maxLine) {
		out.println("<texthi>");
		out.println(XmlAux.breakLines(hi));
		out.println("</texthi>");
	    } else {
		out.println("<texthi>" + hi + "</texthi>");
	    }
	    if (al.length() > maxLine) {
		out.println("<textal>");
		out.println(XmlAux.breakLines(al));
		out.println("</textal>");
	    } else {
		out.println("<textal>" + al + "</textal>");
	    }
	    out.println("<textortho>");
	    out.print(OrthoHelper.writeOrtho(ortho.textortho));
	    out.println("</textortho>");
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
		Vector resourceParts = getPhrase(j).getTier(tierNo);
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
            OrthoPart op = (OrthoPart) t.get(k);
            if (!op.id.equals(""))
                singleton.add(new LabelOffset(op.id, 0));
        }
        return singleton;
    }

    public String positionId(Vector<ResourcePart> tier, int pos, boolean mayCreate) {
        if (0 <= pos && pos < nPhrases()) {
            TextPhrase phrase = getPhrase(pos);
            int tierNo = 0; // the only tier
            Vector<ResourcePart> t = phrase.getTier(tierNo);
            for (int k = 0; k < t.size(); k++) {
                OrthoPart op = (OrthoPart) t.get(k);
                if (op.id.equals(""))
                    return null;
                else
                    return op.id;
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
                OrthoPart op = (OrthoPart) t.get(k);
                if (!op.id.equals(""))
                    singleton.add(new LabelOffset(op.id, 0));
            }
        }
        return singleton;
    }

    public Vector getEditors(TextPhrase phrase) {
	return null;
    }

    public ResourceEditor getEditor(int currentPhrase) {
	return new OrthoEditor(this, currentPhrase);
    }

    // Get empty phrase consisting of single ortho part.
    public TextPhrase emptyPhrase() {
	int tierNo = 0; // the only tier
	OrthoPart part = new OrthoPart("", "", new Vector(), freshId());
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
	int tierNo = 0; // the only tier
	Vector tier1 = phrase1.getTier(tierNo);
	Vector tier2 = phrase2.getTier(tierNo);
	OrthoPart part1 = (OrthoPart) tier1.get(0);
	OrthoPart part2 = (OrthoPart) tier2.get(0);
	ResFragment frag1 = part1.texthiParsed;
	ResFragment frag2 = part2.texthiParsed;
	String textal1 = part1.textal + " ";
	String textal2 = part2.textal;
	Vector textortho1 = part1.textortho;
	Vector textortho2 = part2.textortho;
	int hiSize = frag1.nGlyphs();
	int alSize = TransHelper.charLength(textal1); // extra space
	ResFragment fragJoin = ResComposer.append(frag1, frag2);
	String texthiJoin = fragJoin.toString();
	String textalJoin = textal1 + textal2;
	Vector textortho2Moved = OrthoHelper.move(textortho2, hiSize, alSize);
	Vector textorthoJoin = new Vector();
	textorthoJoin.addAll(textortho1);
	textorthoJoin.addAll(textortho2Moved);
	OrthoPart partJoin = new OrthoPart(texthiJoin, textalJoin, textorthoJoin, part1.id);
	Vector tier = new Vector();
	tier.add(partJoin);
        Vector[] tiers = new Vector[nTiers()];
	tiers[tierNo] = tier;
	TextPhrase join = new TextPhrase(this, tiers);
	return join;
    }

}
