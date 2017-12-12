/***************************************************************************/
/*                                                                         */
/*  Corpus.java                                                            */
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

// Linguistic corpus.

package nederhof.corpus;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.swing.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.alignment.*;
import nederhof.interlinear.*;
import nederhof.interlinear.frame.pdf.*;
import nederhof.util.*;
import nederhof.util.xml.*;

public class Corpus {

	// The index file gathering the texts. This is URI.
	private String location;

	// Is location editable?
	private boolean editable = false;

	// Description of type. E.g. "Ancient Egyptian".
	private String type = "unknown type";

	// Name of corpus. E.g. "St Andrews corpus".
	private String name = "";

	// The list of texts contained in corpus.
	private Vector<Text> texts = new Vector<Text>();

	// Mapping from locations to texts.
	private TreeMap locationToText = new TreeMap();

	// Mapping from kinds of trees to tree.
	private TreeMap<String,TreeSet<TreeNode>> kindToTree = new TreeMap();

	// Make corpus from index file.
	// The index file name is taken as URI.
	public Corpus(String location) throws IOException {
		this.location = location;
		read();
		detectEditable();
	}

	// Make empty corpus.
	private Corpus() {
	}

	// Make fresh corpus in new file. Fail if already exists.
	// Write empty XML content to it.
	public static Corpus makeCorpus(String type, String name, File file) 
		throws IOException {
			if (file.exists())
				throw new IOException("Corpus already exists: " + file);
			else if (!FileAux.hasExtension(file.getName(), "xml"))
				throw new IOException("Corpus file name should end on .xml");

			Corpus corpus = new Corpus();
			corpus.location = file.getPath(); 
			corpus.editable = true;
			corpus.type = type;
			corpus.name = name;
			corpus.save(file);
			return corpus;
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
	// Reading.

	// Parser of corpus files.
	private static DocumentBuilder parser = 
		SimpleXmlParser.construct(false, false);

	// Read corpus from file.
	private void read() throws IOException {
		InputStream in = FileAux.addressToStream(location);
		try {
			Document doc = parser.parse(in);
			processDoc(doc);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}
		in.close();
	}

	// Process document.
	private void processDoc(Document doc) throws IOException {
		Element top = doc.getDocumentElement();
		if (!top.getTagName().equals("corpus"))
			throw new IOException("File is not corpus");

		// type and name
		type = getValue(top.getAttributeNode("type"));
		name = getValue(top.getAttributeNode("name"));

		// texts
		NodeList list = doc.getElementsByTagName("text");
		for (int i = 0; i < list.getLength(); i++) {
			Node item = list.item(i);
			if (item instanceof Element) {
				Element elem = (Element) item;
				String textLocation = getValue(elem.getAttributeNode("location"));
				textLocation = resolve(textLocation);
				Text text = new Text(textLocation);
				texts.add(text);
				locationToText.put(textLocation, text);
			}
		}

		// trees
		NodeList treeList = doc.getElementsByTagName("tree");
		for (int i = 0; i < treeList.getLength(); i++) {
			Node tree = treeList.item(i);
			if (tree instanceof Element) {
				Element elem = (Element) tree;
				String kind = getValue(elem.getAttributeNode("kind"));
				TreeSet<TreeNode> nodes = processTree(elem);
				kindToTree.put(kind, nodes);
			}
		}
	}

	// Process tree from document.
	private TreeSet<TreeNode> processTree(Element node) {
		TreeSet<TreeNode> subtrees = new TreeSet<TreeNode>();
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child instanceof Element) {
				Element elem = (Element) child;
				String tagName = elem.getTagName();
				if (tagName.equals("internal")) {
					String label = getValue(elem.getAttributeNode("label"));
					String key = getValue(elem.getAttributeNode("key"));
					TreeSet subChildren = processTree(elem);
					InternalNode internal = new InternalNode(label, key, subChildren);
					subtrees.add(internal);
				} else if (tagName.equals("leaf")) {
					String label = getValue(elem.getAttributeNode("label"));
					String key = getValue(elem.getAttributeNode("key"));
					String name = getValue(elem.getAttributeNode("name"));
					String location = getValue(elem.getAttributeNode("location"));
					String post = getValue(elem.getAttributeNode("post"));
					Text text = (Text) locationToText.get(resolve(location));
					if (text != null) {
						LeafNode leaf = new LeafNode(label, key, name, text, post);
						subtrees.add(leaf);
					}
				}
			}
		}
		return subtrees;
	}

	// Get attribute value, but "" if attribute is null.
	private static String getValue(Attr attr) {
		return attr == null ? "" : attr.getValue();
	}

	//////////////////////////////////////////
	// Writing.

	// Write index to file.
	// First write to temporary, before overwriting original file.
	public void save() throws IOException {
		if (!editable)
			return;
		File temp = new File(location + "~");
		save(temp);
		save(new File(location));
		temp.delete();
	}

	// Write index to file.
	private void save(File outFile) throws IOException {
		PrintWriter out = new XmlFileWriter(outFile);
		writeIndexHeader(out); 
		writeTexts(out);
		writeTrees(out);
		writeIndexFooter(out); 
		out.close();
	}

	// Write start of XML index file.
	private void writeIndexHeader(PrintWriter out) throws IOException {
		out.println("<corpus type=\"" + XmlAux.escape(type) + "\" " +
				"name=\"" + XmlAux.escape(name) + "\">");
	}

	// Write end of XML index file.
	private void writeIndexFooter(PrintWriter out) throws IOException {
		out.println("</corpus>");
	}

	// Write the list of texts included in corpus.
	private void writeTexts(PrintWriter out) throws IOException {
		for (int i = 0; i < texts.size(); i++) {
			Text text = texts.get(i);
			String relativeText = relative(text.getLocation());
			out.println("<text location=\"" + XmlAux.escape(relativeText) + "\"/>");
		}
	}

	// Write the trees.
	private void writeTrees(PrintWriter out) throws IOException {
		Vector<String> kinds = getKinds();
		for (int i = 0; i < kinds.size(); i++) {
			String kind = kinds.get(i);
			out.println("<tree kind=\"" + XmlAux.escape(kind) + "\">"); 
			TreeSet<TreeNode> nodes = kindToTree.get(kind);
			writeTrees(out, nodes, 1);
			out.println("</tree>");
		}
	}

	// Write subtrees. With indentation.
	private void writeTrees(PrintWriter out, TreeSet nodes, int level) {
		Iterator it = nodes.iterator();
		while (it.hasNext()) {
			TreeNode node = (TreeNode) it.next();
			if (node instanceof LeafNode) {
				LeafNode leaf = (LeafNode) node;
				String relativeText = relative(leaf.text.getLocation());
				writeSpaces(out, level);
				out.print("<leaf label=\"" + XmlAux.escape(leaf.label) + "\" ");
				if (!leaf.key.matches("\\s*"))
					out.print("key=\"" + XmlAux.escape(leaf.key) + "\" ");
				out.println();
				writeSpaces(out, level+5);
				out.println("name=\"" + XmlAux.escape(leaf.name) + "\" " +
						"location=\"" + XmlAux.escape(relativeText) + "\"");
				writeSpaces(out, level+5);
				out.println("post=\"" + XmlAux.escape(leaf.post) + "\"/>");
			} else {
				InternalNode internal = (InternalNode) node;
				writeSpaces(out, level);
				out.print("<internal label=\"" + XmlAux.escape(node.label) + "\"");
				if (!node.key.matches("\\s*"))
					out.print(" key=\"" + XmlAux.escape(node.key) + "\"");
				out.println(">");
				writeTrees(out, internal.children, level+1);
				writeSpaces(out, level);
				out.println("</internal>");
			}
		}
	}

	// Write n spaces.
	private static void writeSpaces(PrintWriter out, int n) {
		for (int i = 0; i < n; i++) 
			out.print(" ");
	}

	//////////////////////////////////////////
	// Writing of HTML/PDF.

	// Put corpus in form accessible via browser, 
	// with index in html and files in PDF.
	public void writePdfTo(File dir, 
			Vector<ResourceGenerator> resourceGenerators, 
			Autoaligner autoaligner,
			PdfRenderParameters params) throws IOException {
		File indexFile = new File(dir, "index.html");
		try {
			if (!dir.exists() || !dir.canWrite())
				throw new IOException("Cannot write to: " + dir);
		} catch (SecurityException e)  {
			throw new IOException("Cannot write to: " + dir);
		}
		params.setSavePath(dir.getPath());
		convertTextsToPdf(dir, resourceGenerators, autoaligner, params);
		printHtmlIndexFile(indexFile);
	}

	// Convert each text to PDF and place in directory.
	private void convertTextsToPdf(File dir, 
			Vector<ResourceGenerator> resourceGenerators, 
			Autoaligner autoaligner,
			PdfRenderParameters params) throws IOException {
		for (int i = 0; i < texts.size(); i++) {
			final Text text = texts.get(i);
			String baseName = baseName(text);
			if (baseName != null) {
				params.setNames(baseName, text.getName());
				Exporter.export(text, resourceGenerators, autoaligner, params);
			} else
				throw new IOException("Text doesn't have extension XML: " + 
						text.getLocation());
		}
	}

	// Get PDF file name for text.
	private String pdfName(Text text) {
		String base = baseName(text);
		if (base != null) 
			return base + ".pdf";
		else
			return null;
	}

	// Get base file name for text.
	private String baseName(Text text) {
		String textLocation = text.getLocation();
		File fullFile = new File(textLocation);
		String fileName = fullFile.getName();
		return FileAux.removeExtension(fileName, "xml");
	}

	// Write file with index.
	public void printHtmlIndexFile(File file) throws IOException {
		PrintWriter out = new HtmlFileWriter(file);
		out.println("<title>" + htmlNormal(name) + "</title>");
		out.println("<body>");
		out.println("<h2>" + htmlNormal(name) + "</h2>");
		out.println();
		printHtmlIndex(out);
		out.println("</body>");
		out.close();
	}

	// Write index for each kind.
	private void printHtmlIndex(PrintWriter out) {
		Vector<String> kinds = getKinds();
		for (int i = 0; i < kinds.size(); i++) {
			String kind = kinds.get(i);
			TreeSet<TreeNode> nodes = kindToTree.get(kind);
			out.println("<h3>" + htmlNormal(kind) + "</h3>");
			out.println("<ul>");
			printHtmlIndex(out, nodes, 1);
			out.println("</ul>");
			out.println();
		}
	}

	// Descend in trees.
	private void printHtmlIndex(PrintWriter out, 
			TreeSet nodes, int level) {
		Iterator it = nodes.iterator();
		while (it.hasNext()) {
			TreeNode node = (TreeNode) it.next();
			if (node instanceof LeafNode) {
				LeafNode leaf = (LeafNode) node;
				writeSpaces(out, level);
				out.println("<li>");
				writeSpaces(out, level+1);
				if (!leaf.label.equals(leaf.name)) 
					out.print(htmlNormal(leaf.label) + "; ");
				String link = pdfName(leaf.text);
				out.println("<a href=\"" + XmlAux.escape(link) + "\">" + 
						htmlNormal(leaf.name) + "</a>");
				if (!leaf.post.matches("\\s*")) {
					writeSpaces(out, level+5);
					out.println("[" + htmlNormal(leaf.post) + "]");
				}
				writeSpaces(out, level);
				out.println("</li>");
			} else {
				InternalNode internal = (InternalNode) node;
				writeSpaces(out, level);
				out.println("<li> " + htmlNormal(node.label));
				writeSpaces(out, level);
				out.println("<ul>");
				printHtmlIndex(out, internal.children, level+1);
				writeSpaces(out, level);
				out.println("</ul>");
				writeSpaces(out, level);
				out.println("</li>");
			}
		}
	}

	// Remove all entities and XML characters.
	private static String htmlNormal(String in) {
		return 
			LatinEntities.introduceEntities(XmlAux.escape(in));
	}

	//////////////////////////////////////////
	// Making trees of index.

	// Refresh trees from files.
	public void refreshTrees() {
		kindToTree = new TreeMap<String,TreeSet<TreeNode>>();
		if (texts.size() > 0)
			kindToTree.put("texts", new TreeSet<TreeNode>());
		getTextTrees();
		collapseSingletons();
	}

	// Build tree by collection items of texts.
	// For kind "texts" store text with standard name, and
	// alternative names in priority language.
	// Further build trees by collections.
	private void getTextTrees() {
		TreeSet<TreeNode> textsTree = kindToTree.get("texts");

		for (int i = 0; i < texts.size(); i++) {
			Text text = texts.get(i);
			Vector<CollectionItem> collections = text.getCollections();
			for (int j = 0; j < collections.size(); j++) {
				CollectionItem item = collections.get(j);
				if (item.kind.matches("\\s*"))
					continue;
				if (kindToTree.get(item.kind) == null)
					kindToTree.put(item.kind, new TreeSet());
				TreeSet<TreeNode> tree = kindToTree.get(item.kind);
				tree = walkDown(tree, item.collect, item.collectKey);
				tree = walkDown(tree, item.section, item.sectionKey);
				tree = walkDown(tree, item.subsection, item.subsectionKey);
				tree = walkDown(tree, item.subsubsection, item.subsubsectionKey);
				tree.add(new LeafNode(text.getName(), 
							text, text.getDescription()));
			}

			String name = text.getName();
			textsTree.add(new LeafNode(name, text, text.getDescription()));

			TreeSet otherNames = text.getNames(Settings.languages[0]);
			for (Iterator it = otherNames.iterator(); it.hasNext(); ) {
				String otherName = (String) it.next();
				TreeSet otherTree = walkDown(textsTree, otherName, "");
				otherTree.add(new LeafNode(text.getName(), text, text.getDescription()));
			}
		}
	}

	// Descend tree according to label.
	// Create new node if not already node with label.
	private TreeSet walkDown(TreeSet tree, String label, String key) {
		if (label.matches("\\s*"))
			return tree;
		else {
			InternalNode node = new InternalNode(label, key);
			InternalNode old = equalNode(tree, node);
			if (old != null) 
				return old.children;
			else {
				tree.add(node);
				return node.children;
			} 
		}
	}

	// Get the node equal to given node, if any, else null.
	private InternalNode equalNode(TreeSet set, InternalNode given) {
		SortedSet tail = set.tailSet(given);
		if (tail.isEmpty())
			return null;
		else {
			Object first = tail.first();
			if (first.equals(given))
				return (InternalNode) first;
			else
				return null;
		}
	}

	// Where tree consists of single leaf, collapse nodes.
	private void collapseSingletons() {
		Iterator<TreeSet<TreeNode>> it = kindToTree.values().iterator();
		while (it.hasNext()) {
			TreeSet<TreeNode> tree = it.next();
			collapseSingletons(tree);
		}
	}
	private void collapseSingletons(TreeSet tree) {
		Vector<InternalNode> toRemove = new Vector<InternalNode>();
		Vector<LeafNode> toAdd = new Vector<LeafNode>();
		Iterator it = tree.iterator();
		while (it.hasNext()) {
			TreeNode node = (TreeNode) it.next();
			if (node instanceof InternalNode) {
				InternalNode internal = (InternalNode) node;
				if (isSingleton(internal)) {
					LeafNode leaf = (LeafNode) internal.children.first();
					LeafNode collapsed = new LeafNode(internal.label, internal.key,
							leaf.name, leaf.text, leaf.post);
					toRemove.add(internal);
					toAdd.add(collapsed);
				} else
					collapseSingletons(internal.children);
			}
		}
		for (int i = 0; i < toRemove.size(); i++) 
			tree.remove(toRemove.get(i));
		for (int i = 0; i < toAdd.size(); i++) 
			tree.add(toAdd.get(i));
	}

	// Is internal node with single leaf child.
	private boolean isSingleton(InternalNode node) {
		return node.children.size() == 1 &&
			(node.children.first() instanceof LeafNode);
	}

	/////////////////////////////////////////////
	// Getters/setters.

	public String getLocation() {
		return location;
	}

	public boolean isEditable() {
		return editable;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public Vector<Text> getTexts() {
		return texts;
	}

	public void setName(String name) {
		this.name = name;
	}

	// Add text for file.
	// Do not add if already in corpus.
	public Text addText(File file) throws IOException {
		File relativeFile = FileAux.getRelativePath(file);
		Text text = (Text) locationToText.get(relativeFile.getPath());
		if (text == null) {
			if (relativeFile.exists())
				text = new Text(relativeFile.getPath());
			else
				text = Text.makeText(relativeFile);
			texts.add(text);
			locationToText.put(relativeFile.getPath(), text);
			return text;
		} else
			throw new IOException("text already in corpus: " + file.getPath());
	}

	// Remove text.
	public void removeText(Text text) {
		texts.remove(text);
		locationToText.put(text.getLocation(), null);
		refreshTrees();
	}

	// Get kinds. Place kind "texts" first.
	public Vector<String> getKinds() {
		Vector<String> kinds = new Vector<String>();
		for (Iterator<String> it = kindToTree.keySet().iterator(); it.hasNext(); ) {
			String kind = it.next();
			if (kind.equals("texts"))
				kinds.add(0, kind);
			else
				kinds.add(kind);
		}
		return kinds;
	}

	// Get tree for kind.
	public TreeSet<TreeNode> getTree(String kind) {
		return kindToTree.get(kind);
	}

	//////////////////////////////////////////
	// Changing file in which corpus is stored.

	// Move corpus.
	public void moveTo(File newLoc) throws IOException {
		File oldLoc = new File(location);
		if (newLoc.equals(oldLoc))
			; // ignore
		else if (newLoc.exists())
			throw new IOException("Target file exists: " + newLoc.getPath());
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

	// File path of text that is relative to directory of corpus
	// converted to path relative to current directory.
	private String resolve(String textLocation) {
		return FileAux.resolve(location, textLocation);
	}

	// File relative to corpus.
	private File relative(File file) {
		File corpusFile = new File(location);
		return FileAux.getRelativePath(file, corpusFile.getParentFile());
	}
	// Same, but for string.
	private String relative(String file) {
		return relative(new File(file)).getPath();
	}

	//////////////////////////////////////////
	// Testing.

	public static void main(String[] args) {
		try {
			/*
			   Corpus c1 = makeCorpus("Ancient Egyptian", 
			   "St Andrews", new File("testfile.xml"));
			 */
			Corpus c = new Corpus("testfile.xml");
			c.save(new File("testfile2.xml"));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
