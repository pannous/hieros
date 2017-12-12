package nederhof.web;

import java.awt.*;
import java.util.*;

import nederhof.alignment.egyptian.*;
import nederhof.alignment.*;
import nederhof.corpus.Text;
import nederhof.corpus.egyptian.*;
import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.egyptian.image.*;
import nederhof.interlinear.egyptian.pdf.*;
import nederhof.interlinear.labels.*;
import nederhof.interlinear.frame.*;

// Vocabulary of text.
public class Vocab {

    // Number of keys.
    private int nKeys = 0;

    // Maps key to uses. For names separate structure.
    private TreeMap<VocabKey,TreeSet<VocabUse>> vocab = new TreeMap<VocabKey,TreeSet<VocabUse>>();
    private TreeSet<VocabKey> vocabNames = new TreeSet<VocabKey>();

    // Maps key to places.
    private TreeMap<VocabKey,Vector<ImagePlace>> vocabPlaces = 
	new TreeMap<VocabKey,Vector<ImagePlace>>();
    private TreeMap<VocabKey,Vector<ImagePlace>> vocabNamesPlaces = 
	new TreeMap<VocabKey,Vector<ImagePlace>>();

    // Maps key to ID.
    private TreeMap<VocabKey,String> vocabId = new TreeMap<VocabKey,String>();
    private TreeMap<VocabKey,String> vocabNamesId = new TreeMap<VocabKey,String>();

    // Constructor.
    public Vocab() {
    }

    // Get new ID.
    private String freshId() {
	return "lex" + (nKeys++);
    }

    public void gatherLexical(LxPdfPart lx, Vector<ImagePlace> places) {
	String cite = lx.cite;
	String keyal = lx.keyal;
	String keytr = lx.keytr;
	String keyfo = lx.keyfo;
	String dictal = lx.dictal;
	String dicttr = lx.dicttr;
	String dictfo = lx.dictfo;
	places = ImageUtil.cluster(places);
	VocabKey key = new VocabKey(keyal, keytr, keyfo);
	if (cite.matches("basic")) {
	    if (vocab.get(key) == null) {
		vocab.put(key, new TreeSet<VocabUse>());
		vocabPlaces.put(key, new Vector<ImagePlace>());
		vocabId.put(key, freshId());
	    }
	    vocabPlaces.get(key).addAll(places);
	    if (!dictal.equals("") || !dicttr.equals("") || !dictfo.equals(""))
		vocab.get(key).add(new VocabUse(dictal, dicttr, dictfo));
	} else {
	    vocabNames.add(new VocabKey(keyal, keytr, keyfo));
	    if (vocabNamesPlaces.get(key) == null) {
		vocabNamesPlaces.put(key, new Vector<ImagePlace>());
		vocabNamesId.put(key, freshId());
	    }
	    vocabNamesPlaces.get(key).addAll(places);
	}
    }
    public void gatherLexical(LxPdfPart lx) {
	gatherLexical(lx, new Vector<ImagePlace>());
    }

    public boolean isEmpty() {
	return vocab.isEmpty();
    }

    // Return mapping from IDs to places.
    public TreeMap<String,Vector<ImagePlace>> lexPlaces() {
	TreeMap<String,Vector<ImagePlace>> allPlaces = 
	    	new TreeMap<String,Vector<ImagePlace>>();
	for (VocabKey key : vocab.keySet()) {
	    Vector<ImagePlace> places = vocabPlaces.get(key);
	    String id = vocabId.get(key);
	    // allPlaces.put(id, ImageUtil.cluster(places));
	    allPlaces.put(id, places);
	}
	for (VocabKey key : vocabNames) {
	    Vector<ImagePlace> places = vocabNamesPlaces.get(key);
	    String id = vocabNamesId.get(key);
	    // allPlaces.put(id, ImageUtil.cluster(places));
	    allPlaces.put(id, places);
	}
	return allPlaces;
    }

    // Convert to HTML. 
    public String toHtml() {
	StringBuffer buf = new StringBuffer();
	for (VocabKey key : vocab.keySet()) {
	    Vector<ImagePlace> places = vocabPlaces.get(key);
	    String id = vocabId.get(key);
	    if (!places.isEmpty()) 
		buf.append("<div class=\"focusable\" id=\"" + id + "\">");
	    buf.append("<p>");
	    buf.append("<span class=\"al\">" + 
		    TransHelper.toUnicode(key.al) + "</span> ");
	    buf.append("<span class=\"fo\">" + key.fo + "</span> ");
	    buf.append("<span class=\"tr\">" + key.tr + "</span>");
	    buf.append("</p>\n");
	    TreeSet<VocabUse> uses = vocab.get(key);
	    if (uses.size() > 0) {
		buf.append("<ul class=\"vocab\">\n");
		for (VocabUse use : uses) {
		    buf.append("<li>");
		    if (!use.al.equals(""))
			buf.append("<span class=\"al\">" + 
				TransHelper.toUnicode(use.al) + "</span>");
		    if (!use.fo.equals("")) {
			if (!use.al.equals(""))
			    buf.append(" ");
			buf.append("<span class=\"fo\">" + use.fo + "</span>");
		    }
		    if (!use.tr.equals("")) {
			if (!use.al.equals("") || !use.fo.equals(""))
			    buf.append(" ");
			buf.append("<span class=\"tr\">" + use.tr + "</span>");
		    }
		    buf.append("</li>\n");
		}
		buf.append("</ul>\n");
	    }
	    if (!places.isEmpty()) 
		buf.append("</div>\n");
	}
	buf.append("<h1>Names</h1>\n");
	for (VocabKey key : vocabNames) {
	    Vector<ImagePlace> places = vocabNamesPlaces.get(key);
	    String id = vocabNamesId.get(key);
	    if (!places.isEmpty()) 
		buf.append("<div class=\"focusable\" id=\"" + id + "\">");
	    buf.append("<p>");
	    buf.append("<span class=\"al\">" +
		    TransHelper.toUnicode(key.al) + "</span> ");
	    buf.append("<span class=\"tr\">" + key.tr + "</span>");
	    buf.append("</p>\n");
	    if (!places.isEmpty())
		buf.append("</div>\n");
	}
	return buf.toString();
    }

}
