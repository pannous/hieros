package nederhof.lexicon.egyptian;

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

import nederhof.util.*;
import nederhof.util.xml.*;

// Comparison of strings, representing Egyptian transliteration.
public class EgyptianLexicon {

	// The file where it is stored.
	private File location;
	// Description of dictionary.
	private String descr;

	private TreeSet<DictLemma> lemmas = new TreeSet<DictLemma>();

	// Constructor.
	public EgyptianLexicon(File location) throws IOException {
		this.location = location;
		read();
	}

	public String getDescr() {
		return descr;
	}

	public TreeSet<DictLemma> getLemmas() {
		return lemmas;
	}

	//////////////////////////////////////////
	// Reading.

	// Read from file. First read images and glyphs directory.
	// Optionally read index file.
	private void read() throws IOException {
		try {
			Document doc = readDocument();
			readDescr(doc);
			readLemmas(doc);
			save();
		} catch (SecurityException e) {
			throw new IOException(e.getMessage());
		}
	}

	// Read XML file.
	private Document readDocument() throws IOException {
		DocumentBuilder parser = SimpleXmlParser.construct(false, false);
		InputStream in = new FileInputStream(location);
		try {
			return parser.parse(in);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}
	}

	// Read name of lexicon.
	private void readDescr(Document doc) {
		Element docElem = doc.getDocumentElement();
		descr = docElem.getAttribute("descr");
	}

	// Read lexicon entries.
	private void readLemmas(Document doc) {
		NodeList elems = doc.getElementsByTagName("lemma");
		for (int i = 0; i < elems.getLength(); i++) {
			Element elemLemma = (Element) elems.item(i);
			DictLemma lemma = readLemma(elemLemma);
			lemmas.add(lemma);
		}
	}

	// Read lemma.
	private DictLemma readLemma(Element elemLemma) {
		String pos = elemLemma.getAttribute("pos");
		String keyhi = elemLemma.getAttribute("keyhi");
		String keyal = elemLemma.getAttribute("keyal");
		String keytr = elemLemma.getAttribute("keytr");
		String keyfo = elemLemma.getAttribute("keyfo");
		String keyco = elemLemma.getAttribute("keyco");
		Vector<DictMeaning> meanings = readMeanings(elemLemma);
		return new DictLemma(pos, keyhi, keyal, keytr, keyfo, keyco, meanings);
	}

	// Read meanings from lemma.
	private Vector<DictMeaning> readMeanings(Element elemLemma) {
		Vector<DictMeaning> meanings = new Vector<DictMeaning>();
		NodeList elems = elemLemma.getElementsByTagName("meaning");
		for (int i = 0; i < elems.getLength(); i++) {
			Element elemMeaning = (Element) elems.item(i);
			DictMeaning meaning = readMeaning(elemMeaning);
			meanings.add(meaning);
		}
		return meanings;
	}

	// Read meaning.
	private DictMeaning readMeaning(Element elemMeaning) {
		String rank = elemMeaning.getAttribute("rank");
		Vector<DictUse> uses = readUses(elemMeaning);
		return new DictMeaning(rank, uses);
	}

	// Read uses.
	private Vector<DictUse> readUses(Element elemMeaning) {
		Vector<DictUse> uses = new Vector<DictUse>();
		NodeList elems = elemMeaning.getChildNodes();
		for (int i = 0; i < elems.getLength(); i++) {
			Node node = elems.item(i);
			if (node instanceof Element) {
				Element elemUse = (Element) node;
				if (elemUse.getTagName().equals("use")) {
					DictUse use = readUse(elemUse);
					uses.add(use);
				}
			}
		}
		return uses;
	}

	// Read use.
	private DictUse readUse(Element elemUse) {
		Vector<DictUsePart> parts = new Vector<DictUsePart>();
		NodeList elems = elemUse.getChildNodes();
		for (int i = 0; i < elems.getLength(); i++) {
			Node node = elems.item(i);
			if (node instanceof Element) {
				Element elem = (Element) node;
				String name = elem.getTagName();
				if (name.equals("hi")) {
					parts.add(new DictHi(textOf(elem)));
				} else if (name.equals("al")) {
					parts.add(new DictAl(textOf(elem)));
				} else if (name.equals("tr")) {
					parts.add(new DictTr(textOf(elem)));
				} else if (name.equals("fo")) {
					parts.add(new DictFo(textOf(elem)));
				} else if (name.equals("co")) {
					parts.add(new DictCo(textOf(elem)));
				} else if (name.equals("alt")) {
					parts.add(readAlt(elem));
				} else if (name.equals("opt")) {
					parts.add(readOpt(elem));
				}
			}
		}
		return new DictUse(parts);
	}

	// Read alternatives.
	private DictAlt readAlt(Element elemAlt) {
		Vector<DictUse> uses = readUses(elemAlt);
		return new DictAlt(uses);
	}

	// Read optional alternatives.
	private DictOpt readOpt(Element elemOpt) {
		Vector<DictUse> uses = readUses(elemOpt);
		return new DictOpt(uses);
	}

	// Text in element.
	private String textOf(Element elem) {
		String s = "";
		NodeList elems = elem.getChildNodes();
		for (int i = 0; i < elems.getLength(); i++) {
			Node node = elems.item(i);
			if (node instanceof CharacterData) {
				CharacterData data = (CharacterData) node;
				s += data.getData();
			}
		}
		return s;
	}

	//////////////////////////////////////////
	// Writing.

	// First write to temporary, before overwriting original file.
	public void save() throws IOException {
		File temp = new File(location.getParentFile(), location.getName() + "~");
		save(temp);
		save(location);
		temp.delete();
	}

	// Write to file.
	private void save(File file) throws IOException {
		PrintWriter out = new XmlFileWriter(file);
		out.print("<dict");
		writeAttr(out, "descr", descr);
		out.println(">\n");
		for (DictLemma lemma : lemmas)
			write(out, lemma);
		out.println("</dict>");
		out.close();
	}

	// Write lemma.
	private void write(PrintWriter out, DictLemma lemma) throws IOException {
		out.print("<lemma");
		writeAttr(out, "pos", lemma.pos);
		writeAttr(out, "keyhi", lemma.keyhi);
		writeAttr(out, "keyal", lemma.keyal);
		writeAttr(out, "keytr", lemma.keytr);
		writeAttr(out, "keyfo", lemma.keyfo);
		writeAttr(out, "keyco", lemma.keyco);
		out.println(">");
		for (DictMeaning meaning : lemma.meanings)
			write(out, meaning);
		out.println("</lemma>\n");
	}

	// Write meaning.
	private void write(PrintWriter out, DictMeaning meaning) throws IOException {
		out.print("<meaning");
		writeAttr(out, "rank", meaning.rank);
		out.println(">");
		for (DictUse use : meaning.uses)
			write(out, use, 0);
		out.println("</meaning>");
	}

	// Write use.
	private void write(PrintWriter out, DictUse use, int nesting) throws IOException {
		writeNesting(out, nesting);
		out.print("<use>" + (nesting == 0 ? "\n" : ""));
		boolean isFirst = true;
		boolean prevWasAltOrOpt = false;
		for (int j = 0; j < use.parts.size(); j++) {
			DictUsePart part = use.parts.get(j);
			if (part instanceof DictHi) {
				if (prevWasAltOrOpt)
					writeNesting(out, nesting);
				else if (!isFirst)
					out.print(" ");
				write(out, "hi", ((DictHi) part).hi);
			} else if (part instanceof DictAl) {
				if (prevWasAltOrOpt)
					writeNesting(out, nesting);
				else if (!isFirst)
					out.print(" ");
				write(out, "al", ((DictAl) part).al);
			} else if (part instanceof DictTr) {
				if (prevWasAltOrOpt)
					writeNesting(out, nesting);
				else if (!isFirst)
					out.print(" ");
				write(out, "tr", ((DictTr) part).tr);
			} else if (part instanceof DictFo) {
				if (prevWasAltOrOpt)
					writeNesting(out, nesting);
				else if (!isFirst)
					out.print(" ");
				write(out, "fo", ((DictFo) part).fo);
			} else if (part instanceof DictCo) {
				if (prevWasAltOrOpt)
					writeNesting(out, nesting);
				else if (!isFirst)
					out.print(" ");
				write(out, "co", ((DictCo) part).co);
			} else if (part instanceof DictAlt) {
				if (!prevWasAltOrOpt)
					out.println();
				write(out, (DictAlt) part, nesting+1);
				prevWasAltOrOpt = true;
			} else if (part instanceof DictOpt) {
				if (!prevWasAltOrOpt)
					out.println();
				write(out, (DictOpt) part, nesting+1);
				prevWasAltOrOpt = true;
			}
			isFirst = false;
		}
		if (!prevWasAltOrOpt && nesting == 0)
			out.println();
		if (prevWasAltOrOpt)
			writeNesting(out, nesting);
		out.println("</use>");
	}

	// Write text element.
	private void write(PrintWriter out, String name, String val) {
		out.print("<" + name + ">" + val + "</" + name + ">");
	}

	// Write alternatives.
	private void write(PrintWriter out, DictAlt alt, int nesting) 
		throws IOException {
			writeNesting(out, nesting);
			out.println("<alt>");
			for (DictUse use : alt.uses)
				write(out, use, nesting+1);
			writeNesting(out, nesting);
			out.println("</alt>");
		}

	// Write optional alternative.
	private void write(PrintWriter out, DictOpt opt, int nesting) 
		throws IOException {
			writeNesting(out, nesting);
			out.println("<opt>");
			for (DictUse use : opt.uses)
				write(out, use, nesting+1);
			writeNesting(out, nesting);
			out.println("</opt>");
		}

	// Print tabs for nesting levels.
	private void writeNesting(PrintWriter out, int nesting) {
		for (int i = 0; i < nesting; i++)
			out.print("\t");
	}

	// Write attribute, if non-empty.
	private void writeAttr(PrintWriter out, String name, String val) {
		if (!val.equals(""))
			out.print(" " + name + "=\"" + val + "\"");
	}

	//////////////////////////////////////////
	// Editing.

	// Add new lemma.
	public void addLemma(DictLemma lemma) {
		lemmas.add(lemma);
	}

	// Remove lemma altogether.
	public void removeLemma(DictLemma lemma) {
		lemmas.remove(lemma);
	}

	// Replace existing lemma.
	public void replaceLemma(DictLemma oldLemma, DictLemma newLemma) {
		removeLemma(oldLemma);
		addLemma(newLemma);
	}

	//////////////////////////////////////////
	// Testing.

	public static void main(String[] args) {
		try {
			new EgyptianLexicon(new File("data/dict/basic.xml"));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

}
