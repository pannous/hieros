/***************************************************************************/
/*                                                                         */
/*  XmlAux.java                                                            */
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

// Conversions that have to do with XML.

package nederhof.util.xml;

public class XmlAux {

    // Replace entities by characters.
    public static String unescape(String s) {
        s = s.replaceAll("&quot;", "\"");
        s = s.replaceAll("&lt;", "<");
        s = s.replaceAll("&gt;", ">");
        s = s.replaceAll("&amp;", "&");
        return s;
    }

    // Replace special characters by XML entities.
    public static String escape(String s) {
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("\"", "&quot;");
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        return s;
    }

    // Replace some spaces by linebreaks, to make each line no more
    // than length. But do not break within XML tags.
    public static String breakLines(String s, int len) {
	char[] chars = s.toCharArray();
	boolean[] inTag = new boolean[chars.length];
	boolean within = false;
	for (int j = 0; j < chars.length; j++) {
	    if (chars[j] == '>') {
		inTag[j] = true;
		within = false;
	    } else if (chars[j] == '<') {
		inTag[j] = true;
		within = true;
	    } else
		inTag[j] = within;
	}

	int i = 0;
	int lastBreak = 0;
	int lastWhite = -1;
	while (i < chars.length) {
	    if (chars[i] == '\n') {
		lastBreak = i+1;
		lastWhite = -1;
	    } else if (Character.isWhitespace(chars[i]) && !inTag[i]) {
		lastWhite = i;
	    } 
	    if (lastWhite >= 0 && i - lastBreak >= len) {
		chars[lastWhite] = '\n';
		i = lastWhite;
		lastBreak = i+1;
		lastWhite = -1;
	    }
	    i++;
	}
	return new String(chars);
    }

    // Default line length for breaking up long lines.
    private static final int defaultLineLength = 70;

    public static String breakLines(String s) {
	return breakLines(s, defaultLineLength);
    }

}
