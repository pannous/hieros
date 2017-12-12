package nederhof.interlinear.egyptian.ortho;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.util.*;
import nederhof.util.xml.*;

/**
 * Suggesting annotations.
 */
public class AnnotationSuggestor {

    // Key: hieroglyph, or sequence of hieroglyphs separated by hyphen.
    // Value is sorted by frequency.
    private HashMap<String, ArrayList<AnnotationSuggestion>> suggestions = 
		new HashMap<String, ArrayList<AnnotationSuggestion>>();

    // Key: hieroglyph, or sequence of hieroglyphs separated by hyphen.
    // Value.
    // This is only used for writing back to file.
    private HashMap<String, ArrayList<String>> useas = 
	new HashMap<String, ArrayList<String>>();

    // Key: function name.
    // Value: frequency.
    private TreeMap<String, Integer> funFreq = new TreeMap<String, Integer>();

    /**
     * Constructor. After reading, sort.
     * Also record frequencies of functions overall.
     */
    public AnnotationSuggestor() throws IOException {
	DocumentBuilder parser = SimpleXmlParser.construct(false, false);
	InputStream in = FileAux.addressToStream(Settings.functionsLocation);
	try {
	    Document doc = parser.parse(in);
	    processDoc(doc);
	} catch (SAXException e) {
	    throw new IOException(e.getMessage());
	}
	in.close();
	for (ArrayList<AnnotationSuggestion> suggs : suggestions.values()) 
	    Collections.sort(suggs, new AnnoComparator());
	for (ArrayList<AnnotationSuggestion> suggs : suggestions.values()) {
	    for (AnnotationSuggestion sugg : suggs) 
		funFreq.put(sugg.fun, 
			(funFreq.get(sugg.fun) == null ? 0 : funFreq.get(sugg.fun))
			+ sugg.count);
	}
    }

    // Process document.
    private void processDoc(Document doc) throws IOException {
        for (Node child = doc.getFirstChild().getFirstChild();
                    child != null;
                    child = child.getNextSibling()) {
            if (child instanceof Element)
                processEntry((Element) child);
        }
    }

    // Process one entry in the table.
    private void processEntry(Element entry) throws IOException {
        String fun = entry.getNodeName();
        String hi = "";
        String arg = "";
        String val = "";
        String lemma = "";
        int count = 0;
        String countStr = entry.getAttribute("count");
	if (countStr.matches("[0-9]+")) 
	    try {
		count = Integer.parseInt(countStr);
	    } catch (NumberFormatException e) {
		// ignore
	    }

	for (Node child = entry.getFirstChild();
		child != null;
		child = child.getNextSibling()) {
	    if (child instanceof Element) {
		Element childEl = (Element) child;
		String childName = childEl.getNodeName();
		String childVal = "";
		NodeList childChildren = childEl.getChildNodes();
		if (childChildren.getLength() > 0) {
		    Node childChild = childChildren.item(0);
		    if (childChild instanceof Text) 
			childVal = ((Text) childChild).getData();
		}
		if (childName.equals("hi")) 
		    hi = childVal;
		else if (childName.equals("lemma")) 
		    lemma = childVal;
		else {
		    arg = childName;
		    val = childVal;
		}
	    }
	}
	if (fun.equals("useas")) {
	    if (useas.get(hi) == null)
		useas.put(hi, new ArrayList<String>());
	    useas.get(hi).add(val);
	} else if (!hi.equals("")) {
	    if (suggestions.get(hi) == null) 
		suggestions.put(hi, new ArrayList<AnnotationSuggestion>());
	    suggestions.get(hi).add(new AnnotationSuggestion(fun, arg, val, lemma, count));
	}
    }

    // Get suggestions in order, given some letters already covered by other functions.
    // First get functions covering uncovered parts.
    // Then get functions covering any part.
    // Then add rest.
    public ArrayList<AnnotationSuggestion> getSuggestions(String hi, 
	    String word, int covered) {
	ArrayList<AnnotationSuggestion> all = suggestions.get(hi);
	ArrayList<AnnotationSuggestion> suggs =
	    new ArrayList<AnnotationSuggestion>();
	if (all == null)
	    return suggs;
	ArrayList<AnnotationSuggestion> remain =
	    new ArrayList<AnnotationSuggestion>();
	for (AnnotationSuggestion sugg : all) {
	    int index = occurrence(sugg, word.substring(covered));
	    if (index >= 0)
		suggs.add(sugg.clone(index));
	    else
		remain.add(sugg);
	}
	all = remain;
	remain = new ArrayList<AnnotationSuggestion>();
	for (AnnotationSuggestion sugg : all) {
	    int index = occurrence(sugg, word);
	    if (index >= 0)
		suggs.add(sugg.clone(index));
	    else
		remain.add(sugg);
	}
	all = remain;
	remain = new ArrayList<AnnotationSuggestion>();
	for (AnnotationSuggestion sugg : all) {
	    int index = occurrenceSoundChange(sugg, word);
	    if (index >= 0)
		suggs.add(sugg.clone(index));
	    else
		remain.add(sugg);
	}
	all = remain;
	suggs = pruneForms(suggs);
	for (AnnotationSuggestion sugg : all) 
	    suggs.add(sugg.clone(0));
	return suggs;
    }

    // Get readings, without filtering by context.
    public ArrayList<AnnotationSuggestion> getReadings(String hi) {
	ArrayList<AnnotationSuggestion> all = suggestions.get(hi);
	ArrayList<AnnotationSuggestion> suggs =
	    new ArrayList<AnnotationSuggestion>();
	if (all != null)
	    for (AnnotationSuggestion sugg : all)
		suggs.add(sugg);
	return suggs;
    }

    // Is the suggestion reasonable in the word ?
    // What is index of occurrence (0 if irrelevant).
    // Negative if not reasonable.
    private int occurrence(AnnotationSuggestion sugg, String word) {
	if (sugg.fun.equals("log") || 
		(sugg.fun.equals("det") && sugg.arg.equals("al"))) {
	    if (word.startsWith(sugg.val))
		return 0;
	    else
		return -1;
	} else if (sugg.arg.equals("al"))
	    return word.indexOf(sugg.val);
	else
	    return 0;
    }
    // As above, but allowing for sound change.
    private int occurrenceSoundChange(AnnotationSuggestion sugg, String word) {
	String wordChanged = soundChange(word);
	if (sugg.fun.equals("log") || 
		(sugg.fun.equals("det") && sugg.arg.equals("al"))) {
	    if (wordChanged.startsWith(soundChange(sugg.val)))
		return 0;
	    else
		return -1;
	} else if (sugg.arg.equals("al"))
	    return wordChanged.indexOf(soundChange(sugg.val));
	else
	    return 0;
    }
    // Change D to d and T to t.
    private String soundChange(String s) {
	s = s.replaceAll("T", "t");
	s = s.replaceAll("D", "d");
	return s;
    }

    // There may be several forms of a word present. 
    // Only the longest is to remain.
    private ArrayList<AnnotationSuggestion> pruneForms(ArrayList<AnnotationSuggestion> suggs) {
	TreeSet<String> detWords = new TreeSet<String>();
	TreeSet<String> logWords = new TreeSet<String>();
	for (AnnotationSuggestion sugg : suggs) {
	    if (sugg.fun.equals("det") && sugg.arg.equals("al")) 
		detWords.add(sugg.val);
	    else if (sugg.fun.equals("log"))
		logWords.add(sugg.val);
	}
	ArrayList<AnnotationSuggestion> pruned = new ArrayList<AnnotationSuggestion>();
	for (AnnotationSuggestion sugg : suggs) {
	    boolean found = false;
	    if (sugg.fun.equals("det") && sugg.arg.equals("al")) {
		for (String longer : detWords)
		    if (longer.length() > sugg.val.length() &&
			    longer.indexOf(sugg.val) >= 0)
			found = true;
	    } else if (sugg.fun.equals("log")) {
		for (String longer : logWords)
		    if (longer.length() > sugg.val.length() &&
			    longer.indexOf(sugg.val) >= 0)
			found = true;
	    }
	    if (!found)
		pruned.add(sugg);
	}
	return pruned;
    }

    // Sort, highest frequency first.
    public class AnnoComparator implements Comparator<AnnotationSuggestion> {
        public int compare(AnnotationSuggestion s1, AnnotationSuggestion s2) {
            if (s1.count != s2.count)
                return s2.count - s1.count;
            else if (funFreq.get(s1.fun) != funFreq.get(s2.fun))
		return funFreq.get(s2.fun) - funFreq.get(s1.fun);
            else if (!s1.fun.equals(s2.fun))
                return s1.fun.compareTo(s2.fun);
            else
                return s1.hashCode() - s2.hashCode();
        }
    }

    // Set all counts to zero.
    public void resetCounts() {
	for (Map.Entry<String, ArrayList<AnnotationSuggestion>> pair : suggestions.entrySet()) {
	    ArrayList<AnnotationSuggestion> suggs = pair.getValue();
	    for (AnnotationSuggestion sugg : suggs) 
		sugg.count = 0;
	}
    }

    //////////////////////////////////////////
    // Writing.

    // Write suggestions back to document (possibly with changed counts).
    public void write() throws IOException {
	try {
	    File f = new File(Settings.functionsLocation);
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document doc = builder.newDocument();
	    write(doc);
	    XmlPretty.print(doc, f);
	} catch (Exception e) {
	    throw new IOException(e.getMessage());
	}
    }
    private void write(Document target) {
	Element signinfo = target.createElement("signinfo");
	target.appendChild(signinfo);
	for (Map.Entry<String, ArrayList<AnnotationSuggestion>> pair : suggestions.entrySet()) {
	    String hi = pair.getKey();
	    ArrayList<AnnotationSuggestion> suggs = pair.getValue();
	    for (AnnotationSuggestion sugg : suggs) {
		Element function = target.createElement(sugg.fun);
		function.setAttribute("count", "" + sugg.count);
		Element hiero = target.createElement("hi");
		Text hiText = target.createTextNode(hi);
		hiero.appendChild(hiText);
		function.appendChild(hiero);
		if (!sugg.val.equals("")) {
		    Element arg = target.createElement(sugg.arg);
		    Text valText = target.createTextNode(sugg.val);
		    arg.appendChild(valText);
		    function.appendChild(arg);
		}
		signinfo.appendChild(function);
	    }
	}
	for (Map.Entry<String, ArrayList<String>> pair : useas.entrySet()) {
	    String hi = pair.getKey();
	    ArrayList<String> alts = pair.getValue();
	    for (String alt : alts) {
		Element function = target.createElement("useas");
		Element hiero = target.createElement("hi");
		Text hiText = target.createTextNode(hi);
		hiero.appendChild(hiText);
		function.appendChild(hiero);
		Element altElem = target.createElement("alt");
		Text altText = target.createTextNode(alt);
		altElem.appendChild(altText);
		function.appendChild(altElem);
		signinfo.appendChild(function);
	    }
	}
    }

    //////////////////////////////////////////
    // Testing.

    public static void main(String[] args) {
        try {
            new AnnotationSuggestor();
        } catch (IOException e) {
        }
    }

}
