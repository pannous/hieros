/***************************************************************************/
/*                                                                         */
/*  TransHelper.java                                                       */
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

// Some auxiliary methods for transliteration.

package nederhof.interlinear.egyptian;

import java.awt.*;
import java.util.*;

import nederhof.fonts.*;
import nederhof.util.*;

public class TransHelper {

    // Transliteration font for lower and upper case.
    public static Font translitLower(int translitStyle, float translitSize) {
        switch (translitStyle) {
            case Font.BOLD:
                return FontUtil.font("data/fonts/TranslitLowerSB.ttf").
                    deriveFont(translitSize);
            case Font.ITALIC:
                return FontUtil.font("data/fonts/TranslitLowerI.ttf").
                    deriveFont(translitSize);
            case Font.BOLD + Font.ITALIC:
                return FontUtil.font("data/fonts/TranslitLowerIB.ttf").
                    deriveFont(translitSize);
            default:
                return FontUtil.font("data/fonts/TranslitLowerS.ttf").
                    deriveFont(translitSize);
        }
    }
    public static Font translitUpper(int translitStyle, float translitSize) {
        switch (translitStyle) {
            case Font.BOLD:
                return FontUtil.font("data/fonts/TranslitUpperSB.ttf").
                    deriveFont(translitSize);
            case Font.ITALIC:
                return FontUtil.font("data/fonts/TranslitUpperI.ttf").
                    deriveFont(translitSize);
            case Font.BOLD + Font.ITALIC:
                return FontUtil.font("data/fonts/TranslitUpperIB.ttf").
                    deriveFont(translitSize);
            default:
                return FontUtil.font("data/fonts/TranslitUpperS.ttf").
                    deriveFont(translitSize);
        }
    }

    // In paragraphs, there can be transliteration with ^ for upper case.
    // Distinguish this into two styles, for styled editor.
    public static Vector splitTransLowerUpper(Vector pars) {
	Vector newPars = new Vector();
	for (int i = 0; i < pars.size(); i++) {
	    Vector par = (Vector) pars.get(i);
	    Vector newPar = new Vector();
	    for (int j = 0; j < par.size(); j++) {
		Object[] s = (Object[]) par.get(j);
		String kind = (String) s[0];
		if (kind.equals("trans")) {
		    String info = (String) s[1];
		    newPar.addAll(lowerUpperParts(info));
		} else
		    newPar.add(s);
	    }
	    newPars.add(newPar);
	}
	return newPars;
    }

    // There are lower and upper case letters, plus
    // spaces, which can be either.
    private static final int LOWER = 0;
    private static final int UPPER = 1;
    private static final int EITHER = 2;

    // For string of transliteration, split up into style elements for 
    // alternating lower and upper case.
    public static Vector<Object[]> lowerUpperParts(String s) {
	Vector<Object[]> parts = new Vector<Object[]>();
	String buffer = "";
	int kind = EITHER; // in buffer is lower/upper/either?
	for (int i = 0; i < s.length(); i++) {
	    char c = s.charAt(i);
	    if (Character.isWhitespace(c)) {
		buffer += c;
	    } else if (c == '^') {
		if (i < s.length()-1) {
		    i++;
		    c = s.charAt(i);
		    if (!buffer.equals("") && kind == LOWER) {
			parts.add(new Object[] {"translower", buffer});
			buffer = "";
		    }
		    buffer += c;
		    kind = UPPER;
		}
	    } else {
		if (!buffer.equals("") && kind == UPPER) {
		    parts.add(new Object[] {"transupper", buffer});
		    buffer = "";
		}
		buffer += c;
		kind = LOWER;
	    }
	}
	if (!buffer.equals("")) {
	    if (kind != UPPER) 
		parts.add(new Object[] {"translower", buffer});
	    else
		parts.add(new Object[] {"transupper", buffer});
	}
	return parts;
    }

    // Number of characters in transliteration string, excluding ^.
    public static int charLength(String s) {
	int n = 0;
	for (int i = 0; i < s.length(); i++) {
	    char c = s.charAt(i);
	    if (Character.isWhitespace(c)) 
		; // (not counting) n++;
	    else if (c == '^') {
		if (i < s.length()-1) {
		    i++;
		    n++;
		}
	    } else
		n++;
	}
	return n;
    }

    // Merge split lower and upper case transliteration styles, with ^ for
    // upper case.
    public static Vector mergeTransLowerUpper(Vector par) {
	Vector newPar = new Vector();
	String buffer = "";
	for (int j = 0; j < par.size(); j++) {
	    Object[] s = (Object[]) par.get(j);
	    String kind = (String) s[0];
	    if (kind.equals("translower")) {
		String info = (String) s[1];
		buffer += info;
	    } else if (kind.equals("transupper")) {
		String info = (String) s[1];
		for (int i = 0; i < info.length(); i++) {
		    char c = info.charAt(i);
		    if (Character.isWhitespace(c)) 
			buffer += c;
		    else
			buffer += "^" + c;
		}
	    } else {
		if (!buffer.equals("")) {
		    newPar.add(new Object[] {"trans", buffer});
		    buffer = "";
		}
		newPar.add(s);
	    }
	}
	if (!buffer.equals(""))
	    newPar.add(new Object[] {"trans", buffer});
	return newPar;
    }

    // Same as above, but guaranteed there are only translower and transupper.
    public static String transLowerUpperToString(Vector par) {
	String buffer = "";
	for (int j = 0; j < par.size(); j++) {
	    Object[] s = (Object[]) par.get(j);
	    String kind = (String) s[0];
	    String info = (String) s[1];
	    if (kind.equals("translower")) {
		buffer += info;
	    } else if (kind.equals("transupper")) {
		for (int i = 0; i < info.length(); i++) {
		    char c = info.charAt(i);
		    if (Character.isWhitespace(c)) 
			buffer += c;
		    else
			buffer += "^" + c;
		}
	    } 
	}
	return buffer;
    }

    // Merge split lower and upper case transliteration styles, with ^ for
    // upper case. Assume the argument only contains transliteration.
    public static String simpleMergeTransLowerUpper(Vector par) {
	Vector v = mergeTransLowerUpper(par);
	if (v.size() == 0)
	    return "";
	else
	    return (String) ((Object[]) v.get(0))[1];
    }

    // Split string in two halves at split point, which is
    // number of characters (not spaces) in first half.
    public static void splitTrans(String s, int pos, StringBuffer buf1, StringBuffer buf2) {
	int n = 0;
	int i = 0;
	while (i < s.length()) {
	    char c = s.charAt(i);
	    if (Character.isWhitespace(c)) 
		;
	    else {
		n++;
		if (n > pos)
		    break;
		if (c == '^' && i < s.length()-1) 
		    i++;
	    }
	    i++;
	}
	String s1 = s.substring(0, i);
	String s2 = s.substring(i);
	buf1.append(s1);
	buf2.append(s2);
    }
    // Split string in two halves at split point, which is
    // number of characters (including spaces) in first half.
    public static void splitTransWithSpaces(String s, 
	    int pos, StringBuffer buf1, StringBuffer buf2) {
	int n = 0;
	int i = 0;
	while (i < s.length()) {
	    char c = s.charAt(i);
	    n++;
	    if (n > pos)
		break;
	    if (c == '^' && i < s.length()-1) 
		i++;
	    i++;
	}
	String s1 = s.substring(0, i);
	String s2 = s.substring(i);
	buf1.append(s1);
	buf2.append(s2);
    }

    // Split string into letters (with ^ groups with following letter).
    public static Vector<String> letters(String s) {
	Vector<String> letters = new Vector<String>();
	int i = 0;
	while (i < s.length()) {
	    char c = s.charAt(i);
	    if (Character.isWhitespace(c))
		;
	    else if (c == '^' && i < s.length()-1) {
		letters.add(s.substring(i, i+2));
		i++;
	    } else
		letters.add(s.substring(i, i+1));
	    i++;
	}
	return letters;
    }

    // Find length of first word (point up to first space or end of string).
    public static int firstWordLength(String s) {
	int i = 0;
	int n = 0;
	while (i < s.length()) {
	    char c = s.charAt(i);
	    if (Character.isWhitespace(c))
		return n;
	    else if (c == '^' && i < s.length()-1) 
		i++;
	    i++;
	    n++;
	}
	return n;
    }

    // Convert MdC encoding to Unicode.
    public static String toUnicode(String s, boolean upper) {
	StringBuffer buf = new StringBuffer();
	for (int i = 0; i < s.length(); i++) 
	    switch (s.charAt(i)) {
		case 'A': buf.append(upper ? "\uA722" : "\uA723"); break;
		case 'j': buf.append(upper ? "J" : "j"); break;
		case 'i': buf.append(upper ? "I\u0313" : "i\u0313"); break;
		case 'y': buf.append(upper ? "Y" : "y"); break;
		case 'a': buf.append(upper ? "\uA724" : '\uA725'); break;
		case 'w': buf.append(upper ? "W" : "w"); break;
		case 'b': buf.append(upper ? "B" : "b"); break;
		case 'p': buf.append(upper ? "P" : "p"); break;
		case 'f': buf.append(upper ? "F" : "f"); break;
		case 'm': buf.append(upper ? "M" : "m"); break;
		case 'n': buf.append(upper ? "N" : "n"); break;
		case 'r': buf.append(upper ? "R" : "r"); break;
		case 'l': buf.append(upper ? "L" : "l"); break;
		case 'h': buf.append(upper ? "H" : "h"); break;
		case 'H': buf.append(upper ? "\u1E24" : "\u1E25"); break;
		case 'x': buf.append(upper ? "\u1E2A" : "\u1E2B"); break;
		case 'X': buf.append(upper ? "H\u0331" : "\u1E96"); break;
		case 'z': buf.append(upper ? "Z" : "z"); break;
		case 's': buf.append(upper ? "S" : "s"); break;
		case 'S': buf.append(upper ? "\u0160" : "\u0161"); break;
		case 'q': buf.append(upper ? "Q" : "q"); break;
		case 'K': buf.append(upper ? "\u1E32" : "\u1E33"); break;
		case 'k': buf.append(upper ? "K" : "k"); break;
		case 'g': buf.append(upper ? "G" : "g"); break;
		case 't': buf.append(upper ? "T" : "t"); break;
		case 'T': buf.append(upper ? "\u1E6E" : "\u1E6F"); break;
		case 'd': buf.append(upper ? "D" : "d"); break;
		case 'D': buf.append(upper ? "\u1E0E" : "\u1E0F"); break;
		// case 'D': buf.append(upper ? "D\u0331" : "d\u0331"); break;
		default: buf.append(s.charAt(i)); break;
	    }
	return buf.toString();
    }
    // As above, but first split into lower and upper case substrings.
    public static String toUnicode(String s) {
	StringBuffer buf = new StringBuffer();
	Vector<Object[]> parts = lowerUpperParts(s);
	for (Object[] part : parts) {
	    String kind = (String) part[0];
	    String data = (String) part[1];
	    buf.append(toUnicode(data, kind.equals("transupper")));
	}
	return buf.toString();
    }

}
