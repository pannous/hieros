// Table of annotated sign list.

package nederhof.alignment.egyptian;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.swing.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.egyptian.trans.*;
import nederhof.util.*;
import nederhof.util.xml.*;

public class SignTable {

    // The root of trie, by which sign functions can be found
    // given sequence of hieroglyphs.
    private TrieList<Function> root = new TrieList<Function>();

    // Special table for numerals.
    private TreeMap<String,Integer> numerals = new TreeMap<String,Integer>();

    // Constructor.
    public SignTable(String location) throws IOException {
	DocumentBuilder parser = SimpleXmlParser.construct(false, false);
	InputStream in = FileAux.addressToStream(location);
	try {
	    Document doc = parser.parse(in);
	    processDoc(doc);
	} catch (SAXException e) {
	    throw new IOException(e.getMessage());

	}
	in.close();
    }

    // Get root of table (trie).
    public TrieList<Function> getRoot() {
	return root;
    }

    // Get value of numeral, if any.
    public boolean isNumeral(String hi) {
	return numerals.get(hi) != null;
    }
    public int getNumeral(String hi) {
	return numerals.get(hi);
    }

    // Process document.
    private void processDoc(Document doc) throws IOException {
	Node root = doc.getFirstChild();
	if (root == null)
	    return;
	for (Node child = root.getFirstChild(); child != null; child = child.getNextSibling()) 
	    if (child instanceof Element) 
		processEntry((Element) child);
	processSpecialFemPlur();
    }

    // Process one entry in the table.
    private void processEntry(Element entry) throws IOException {
	String className = entry.getNodeName();

	String hiString = processEntryChild(entry, "hi");
	String alString = processEntryChild(entry, "al");
	boolean femPlur = processEntryChildAttr(entry, "al", "femplur");
	String lemmaString = processEntryChild(entry, "lemma");
	String descrString = processEntryChild(entry, "descr");
	// No use for this at the moment.
	// String altString = processEntryChild(entry, "alt");

	String[] hiList = hiString.equals("") ? null : splitHiero(hiString);
	if (hiList == null)
	    return;
	TrieList<Function> hiTrie = root.next(hiList);

	if (!alString.equals("") && lemmaString.equals(""))
	    lemmaString = alString;

	// No use for this at the moment.
	// String[] altList = altString.equals("") ? null : splitHiero(altString);

	if (className.equals("log") && !alString.equals(""))
	    storeLog(hiList, hiTrie, lemmaString, alString, femPlur);
	else if (className.equals("det") && !descrString.equals(""))
	    storeDetDescr(hiList, hiTrie, descrString);
	else if (className.equals("det") && !alString.equals(""))
	    storeDetWord(hiList, hiTrie, lemmaString, alString, femPlur);
	else if (className.equals("phon") && !alString.equals(""))
	    storePhon(hiList, hiTrie, alString);
	else if (className.equals("phondet") && !alString.equals(""))
	    storePhondet(hiList, hiTrie, alString);
	else if (className.equals("num") && !alString.equals(""))
	    storeNum(hiList, hiTrie, alString);
	else if (className.equals("typ") && !descrString.equals(""))
	    storeTyp(hiList, hiTrie, descrString);

	/* No use for this at the moment 
	// ignored is N33-N33-N33 used as Z1-Z1-Z1
	else if (className.equals("useas") && hiList.length == 1)
	    storeUseAs(hiList, hiTrie, altList);
	*/
    }

    // Special case of t with plural marker.
    private void processSpecialFemPlur() {
	String[] hiList = new String[] {"X1"};
	TrieList<Function> hiTrie = root.next(hiList);
	storeSpecialFemPlur(hiList, hiTrie, "t", "wt");
    }

    // Get text in child with name. Return "" if nothing.
    private String processEntryChild(Element entry, String name) {
	Element child = (Element) entry.getElementsByTagName(name).item(0);
	if (child != null) {
	    Node node = child.getChildNodes().item(0);
	    if (node instanceof Text) {
		Text childText = (Text) child.getChildNodes().item(0);
		if (childText != null)
		    return childText.getData();
	    }
	}
	return "";
    }

    // Get boolean attribute of child with name. Return false if nothing.
    private boolean processEntryChildAttr(Element entry, String name, String attr) {
	Element child = (Element) entry.getElementsByTagName(name).item(0);
	if (child != null) {
	    String val = child.getAttribute(attr);
	    return val.equals("true");
	}
	return false;
    }

    // Split hieroglyphic separated by '-' into individual Gardiner names.
    private String[] splitHiero(String hi) {
	return hi.split("-");
    }

    // Logogram.
    private void storeLog(String[] hi, TrieList<Function> hiTrie, String lemma, String al, 
	    		boolean femPlur) {
	hiTrie.add(new FunctionLog(hi, new TransMdc(lemma), toLow(al), femPlur));
    }

    // Determinative specific to word.
    private void storeDetWord(String[] hi, TrieList<Function> hiTrie, String lemma, String al, 
	    		boolean femPlur) {
	hiTrie.add(new FunctionDetWord(hi, new TransMdc(lemma), toLow(al), femPlur));
    }

    // Determinative with description.
    private void storeDetDescr(String[] hi, TrieList<Function> hiTrie, String descr) {
	hiTrie.add(new FunctionDetDescr(hi, descr));
    }

    // Phonogram.
    private void storePhon(String[] hi, TrieList<Function> hiTrie, String al) {
    	TransLow historical = toLow(al);
	hiTrie.add(new FunctionPhon(hi, historical, historical, false, false));
	for (TransLow derived : historical.soundDerived())
	    hiTrie.add(new FunctionPhon(hi, historical, derived, true, false));
    }

    // Special treatment of phoneme t, with plural w marker in front.
    private void storeSpecialFemPlur(String[] hi, TrieList<Function> hiTrie, 
	    String lemma, String al) {
	hiTrie.add(new FunctionPhon(hi, toLow(lemma), toLow(al), false, true));
    }

    // Phonetic determinative.
    private void storePhondet(String[] hi, TrieList<Function> hiTrie, String al) {
	TransLow historical = toLow(al);
	hiTrie.add(new FunctionPhondet(hi, historical, historical, false));
	for (TransLow derived : historical.soundDerived())
	    hiTrie.add(new FunctionPhondet(hi, historical, derived, true));
    }

    private void storeNum(String[] hi, TrieList<Function> hiTrie, String al) {
	try {
	    int num = Integer.parseInt(al);
	    if (hi.length == 1)
		numerals.put(hi[0], num);
	} catch (NumberFormatException e) {
	    // ignore
	}
    }

    // Typographical symbol.
    private void storeTyp(String[] hi, TrieList<Function> hiTrie, String descr) {
	if (descr.equals("plurality or collectivity")) {
	    hiTrie.add(new FunctionTypSuffix(hi, descr, toLow("w")));
	    hiTrie.add(new FunctionTypFemPlur(hi, descr));
	    hiTrie.add(new FunctionTypSem(hi, descr));
	} else if (descr.equals("duality")) {
	    hiTrie.add(new FunctionTypSuffix(hi, descr, toLow("wj")));
	    hiTrie.add(new FunctionTypSuffix(hi, descr, toLow("j")));
	    hiTrie.add(new FunctionTypSem(hi, descr));
	} else if (descr.equals("repetition of the preceding sequence of consonants")) {
	    hiTrie.add(new FunctionTypSpSn(hi, descr, 1));
	    hiTrie.add(new FunctionTypSpSn(hi, descr, 2));
	    hiTrie.add(new FunctionTypSpSn(hi, descr, 3));
	} else {
	    hiTrie.add(new FunctionTypSem(hi, descr));
	}
    }

    // Convert to lower case transliteration.
    private TransLow toLow(String s) {
	return new TransLow(new TransMdc(s));
    }

    /* No use for this at the moment.
    private void storeUseAs(String[] hi, TrieList<Function> hiTrie, String[] alt) {
	// TODO
    }
    */

    //////////////////////////////////////////
    // Auxiliary.

    // Is al the feminine plural form of the lemma?
    private static boolean isFemPlur(String al, String lemma) {
	return al.matches(".*wt$") && lemma.matches(".*t$") && !lemma.matches(".*wt$");
    }
    private static boolean isFemPlurEnding(String al, String lemma) {
	return al.matches("^wt$") && lemma.matches("^t$") && !lemma.matches("^wt$");
    }
    public static boolean isFemPlur(TransLow al, TransMdc lemma) {
	return isFemPlur(al.toString(), lemma.toString());
    }
    public static boolean isFemPlurEnding(TransLow al, TransLow lemma) {
	return isFemPlurEnding(al.toString(), lemma.toString());
    }

    //////////////////////////////////////////
    // Testing.

    public static void main(String[] args) {
	try {
	    new SignTable("data/ortho/functions.xml");
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	}
    }

}
