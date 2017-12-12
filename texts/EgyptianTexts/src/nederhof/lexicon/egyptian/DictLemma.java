package nederhof.lexicon.egyptian;

import java.util.*;
import java.util.regex.*;

// Lemma in dictionary.
public class DictLemma implements Comparable<DictLemma> {
    public String pos;
    public String keyhi;
    public String keyal;
    public String keytr;
    public String keyfo;
    public String keyco;
    public Vector<DictMeaning> meanings;

    public DictLemma(String pos, 
	    String keyhi, String keyal, String keytr, String keyfo, String keyco,
	    Vector<DictMeaning> meanings) {
	this.pos = pos;
	this.keyhi = keyhi;
	this.keyal = keyal;
	this.keytr = keytr;
	this.keyfo = keyfo;
	this.keyco = keyco;
	this.meanings = meanings;
    }
    public DictLemma() {
	this("", "", "", "", "", "", new Vector<DictMeaning>());
    }

    // Needs at least one of the main key values.
    public boolean isEmpty() {
	return keyhi.equals("") &&
	    keyal.equals("") &&
	    keytr.equals("");
    }

    // First look at pos.
    // Then look at al, then tr, then fo, then co, then hi.
    public int compareTo(DictLemma lem) {
	if (TranslitComparator.numsCompare(nums(pos), nums(lem.pos)) == 0) {
	    if (TranslitComparator.compareTranslit(keyal, lem.keyal) == 0) {
		if (keytr.compareTo(lem.keytr) == 0) {
		    if (keyfo.compareTo(lem.keyfo) == 0) {
			if (keyco.compareTo(lem.keyco) == 0) {
			    return keyhi.compareTo(lem.keyhi);
			} else
			    return keyco.compareTo(lem.keyco);
		    } else
			return keyfo.compareTo(lem.keyfo);
		} else
		    return keytr.compareTo(lem.keytr);
	    } else
		return TranslitComparator.compareTranslit(keyal, lem.keyal);
	} else
	    return TranslitComparator.numsCompare(nums(pos), nums(lem.pos));
    }

    public static Pattern numPat = Pattern.compile("(\\d+)");

    // Extract numbers from string.
    private int[] nums(String s) {
	Vector<Integer> nums = new Vector<Integer>();
	Matcher matcher = numPat.matcher(s);
	while (matcher.find()) {
	    try {
		nums.add(Integer.parseInt(matcher.group()));
	    } catch (Exception e) { /* ignore */ }
	}
	int[] numAr = new int[nums.size()];
	for (int i = 0; i < nums.size(); i++) 
	    numAr[i] = nums.get(i);
	return numAr;
    }
}
