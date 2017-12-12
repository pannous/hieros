/***************************************************************************/
/*                                                                         */
/*  StyledHelper.java                                                      */
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

// Helper for working with styled text.

package nederhof.interlinear.frame;

import java.io.*;
import javax.xml.parsers.*;
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.util.*;
import nederhof.util.xml.*;

public class StyledHelper {

    // Custom pairs of XML tag and style.
    public Vector extraTypes = new Vector();

    ////////////////////////////////////////////////////////
    // From document.

    // Should we expect to see <li>, which
    // is to be transformed into bullet.
    public boolean allowBullets = false;

    // For element representing section in document, get paragraphs.
    public Vector getParagraphs(Element elem) throws IOException {
	Vector paragraphs = new Vector();
	NodeList children = elem.getChildNodes();
	for (int i = 0; i < children.getLength(); i++) {
	    Node child = children.item(i);
	    if (child instanceof Element) {
		Element childElem = (Element) child;
		String tagName = childElem.getTagName();
		Vector paragraph = retrieveParagraph(childElem);
		if (allowBullets) {
		    if (tagName.equals("li")) {
			String bullet = StyledTextPane.itemStart + " ";
			paragraph.add(0, new Object[] {"plain", bullet});
		    }
		}
		paragraphs.add(paragraph);
	    }
	}
	return paragraphs;
    }

    // Get elements from a paragraph.
    // Make sure there is no leading and trailing white space.
    private Vector retrieveParagraph(Element elem) throws IOException {
        Vector items = new Vector();
        NodeList children = elem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof CharacterData) {
                CharacterData data = (CharacterData) child;
                String dataString = data.getData();
		dataString = dataString.replaceAll("\\s\\s*", " ");
                if (i == 0)
                    dataString = dataString.replaceAll("^\\s*", "");
                if (i == children.getLength() - 1)
                    dataString = dataString.replaceAll("\\s*$", "");
                items.add(new Object[] {"plain", dataString});
            } else if (child instanceof Element) {
                Element childElem = (Element) child;
                String tagName = childElem.getTagName();
                if (tagName.equals("i")) {
                    items.add(new Object[] {"italic", data(childElem)});
                } else if (tagName.equals("a")) {
                    String href = getValue(childElem.getAttributeNode("href"));
                    String name = data(childElem);
                    items.add(new Object[] {"link", new String[] {href, name}});
                } else 
		    for (int j = 0; j < extraTypes.size(); j++) {
			Object[] pair = (Object[]) extraTypes.get(j);
			String tag = (String) pair[0];
			String style = (String) pair[1];
			if (tagName.equals(tag))
			    items.add(new Object[] {style, data(childElem)});
		    }
            }
        }
        return items;
    }

    // Get data immediately below child.
    private String data(Element elem) throws IOException {
        String data = "";
        NodeList children = elem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof CharacterData) {
                CharacterData d = (CharacterData) child;
                data += d.getData();
            }
        }
        return data;
    }

    // Get attribute value, but "" if attribute is null.
    private static String getValue(Attr attr) {
        return attr == null ? "" : attr.getValue();
    }

    //////////////////////////////////////////////
    // To document.

    // All paragraphs are to be li.
    public boolean itemPars = false;

    // Paragraphs are to be unmarked, used in non-XML text.
    public boolean unmarkedPars = false;

    public String writeParagraph(Vector par) {
	StringBuffer buf = new StringBuffer();
	boolean isListItem = itemPars;
	for (int j = 0; j < par.size(); j++) {
	    Object[] s = (Object[]) par.get(j);
	    String kind = (String) s[0];
	    if (kind.equals("link")) {
		String[] linkText = (String[]) s[1];
		buf.append("<a href=\"" + 
			XmlAux.escape(linkText[0]) + "\">" + 
			    XmlAux.escape(linkText[1]) + "</a>");
	    } else {
		String info = (String) s[1];
		if (j == 0 && info.startsWith(StyledTextPane.itemStart)) {
		    info =
			info.replaceFirst(StyledTextPane.itemStart + "\\s*", "");
		    isListItem = true;
		}
		info = XmlAux.escape(info);
		if (j == 0) {
		    if (unmarkedPars && !isListItem) {
			;
		    } else if (isListItem) 
			buf.append("<li>\n");
		    else
			buf.append("<p>\n");
		}
		if (!info.matches("")) {
		    if (kind.equals("plain"))
			buf.append(info);
		    else if (kind.equals("italic"))
			buf.append("<i>" + info + "</i>");
		    else 
			for (int i = 0; i < extraTypes.size(); i++) {
			    Object[] pair = (Object[]) extraTypes.get(i);
			    String tag = (String) pair[0];
			    String style = (String) pair[1];
			    if (kind.equals(style))
				buf.append("<" + tag + ">" + info + "</" + tag + ">");
			}
		}
	    }
	}
	// Remove trailing spaces.
	String parString = buf.toString().replaceFirst("\\s*$","");
	parString = XmlAux.breakLines(parString);
	if (unmarkedPars) {
	    if (isListItem)
		return parString + "\n</li>\n";
	    else
		return parString + "\n";
	} else if (isListItem)
	    return parString + "\n</li>";
	else
	    return parString + "\n</p>";
    }

}
