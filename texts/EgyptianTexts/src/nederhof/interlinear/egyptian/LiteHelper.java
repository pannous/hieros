/***************************************************************************/
/*                                                                         */
/*  LiteHelper.java                                                        */
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

// Helper for the lite format.

package nederhof.interlinear.egyptian;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import nederhof.interlinear.*;
import nederhof.interlinear.labels.*;
import nederhof.util.xml.*;

public class LiteHelper {

    private static final Pattern longPattern =
	Pattern.compile(".*[\\s@\\^].*");
    private static final Pattern numberPattern =
	Pattern.compile(".*[0-9].*");

    // A longer form is needed if some problematic characters
    // are included.
    public static boolean requiresLongTagEncoding(String s) {
	Matcher m1 = longPattern.matcher(s);
	return m1.find();
    }

    // For ID, furthermore, there must be at least one digit.
    public static boolean idRequiresLongTagEncoding(String s) {
	Matcher m1 = longPattern.matcher(s);
	Matcher m2 = numberPattern.matcher(s);
	return m1.find() || !m2.find();
    }

    private static final String numTag = 
	"[^\\s@\\^<>\"]*[0-9][^\\s@\\^<>\"]*";
    private static final String stringTag = 
	"[^<>\"]+";
    private static final String alphaTag = 
	"[^\\s@\\^<>\"]+";
    // <@3> -> <pos id="3"/>
    private static final Pattern shortTag1 =
	Pattern.compile("<@(" + numTag + ")>");
    // <@"3"> -> <pos id="3"/>
    private static final Pattern shortTag2 =
	Pattern.compile("<@\"(" + stringTag + ")\">");
    // <23> -> <coord id="23"/>
    private static final Pattern shortTag3 =
	Pattern.compile("<(" + numTag + ")>");
    // <"23"> -> <coord id="23"/>
    private static final Pattern shortTag4 =
	Pattern.compile("<\"(" + stringTag + ")\">");
    // <23^pre> -> <pre id="23"/>
    private static final Pattern shortTag5 =
	Pattern.compile("<(" + numTag + ")\\^pre>");
    // <"23"^pre> -> <pre id="23"/>
    private static final Pattern shortTag6 =
	Pattern.compile("<\"(" + stringTag + ")\"\\^pre>");
    // <23^post> -> <post id="23"/>
    private static final Pattern shortTag7 =
	Pattern.compile("<(" + numTag + ")\\^post>");
    // <"23",post> -> <post id="23"/>
    private static final Pattern shortTag8 =
	Pattern.compile("<\"(" + stringTag + ")\",post>");

    // Replace short tags by long tags.
    public static String expandTags(String s) {
	Matcher m;
        m = shortTag1.matcher(s);
	s = m.replaceAll("<pos id=\"$1\"/>");
	m = shortTag2.matcher(s);
	s = m.replaceAll("<pos id=\"$1\"/>");
	m = shortTag3.matcher(s);
	s = m.replaceAll("<coord id=\"$1\"/>");
	m = shortTag4.matcher(s);
	s = m.replaceAll("<coord id=\"$1\"/>");
	m = shortTag5.matcher(s);
	s = m.replaceAll("<pre id=\"$1\"/>");
	m = shortTag6.matcher(s);
	s = m.replaceAll("<pre id=\"$1\"/>");
	m = shortTag7.matcher(s);
	s = m.replaceAll("<post id=\"$1\"/>");
	m = shortTag8.matcher(s);
	s = m.replaceAll("<post id=\"$1\"/>");
	return s;
    }

    // Write phrase, consisting of hi, al, tr, lx.
    // For the first three, there are a number of possibilities:
    // hi , al ; tr 
    // hi , al
    // hi ,; tr 
    // hi ,
    // al ; tr
    // al ;
    // tr
    public static void writePhrase(PrintWriter out,
	    Vector hi, Vector al, Vector tr, Vector lx) throws IOException {
	out.println();
	if (!hi.isEmpty()) {
	    out.println(ParsingHelper.writePartsHi(hi, true));
	    if (al.isEmpty() && !tr.isEmpty()) {
		out.println(",;");
		out.println(ParsingHelper.writePartsTr(tr, true));
	    } else {
		out.println(",");
		if (!al.isEmpty()) 
		    out.println(ParsingHelper.writePartsAl(al, true));
		if (!tr.isEmpty()) {
		    out.println(";");
		    out.println(ParsingHelper.writePartsTr(tr, true));
		}
	    }
	} else {
	    if (!al.isEmpty()) {
		out.println(ParsingHelper.writePartsAl(al, true));
		out.println(";");
	    }
	    if (!tr.isEmpty()) 
		out.println(ParsingHelper.writePartsTr(tr, true));
	}
	if (!lx.isEmpty()) {
	    out.println(":");
	    out.println(ParsingHelper.writePartsLx(lx, true));
	}
    }

    // For positions, print precedence.
    public static void writePosPrecedence(PrintWriter out, 
	    PosPrecedence precedence, Vector positions) throws IOException {
        for (int i = 0; i < positions.size(); i++) {
            String id1 = (String) positions.get(i);
            LinkedList links = precedence.getFrom(id1);
	    for (Iterator it = links.iterator(); it.hasNext(); ) {
		Link link = (Link) it.next();
		String type1 = link.type1;
		String id2 = link.id2;
		if (id1.compareTo(id2) != 0) {
		    id1 = XmlAux.escape(id1);
		    id2 = XmlAux.escape(id2);
		    out.println(
			    (type1.equals("start") ? "<> " : "<< ") +
			    ( idRequiresLongTagEncoding(id1)  ||
				    idRequiresLongTagEncoding(id2) ?
			      "\"" + id1 + "\" \"" + id2 + "\"" :
			      id1 + " " + id2) );
		}
	    }
        }
    }

    // precedence in lite.
    // <> 3 4 -> <prec id1="3" id2="4"/>
    // << 3 4 -> <prec id1="3" type="after" id2="4"/>
    private static final Pattern posAlignTag1 =
	Pattern.compile("<>\\s+(" + numTag + ")\\s+(" + numTag + ")");
    private static final Pattern posAlignTag2 =
	Pattern.compile("<>\\s+\"(" + stringTag + ")\"\\s+\"(" + stringTag + ")\"");
    private static final Pattern posAlignAfterTag1 =
	Pattern.compile("<<\\s+(" + numTag + ")\\s+(" + numTag + ")");
    private static final Pattern posAlignAfterTag2 =
	Pattern.compile("<<\\s+\"(" + stringTag + ")\"\\s+\"(" + stringTag + ")\"");

    private static final int nTiers = 5;
    private static final int hiTier = 0;
    private static final int alTier = 1;
    private static final int trTier = 2;
    private static final int lxTier = 3;
    private static final int prec = 4;

    // Read hi, al, tr, lx. For the first three, we can expect:
    // hi , al ; tr 
    // hi , al
    // hi ,; tr 
    // hi ,
    // al ; tr
    // al ;
    // tr
    public static String[] readPhrase(LineNumberReader in) throws IOException {
	String[] tiers = new String[nTiers];
	for (int i = 0; i < nTiers; i++)
	    tiers[i] = "";
	boolean allEmpty = true;
	String buffer = "";
	String bufferType = "";
	String line = in.readLine();
	while (line != null) {
	    Matcher m1 = posAlignTag1.matcher(line);
	    Matcher m2 = posAlignTag2.matcher(line);
	    Matcher m3 = posAlignAfterTag1.matcher(line);
	    Matcher m4 = posAlignAfterTag2.matcher(line);
	    if (m1.find()) {
		tiers[prec] += 
		    "<prec id1=\"" + m1.group(1) + "\" " +
		    "id2=\"" + m1.group(2) + "\"/>\n";
	    } else if (m2.find()) {
		tiers[prec] += 
		    "<prec id1=\"" + m2.group(1) + "\" " +
		    "id2=\"" + m2.group(2) + "\"/>\n";
	    } else if (m3.find()) {
		tiers[prec] += 
		    "<prec id1=\"" + m3.group(1) + "\" " +
		    "type1=\"after\" " +
		    "id2=\"" + m3.group(2) + "\"/>\n";
	    } else if (m4.find()) {
		tiers[prec] += 
		    "<prec id1=\"" + m4.group(1) + "\" " +
		    "type1=\"after\" " +
		    "id2=\"" + m4.group(2) + "\"/>\n";
	    } else if (line.matches("\\s*")) {
		if (!allEmpty) {
		    storeBuffer(tiers, buffer, bufferType);
		    return tiers;
		}
	    } else if (line.matches(",\\s*")) {
		storeBuffer(tiers, buffer, "hi");
		buffer = "";
		bufferType = "al";
	    } else if (line.matches(";\\s*")) {
		storeBuffer(tiers, buffer, "al");
		buffer = "";
		bufferType = "tr";
	    } else if (line.matches(",;\\s*")) {
		storeBuffer(tiers, buffer, "hi");
		buffer = "";
		bufferType = "tr";
	    } else if (line.matches(":\\s*")) {
		storeBuffer(tiers, buffer, bufferType);
		buffer = "";
		bufferType = "lx";
	    } else {
		buffer += line + " ";
		allEmpty = false;
	    }
	    line = in.readLine();
	}
	if (!allEmpty) {
	    storeBuffer(tiers, buffer, bufferType);
	    return tiers;
	} else 
	    return null;
    }

    private static void storeBuffer(String[] tiers, String buffer, String bufferType) {
	if (bufferType.equals(""))
	    tiers[trTier] = buffer; // tr
	else if (bufferType.equals("hi"))
	    tiers[hiTier] = buffer; 
	else if (bufferType.equals("al"))
	    tiers[alTier] = buffer; 
	else if (bufferType.equals("tr"))
	    tiers[trTier] = buffer; 
	else if (bufferType.equals("lx"))
	    tiers[lxTier] = buffer; 
    }

    // For testing.
    public static void main(String[] args) {
	String[] tested = new String[] 
	{ "abc3", "a<2", "a>b3", "&2", "\"5",
	    "1a bc", "a=b2", "a(b2", "a", "1", "aa0"};
	/*
	for (int i = 0; i < tested.length; i++) {
	    String s = tested[i];
	    System.out.println(s);
	    System.out.println(requiresLongTagEncoding(s));
	}
	*/
	System.out.println(expandTags("<i>\n" +
		    "<@2>\n" +
		    "<\"a a\">\n" +
		    "<@\"a a\">\n" +
		    "<\"a a\"^pre>\n" +
		    "<3aa^pre>\n" +
		    "<\"a a\"^post>\n" +
		    "<3aa^post>\n"));
    }

}
