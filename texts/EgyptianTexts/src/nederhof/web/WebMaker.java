package nederhof.web;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.egyptian.image.*;
import nederhof.util.*;
import nederhof.util.math.*;
import nederhof.util.xml.*;

// Makes web page for text out of several resources of text.
public class WebMaker {

    // The file containing the web specification read.
    private String webspecs;

    // Name of text.
    private String textName = "";
    // The text location.
    private String textLocation = null;
    // Helps with processing texts and resources.
    private TextHelper helper;

    // Path for target.
    private String targetPath = null;
    // Name of main HTML.
    private final String htmlName = "index.html";

    // Pages (tabs) in web page.
    private Vector<Element> pages = new Vector<Element>();

    // Images in running text.
    private Vector<String> textImages = new Vector<String>();

    // Reference between image area and text.
    private Vector<Ref> refs = new Vector<Ref>();
    private class Ref {
	public String area;
	public String text;
	public Ref(String area, String text) {
	    this.area = area;
	    this.text = text;
	}
    }

    // Constructor.
    public WebMaker(String webspecs, String targetPath) {
	this.webspecs = webspecs;
	this.targetPath = targetPath;
	try {
	    readWebspecs(webspecs);
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(null,
		    "Cannot read in WebMaker: " + e.getMessage(),
		    "File error", JOptionPane.ERROR_MESSAGE);
	}
	try {
	    helper = new TextHelper(textLocation);
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(null,
		    "Cannot process texts in WebMaker: " + e.getMessage(),
		    "File error", JOptionPane.ERROR_MESSAGE);
	}
	getPageParts();
	try {
	    writeHtml();
	} catch (SecurityException e) {
	    JOptionPane.showMessageDialog(null,
		    "Cannot write in WebMaker: " + e.getMessage(),
		    "File error", JOptionPane.ERROR_MESSAGE);
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(null,
		    "Cannot write in WebMaker: " + e.getMessage(),
		    "File error", JOptionPane.ERROR_MESSAGE);
	}
	try {
	    copyImages();
	} catch (FileNotFoundException e) {
	    JOptionPane.showMessageDialog(null,
		    "Cannot copy in WebMaker: " + e.getMessage(),
		    "File error", JOptionPane.ERROR_MESSAGE);
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(null,
		    "Cannot copy in WebMaker: " + e.getMessage(),
		    "File error", JOptionPane.ERROR_MESSAGE);
	}
	try {
	    copyAuxFiles();
	    copyFontFiles();
	} catch (FileNotFoundException e) {
	    JOptionPane.showMessageDialog(null,
		    "Cannot copy in WebMaker: " + e.getMessage(),
		    "File error", JOptionPane.ERROR_MESSAGE);
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(null,
		    "Cannot copy in WebMaker: " + e.getMessage(),
		    "File error", JOptionPane.ERROR_MESSAGE);
	}
    }

    ////////////////////////////////////////////
    // Reading XML.

    // Parser of corpus files.
    private DocumentBuilder parser =
        SimpleXmlParser.construct(false, false);

    // Read specification of how web pages are to be created.
    private void readWebspecs(String specs) throws IOException {
        InputStream in = FileAux.addressToStream(specs);
        if (FileAux.hasExtension(specs, "xml")) {
            try {
                Document doc = parser.parse(in);
                read(doc);
            } catch (SAXException e) {
                throw new IOException(e.getMessage());
            }
        }
        in.close();
    }

    // Process specs.
    private void read(Document doc) throws IOException {
	Element top = doc.getDocumentElement();
	if (!top.getTagName().equals("webspecs"))
	    throw new IOException("File is not specification of web pages");
	String name = getValue(top.getAttributeNode("name"));
	if (!name.equals(""))
	    textName = name;
	NodeList textList = doc.getElementsByTagName("text");
	if (textList.getLength() != 1)
	    throw new IOException("Exactly one text is needed");
	Node textNode = textList.item(0);
	if (textNode instanceof Element) {
	    textLocation = getValue(((Element) textNode).getAttributeNode("location"));
	    if (textLocation.equals(""))
		throw new IOException("Text location is missing");
	}
	NodeList pageList = doc.getElementsByTagName("page");
	for (int i = 0; i < pageList.getLength(); i++) {
	    Node page = pageList.item(i);
	    if (page instanceof Element) 
	    	readPage((Element) page);
	}
	NodeList refList = doc.getElementsByTagName("ref");
	for (int i = 0; i < refList.getLength(); i++) {
	    Node ref = refList.item(i);
	    if (ref instanceof Element) 
	    	readRef((Element) ref);
	}
	NodeList imgList = doc.getElementsByTagName("img");
	for (int i = 0; i < imgList.getLength(); i++) {
	    Node img = imgList.item(i);
	    if (img instanceof Element) 
	    	readImg((Element) img);
	}
    }

    // Process page.
    private void readPage(Element p) {
	pages.add(p);
    }

    // Process reference from text to number of areas in images.
    private void readRef(Element ref) {
	String area = getValue(ref.getAttributeNode("area-tag"));
	String text = getValue(ref.getAttributeNode("text-tag"));
	refs.add(new Ref(area, text));
    }

    // Process reference in page to image.
    private void readImg(Element img) {
	String src = getValue(img.getAttributeNode("src"));
	if (src.matches(".*jpg"))
	    textImages.add(src);
    }

    // Get attribute value, but "" if attribute is null.
    private static String getValue(Attr attr) {
	return attr == null ? "" : attr.getValue();
    }

    ////////////////////////////////////////////
    // Reading plain text from files.

    // Pieces of HTML to be pasted together.
    private String htmlStr1; 
    private String htmlStr2; 
    private String htmlStr3; 
    private String htmlStr4; 
    private String htmlStr5; 
    private String htmlStr6; 
    private String htmlStr7; 

    // Get parts of pages to be put together.
    private void getPageParts() {
	try {
	    htmlStr1 = getPagePart("data/web/html.part1");
	    htmlStr2 = getPagePart("data/web/html.part2");
	    htmlStr3 = getPagePart("data/web/html.part3");
	    htmlStr4 = getPagePart("data/web/html.part4");
	    htmlStr5 = getPagePart("data/web/html.part5");
	    htmlStr6 = getPagePart("data/web/html.part6");
	    htmlStr7 = getPagePart("data/web/html.part7");
	} catch (MalformedURLException e) {
	    JOptionPane.showMessageDialog(null,
		    "File not found in WebMaker: " + e.getMessage(),
		    "File error", JOptionPane.ERROR_MESSAGE);
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(null,
		    "File exception in WebMaker: " + e.getMessage(),
		    "File error", JOptionPane.ERROR_MESSAGE);
	}
    }

    // Read string from file.
    private String getPagePart(String adr) 
		throws MalformedURLException, IOException {
	URL url = FileAux.fromBase(adr);
	if (url == null)
	    throw new MalformedURLException();
	InputStream in = url.openStream();
	BufferedReader reader =
	    new BufferedReader(new InputStreamReader(in));
	StringBuffer buf = new StringBuffer();
	String line = reader.readLine();
	while (line != null) {
	    buf.append(line + "\n");
	    line = reader.readLine();
	}
	return buf.toString();
    }

    /////////////////////////////////////////////////////////
    // Writing files.

    // Write main HTML.
    private void writeHtml() throws IOException, SecurityException {
	File dirFile = new File(targetPath);
	if (!dirFile.exists()) 
	    dirFile.mkdir();
	String html = getHtml();
	File htmlFile = new File(dirFile, htmlName);
	PrintWriter out = new Utf8FileWriter(htmlFile);
	out.print(html);
	out.close();
    }

    // Write main HTML, composed of parts.
    private String getHtml() throws IOException {
	StringBuffer buf = new StringBuffer();
	htmlStr1 = htmlStr1.replaceAll("TEXTNAME", textName);
	buf.append(htmlStr1);
	addImages(buf);
	addAreas(buf);
	addRefs(buf);
	addLexicalAreas(buf);
	buf.append(htmlStr2);
	addPopup(buf);
	buf.append(htmlStr3);
	addPageTabs(buf);
	buf.append(htmlStr4);
	addPages(buf);
	buf.append(htmlStr5);
	if (hasMultipleImages()) // 
	    buf.append(htmlStr6);
	buf.append(htmlStr7);
	return buf.toString();
    }

    private boolean hasMultipleImages() {
	if (helper.getImageResource() == null)
	    return false;
	Vector<String> images = (Vector<String>) 
	    helper.getImageResource().getProperty("images");
	return images.size() > 1;
    }

    // Add images.
    private void addImages(StringBuffer buf) throws IOException {
	EgyptianImage res = helper.getImageResource();
	if (res == null)
	    return;
	Vector<String> images = (Vector<String>) res.getProperty("images");
	for (String image : images) {
	    addImage(buf, res, image);
	}
    }

    // Add one image. If it's big, add a low resolution one.
    private void addImage(StringBuffer buf, EgyptianImage res, String image) throws IOException {
	String resolved = FileAux.resolve(res.getLocation(), image);
	File file = new File(resolved);
	String name = file.getName();
	String copyPath = targetPath + "/" + name;
	File copyFile = new File(copyPath);
	BufferedImage im = null;
	try {
	    FileAux.copyBinaryFile(file, copyFile);
	    im = ImageIO.read(file);
	} catch (FileNotFoundException e) {
	    throw new IOException(e.getMessage());
	}
	String lowresName = name;
	String nameBase = FileAux.removeExtension(name);
	String ext = FileAux.getExtension(name);
	int width = im.getWidth();
	int height = im.getHeight();
	final int sizeLimit = 1000;
	float scale = Math.min(1.0f * sizeLimit / width, 1.0f * sizeLimit / height);
	if (scale < 1) {
	    lowresName = nameBase + "_lowres." + ext;
	    String lowresPath = targetPath + "/" + lowresName;
	    addLowresImage(im, scale, lowresPath);
	}
	buf.append("addImage(\"" + name + "\"," +
	    "\"" + width + "\"," +
	    "\"" + height + "\"," +
	    "\"" + lowresName + "\"" +
	    ");\n");
    }

    // Put low resolution image in file.
    private void addLowresImage(BufferedImage image, float scale, String fileName) 
    		throws IOException {
	int width = Math.round(image.getWidth() * scale);
	int height = Math.round(image.getHeight() * scale);
	BufferedImage scaled = new BufferedImage(width, height, image.getType());
	Graphics2D g = scaled.createGraphics();
	g.setComposite(AlphaComposite.Src);
	g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	g.drawImage(image, 0, 0, width, height, null);
	g.dispose();
	ImageIO.write(scaled, FileAux.getExtension(fileName), new File(fileName));
    }

    // Add areas of images.
    private void addAreas(StringBuffer buf) {
	if (helper.getImageResource() == null)
	    return;
	Vector<TaggedBezier> areas = 
	    (Vector<TaggedBezier>) helper.getImageResource().getProperty("areas");
	for (TaggedBezier area : areas) 
	    addArea(buf, area, 4);
    }

    // Add area.
    private void addArea(StringBuffer buf, TaggedBezier area, int dashSize) {
	String name = area.getName();
	int imNum = area.getNum();
	buf.append("addArea(" + imNum + ",\"" + name + "\"," + dashSize + ");\n");
	int size = area.getSegmentSize();
	for (int i = 0; i < size; i++) {
	    CubicCurve2D.Double curve = area.getSegment(i);
	    buf.append("addCurve(\"" + name + "\"," + 
		(int) curve.x1 + "," +
		(int) curve.y1 + "," +
		(int) curve.ctrlx1 + "," +
		(int) curve.ctrly1 + "," +
		(int) curve.ctrlx2 + "," +
		(int) curve.ctrly2 + "," +
		(int) curve.x2 + "," +
		(int) curve.y2 + 
		");\n");
	}
    }

    // Add links between text and images.
    private void addRefs(StringBuffer buf) {
	for (Ref ref : refs) 
	    addRef(buf, ref.area, ref.text);
    }

    // Add link between text and image.
    private void addRef(StringBuffer buf, String areaTag, String textTag) {
	buf.append("addTagId(\"" + areaTag + "\",\"" + textTag + "\");\n");
    }

    // Add rectangles of word occurrences.
    private void addLexicalAreas(StringBuffer buf) {
	TreeMap<String,Vector<ImagePlace>> places = helper.getVocab().lexPlaces();
	Vector<TaggedBezier> beziers = ImageUtil.areas(places);
	for (TaggedBezier bezier : beziers) {
	    String lexName = bezier.getName().replaceAll(":[0-9]*$", "");
	    addArea(buf, bezier, 2);
	    addRef(buf, bezier.getName(), lexName);
	}
    }

    // Add references to pages.
    private void addPageTabs(StringBuffer buf) {
	for (Element page : pages) {
	    String ref = getValue(page.getAttributeNode("ref"));
	    String name = getValue(page.getAttributeNode("name"));
	    buf.append("       <li><a href=\"" + ref + 
		    "\" class=\"text_tab\">" + name + "</a></li>\n");
	}
    }

    // Add pages.
    private void addPages(StringBuffer buf) {
	for (Element page : pages) {
	    String ref = getValue(page.getAttributeNode("ref"));
	    buf.append("    <div id=\"" + ref + "\">");
	    copyChildren(page, buf);
	    buf.append("    </div>\n\n");
	}
    }

    // Add popup material.
    private void addPopup(StringBuffer buf) {
	helper.getPopupText(buf);
    }

    // Copy images mentioned in text.
    private void copyImages() throws FileNotFoundException, IOException {
	File specFile = new File(webspecs);
	File parentDir = specFile.getParentFile();
	for (String img : textImages) {
	    File file = new File(parentDir, img);
	    File copyFile = new File(targetPath + "/" + img);
	    FileAux.copyBinaryFile(file, copyFile);
	}
    }

    // Copy auxiliary files.
    private void copyAuxFiles() throws FileNotFoundException, IOException {
	copyAuxFile("res.js");
	copyAuxFile("viewer.js");
	copyAuxFile("viewer.css");
    }
    private void copyAuxFile(String name) throws FileNotFoundException, IOException {
	File sourceFile = new File("data/web/" + name);
	File targetFile = new File(targetPath + "/" + name);
	FileAux.copyFile(sourceFile, targetFile);
    }

    // Copy fonts.
    private void copyFontFiles() throws FileNotFoundException, IOException {
	copyFontFile("NewGardiner.ttf");
	copyFontFile("HieroglyphicAux.ttf");
	copyFontFile("DejaVuSans-Bold.ttf");
    }
    private void copyFontFile(String name) throws FileNotFoundException, IOException {
	File sourceFile = new File("data/fonts/" + name);
	File targetFile = new File(targetPath + "/" + name);
	FileAux.copyBinaryFile(sourceFile, targetFile);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Copying XML, except for special elements.

    private void copyElement(Element elem, StringBuffer buf) {
	String name = elem.getTagName();
	if (name.equals("interlinear")) {
	    String fromMarker = getValue(elem.getAttributeNode("from"));
	    String toMarker = getValue(elem.getAttributeNode("to"));
	    getInterlinear(buf, fromMarker, toMarker);
	} else if (name.equals("al")) {
	    String translit = TransHelper.toUnicode(childText(elem));
	    getTranslit(buf, translit);
	} else if (name.equals("vocab")) {
	    getVocab(buf);
	} else {
	    buf.append("<" + name);
	    copyAttributes(elem, buf);
	    buf.append(">");
	    copyChildren(elem, buf);
	    buf.append("</" + name + ">");
	}
    }

    private void copyChildren(Element elem, StringBuffer buf) {
	NodeList children = elem.getChildNodes();
	for (int i = 0; i < children.getLength(); i++) {
	    Node node = children.item(i);
	    if (node instanceof Element) 
		copyElement((Element) node, buf);
	    else if (node instanceof Text)
		buf.append(((Text) node).getData());
	}
    }

    private void copyAttributes(Element elem, StringBuffer buf) {
	NamedNodeMap atts = elem.getAttributes();
	for (int i = 0; i < atts.getLength(); i++) {
	    Attr att = (Attr) atts.item(i);
	    String name = att.getName();
	    String value = att.getValue();
	    buf.append(" " + name + "=\"" + value + "\"");
	}
    }

    private String childText(Element elem) {
	String text = "";
	NodeList children = elem.getChildNodes();
	for (int i = 0; i < children.getLength(); i++) {
	    Node node = children.item(i);
	    if (node instanceof Text)
		text += ((Text) node).getData();
	}
	return text;
    }

    ////////////////////////////////////////
    // Elements with special meaning.

    private void getInterlinear(StringBuffer buf, String fromMarker, String toMarker) {
	helper.getInterlinear(buf, fromMarker, toMarker);
    }

    private void getTranslit(StringBuffer buf, String translit) {
	buf.append("<span class=\"al\">" + translit + "</span>");
    }

    private void getVocab(StringBuffer buf) {
	Vocab vocab = helper.getVocab();
	buf.append(vocab.toHtml());
    }

    ////////////////////////////////////////
    // Testing.

    public static void main(String[] args) {
	new WebMaker(args[0], args[1]);
    }

}
