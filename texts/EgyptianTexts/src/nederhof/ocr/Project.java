package nederhof.ocr;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import javax.swing.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.ocr.images.*;
import nederhof.util.*;
import nederhof.util.xml.*;

// Project of OCR. Containing record of work done before.

public abstract class Project {

	// Name of index file.
	private final String INDEX = "index.xml";
	// Name of temporary index file while index file is being overwritten.
	private final String TEMP_INDEX = INDEX + "~";
	// Name of subdirectory where glyph images are stored.
	private final String GLYPHSDIR = "glyphs";

	// The directory.
	private File location;
	// Where images of glyphs are stored.
	private File glyphsDir;

	// Mapping from page names (taken from filenames) to images.
	public Map<String,BinaryImage> images = new TreeMap<String,BinaryImage>();
	// Mapping from page names to pages.
	public Map<String,Page> pages = new TreeMap<String,Page>();

	// Make project from directory.
	public Project(File location) throws IOException {
		this.location = location;
		this.glyphsDir = new File(location, GLYPHSDIR);
		if (!dirEditable())
			throw new IOException("Directory not editable");
		read();
	}

	// Directory of this project.
	public File dir() {
		return location;
	}

	// Analyse location whether editable.
	private boolean dirEditable() throws IOException {
		try {
			return location.canWrite();
		} catch (SecurityException e) {
			throw new IOException(e.getMessage());
		}
	}

	//////////////////////////////////////////
	// Reading.

	// Read from file. First read images and glyphs directory.
	// Optionally read index file.
	private void read() throws IOException {
		try {
			readPages();
			if (images.isEmpty())
				throw new IOException("No PNG images found");
			openGlyphs();
			maybeReadIndex();
		} catch (SecurityException e) {
			throw new IOException(e.getMessage());
		}
	}

	// Read images of pages.
	private void readPages() throws IOException {
		File[] files = location.listFiles();
		for (File f : files) {
			if (f.isFile() && FileAux.hasExtension(f.getName(), "png")) {
				String pageName = FileAux.removeExtension(f.getName(), "png");
				BinaryImage im = new BinaryImage(f);
				images.put(pageName, im);
				pages.put(pageName, new Page(pageName));
			}
		}
	}

	// Open directory where glyphs are stored.
	private void openGlyphs() throws IOException, SecurityException {
		if (!glyphsDir.exists())
			glyphsDir.mkdir();
		if (!glyphsDir.canWrite())
			throw new IOException("Glyph image subdirectory not editable");
	}

	// Read index. If none, create.
	private void maybeReadIndex() throws IOException, SecurityException {
		File index = new File(location, INDEX);
		if (!index.exists()) {
			save(index);
		} else if (!index.canRead() || !index.canWrite()) 
			throw new IOException("Cannot access: " + index);
		else {
			DocumentBuilder parser = SimpleXmlParser.construct(false, false);
			InputStream in = new FileInputStream(index);
			try {
				Document doc = parser.parse(in);
				processIndex(doc);
			} catch (SAXException e) {
				throw new IOException(e.getMessage());
			}
			in.close();
		}
	}

	// Process index.
	private void processIndex(Document doc) throws IOException {
		NodeList children = doc.getElementsByTagName("page");
		for (int i = 0; i < children.getLength(); i++) {
			Element page = (Element) children.item(i);
			processPage(page);
		}
	}

	// Process one page in the index.
	private void processPage(Element pageEl) throws IOException {
		String pageName = pageEl.getAttribute("name");
		if (!pages.containsKey(pageName))
			pages.put(pageName, new Page(pageName));
		Page page = pages.get(pageName);
		NodeList children = pageEl.getElementsByTagName("line");
		for (int i = 0; i < children.getLength(); i++) {
			Element line = (Element) children.item(i);
			processLine(line, page.lines, pageName);
		}
	}

	// Process one line.
	private void processLine(Element lineEl, 
			Vector<Line> lines, String pageName) throws IOException {
		String direction = lineEl.getAttribute("dir");
		Polygon poly = processPolygon(lineEl);
		Line line = new Line(direction, poly, pageName);
		processGlyphs(lineEl, line.glyphs);
		processFormats(lineEl, line.formatted);
		lines.add(line);
	}

	// Process polygon delimiting line.
	private Polygon processPolygon(Element lineEl) throws IOException {
		Polygon poly = new Polygon();
		NodeList children = lineEl.getElementsByTagName("point");
		for (int i = 0; i < children.getLength(); i++) {
			Element pointEl = (Element) children.item(i);
			int x = strToInt(pointEl.getAttribute("x"));
			int y = strToInt(pointEl.getAttribute("y"));
			poly.addPoint(x, y);
		}
		return poly;
	}		

	// Process glyphs.
	private void processGlyphs(Element lineEl, 
			Vector<Blob> glyphs) throws IOException {
		NodeList children = lineEl.getElementsByTagName("glyph");
		for (int i = 0; i < children.getLength(); i++) {
			Element glyph = (Element) children.item(i);
			processGlyph(glyph, glyphs);
		}
	}

	// Process one glyph.
	private void processGlyph(Element glyphEl, 
			Vector<Blob> glyphs) throws IOException {
		String id = glyphEl.getAttribute("id");
		File file = new File(glyphsDir, id + ".png");
		int x = strToInt(glyphEl.getAttribute("x"));
		int y = strToInt(glyphEl.getAttribute("y"));
		String name = glyphEl.getAttribute("name");
		String note = glyphEl.getAttribute("note");
		Blob glyph = new Blob(file, x, y);
		glyph.setName(name);
		glyph.setNote(note);
		glyphs.add(glyph);
	}

	// Process formatted elements.
	protected void processFormats(Element lineEl,
			Vector<LineFormat> formatted) throws IOException {
		NodeList formats = lineEl.getElementsByTagName("format");
		for (int i = 0; i < formats.getLength(); i++) {
			Element format = (Element) formats.item(i);
			processFormat(format, formatted);
		}
	}

	// Process one formatted element.
	protected abstract void processFormat(Element formatEl,
			Vector<LineFormat> formatted) throws IOException;

	// String to integer.
	protected int strToInt(String s) throws IOException {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throw new IOException(e.getMessage());
		}
	}

	//////////////////////////////////////////
	// Writing.

	// Index of maximum glyph image id.
	private int maxGlyphImageNum = -1;

	// First write to temporary, before overwriting original file.
	public void save() throws IOException {
		investigateImageDir();
		File temp = new File(location, TEMP_INDEX);
		save(temp);
		save(new File(location, INDEX));
		temp.delete();
	}

	// Look in directory of images. Find largest id.
	private void investigateImageDir() throws IOException {
		maxGlyphImageNum = -1;
		File[] files = glyphsDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			String fileName = FileAux.removeExtension(files[i].getName());
			try {
				int num = Integer.parseInt(fileName);
				maxGlyphImageNum = Math.max(maxGlyphImageNum, num);
			} catch (NumberFormatException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	// Write to file.
	private void save(File f) throws IOException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element projectEl = doc.createElement("project");
			getPages(doc, projectEl);
			doc.appendChild(projectEl);
			XmlPretty.print(doc, f);
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	private void getPages(Document doc, Element projectEl) throws IOException {
		for (Map.Entry<String,Page> entry : pages.entrySet()) {
			String pageName = entry.getKey();
			Page page = entry.getValue();
			Element pageEl = doc.createElement("page");
			pageEl.setAttribute("name", pageName);
			getLines(doc, pageEl, page);
			removeOrphans(page);
			projectEl.appendChild(pageEl);
		}
	}

	private void getLines(Document doc, Element pageEl, 
			Page page) throws IOException {
		for (Line line : page.lines) {
			Element lineEl = doc.createElement("line");
			lineEl.setAttribute("dir", line.dir);
			getPolygon(doc, lineEl, line.polygon);
			getFormat(doc, lineEl, line.formatted);
			getGlyphs(doc, lineEl, line.aliveGlyphs());
			pageEl.appendChild(lineEl);
		}
	}

	private void getPolygon(Document doc, Element lineEl, 
			Polygon poly) throws IOException {
		for (int i = 0; i < poly.npoints; i++) {
			Element pointEl = doc.createElement("point");
			pointEl.setAttribute("x", "" + poly.xpoints[i]);
			pointEl.setAttribute("y", "" + poly.ypoints[i]);
			lineEl.appendChild(pointEl);
		}
	}

	protected abstract void getFormat(Document doc, Element lineEl, 
			Vector<LineFormat> formatted) throws IOException;

	private void getGlyphs(Document doc, Element lineEl, 
			Vector<Blob> glyphs) throws IOException {
		for (Blob blob : glyphs) {
			if (!blob.isSaved()) 
				blob.save(new File(glyphsDir, "" + (++maxGlyphImageNum) + ".png"));
			Element glyphEl = doc.createElement("glyph");
			getGlyphAttributes(glyphEl, blob);
			lineEl.appendChild(glyphEl);
		}
	}

	private void getGlyphAttributes(Element glyphEl, Blob blob) {
		glyphEl.setAttribute("id", blob.getBaseName());
		glyphEl.setAttribute("x", "" + blob.x());
		glyphEl.setAttribute("y", "" + blob.y());
		if (!blob.getName().equals(""))
			glyphEl.setAttribute("name", blob.getName());
		else if (blob.getGuessed() != null && blob.getGuessed().size() > 0)
			glyphEl.setAttribute("name", blob.getGuessed().get(0));
		if (!blob.getNote().equals(""))
			glyphEl.setAttribute("note", blob.getNote());
	}

	private void removeOrphans(Page page) {
		for (Blob blob : page.orphanGlyphs) 
			blob.remove();
	}

}
