/***************************************************************************/
/*                                                                         */
/*  ResScanner.java                                                        */
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

package nederhof.res.editor;

import javax.swing.text.*;

import nederhof.res.*;

// Recognizes a substring that is or might be (!) RES.

public class ResScanner {

    // The complete text.
    private JTextComponent text;

    // Are XML entities allowed for strings?
    private boolean xmlSensitive = true;

    // Initialize scanning.
    public ResScanner(JTextComponent text) {
	this.text = text;
    }

    // XML entities allowed for strings.
    public void setXmlSensitive(boolean allowed) {
	xmlSensitive = allowed;
    }

    //////////////////////////////////////////////////////////////////////
    // Utility methods.

    // Get character at specified location.
    // Return -1 if at end, or otherwise unsuccessful.
    private int get(int i) {
	if (i < 0)
	    return -1;
	try {
	    String substring = text.getText(i, 1);
	    return substring.charAt(0);
	} catch (BadLocationException e) {
	    return -1;
	} catch (IndexOutOfBoundsException e) {
	    return -1;
	}
    }

    // Get string between specified locations.
    private String get(int i, int j) {
	try {
	    return text.getText(i, j-i);
	} catch (BadLocationException e) {
	    return "";
	}
    }

    // Is next character a specified character?
    // If so, return next position.
    private int is(int i, char wanted) {
	if (i < 0)
	    return -1;
	int c = get(i);
	if (c == wanted)
	    return i + 1;
	else
	    return -1;
    }

    // Is next character one of two characters?
    private int is(int i, char wanted1, char wanted2) {
	if (i < 0)
	    return -1;
	int c = get(i);
	if (c == wanted1 || c == wanted2)
	    return i + 1;
	else
	    return -1;
    }

    // Is next character alphanumeric?
    private int isAlphaNumeric(int i) {
	if (i < 0)
	    return -1;
	int c = get(i);
	if (c < 0)
	    return -1;
	else if (Character.isLetterOrDigit((char) c))
	    return i + 1;
	else
	    return -1;
    }

    // Is next character alphanumeric or dot?
    private int isAlphaNumericOrDot(int i) {
	if (i < 0)
	    return -1;
	int c = get(i);
	if (c < 0)
	    return -1;
	else if (Character.isLetterOrDigit((char) c) || c == '.')
	    return i + 1;
	else
	    return -1;
    }

    // Is next character whitespace?
    private int isWhitespace(int i) {
	if (i < 0)
	    return -1;
	int c = get(i);
	if (c < 0)
	    return -1;
	else if (Character.isWhitespace((char) c))
	    return i + 1;
	else
	    return -1;
    }

    /////////////////////////////////////////////////////////////////
    // Forward reading.

    // A fragment.
    // Return end point if successful, otherwise -1.
    // A fragment is non-trivial if it contains a '-'.
    private int isFragment(int i, boolean maybeTrivial) {
	if (i < 0)
	    return -1;
	i = isWhitespaces(i);
	i = isOptBracketedList(i);
	i = isWhitespaces(i);
	i = isSwitches(i);
	return isHieroglyphic(i, maybeTrivial, maybeTrivial);
    }

    // Hieroglyphic.
    private int isHieroglyphic(int i, boolean maybeTrivial, boolean isOptional) {
	if (i < 0)
	    return -1;
	int j = isTopGroup(i);
	if (j < 0 && isOptional)
	    return i;
	else
	    i = j;
	j = is(i, '-');
	if (j >= 0)
	    return isHieroglyphicRest(j);
	else if (maybeTrivial)
	    return i;
	else
	    return -1;
    }

    // Rest of hieroglyphic after '-'.
    private int isHieroglyphicRest(int i) {
	if (i < 0)
	    return -1;
	i = isOptBracketedList(i);
	i = isWs(i);
	return isHieroglyphic(i, true, false);
    }

    // Top group without looking at operator binding.
    private int isTopGroup(int i) {
	if (i < 0)
	    return -1;
	int j = is(i, '(');
	if (j >= 0) {
	    i = isWs(j);
	    i = isTopGroup(i);
	    i = is(i, ')');
	    i = isWs(i);
	} else 
	    i = isBasicGroup(i);
	j = is(i, ':', '*');
	if (j >= 0) {
	    i = isOptBracketedList(j);
	    i = isWs(i);
	    return isTopGroup(i);
	} else
	    return i;
    }

    // Basic group.
    private int isBasicGroup(int i) {
	if (i < 0)
	    return -1;
	int j = is(i, '.');
	if (j >= 0) // point glyph
	    return isPointGlyphRest(j);
	j = is(i, '\"');
	if (j >= 0) { // short string
	    String head = get(j, j+3);
	    if (head.equals("\\\"\"") || 
		    head.equals("\\\\\"")) {
		return isNamedGlyphRest(j+3);
	    }
	    head = get(j, j+2);
	    if (head.matches("[^\"\\\\]\""))
		return isNamedGlyphRest(j+2);
	    return -1;
	}
	if (xmlSensitive) {
	    j = isEntity(i, "quot"); 
	    if (j >= 0) { // short string in XML
		String head = get(j, j+13);
		if (head.equals("\\&quot;&quot;"))
		    return isNamedGlyphRest(j+13);
		head = get(j, j+8);
		if (head.equals("\\\\&quot;"))
		    return isNamedGlyphRest(j+8);
		head = get(j, j+11);
		if (head.equals("&amp;&quot;"))
		    return isNamedGlyphRest(j+11);
		head = get(j, j+12);
		if (head.equals("&apos;&quot;"))
		    return isNamedGlyphRest(j+12);
		head = get(j, j+10);
		if (head.equals("&lt;&quot;") || 
			head.equals("&gt;&quot;"))
		    return isNamedGlyphRest(j+10);
		head = get(j, j+7);
		if (head.matches("[^\"\\\\]&quot;"))
		    return isNamedGlyphRest(j+7);
		return -1;
	    }
	}
	j = isAlphaNumericString(i);
	if (j >= 0) {
	    String head = get(i, j);
	    if (head.matches("([A-I]|[K-Z]|Aa|NL|NU)[0-9]+[a-z]?") ||
		    head.equals("open") || 
		    head.equals("close") || 
		    head.equals("10") ||
		    head.equals("100"))
		return isNamedGlyphRest(j);
	    else if (head.equals("empty"))
		return isEmptyGlyphRest(j);
	    else if (head.equals("stack"))
		return isStackRest(j);
	    else if (head.equals("insert"))
		return isInsertRest(j);
	    else if (head.equals("modify"))
		return isModifyRest(j);
	    else if (head.matches("[a-zA-Z]+"))
		return isNamedOrBoxRest(j);
	}
	return -1;
    }

    // The head of a basic group consisting of letters and digits.
    private int isAlphaNumericString(int i) {
	if (i < 0)
	    return -1;
	i = isAlphaNumeric(i);
	int j = i;
	while (j >= 0) {
	    i = j;
	    j = isAlphaNumeric(i);
	}
	return i;
    }

    // What follows head in named glyph.
    private int isNamedGlyphRest(int i) {
	if (i < 0)
	    return -1;
	i = isOptBracketedList(i);
	i = isWhitespaces(i);
	i = isNotes(i);
	return isSwitches(i);
    }

    // What follows "empty" in an empty glyph.
    private int isEmptyGlyphRest(int i) {
	if (i < 0)
	    return -1;
	i = isOptBracketedList(i);
	i = isWhitespaces(i);
	i = isOptNote(i);
	return isSwitches(i);
    }

    // What follows period in an empty glyph.
    private int isPointGlyphRest(int i) {
	if (i < 0)
	    return -1;
	i = isWhitespaces(i);
	i = isOptNote(i);
	return isSwitches(i);
    }

    // Is remainder of stack after "stack".
    private int isStackRest(int i) {
	if (i < 0)
	    return -1;
	i = isOptBracketedList(i);
	i = isWhitespaces(i);
	i = is(i, '(');
	i = isWs(i);
	i = isTopGroup(i);
	i = is(i, ',');
	i = isWs(i);
	i = isTopGroup(i);
	i = is(i, ')');
	return isWs(i);
    }
    // Is remainder of insert after "insert".
    private int isInsertRest(int i) {
	if (i < 0)
	    return -1;
	i = isOptBracketedList(i);
	i = isWhitespaces(i);
	i = is(i, '(');
	i = isWs(i);
	i = isTopGroup(i);
	i = is(i, ',');
	i = isWs(i);
	i = isTopGroup(i);
	i = is(i, ')');
	return isWs(i);
    }

    // Is remainder of modify after "modify".
    private int isModifyRest(int i) {
	if (i < 0)
	    return -1;
	i = isOptBracketedList(i);
	i = isWhitespaces(i);
	i = is(i, '(');
	i = isWs(i);
	i = isTopGroup(i);
	i = is(i, ')');
	return isWs(i);
    }

    // Is remainer of named glyph or box.
    private int isNamedOrBoxRest(int i) {
	if (i < 0)
	    return -1;
	i = isOptBracketedList(i);
	i = isWhitespaces(i);
	int j = is(i, '(');
	if (j >= 0) {
	    i = isWs(j);
	    j = is(i, ')');
	    if (j >= 0)
		i = isWhitespaces(j);
	    else {
		i = isHieroglyphic(i, true, false);
		i = is(i, ')'); 
		i = isWhitespaces(i);
	    }
	} 
	i = isNotes(i);
	return isSwitches(i);
    }

    // Notes.
    private int isNotes(int i) {
	while (i >= 0) {
	    int j = is(i, '^');
	    if (j >= 0) {
		i = isString(j);
		i = isOptBracketedList(i);
		i = isWhitespaces(i);
	    } else 
		return i;
	}
	return -1;
    }

    // Optional note.
    private int isOptNote(int i) {
	if (i < 0)
	    return -1;
	int j = is(i, '^');
	if (j >= 0) {
	    i = isString(j);
	    i = isOptBracketedList(i);
	    return isWhitespaces(i);
	} else
	    return i;
    }

    // String.
    private int isString(int i) {
	if (i < 0)
	    return -1;
	int j = is(i, '\"');
	if (j < 0 && xmlSensitive)
	    j = isEntity(i, "quot");
	while (j >= 0) {
	    int c = get(j);
	    if (c < 0)
		return -1;
	    else if (c == '\"')
		return j + 1;
	    else if (c == '\\')
		j += 2;
	    else if (c == ' ')
		j++;
	    else if (Character.isWhitespace((char) c))
		return -1;
	    else if (xmlSensitive && isEntity(j, "quot") >= 0)
		return isEntity(j, "quot");
	    else if (xmlSensitive && isEntity(j) >= 0)
		j = isEntity(j);
	    else
		j++;
	}
	return -1;
    }

    // XML entity.
    private int isEntity(int i, String entity) {
	int j = is(i, '&');
	if (j >= 0) {
	    String name = get(j, j+entity.length());
	    if (name.equals(entity)) {
		int k = is(j+entity.length(), ';');
		if (k >= 0)
		    return k;
	    }
	} 
	return -1;
    }

    // XML entity except quote.
    private int isEntity(int i) {
	int j = is(i, '&');
	if (j < 0)
	    return -1;
	j = isEntity(i, "amp");
	if (j >= 0)
	    return j;
	j = isEntity(i, "apos");
	if (j >= 0)
	    return j;
	j = isEntity(i, "lt");
	if (j >= 0)
	    return j;
	j = isEntity(i, "gt");
	if (j >= 0)
	    return j;
	return -1;
    }

    // Ws.
    private int isWs(int i) {
	if (i < 0)
	    return -1;
	i = isWhitespaces(i);
	return isSwitches(i);
    }

    // Switches.
    private int isSwitches(int i) {
	while (i >= 0) {
	    int j = is(i, '!');
	    if (j >= 0) {
		i = isOptBracketedList(j);
		i = isWhitespaces(i);
	    } else 
		return i;
	}
	return -1;
    }

    // Optional bracketed list.
    private int isOptBracketedList(int i) {
	if (i < 0)
	    return -1;
	int j = is(i, '[');
	if (j >= 0) {
	    i = isWhitespaces(j);
	    j = is(i, ']');
	    if (j >= 0)
		return j;
	    i = isArg(i);
	    while (i >= 0) {
		i = isWhitespaces(i);
		j = is(i, ']');
		if (j >= 0)
		    return j;
		i = is(i, ',');
		i = isWhitespaces(i);
		i = isArg(i);
	    }
	} 
	return i;
    }

    // Argument in bracketed list.
    private int isArg(int i) {
	if (i < 0)
	    return -1;
	i = isArgPart(i);
	int j = is(i, '=');
	if (j >= 0) 
	    return isArgPart(j);
	else
	    return i;
    }

    // Is part of argument, consisting of alphanumerics and dot.
    private int isArgPart(int i) {
	if (i < 0)
	    return -1;
	i = isAlphaNumericOrDot(i);
	int j = i;
	while (j >= 0) {
	    i = j;
	    j = isAlphaNumericOrDot(i);
	}
	return i;
    }

    // Is whitespace.
    private int isWhitespaces(int i) {
	if (i < 0)
	    return -1;
	int j = isWhitespace(i);
	while (j >= 0) {
	    i = j;
	    j = isWhitespace(i);
	}
	return i;
    }

    ///////////////////////////////////////////////////////////////////
    // Locating RES in text.

    // Look after position i. Skip input that looks like normal text.
    public Substring findResAfter(int i) {
	while (get(i) >= 0) {
	    i = skipNonRes(i);
	    int j = isFragment(i, false);
	    if (j >= 0) 
		return nonSpaceSubstring(i, j);
	    else 
		i++;
	}
	return null;
    }

    // Look immediately at position i. 
    public Substring findResAt(int i) {
	int j = isFragment(i, true);
	if (j >= 0)
	    return nonSpaceSubstring(i, j);
	else
	    return null;
    }

    // Skip material that does not look like RES, for the sake of performance.
    // We take one potential word at a time. If two words follow, we skip
    // ahead.
    private int skipNonRes(int i) {
	i = isWhitespaces(i);
	int j = isAlphaNumericString(i);
	while (j >= 0) {
	    j = isWhitespaces(j);
	    int k = isAlphaNumericString(j);
	    if (k >= 0) {
		i = j;
		j = k;
	    } else
		break;
	}
	return i;
    }

    // Return substring, removing leading and trailing whitespace.
    // If substring empty, return null.
    private Substring nonSpaceSubstring(int i, int j) {
	i = isWhitespaces(i);
	j = isWhitespacesBackward(j);
	if (i < j)
	    return new Substring(i, j);
	else
	    return null;
    }

    // Read backwards, skipping whitespace.
    private int isWhitespacesBackward(int i) {
	int j = isWhitespace(i-1);
	while (j >= 0) {
	    i--;
	    j = isWhitespace(i-1);
	}
	return i;
    }
}
