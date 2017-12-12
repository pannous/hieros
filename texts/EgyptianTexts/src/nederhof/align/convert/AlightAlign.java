/***************************************************************************/
/*                                                                         */
/*  AlightAlign.java                                                       */
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

package nederhof.align.convert;

// Input is an abbreviated form of text annotation consisting of
// 1) hieroglyphic in RES or REScode 
// 2) transliteration
// 3) translation
// 4) lexical annotation
// This Java program converts this format into an XML format 
// called AELalign, version 0.3.

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

public class AlightAlign {

    // Main routine. The conversion is done for each argument in command line.
    // If argument does not end in .light ,
    // then the input file is that name plus extension .light .
    // Otherwise, the argument denotes input file.
    // The output file is the name without .light plus extension .xml .
    public static void main(String[] args) {
	for (int argn = 0; argn < args.length; argn++) {
	    String fileName = args[argn];
	    String ending = ".light";
	    if (fileName.endsWith(ending))
		fileName = fileName.substring(0, fileName.length()-ending.length());
	    String inName = fileName + ".light";
	    String outName = fileName + ".xml";
	    File inFile = new File(inName);
	    File outFile = new File(outName);
	    convertFile(inFile, outFile);
	}
	System.exit(0);
    }

    // Convert one file. Strip suffix .light of file name if present.
    // Also provided are directories for input and output.
    // inName is name of input file, to be used, amongst others, in error messages.
    public static void convertFile(File inFile, File outFile) {
	String inName = inFile.getName();
	try {
	    LineNumberReader in = 
		new LineNumberReader(new FileReader(inFile));
	    PrintWriter out = 
		new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
	    copyHeader(in, out);
	    processBody(in, out, inName);
	    finishFile(out);
	    in.close();
	    out.close();
	} catch (FileNotFoundException e) {
	    System.err.println(e.getMessage());
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	}
    }

    ///////////////////////////////////////////////////////////////////
    // Header.

    // Process header.
    private static void copyHeader(LineNumberReader in, PrintWriter out) 
	throws IOException {
	    String now = dateString();
	    out.println("<?xml version=\"1.0\"?>");
	    out.println("<!DOCTYPE resource SYSTEM \"AELalign.0.3.dtd\">");
	    out.println("<resource>");
	    String line = trimEnd(in.readLine());
	    while (line != null && !line.matches("\\s*###")) {
		line = line.replaceAll("#DATE#", now);
		out.println(line);
		line = trimEnd(in.readLine());
	    }
	}

    // Current date as string.
    private static String dateString() {
	Date date = new Date();
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
	return format.format(date);
    }

    ///////////////////////////////////////////////////////////////////
    // Body.

    // The type, which can change during processing of file.
    private static class MutableString {
	public String str = null;
    }

    // Process body.
    private static void processBody(LineNumberReader in, PrintWriter out, String inName)
	throws IOException {
	    out.println("<body>\n");
	    MutableString type = new MutableString();
	    while (processParagraph(in, out, inName, type))
		;
	    out.println("</body>");
    }

    // Process paragraph, and return false if end of input reached.
    private static boolean processParagraph(LineNumberReader in, PrintWriter out, String inName,
	    MutableString type)
	throws IOException {
	    String line = trimEnd(in.readLine());
	    while (line != null && line.matches("\\s*"))
		line = trimEnd(in.readLine());
	    if (line == null)
		return false;
	    else if (line.matches("\\s*version\\s*=.*"))
		return processVersionChange(line, in, out, inName);
	    else if (line.matches("\\s*type\\s*=.*"))
		return processTypeChange(line, in, out, inName, type);
	    else if (line.matches("\\s*[^\\s]+\\s*==.*"))
		return processEquates(line, in, out, inName);
	    else if (line.matches("\\s*,\\s*"))
		return processTextAl("", "", in, out, inName);
	    else if (line.matches("\\s*,;\\s*"))
		return processTextTr("", null, "", in, out, inName);
	    else if (line.matches("\\s*;\\s*"))
		return processTextTr(null, "", "", in, out, inName);
	    else if (line.matches("\\s*:\\s*")) 
		return processTextLx(null, null, null, null, "", in, out, inName);
	    else
		return processText(line, in, out, inName, type);
	}

    // version = 'some_version_name'('some_scheme')
    private static final Pattern versionPat1 =
	Pattern.compile("^\\s*version\\s*=\\s*([^\\s]*)\\(([^\\s]*)\\)\\s*$");
    // version = 'some_version_name'
    private static final Pattern versionPat2 =
	Pattern.compile("^\\s*version\\s*=\\s*([^\\s]*)\\s*$");
    // Process paragraph containing change of version, given first line.
    private static boolean processVersionChange(String txt,
	    LineNumberReader in, PrintWriter out, String inName) 
	throws IOException {
	    Matcher m;
	    m = versionPat1.matcher(txt);
	    if (m.find()) {
		String version = m.group(1);
		String scheme = m.group(2);
		out.println("<version" +
			(version.equals("") ? "" : " name=\"" + version + "\"") + 
			(scheme.equals("") ? "" : " scheme=\"" + scheme + "\"") + 
			"/>\n");
	    } else {
		m = versionPat2.matcher(txt);
		if (m.find()) {
		    String version = m.group(1);
		    out.println("<version" +
			    (version.equals("") ? "" : " name=\"" + version + "\"") + 
			    "/>\n");
		} else 
		    return reportLine(txt, in, inName);
	    }
	    txt = trimEnd(in.readLine());
	    if (txt == null)
		return false;
	    else if (txt.matches("\\s*")) 
		return true;
	    else
		return reportLine(txt, in, inName);
	}

    // type = 'some_type'
    private static final Pattern typePat =
	Pattern.compile("^\\s*type\\s*=\\s*(hi|al|tr)\\s*$");
    // Process paragraph containing change of text type, given first line.
    private static boolean processTypeChange(String txt,
	    LineNumberReader in, PrintWriter out, String inName, MutableString type) 
	throws IOException {
	    Matcher m = typePat.matcher(txt);
	    if (m.find()) 
		type.str = m.group(1);
	    else 
		return reportLine(txt, in, inName);
	    txt = trimEnd(in.readLine());
	    if (txt == null)
		return false;
	    else if (txt.matches("\\s*")) 
		return true;
	    else
		return reportLine(txt, in, inName);
	}

    // 'some_position' == 'some_version_name'('some_scheme') 'other_position'
    private static final Pattern equatePat1 =
	Pattern.compile("^\\s*([^\\s]+)\\s*==\\s*([^\\s]*)\\(([^\\s]*)\\)\\s*([^\\s]+)\\s*$");
    // 'some_position' == 'some_version_name' 'other_position'
    private static final Pattern equatePat2 =
	Pattern.compile("^\\s*([^\\s]+)\\s*==\\s*([^\\s]+)\\s+([^\\s]+)\\s*$");
    // 'some_position' == 'other_position'
    private static final Pattern equatePat3 =
	Pattern.compile("^\\s*([^\\s]+)\\s*==\\s*([^\\s]+)\\s*$");
    // Process paragraph containing equations of positions, given first line.
    private static boolean processEquates(String txt,
	    LineNumberReader in, PrintWriter out, String inName) 
	throws IOException {
	    Matcher m;
	    m = equatePat1.matcher(txt);
	    if (m.find()) {
		String pos1 = m.group(1);
		String version = m.group(2);
		String scheme = m.group(3);
		String pos2 = m.group(4);
		out.println("<equatepos" +
			" pos1=\"" + pos1 + "\"" +
			(version.equals("") ? "" : " name2=\"" + version + "\"") + 
			(scheme.equals("") ? "" : " scheme2=\"" + scheme + "\"") + 
			" pos2=\"" + pos2 + "\"" +
			"/>");
	    } else {
		m = equatePat2.matcher(txt);
		if (m.find()) {
		    String pos1 = m.group(1);
		    String version = m.group(2);
		    String pos2 = m.group(3);
		    out.println("<equatepos" +
			    " pos1=\"" + pos1 + "\"" +
			    (version.equals("") ? "" : " name2=\"" + version + "\"") + 
			    " pos2=\"" + pos2 + "\"" +
			    "/>");
		} else {
		    m = equatePat3.matcher(txt);
		    if (m.find()) {
			String pos1 = m.group(1);
			String pos2 = m.group(2);
			out.println("<equatepos" +
				" pos1=\"" + pos1 + "\"" +
				" pos2=\"" + pos2 + "\"" +
				"/>");
		    } else 
			return reportLine(txt, in, inName);
		}
	    }
	    txt = trimEnd(in.readLine());
	    if (txt == null) {
		out.println("");
		return false;
	    } else if (txt.matches("\\s*")) {
		out.println("");
		return true;
	    } else if (txt.matches("\\s*[^\\s]+\\s*==.*")) 
		return processEquates(txt, in, out, inName);
	    else {
		out.println("");
		return reportLine(txt, in, inName);
	    }
	}

    // Process paragraph containing text, given first line.
    private static boolean processText(String txt,
	    LineNumberReader in, PrintWriter out, String inName, MutableString type) 
	throws IOException {
	    String line = trimEnd(in.readLine());
	    if (line == null || line.matches("\\s*")) {
		if (type.str == null)
		    return reportLine(line, in, inName);
		else if (type.str.equals("hi"))
		    makePar(txt, null, null, null, out);
		else if (type.str.equals("al"))
		    makePar(null, txt, null, null, out);
		else // type.str.equals("tr")
		    makePar(null, null, txt, null, out);
		return (line != null);
	    } else if (line.matches("\\s*,\\s*"))
		return processTextAl(txt, "", in, out, inName);
	    else if (line.matches("\\s*,;\\s*"))
		return processTextTr(txt, null, "", in, out, inName);
	    else if (line.matches("\\s*;\\s*"))
		return processTextTr(null, txt, "", in, out, inName);
	    else if (line.matches("\\s*:\\s*")) {
		if (type.str == null)
		    return reportLine(line, in, inName);
		else if (type.str.equals("hi"))
		    return processTextLx(txt, null, null, null, "", in, out, inName);
		else if (type.str.equals("al"))
		    return processTextLx(null, txt, null, null, "", in, out, inName);
		else // type.str.equals("tr")
		    return processTextLx(null, null, txt, null, "", in, out, inName);
	    } else 
		return processText(lineConcat(txt, line), in, out, inName, type);
	}

    // Process rest of paragraph, within transliteration.
    private static boolean processTextAl(String hi, String al,
	    LineNumberReader in, PrintWriter out, String inName) 
	throws IOException {
	    String line = trimEnd(in.readLine());
	    if (line == null || line.matches("\\s*")) {
		makePar(hi, al, null, null, out);
		return (line != null);
	    } else if (line.matches("\\s*(,|,;)\\s*"))
		return reportLine(line, in, inName);
	    else if (line.matches("\\s*;\\s*"))
		return processTextTr(hi, al, "", in, out, inName);
	    else if (line.matches("\\s*:\\s*")) 
		return processTextLx(hi, al, null, null, "", in, out, inName);
	    else
		return processTextAl(hi, lineConcat(al, line), in, out, inName);
	}

    // Process rest of paragraph, within translation.
    private static boolean processTextTr(String hi, String al, String tr,
	    LineNumberReader in, PrintWriter out, String inName) 
	throws IOException {
	    String line = trimEnd(in.readLine());
	    if (line == null || line.matches("\\s*")) {
		makePar(hi, al, tr, null, out);
		return (line != null);
	    } else if (line.matches("\\s*(,|,;|;)\\s*"))
		return reportLine(line, in, inName);
	    else if (line.matches("\\s*:\\s*")) 
		return processTextLx(hi, al, tr, null, "", in, out, inName);
	    else
		return processTextTr(hi, al, lineConcat(tr, line), in, out, inName);
	}

    // texthi = 'some_hieroglyphic'
    // etc...
    private static final Pattern lexPat =
	Pattern.compile(
		"^\\s*(cite|href|(?:text|key|dict)(?:hi|al|tr|fo))\\s*=\\s*(.*)\\s*$");
    // Process rest of paragraph, within lexical item.
    private static boolean processTextLx(String hi, String al, String tr, String lx,
	    String entry, LineNumberReader in, PrintWriter out, String inName) 
	throws IOException {
	    String line = trimEnd(in.readLine());
	    if (line == null || line.matches("\\s*")) {
		if (entry != null) {
		    entry = "<lx\n" + entry + "/>";
		    lx = lineConcat(lx, entry);
		}
		makePar(hi, al, tr, lx, out);
		return (line != null);
	    } else if (line.matches("\\s*(,|,;|;)\\s*"))
		return reportLine(line, in, inName);
	    else if (line.matches("\\s*:\\s*")) {
		if (entry != null) {
		    entry = "<lx\n" + entry + "/>";
		    lx = lineConcat(lx, entry);
		}
		return processTextLx(hi, al, tr, lx, null, in, out, inName);
	    } else if (line.matches("\\s*<.*")) {
		if (entry != null) {
		    entry = "<lx\n" + entry + "/>";
		    lx = lineConcat(lx, entry);
		}
		lx = lineConcat(lx, line);
		return processTextLx(hi, al, tr, lx, null, in, out, inName);
	    } else {
		Matcher m = lexPat.matcher(line);
		if (m.find()) {
		    String key = m.group(1);
		    String val = m.group(2);
		    String keyval = "\t" + key + "=\"" + val + "\"";
		    return processTextLx(hi, al, tr, lx, lineConcat(entry, keyval), in, out, inName);
		} else 
		    return reportLine(line, in, inName);
	    }
	}

    // Print paragraph
    private static void makePar(String hi, String al, String tr, String lx,
	    PrintWriter out) 
	throws IOException {
	    if (hi != null || al != null || tr != null || lx != null) {
		out.println("<phrase>");
		if (hi != null) {
		    out.println("<texthi>");
		    if (!hi.matches("\\s*"))
			out.println(replaceTags(hi));
		    out.println("</texthi>");
		}
		if (al != null) {
		    out.println("<textal>");
		    if (!al.matches("\\s*"))
			out.println(replaceTags(al));
		    out.println("</textal>");
		}
		if (tr != null) {
		    out.println("<texttr>");
		    if (!tr.matches("\\s*"))
			out.println(replaceTags(tr));
		    out.println("</texttr>");
		}
		if (lx != null) {
		    out.println("<textlx>");
		    if (!lx.matches("\\s*"))
			out.println(replaceTags(lx));
		    out.println("</textlx>");
		}
		out.println("</phrase>\n");
	    }
	}

    // <'some_version_name'('some_scheme')='some position'>'some_text'</>
    private static final Pattern pat1 = 
	Pattern.compile("(?s)<!?([^<>]*)\\(([^<>]*)\\)=([^<>]*)>([^<>]*)</>");
    // <'some_version_name'('some_scheme')='some position'>
    private static final Pattern pat2 = 
	Pattern.compile("<!?([^<>]*)\\(([^<>]*)\\)=([^<>]*)>");
    // <'some_version_name'='some position'>'some_text'</>
    private static final Pattern pat3 = 
	Pattern.compile("(?s)<!?([^<>]*)=([^<>]*)>([^<>]*)</>");
    // <'some_version_name'='some position'>
    private static final Pattern pat4 = 
	Pattern.compile("<!?([^<>]*)=([^<>]*)>");
    // <'some_position'>'some_text'</>
    private static final Pattern pat5 = 
	Pattern.compile("(?s)<!?([^<>]*)>([^<>]*)</>");
    // <'some_position'>
    private static final Pattern pat6 = 
	Pattern.compile("<!?([^<>]*)>");
    // Replace patterns between < and > with coord or align tags.
    private static String replaceTags(String str) {
	Matcher m;
	m = pat1.matcher(str);
	while (m.find()) {
	    if (!isTag(m.group())) {
		String version = m.group(1);
		String scheme = m.group(2);
		String pos = m.group(3);
		String txt = m.group(4);
		str = str.substring(0, m.start()) +
		    "<align" +
		    (version.equals("") ? "" : " name=\"" + version + "\"") +
		    " scheme=\"" + scheme + "\"" +
		    " pos=\"" + pos + "\">" + 
		    txt + "</align>" +
		    str.substring(m.end());
		return replaceTags(str);
	    }
	}
	m = pat2.matcher(str);
	while (m.find()) {
	    if (!isTag(m.group())) {
		String version = m.group(1);
		String scheme = m.group(2);
		String pos = m.group(3);
		str = str.substring(0, m.start()) +
		    "<align" +
		    (version.equals("") ? "" : " name=\"" + version + "\"") +
		    " scheme=\"" + scheme + "\"" +
		    " pos=\"" + pos + "\"/>" +
		    str.substring(m.end());
		return replaceTags(str);
	    }
	}
	m = pat3.matcher(str);
	while (m.find()) {
	    if (!isTag(m.group())) {
		String version = m.group(1);
		String pos = m.group(2);
		String txt = m.group(3);
		str = str.substring(0, m.start()) +
		    "<align name=\"" + version + "\"" +
		    " pos=\"" + pos + "\">" + 
		    txt + "</align>" +
		    str.substring(m.end());
		return replaceTags(str);
	    }
	}
	m = pat4.matcher(str);
	while (m.find()) {
	    if (!isTag(m.group())) {
		String version = m.group(1);
		String pos = m.group(2);
		str = str.substring(0, m.start()) +
		    "<align name=\"" + version + "\"" +
		    " pos=\"" + pos + "\"/>" + 
		    str.substring(m.end());
		return replaceTags(str);
	    }
	}
	m = pat5.matcher(str);
	while (m.find()) {
	    if (!isTag(m.group())) {
		String pos = m.group(1);
		String txt = m.group(2);
		str = str.substring(0, m.start()) +
		    "<coord pos=\"" + pos + "\">" + 
		    txt + "</coord>" +
		    str.substring(m.end());
		return replaceTags(str);
	    }
	}
	m = pat6.matcher(str);
	while (m.find()) {
	    if (!isTag(m.group())) {
		String pos = m.group(1);
		str = str.substring(0, m.start()) +
		    "<coord pos=\"" + pos + "\"/>" +
		    str.substring(m.end());
		return replaceTags(str);
	    }
	}
	return str;
    }

    // Is AELalign tag within field of phrase?
    private static boolean isTag(String str) {
	return str.matches("(?s)<(/|(note|hi|al|tr|no|lx|etc|coord|align|markup)[^a-z]).*");
    }

    // Remove spurious whitespace symbols at end of string.
    private static String trimEnd(String str) {
	if (str == null)
	    return str;
	else
	    return str.replaceFirst("\\s+$", "");
    }

    // Concatenation, but allowing null strings in str1.
    // Avoid newline separating empty strings.
    private static String lineConcat(String str1, String str2) {
	if (str1 == null || str1.equals(""))
	    return str2;
	else 
	    return str1 + "\n" + str2;
    }

    // Report ill-formed line, and finish paragraph.
    // Return false at end of input.
    private static boolean reportLine(String line, LineNumberReader in, String inName) 
	throws IOException {
	    int num = in.getLineNumber();
	    if (line == null) {
		System.err.println("Unexpected end of file " + inName +
			" at line " + num);
		return false;
	    } else {
		System.err.println("Line " + num + 
			" of " + inName + " looks strange:");
		System.err.println(line);
		line = trimEnd(in.readLine());
		while (line != null && !line.matches("\\s*"))
		    line = trimEnd(in.readLine());
		return (line != null);
	    }
	}

    ///////////////////////////////////////////////////////////////////
    // End.

    // Print final line.
    private static void finishFile(PrintWriter out)
	throws IOException {
	    out.println("</resource>");
	}
}
