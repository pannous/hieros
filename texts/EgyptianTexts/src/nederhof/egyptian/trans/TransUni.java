package nederhof.egyptian.trans;

import java.text.*;
import java.util.*;

// Unicode representation of Egyptological transliteration.
public class TransUni {

    // The bare string.
    private String s;

    // The string broken up into graphemes.
    private List<String> graphemes;

    // Constructor from transliteration in MdC notation.
    public TransUni(TransMdc mdc) {
	s = mdcToUni(mdc);
	graphemes = graphemes(s);
    }

    // Get string.
    public String toString() {
	return s;
    }

    // Get grapheme.
    public String get(int i) {
	return graphemes.get(i);
    }

    // Get number of graphemes.
    public int len() {
	return graphemes.size();
    }

    // Convert from MdC to Unicode.
    private static String mdcToUni(TransMdc mdc) {
	String s = mdc.toString();
	StringBuffer buf = new StringBuffer();
	int i = 0;
	boolean upper = false;
	while (i < s.length()) {
	    char c = s.charAt(i);
	    if (c == '^')
		upper = true;
	    else {
		buf.append(mdcToUni(c, upper));
		upper = false;
	    }
	    i++;
	}
	return buf.toString();
    }

    // Convert MdC letter to Unicode letter.
    private static String mdcToUni(char c, boolean upper) {
	switch (c) {
	    case 'A': return upper ? "\uA722" : "\uA723";
	    case 'j': return upper ? "J" : "j"; 
	    case 'i': return upper ? "I\u0313" : "i\u0313"; 
	    case 'y': return upper ? "Y" : "y"; 
	    case 'a': return upper ? "\uA724" : "\uA725"; 
	    case 'w': return upper ? "W" : "w"; 
	    case 'b': return upper ? "B" : "b"; 
	    case 'p': return upper ? "P" : "p"; 
	    case 'f': return upper ? "F" : "f"; 
	    case 'm': return upper ? "M" : "m"; 
	    case 'n': return upper ? "N" : "n"; 
	    case 'r': return upper ? "R" : "r"; 
	    case 'l': return upper ? "L" : "l"; 
	    case 'h': return upper ? "H" : "h"; 
	    case 'H': return upper ? "\u1E24" : "\u1E25"; 
	    case 'x': return upper ? "\u1E2A" : "\u1E2B"; 
	    case 'X': return upper ? "H\u0331" : "\u1E96"; 
	    case 'z': return upper ? "Z" : "z"; 
	    case 's': return upper ? "S" : "s"; 
	    case 'S': return upper ? "\u0160" : "\u0161"; 
	    case 'q': return upper ? "Q" : "q"; 
	    case 'K': return upper ? "\u1E32" : "\u1E33"; 
	    case 'k': return upper ? "K" : "k"; 
	    case 'g': return upper ? "G" : "g"; 
	    case 't': return upper ? "T" : "t"; 
	    case 'T': return upper ? "\u1E6E" : "\u1E6F"; 
	    case 'd': return upper ? "D" : "d"; 
	    case 'D': return upper ? "\u1E0E" : "\u1E0F"; 
	    default: return "" + c; 
	}
    }

    // Convert to MdC.
    public TransMdc toMdc() {
	StringBuffer buf = new StringBuffer();
	for (String g : graphemes) {
	    if (g.length() == 1) {
		char c = g.charAt(0);
		switch (c) {
		    case '\uA722': buf.append("^A"); break;
		    case '\uA723': buf.append("A"); break;
		    case 'J': buf.append("^j"); break;
		    case 'j': buf.append("j"); break;
		    case 'I': buf.append("^i"); break;
		    case 'i': buf.append("i"); break;
		    case 'Y': buf.append("^y"); break;
		    case 'y': buf.append("y"); break;
		    case '\uA724': buf.append("^a"); break;
		    case '\uA725': buf.append("a"); break;
		    case 'W': buf.append("^w"); break;
		    case 'w': buf.append("w"); break;
		    case 'B': buf.append("^b"); break;
		    case 'b': buf.append("b"); break;
		    case 'P': buf.append("^p"); break;
		    case 'p': buf.append("p"); break;
		    case 'F': buf.append("^f"); break;
		    case 'f': buf.append("f"); break;
		    case 'M': buf.append("^m"); break;
		    case 'm': buf.append("m"); break;
		    case 'N': buf.append("^n"); break;
		    case 'n': buf.append("n"); break;
		    case 'R': buf.append("^r"); break;
		    case 'r': buf.append("r"); break;
		    case 'L': buf.append("^l"); break;
		    case 'l': buf.append("l"); break;
		    case 'H': buf.append("^h"); break;
		    case 'h': buf.append("h"); break;
		    case '\u1E24': buf.append("^H"); break;
		    case '\u1E25': buf.append("H"); break;
		    case '\u1E2A': buf.append("^x"); break;
		    case '\u1E2B': buf.append("x"); break;
		    case '\u1E96': buf.append("X"); break;
		    case 'Z': buf.append("^z"); break;
		    case 'z': buf.append("z"); break;
		    case 'S': buf.append("^s"); break;
		    case 's': buf.append("s"); break;
		    case '\u0160': buf.append("^S"); break;
		    case '\u0161': buf.append("S"); break;
		    case 'Q': buf.append("^q"); break;
		    case 'q': buf.append("q"); break;
		    case '\u1E32': buf.append("^K"); break;
		    case '\u1E33': buf.append("K"); break;
		    case 'K': buf.append("^k"); break;
		    case 'k': buf.append("k"); break;
		    case 'G': buf.append("^g"); break;
		    case 'g': buf.append("g"); break;
		    case 'T': buf.append("^t"); break;
		    case 't': buf.append("t"); break;
		    case '\u1E6E': buf.append("^T"); break;
		    case '\u1E6F': buf.append("T"); break;
		    case 'D': buf.append("^d"); break;
		    case 'd': buf.append("d"); break;
		    case '\u1E0E': buf.append("^D"); break;
		    case '\u1E0F': buf.append("D"); break;
		    default: buf.append(c); break;
		}
	    } else if (g.equals("I\u0313"))
		buf.append("^i");
	    else if (g.equals("i\u0313"))
		buf.append("i");
	    else if (g.equals("H\u0331"))
		buf.append("^X");
	    else
		buf.append(g);
	}
	return new TransMdc(buf.toString());
    }

    // Decompose string into graphemes.
    private static List<String> graphemes(String s) {
	List<String> graphemes = new ArrayList<String>(s.length());
	BreakIterator it = BreakIterator.getCharacterInstance();
	it.setText(s);
	for (int i = it.first(), j = it.next(); j != BreakIterator.DONE; i = j, j = it.next()) 
		graphemes.add(s.substring(i, j));
	return graphemes;
    }

    // Testing.
    public static void main(String[] args) {
	TransUni t = new TransUni(new TransMdc("rhXH^X^H"));
	System.out.println(t.toMdc());
    }

}
