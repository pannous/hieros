/***************************************************************************/
/*                                                                         */
/*  CollectionItem.java                                                    */
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

// Some name, e.g. museum or book, that gathers a number of texts,
// together with particulars of one text within that collection.

package nederhof.corpus;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import nederhof.util.xml.*;

public class CollectionItem {

    // What kind of collection? E.g. "kings", "museums", "publications".
    public String kind = "";

    // E.g. museum or book series.
    // Also the key to sorting.
    public String collect = "";
    public String collectKey = "";

    // E.g. volume number or museum catalogue number.
    public String section = "";
    public String sectionKey = "";

    // E.g. page or section number within volume.
    public String subsection = "";
    public String subsectionKey = "";

    // Further subdivision.
    public String subsubsection = "";
    public String subsubsectionKey = "";

    // Make empty collection item.
    public CollectionItem() {
	// all fields are empty string
    }

    // Make item from strings.
    public CollectionItem(
	    String kind,
	    String collect,
	    String collectKey,
	    String section,
	    String sectionKey,
	    String subsection,
	    String subsectionKey,
	    String subsubsection,
	    String subsubsectionKey) {
	this.kind = kind;
	this.collect = collect;
	this.collectKey = collectKey;
	this.section = section;
	this.sectionKey = sectionKey;
	this.subsection = subsection;
	this.subsectionKey = subsectionKey;
	this.subsubsection = subsubsection;
	this.subsubsectionKey = subsubsectionKey;
    }

    // Read as part of XML file.
    public CollectionItem(Element el) {
	kind = getValue(el.getAttributeNode("kind"));
	collect = getValue(el.getAttributeNode("collect"));
	collectKey = getValue(el.getAttributeNode("collectkey"));
	section = getValue(el.getAttributeNode("section"));
	sectionKey = getValue(el.getAttributeNode("sectionkey"));
	subsection = getValue(el.getAttributeNode("subsection"));
	subsectionKey = getValue(el.getAttributeNode("subsectionkey"));
	subsubsection = getValue(el.getAttributeNode("subsubsection"));
	subsubsectionKey = getValue(el.getAttributeNode("subsubsectionkey"));
    }

    // Get attribute value, but "" if attribute is null.
    private static String getValue(Attr attr) {
	return attr == null ? "" : attr.getValue();
    }

    // Print as part of XML file.
    public void print(PrintWriter out) throws IOException {
	if (!kind.matches("\\s*") && !collect.matches("\\s*")) {
	    out.print("<collection kind=\"" + XmlAux.escape(kind) + "\" " +
		    "collect=\"" + XmlAux.escape(collect) + "\"");
	    if (!collectKey.matches("\\s*")) 
		out.print(" collectkey=\"" + 
			XmlAux.escape(collectKey) + "\"");
	    if (!section.matches("\\s*")) {
		out.print("\n    section=\"" + 
			XmlAux.escape(section) + "\"");
		if (!sectionKey.matches("\\s*")) 
		    out.print(" sectionkey=\"" + 
			    XmlAux.escape(sectionKey) + "\"");
		if (!subsection.matches("\\s*")) {
		    out.print("\n    subsection=\"" + 
			    XmlAux.escape(subsection) + "\"");
		    if (!subsectionKey.matches("\\s*")) 
			out.print(" subsectionkey=\"" + 
			    XmlAux.escape(subsectionKey) + "\"");
		    if (!subsubsection.matches("\\s*")) {
			out.print("\n    subsubsection=\"" + 
			    XmlAux.escape(subsubsection) + "\"");
			if (!subsubsectionKey.matches("\\s*")) 
			    out.print(" subsubsectionkey=\"" + 
				XmlAux.escape(subsubsectionKey) + "\"");
		    }
		}
	    }
	    out.println("/>");
	}
    }

}
