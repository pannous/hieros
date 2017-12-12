package nederhof.egyptian.trans;

import java.util.*;

// MdC representation of Egyptological transliteration,
// but without caret (only lower case letters).
public class TransLow implements Comparable<TransLow> {

    public static final char[] WORD_SEPS = new char[] {'-', '=', ' '};

    public static final char[] PUNCTUATION = new char[] {'-', '=', '.', ' '};

    public static final char[] WEAK = new char[] {'w', 'j', 'y'};

    // The bare string.
    private String s;

    // Constructor given string in MdC notation without carets.
    public TransLow(String s) {
	this.s = s;
    }
    // Constructor given MdC (possibly with carets, which are removed).
    public TransLow(TransMdc mdc) {
	String s = mdc.toString();
	StringBuffer buf = new StringBuffer();
	for (int i = 0; i < s.length(); i++)
	    if (s.charAt(i) != '^')
		buf.append(s.charAt(i));
        this.s = buf.toString();
    }

    public int length() {
	return s.length();
    }

    public char charAt(int i) {
	return s.charAt(i);
    }

    public int indexOf(char c, int i) {
	return s.indexOf(c, i);
    }

    public boolean equals(TransLow other) {
	return s.equals(other.s);
    }

    public boolean equals(TransMdc other) {
        return s.equals(other.toString());
    }

    public int hashCode() {
	return s.hashCode();
    }

    public String toString() {
        return s;
    }

    public TransLow substring(int i) {
	return new TransLow(s.substring(i));
    }

    public TransLow substring(int i, int j) {
	return new TransLow(s.substring(i, j));
    }

    public TransLow letter(int i) {
	return substring(i, i+1);
    }

    public int compareTo(TransLow other) {
	return s.compareTo(other.s);
    }

    public static TransLow concat(TransLow t1, TransLow t2) {
	return new TransLow(t1.s + t2.s);
    }

    // Consists only of separators.
    public boolean isWordSep() {
	return is(WORD_SEPS);
    }
    // Consists only of punctuation characters.
    public boolean isPunctuation() {
	return is(PUNCTUATION);
    }
    // Consists only of weak consonants.
    public boolean isWeak() {
	return is(WEAK);
    }
    // Consists only of character in array.
    private boolean is(char[] chars) {
	for (int i = 0; i < s.length(); i++) {
	    boolean found = false;
	    for (int j = 0; j < chars.length; j++) 
		if (chars[j] == s.charAt(i)) {
		    found = true;
		    break;
		}
	    if (!found)
		return false;
	}
	return true;
    }

    // Get list of variations, with sound change D versus d and T versus t.
    public List<TransLow> soundDerived() {
	return soundDerived(this.s, 0, false);
    }
    // Get list of variations, starting from position.
    private static List<TransLow> soundDerived(String s, int i, boolean changed) {
	List<TransLow> derived = new ArrayList<TransLow>();
	if (i < s.length()) {
	    switch (s.charAt(i)) {
		case 't': derived.addAll(soundDerived(s, i, 'T')); break;
		case 'T': derived.addAll(soundDerived(s, i, 't')); break; 
		case 'd': derived.addAll(soundDerived(s, i, 'D')); break;
		case 'D': derived.addAll(soundDerived(s, i, 'd')); break;
	    }
	    derived.addAll(soundDerived(s, i+1, changed));
	} else if (changed) 
	    derived.add(new TransLow(s));
	return derived;
    }
    // Get list of variations, after making local change.
    private static List<TransLow> soundDerived(String s, int i, char change) {
	char[] chars = s.toCharArray();
	chars[i] = change;
	s = new String(chars);
	return soundDerived(s, i+1, true);
    }

    // Convert D to d and T to t.
    public TransLow soundNormalized() {
	StringBuffer normal = new StringBuffer();
	for (int i = 0; i < s.length(); i++) 
	    switch (s.charAt(i)) {
		case 'T': normal.append("t"); break;
		case 'D': normal.append("d"); break;
		default: normal.append(s.charAt(i)); break;
	    }
	return new TransLow(normal.toString());
    }

}
