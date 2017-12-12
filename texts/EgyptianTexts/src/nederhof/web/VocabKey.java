package nederhof.web;

import java.util.*;

import nederhof.lexicon.egyptian.*;

// Identifies lemma in vocabular.
public class VocabKey implements Comparable<VocabKey> {

    public String al;
    public String alTrim;
    public String tr;
    public String fo;

    public VocabKey(String al, String tr, String fo) {
	this.al = al;
	this.alTrim = trim(al, fo);
	this.tr = tr;
	this.fo = fo;
    }

    // First compare al, then tr, then fo.
    public int compareTo(VocabKey other) {
	if (TranslitComparator.compareTranslit(alTrim, other.alTrim) == 0) {
	    if (tr.compareTo(other.tr) == 0) {
		return fo.compareTo(other.fo);
	    } else
		return tr.compareTo(other.tr);
	} else
	    return TranslitComparator.compareTranslit(alTrim, other.alTrim);
    }

    public boolean equals(VocabKey o) {
	if (o instanceof VocabKey) {
	    VocabKey other = (VocabKey) o;
	    return TranslitComparator.compareTranslit(al, other.al) == 0 &&
		tr.compareTo(other.tr) == 0 &&
		fo.compareTo(other.fo) == 0;
	} else
	    return false;
    }

    // Trim key to exclude feminine endings. Also weak consonant.
    public static String trim(String al, String fo) {
	if (fo.matches(".*(f).*")) {
	    if (al.length() > 2 && al.matches("wt"))
		al = al.replaceAll("wt$", "");
	    else if (al.length() > 2 && al.matches("yt"))
		al = al.replaceAll("yt$", "");
	    else
		al = al.replaceAll("t$", "");
	} if (fo.matches(".*[34]inf.*"))
	    al = al.replaceAll("j$", "");
	return al;
    }

}
