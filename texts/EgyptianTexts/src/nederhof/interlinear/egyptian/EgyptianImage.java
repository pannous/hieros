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
import nederhof.interlinear.egyptian.image.*;
import nederhof.interlinear.egyptian.pdf.*;
import nederhof.interlinear.frame.*;
import nederhof.interlinear.frame.pdf.*;
import nederhof.interlinear.labels.*;
import nederhof.res.*;
import nederhof.util.*;
import nederhof.util.math.*;
import nederhof.util.xml.*;

// Linkage between hieroglyphic text and images.

public class EgyptianImage extends TextResource {

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
	public EgyptianImage() {
		propertyNames = new String[] {
			"creator",
				"name",
				"labelname",
				"created",
				"modified",
				"version",
				"header",
				"bibliography"
		};
		setTierNames(new String[] {
				"signplaces"
				});
	}

	// Make from file.
	public EgyptianImage(String location) throws IOException {
		this();
		this.location = location;
		read();
		detectEditable();
	}

	// Make from parsed XML file.
	public EgyptianImage(String location, Document doc) throws IOException {
		this();
		this.location = location;
		read(doc);
		detectEditable();
	}

	// Make fresh resource. Fail if already exists.
	// Write empty XML content to it.
	public static EgyptianImage make(File file) throws IOException {
		if (file.exists())
			throw new IOException("File already exists: " + file);
		else if (!FileAux.hasExtension(file.getName(), "xml"))
			throw new IOException("Resource file name should end on .xml");

		EgyptianImage resource = new EgyptianImage();
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
		return super.getName() +
			(!version.matches("\\s*") ? " (" + version + ")" : "");
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
		if (!top.getTagName().equals("images"))
			throw new IOException("File is not Ancient Egyptian text/image linkage");

		retrieveString(top, "creator");
		retrieveString(top, "name");
		retrieveString(top, "labelname");
		retrieveString(top, "created");
		retrieveString(top, "modified");
		retrieveString(top, "version");

		retrieveHeader(doc);
		retrieveBibliography(doc);
		retrieveUse(doc);
		retrieveImages(doc);
		retrieveSigns(doc);
		retrieveAreas(doc);
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
		if (mode.equals(IGNORED))
			setMode(tierNo, IGNORED);
		else
			setMode(tierNo, OMITTED);
	}

	// Get addresses of images.
	private void retrieveImages(Document doc) throws IOException {
		Vector<String> images = new Vector<String>();
		NodeList list = doc.getElementsByTagName("image");
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (node instanceof Element) {
				Element elem = (Element) node;
				String image = getValue(elem.getAttributeNode("href"));
				images.add(image);
			}
		}
		initProperty("images", images);
	}

	// Get signs and their places within images.
	private void retrieveSigns(Document doc) throws IOException {
		NodeList list = doc.getElementsByTagName("sign");
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (node instanceof Element) {
				Element elem = (Element) node;
				String id = getValue(elem.getAttributeNode("id"));
				String name = getValue(elem.getAttributeNode("name"));
				Vector<ImagePlace> places = retrievePlaces(elem);
				Vector<ResourcePart>[] tiers = new Vector[nTiers()];
				Vector<ResourcePart> tier = new Vector<ResourcePart>();
				tier.add(new ImagePlacePart(new ImageSign(name, places), id));
				tiers[0] = tier;
				TextPhrase phrase = new TextPhrase(this, tiers);
				addPhrase(phrase);
			}
		}
	}

	// Get places (of sign).
	private Vector<ImagePlace> retrievePlaces(Element sign) throws IOException {
		Vector<ImagePlace> places = new Vector<ImagePlace>();
		NodeList list = sign.getElementsByTagName("place");
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (node instanceof Element) {
				Element elem = (Element) node;
				String numStr = getValue(elem.getAttributeNode("num"));
				String xStr = getValue(elem.getAttributeNode("x"));
				String yStr = getValue(elem.getAttributeNode("y"));
				String widthStr = getValue(elem.getAttributeNode("width"));
				String heightStr = getValue(elem.getAttributeNode("height"));
				try {
					int num = Integer.parseInt(numStr);
					int x = Integer.parseInt(xStr);
					int y = Integer.parseInt(yStr);
					int width = Integer.parseInt(widthStr);
					int height = Integer.parseInt(heightStr);
					places.add(new ImagePlace(num, x, y, width, height));
				} catch (NumberFormatException e) {
					// ignore
				}
			}
		}
		return places;
	}

	// Get areas within images.
	private void retrieveAreas(Document doc) throws IOException {
		Vector<TaggedBezier> areas = new Vector<TaggedBezier>();
		NodeList list = doc.getElementsByTagName("area");
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (node instanceof Element) {
				Element elem = (Element) node;
				String numStr = getValue(elem.getAttributeNode("num"));
				String name = getValue(elem.getAttributeNode("name"));
				TaggedBezier bezier = new TaggedBezier();
				retrievePoints(elem, bezier);
				try {
					int num = Integer.parseInt(numStr);
					bezier.setNum(num);
					bezier.setName(name);
					if (bezier.getPointSize() > 2) {
						bezier.close();
						areas.add(bezier);
					}

				} catch (NumberFormatException e) {
					// ignore
				}
			}
		}
		initProperty("areas", areas);
	}

	// Get points (of polygon).
	private void retrievePoints(Element area, Bezier bezier) throws IOException {
		Vector<ImagePlace> places = new Vector<ImagePlace>();
		NodeList list = area.getElementsByTagName("point");
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (node instanceof Element) {
				Element elem = (Element) node;
				String xStr = getValue(elem.getAttributeNode("x"));
				String yStr = getValue(elem.getAttributeNode("y"));
				String type = getValue(elem.getAttributeNode("type"));
				boolean sm = type.equals("smooth");
				try {
					int x = Integer.parseInt(xStr);
					int y = Integer.parseInt(yStr);
					bezier.add(new Point(x, y), sm);
				} catch (NumberFormatException e) {
					// ignore
				}
			}
		}
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
		writeImages(out);
		writeSigns(out);
		writeAreas(out);
		writeFooter(out);
	}

	// Write start of XML index file.
	protected void writeHeader(PrintWriter out) throws IOException {
		out.println(XmlFileWriter.header);
		out.println("<images\n" +
				"  creator=\"" + getEscapedProperty("creator") + "\"\n" +
				"  name=\"" + getEscapedProperty("name") + "\"\n" +
				"  labelname=\"" + getEscapedProperty("labelname") + "\"\n" +
				"  created=\"" + getEscapedProperty("created") + "\"\n" +
				"  modified=\"" + getEscapedProperty("modified") + "\"\n" +
				"  version=\"" + getEscapedProperty("version") + "\"\n" +
				">");
	}

	// Write end of XML index file.
	private void writeFooter(PrintWriter out) throws IOException {
		out.println("</images>");
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

	// Write image locations.
	private void writeImages(PrintWriter out) throws IOException {
		Vector<String> images = (Vector<String>) getProperty("images");
		if (images != null)
			for (String image : images) 
				out.println("<image href=\"" + image + "\"/>");
	}

	// Write signs within images.
	private void writeSigns(PrintWriter out) throws IOException {
		boolean isFirst = true;
		for (int i = 0; i < nPhrases(); i++) {
			TextPhrase phrase = getPhrase(i);
			if (!phrase.tiersEmpty()) {
				if (!isFirst)
					out.println();
				else
					isFirst = false;
				writeSign(out, phrase);
			}
		}
	}

	// Write sign with places.
	private void writeSign(PrintWriter out, TextPhrase phrase)
		throws IOException {
			int tierNo = 0; // the only tier
			Vector tier = phrase.getTier(tierNo);
			for (int k = 0; k < tier.size(); k++) { // normally size == 1
				ImagePlacePart part = (ImagePlacePart) tier.get(k);
				String id = part.id;
				ImageSign sign = part.info;
				String name = sign.getName();
				Vector<ImagePlace> places = sign.getPlaces();
				out.println("<sign id=\"" + id + "\"" +
						" name=\"" + XmlAux.escape(name) + "\">");
				for (ImagePlace place : places) 
					writePlace(out, place);
				out.println("</sign>");
			}
		}

	// Write areas.
	private void writeAreas(PrintWriter out) throws IOException {
		Vector<TaggedBezier> areas = (Vector<TaggedBezier>) getProperty("areas");
		if (areas != null)
			for (TaggedBezier area : areas) {
				out.println("<area num=\"" + area.getNum() + "\"" +
						" name=\"" + XmlAux.escape(area.getName()) + "\">");
				writePoints(out, area);
				out.println("</area>");
			}
	}

	// Write points of path.
	private void writePoints(PrintWriter out, Bezier bezier) throws IOException {
		for (int i = 0; i < bezier.getPointSize(); i++) {
			Point p = bezier.getPoint(i);
			boolean sm = bezier.getSmooth(i);
			String type = sm ? "smooth" : "ragged";
			out.println("<point" +
					" x=\"" + p.x + "\"" +
					" y=\"" + p.y + "\"" +
					" type=\"" + type + "\"" +
					"/>");
		}
	}

	// Write places.
	private void writePlace(PrintWriter out, ImagePlace place)
		throws IOException {
			out.println("<place" +
					" num=\"" + place.getNum() + "\"" +
					" x=\"" + place.getX() + "\"" +
					" y=\"" + place.getY() + "\"" +
					" width=\"" + place.getWidth() + "\"" +
					" height=\"" + place.getHeight() + "\"" +
					"/>");
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
	// Use of tiers.

	// Overrides default.
	public Vector<String> nonIgnoreModes() {
		Vector<String> nonIgnoreModes = new Vector<String>();
		nonIgnoreModes.add(OMITTED);
		return nonIgnoreModes;
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
		if (getMode(tierNo).equals(OMITTED)) {
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
			if (nPhrases() == 0)
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
	// External viewers.

	public boolean hasViewer() {
		int tierNo = 0; // the only tier
		return getMode(tierNo).equals(OMITTED);
	}
	// Give external reviewer.
	public ResourceViewer getViewer() {
		int tierNo = 0; // the only tier
		if (getMode(tierNo).equals(OMITTED))
			return new EgyptianImageViewer(this);
		else
			return null;
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
			ImagePlacePart place = (ImagePlacePart) t.get(k);
			if (!place.id.equals(""))
				singleton.add(new LabelOffset(place.id, 0));
		}
		return singleton;
	}

	public String positionId(Vector<ResourcePart> tier, int pos, boolean mayCreate) {
		if (0 <= pos && pos < nPhrases()) {
			TextPhrase phrase = getPhrase(pos);
			int tierNo = 0; // the only tier
			Vector<ResourcePart> t = phrase.getTier(tierNo);
			for (int k = 0; k < t.size(); k++) {
				ImagePlacePart place = (ImagePlacePart) t.get(k);
				if (place.id.equals(""))
					return null;
				else
					return place.id;
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
				ImagePlacePart place = (ImagePlacePart) t.get(k);
				if (!place.id.equals(""))
					singleton.add(new LabelOffset(place.id, 0));
			}
		}
		return singleton;
	}

	public Vector getEditors(TextPhrase phrase) {
		return null;
	}

	public ResourceEditor getEditor(int currentPhrase) {
		return new EgyptianImageEditor(this, currentPhrase);
	}

	// Get empty phrase consisting of single image part.
	public TextPhrase emptyPhrase() {
		int tierNo = 0; // the only tier
		String name = "\"?\"";
		Vector<ImagePlace> places = new Vector<ImagePlace>();
		ImagePlacePart part = new ImagePlacePart(new ImageSign(name, places), freshId());
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

	// Join two phrases. Return null to indicate this is not appropriate for this class.
	public TextPhrase joinPhrases(TextPhrase phrase1, TextPhrase phrase2) {
		return null;
	}

}
