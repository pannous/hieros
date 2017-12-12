/***************************************************************************/
/*                                                                         */
/*  TrMap.java                                                             */
/*                                                                         */
/*  Copyright (c) 2008 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Mapping on transliteration characters,
// mapping ordinary Latin chars to Egyptological characters
// in font, represented by a .txt file.
// In this file, each line is one of:
// 1) <character> <lower_and_upper_letter_code_in_font>
// 2) <character> <lower_letter_code_in_font> <upper_letter_code_in_font>
// 3) line starting with % 
// 4) completely empty line 
// The character codes can be in decimal, octal or hexadecimal.
// The lines of forms 3) and 4) are ignored.

package nederhof.align;

import java.io.*;
import java.net.*;
import java.util.regex.*;

import nederhof.fonts.*;
import nederhof.util.*;

public final class TrMap {

    // Size on which the mapping can have non-default values.
    private static final int asciiSize = 256;

    // Upper and lower case letters.
    private char[] lowerMapping;
    private char[] upperMapping;

    // Two types of line.
    private static final Pattern pat1 =
	Pattern.compile("^\\s*([a-zA-Z])\\s+([x0-9A-F]+)\\s*$");
    private static final Pattern pat2 =
	Pattern.compile("^\\s*([a-zA-Z])\\s+([x0-9A-F]+)\\s+([x0-9A-F]+)\\s*$");

    // Create empty mapping.
    public TrMap() {
	lowerMapping = new char[asciiSize];
	upperMapping = new char[asciiSize];
	for (char i = 0; i < asciiSize; i++) {
	    lowerMapping[i] = i;
	    if (i < 0x20)
		upperMapping[i] = i;
	    else
		upperMapping[i] = (char) (i - 0x20);
	}
    }

    // Create mapping from text file of mapping.
    public TrMap(String filename) {
	this();
	processFile(filename);
    }

    // Process file.
    private void processFile(String filename) {
	URL url = null;
	InputStream stream = null;
	Reader reader = null;
	try {
	    url = FileAux.fromBase(filename);
	    if (url == null)
		throw new MalformedURLException();
	    stream = url.openStream();
	    reader = new InputStreamReader(stream);
	} catch (MalformedURLException e) {
	    System.err.println("File not found " + filename);
	    return;
	} catch (IOException e) {
	    System.err.println("File not found " + filename);
	    return;
	}
	try {
	    BufferedReader in = new BufferedReader(reader);
	    String line = in.readLine();
	    while (line != null) {
		processLine(line, filename);
		line = in.readLine();
	    }
	    in.close();
	    reader.close();
	    stream.close();
	} catch (IOException e) {
	    System.err.println("In " + filename);
	    System.err.println(e.getMessage());
	}
    }

    // Process line.
    private void processLine(String line, String filename) throws IOException {
	Matcher m;
	m = pat1.matcher(line);
	try {
	    if (m.find()) {
		String latin = m.group(1);
		String codeLower = m.group(2);
		char latinNum = latin.charAt(0);
		char codeLowerNum = (char) Integer.decode(codeLower).intValue();
		lowerMapping[latinNum] = codeLowerNum;
		upperMapping[latinNum] = codeLowerNum;
	    } else {
		m = pat2.matcher(line);
		if (m.find()) {
		    String latin = m.group(1);
		    String codeLower = m.group(2);
		    String codeUpper = m.group(3);
		    char latinNum = latin.charAt(0);
		    char codeLowerNum = (char) Integer.decode(codeLower).intValue();
		    char codeUpperNum = (char) Integer.decode(codeUpper).intValue();
		    lowerMapping[latinNum] = codeLowerNum;
		    upperMapping[latinNum] = codeUpperNum;
		} else if (!line.matches("\\s*%.*") && !line.matches("\\s*")) {
		    System.err.println("Strange line in " + filename);
		    System.err.println(line);
		}
	    }
	} catch (NumberFormatException e) {
	    System.err.println("Strange line in " + filename);
	    System.err.println(line);
	}
    }

    // Transform string according to mapping.
    // At ^, take upper case for following char.
    public String mapString(String str) {
	StringBuffer buf = new StringBuffer(str);
	int i = 0;
	while (i < buf.length()) {
	    if (buf.charAt(i) == '^' && 
		    i + 1 < buf.length() &&
		    buf.charAt(i+1) < asciiSize) {
		buf.deleteCharAt(i);
		buf.setCharAt(i, upperMapping[buf.charAt(i)]);
	    } else if (buf.charAt(i) < asciiSize) 
		buf.setCharAt(i, lowerMapping[buf.charAt(i)]);
	    i++;
	}
	return buf.toString();
    }
}
