/***************************************************************************/
/*                                                                         */
/*  ParseBuffer.java                                                       */
/*                                                                         */
/*  Copyright (c) 2006 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// String plus position, used for parsing REScode.

package nederhof.res;

class ParseBuffer {
    public String string;
    public int length;
    public int pos;

    // Constructor.
    public ParseBuffer(String str) {
	string = str;
	length = string.length();
	pos = 0;
    }

    // Nothing left in string?
    public boolean isEmpty() {
	return pos >= length;
    }

    // What remains after pos.
    public String remainder() {
	return string.substring(pos);
    }

    // Parsing error. Print first few symbols of remaining input.
    public void parseError(String message) {
	System.err.println(message + ":");
	final int PREF_LEN = 80;
	int endPos = Math.min(length, pos + PREF_LEN);
	String pref = string.substring(pos, endPos);
	System.err.println(pref);
    }

    // Jump to first non-whitespace symbol.
    public void readToNonspace() {
	while (!isEmpty() && 
		Character.isWhitespace(string.charAt(pos))) 
	    pos++;
    }

    // Jump to first whitespace symbol.
    public void readToSpace() {
	while (!isEmpty() &&
		!Character.isWhitespace(string.charAt(pos)))
	    pos++;
    }

    // Jump to after character 'e'.
    // This marks end of one chunk of REScode.
    public void readToEnd() {
	while (!isEmpty() && string.charAt(pos) != 'e')
	    pos++;
	if (!isEmpty())
	    pos++;
    }

    // Read given single non-space character.
    // Return whether successful.
    public boolean readChar(char c) {
	int oldPos = pos;
	readToSpace();
	if (pos == oldPos+1 && string.charAt(oldPos) == c) {
	    readToNonspace();
	    return true;
	} else {
	    pos = oldPos;
	    return false;
	}
    }

    // As above, but no following whitespace is read.
    public boolean readSingleChar(char c) {
	if (!isEmpty() && string.charAt(pos) == c) {
	    pos++;
	    return true;
	} else 
	    return false;
    }

    // Much as above, but do not increase position.
    public boolean peekChar(char c) {
	return !isEmpty() && string.charAt(pos) == c;
    }

    // Read direction, which is int. Return -1 if there is none.
    public int readDirection() {
	int oldPos = pos;
	int dir;
	readToSpace();
	if (pos != oldPos+3) {
	    pos = oldPos;
	    return -1;
	} else if (string.startsWith("hlr", oldPos))
	    dir = ResValues.DIR_HLR;
	else if (string.startsWith("hrl", oldPos))
	    dir = ResValues.DIR_HRL;
	else if (string.startsWith("vlr", oldPos))
	    dir = ResValues.DIR_VLR;
	else if (string.startsWith("vrl", oldPos))
	    dir = ResValues.DIR_VRL;
	else {
	    pos = oldPos;
	    return -1;
	}
	readToNonspace();
	return dir;
    }

    // Read int. If fails, return Integer.MAX_VALUE.
    public int readInt() {
	int oldPos = pos;
	readToSpace();
	if (pos <= oldPos)
	    return Integer.MAX_VALUE;
	int i = Integer.MAX_VALUE;
	try {
	    i = Integer.parseInt(string.substring(oldPos, pos));
	} catch (NumberFormatException e) {
	    pos = oldPos;
	    return Integer.MAX_VALUE;
	}
	readToNonspace();
	return i;
    }

    // Read long. If fails, return Long.MAX_VALUE.
    public long readLong() {
	int oldPos = pos;
	readToSpace();
	if (pos <= oldPos)
	    return Long.MAX_VALUE;
	long i = Long.MAX_VALUE;
	try {
	    i = Long.parseLong(string.substring(oldPos, pos));
	} catch (NumberFormatException e) {
	    pos = oldPos;
	    return Long.MAX_VALUE;
	}
	readToNonspace();
	return i;
    }

    // Try to read string.
    // String is " followed by characters (at least one) followed by ",
    // followed by end of input or white space.
    // Return string. If unsuccessful, return null.
    public String readString() {
	int end = readAcrossString();
	if (end >= pos + 3) {
	    String sub = string.substring(pos, end);
	    pos = end;
	    readToNonspace();
	    return sub;
	} else 
	    return null;
    }

    // Read from beginning to past end of string. 
    // Return end position or -1 if failed.
    // Internal in string there can be printable, \" or \\.
    private int readAcrossString() {
	int newPos = pos;
	if (string.charAt(newPos) == '\"') {
	    newPos++;
	    while (newPos < length) {
		if (string.charAt(newPos) == '\"') {
		    newPos++;
		    if (newPos >= length || 
			    Character.isWhitespace(string.charAt(newPos)))
			return newPos;
		    else
			return -1; // premature end of string
		} else if (string.charAt(newPos) == '\\') {
		    newPos++;
		    if (string.charAt(newPos) == '\"' || 
			    string.charAt(newPos) == '\\')
			newPos++;
		    else
			return -1; // \ not followed by " or \ 
		} else if (string.charAt(newPos) == ' ' ||
			!Character.isWhitespace(string.charAt(newPos)))
		    newPos++;
		else
		    return -1; // non-printable in string
	    }
	    return -1; // unfinished string
	} else
	    return -1; // no start of string
    }
}
