/***************************************************************************/
/*                                                                         */
/*  LatinEntities.java                                                     */
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

// Maps between ASCII characters and entity names in ISOlat1.

package nederhof.util.xml;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class LatinEntities {

    private static char firstEntity = 160;
    // Entity names between 160 and 255.
    private static String[] entityNames = new String[] {
	"nbsp",
	"iexcl",
	"cent",
	"pound",
	"curren",
	"yen",
	"brvbar",
	"sect",
	"uml",
	"copy",
	"ordf",
	"laquo",
	"not",
	"shy",
	"reg",
	"macr",
	"deg",
	"plusmn",
	"sup2",
	"sup3",
	"acute",
	"micro",
	"para",
	"middot",
	"cedil",
	"sup1",
	"ordm",
	"raquo",
	"frac14",
	"frac12",
	"frac34",
	"iquest",
	"Agrave",
	"Aacute",
	"Acirc",
	"Atilde",
	"Auml",
	"Aring",
	"AElig",
	"Ccedil",
	"Egrave",
	"Eacute",
	"Ecirc",
	"Euml",
	"Igrave",
	"Iacute",
	"Icirc",
	"Iuml",
	"ETH",
	"Ntilde",
	"Ograve",
	"Oacute",
	"Ocirc",
	"Otilde",
	"Ouml",
	"times",
	"Oslash",
	"Ugrave",
	"Uacute",
	"Ucirc",
	"Uuml",
	"Yacute",
	"THORN",
	"szlig",
	"agrave",
	"aacute",
	"acirc",
	"atilde",
	"auml",
	"aring",
	"aelig",
	"ccedil",
	"egrave",
	"eacute",
	"ecirc",
	"euml",
	"igrave",
	"iacute",
	"icirc",
	"iuml",
	"eth",
	"ntilde",
	"ograve",
	"oacute",
	"ocirc",
	"otilde",
	"ouml",
	"divide",
	"oslash",
	"ugrave",
	"uacute",
	"ucirc",
	"uuml",
	"yacute",
	"thorn",
	"yuml"};

    // Mapping from entities to characters.
    private static HashMap entityToChar = new HashMap();
    static {
	for (char i = firstEntity; i-firstEntity < entityNames.length; i++) {
	    entityToChar.put(entityNames[i-firstEntity], new Character(i));
	}
    }

    // Get character for entity name. Or -1 if none.
    public static int getChar(String name) {
	Character c = (Character) entityToChar.get(name);
	if (c == null)
	    return -1;
	else
	    return c.charValue();
    }

    public static String charToEntity(char c) {
	if (c >= firstEntity && c < firstEntity + entityNames.length) 
	    return "&" + entityNames[c-firstEntity] + ";";
	else 
	    return String.valueOf(c);
    }

    // Pattern of entity,
    public static final Pattern entityPat = Pattern.compile("&([a-zA-Z]+[0-9]*);");

    public static String removeEntities(String s) {
	StringBuffer buf = new StringBuffer();
	Matcher matcher = entityPat.matcher(s);
	while (matcher.find()) {
	    String name = matcher.group(1);
	    int i = getChar(name);
	    if (i >= 0)
		matcher.appendReplacement(buf, "" + (char) i);
	}
	matcher.appendTail(buf);
	return buf.toString();
    }

    public static String introduceEntities(String s) {
	StringBuffer out = new StringBuffer();
	for (int i = 0; i < s.length(); i++) {
	    char thisChar = s.charAt(i);
	    out.append(charToEntity(thisChar));
	}
	return out.toString();
    }

    public static void removeEntities(String inFileName, String outFileName) {
	File inFile = new File(inFileName);
	File outFile = new File(outFileName);
	try {
	    LineNumberReader in =
		new LineNumberReader(new FileReader(inFile));
	    PrintWriter out =
		new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
	    removeEntities(in, out);
	    in.close();
	    out.close();
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void removeEntities(LineNumberReader in, PrintWriter out)
	    throws IOException {
	String line = in.readLine();
	while (line != null) {
	    line = removeEntities(line);
	    out.println(line);
	    line = in.readLine();
	}
    }

    public static void main(String[] args) {
	removeEntities(args[0], args[1]);
    }

}
