/***************************************************************************/
/*                                                                         */
/*  REStoREScode.java                                                      */
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

// Transforms all RES in an XML file (complying with AELalign)
// to REScode.

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.res.*;

public class REStoREScode {

    // The conversion can be done by pure Java code, or
    // by an external program res2image, found at
    // http://www.let.rug.nl/~markjan/egyptian/res
    // The latter is perhaps faster but a lot less practical. 
    // If the external program res2image is to be used,
    // make sure it is included in the $PATH variable.
    private static final boolean useExternal = false;

    // If the pure Java code is to be used, this
    // requires a rendering enviroment with default arguments.
    private static final int fontSizePt = 45;
    private static final HieroRenderContext context = 
	new HieroRenderContext(fontSizePt, true);

    // Build XML parser.
    private static DocumentBuilder constructParser() {
	DocumentBuilder parser = null;
	try {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setCoalescing(false);
	    factory.setExpandEntityReferences(true);
	    factory.setIgnoringComments(true);
	    factory.setIgnoringElementContentWhitespace(false);
	    factory.setNamespaceAware(false);
	    factory.setValidating(false);
	    parser = factory.newDocumentBuilder();
	} catch (ParserConfigurationException e) {
	    System.err.println(e.getMessage());
	    System.exit(-1);
	}
	return parser;
    }

    // Main method. First argument is input .xml file.
    // If one argument, then write to standard output.
    // If two arguments, then write to file in second argument,
    // which may be identical to first argument so that file is overwritten.
    public static void main(String[] args) {
	if (args.length == 0 || args.length > 2)
	    System.err.println("Wrong number of arguments: " + args.length);
	else if (args.length == 1)
	    encodeRES(args[0]);
	else
	    encodeRES(args[0], args[1]);
	System.exit(0);
    }

    // As above, with one filename.
    public static void encodeRES(String file1) {
	encodeRES(file1, null);
    }

    // As above, with two filenames. The second may be null; if so,
    // write to standard output.
    public static void encodeRES(String file1, String file2) {
	String ending = ".xml";
	if (!file1.endsWith(ending))
	    file1 += ending;
	if (file2 != null && !file2.endsWith(ending))
	    file2 += ending;
	ShellInputOutput shell = prepareRuntime();
	convertFile(file1, file2, shell);
	finishRuntime(shell);
    }

    // For streams related to shell command.
    private static class ShellInputOutput {
	public PrintWriter runtimeIn;
	public BufferedReader runtimeOut;
	public BufferedReader runtimeErr;
    }

    // Prepare for calling res2image in shell.
    private static ShellInputOutput prepareRuntime() {
	if (!useExternal)
	    return null;
	ShellInputOutput shell = new ShellInputOutput();
	Runtime runtime = Runtime.getRuntime();
	try {
	    Process proc = runtime.exec("res2image -multi -code -hlr");
	    shell.runtimeIn = new PrintWriter(proc.getOutputStream(), true);
	    shell.runtimeOut = 
		new BufferedReader(
			new InputStreamReader(proc.getInputStream()));
	    shell.runtimeErr = 
		new BufferedReader(
			new InputStreamReader(proc.getErrorStream()));
	    reportAnyExternalError(shell);
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	    System.exit(-1);
	}
	return shell;
    }

    // See whether use of external RES to REScode conversion has caused errors.
    // If so, report these and abort.
    private static void reportAnyExternalError(ShellInputOutput shell) {
	try {
	    if (shell.runtimeErr.ready()) {
		System.err.println("Use of res2image gave error");
		while (shell.runtimeErr.ready()) {
		    System.err.println(shell.runtimeErr.readLine());
		}
		System.exit(-1);
	    }
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	    System.exit(-1);
	}
    }

    // Close streams.
    private static void finishRuntime(ShellInputOutput shell) {
	if (shell == null)
	    return;
	try {
	    shell.runtimeIn.close();
	    shell.runtimeErr.close();
	    shell.runtimeOut.close();
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	    System.exit(-1);
	}
    }

    // Read XML file, transform, and print.
    private static void convertFile(String name, String outName, ShellInputOutput shell) {
	DocumentBuilder parser = constructParser();
	Document doc;
	try {
	    doc = parser.parse(name);
	} catch (IOException e) {
	    System.err.println("In " + name);
	    System.err.println(e.getMessage());
	    return;
	} catch (SAXException e) {
	    System.err.println("In " + name);
	    System.err.println(e.getMessage());
	    return;
	}
	try {
	    PrintWriter out;
	    if (outName == null)
		out = new PrintWriter(System.out);
	    else
		out = new PrintWriter(new BufferedWriter(new FileWriter(outName)));
	    printDocument(doc, out, shell);
	    out.println();
	    out.close();
	} catch (FileNotFoundException e) {
	    System.err.println(e.getMessage());
	} catch (IOException e) {
	    System.err.println("In " + name);
	    System.err.println(e.getMessage());
	}
    }

    // Current version name and scheme.
    // Map from name and scheme to RES header and switches.
    private static class GlobalProps {
	public String name = "";
	public String scheme = "";
	public HashMap prelude = new HashMap();
    }

    // Transform and print.
    private static void printDocument(Document doc, PrintWriter out, ShellInputOutput shell) {
	GlobalProps props = new GlobalProps();
	out.println("<?xml version=\"1.0\"?>");
	out.println("<!DOCTYPE resource SYSTEM \"AELalign.0.3.dtd\">");
	printElement(doc.getDocumentElement(), out, false, false, shell, props);
	out.println();
    }

    // Print recursively. If is hieroglyphic, transform to REScode.
    // If empty string, do nothing.
    // If starts with @ (already REScode), do nothing.
    private static void printElement(Element el, PrintWriter out, boolean isHiero,
	    	boolean isHieroStream, ShellInputOutput shell, GlobalProps props) {
	String name = el.getTagName();
	if (name.equals("coord") || name.equals("align"))
	    ;
	else if (name.equals("texthi")) {
	    isHiero = true;
	    isHieroStream = true;
	} else if (name.equals("hi"))  {
	    isHiero = true;
	    isHieroStream = false;
	} else {
	    isHiero = false;
	    isHieroStream = false;
	}
	NamedNodeMap attrs = el.getAttributes();
	NodeList children = el.getChildNodes();
	out.print("<" + name);
	for (int i = 0; i < attrs.getLength(); i++) {
	    Node node = attrs.item(i);
	    if (node instanceof Attr) {
		String atName = ((Attr) node).getName();
		String atVal = ((Attr) node).getValue();
		if (!atVal.equals("") || !defaultsEmpty(name, atName)) {
		    if (atName.equals("texthi") ||
			    atName.equals("keyhi") ||
			    atName.equals("dicthi")) {
			String outCode = convertCoding(atVal, false, shell, props);
			out.print("\n" + atName + "=\"");
			printCode(outCode, out);
			out.print("\"");
		    } else {
			if (name.equals("lx"))
			    out.print("\n" + atName + "=\"");
			else
			    out.print(" " + atName + "=\"");
			printText(atVal, out);
			out.print("\"");
		    }
		}
	    }
	}
	if (children.getLength() == 0) {
	    out.print("/>");
	} else {
	    out.print(">");
	    for (int i = 0; i < children.getLength(); i++) {
		Node ch = children.item(i);
		if (ch instanceof Element) {
		    printElement((Element) ch, out, isHiero, isHieroStream, shell, props);
		} else if (ch instanceof CharacterData) {
		    String str = ((CharacterData) ch).getData();
		    if (isHiero && 
			    !str.matches("\\s*") &&
			    !str.matches("(?s)\\s*\\$.*")) {
			String outCode = convertCoding(str, isHieroStream, shell, props);
			printCode(outCode, out);
		    } else 
			printText(str, out);
		} else if (ch instanceof EntityReference) { // presently cannot happen
		    String str = ((EntityReference) ch).getNodeName();
		    out.print("&" + str + ";");
		}
	    }
	    out.print("</" + name + ">");
	}
	applyVersionChange(el, name, props);
    }

    // Convert RES to REScode, through process in runtime system.
    // If starts with "[" (which implies header), then ignore prelude.
    // If RES ends on "-[ ]", then remove it.
    private static String convertCoding(String in, boolean isHieroStream, 
	    ShellInputOutput shell, GlobalProps props) {
	if (in.matches("(?s).*-(\\s*\\[[^\\]\"]*\\])?\\s*"))
	    in += ".";
	try {
	    if (shell == null) {
		if (ResOrLite.isResLite(in))
		    return in;
		else {
		    /*
		    RES res = RES.createRES(in, context);
		    ResDivision div = new ResDivision(res, context);
		    return div.toREScode().toString();
		    */
		    return null;
		}
	    }
	    in = in.replaceAll("\\s+", " ");
	    if (isHieroStream && !in.matches("(?s)\\s*\\[.*"))
		in = getPrelude(props) + in;
	    shell.runtimeIn.println(in);
	    reportAnyExternalError(shell);
	    String code = shell.runtimeOut.readLine();
	    String prelude = shell.runtimeOut.readLine();
	    if (isHieroStream)
		setPrelude(prelude, props);
	    return code;
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	    return "";
	}
    }

    // Print string, but insert newlines to avoid ridiculously long lines.
    // Newlines should be at space, but not inside REScode strings.
    private static void printCode(String code, PrintWriter out) {
	final int maxLineLen = 70;
	boolean insideString = false;
	int lineLen = 0;
	for (int i = 0; i < code.length(); i++) {
	    char thisChar = code.charAt(i);
	    if (thisChar == '\"')  {
		insideString = !insideString;
		lineLen += printSpecial(thisChar, out);
	    }
	    else if (thisChar == '\\') {
		out.print(thisChar);
		lineLen++;
		i++;
		lineLen += printSpecial(code.charAt(i), out);
	    } else if (thisChar == ' ' && !insideString && lineLen > maxLineLen) {
		out.println();
		lineLen = 0;
	    } else {
		lineLen += printSpecial(thisChar, out);
	    }
	}
    }

    // Print string. Convert characters to entities where needed.
    private static void printText(String str, PrintWriter out) {
	for (int i = 0; i < str.length(); i++) {
	    char thisChar = str.charAt(i);
	    if (ISOlatEntities.isEntity(thisChar))
		out.print(ISOlatEntities.charToEntity(thisChar));
	    else
		printSpecial(thisChar, out);
	}
    }

    // Possible change version and scheme.
    private static void applyVersionChange(Element el, String tagName, GlobalProps props) {
	if (tagName.equals("version")) {
	    props.name = el.getAttributeNode("name").getValue();
	    props.scheme = el.getAttributeNode("scheme").getValue();
	}
    }

    // Set header and switches for name and scheme.
    private static void setPrelude(String prel, GlobalProps props) {
	String version = props.name + "\n" + props.scheme;
	props.prelude.put(version, prel);
    }

    // Get header and switches for name and scheme.
    private static String getPrelude(GlobalProps props) {
	String version = props.name + "\n" + props.scheme;
	if (props.prelude.containsKey(version)) 
	    return (String) props.prelude.get(version);
	else
	    return "";
    }

    // Is some attribute that defaults to empty?
    private static boolean defaultsEmpty(String parent, String atVal) {
	return
	    atVal.equals("encoding") ||
	    atVal.equals("name") && !parent.equals("header") ||
	    atVal.equals("name2") ||
	    atVal.equals("scheme") ||
	    atVal.equals("scheme2") ||
	    atVal.equals("type");
    }

    // Output, replacing special characters by entities.
    // Return how many been written.
    private static int printSpecial(char c, PrintWriter out) {
	switch (c) {
	    case '&':
		out.print("&amp;");
		return 5;
	    case '\'':
		out.print("&apos;");
		return 6;
	    case '\"':
		out.print("&quot;");
		return 6;
	    case '<':
		out.print("&lt;");
		return 4;
	    case '>':
		out.print("&gt;");
		return 4;
	    default:
		out.print(c);
		return 1;
	}
    }
}
