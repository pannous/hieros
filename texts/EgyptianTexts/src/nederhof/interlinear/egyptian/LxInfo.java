/***************************************************************************/
/*                                                                         */
/*  LxInfo.java                                                            */
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

// Gathers information in lexical entry.

package nederhof.interlinear.egyptian;

import org.w3c.dom.*;
import org.xml.sax.*;

public class LxInfo {

    public String texthi = "";
    public String textal = "";
    public String texttr = "";
    public String textfo = "";
    public String cite = "";
    public String href = "";
    public String keyhi = "";
    public String keyal = "";
    public String keytr = "";
    public String keyfo = "";
    public String dicthi = "";
    public String dictal = "";
    public String dicttr = "";
    public String dictfo = "";

    // Make empty information;
    public LxInfo() {
    }

    // Make from strings.
    public LxInfo(
	    String texthi,
	    String textal,
	    String texttr,
	    String textfo,
	    String cite,
	    String href,
	    String keyhi,
	    String keyal,
	    String keytr,
	    String keyfo,
	    String dicthi,
	    String dictal,
	    String dicttr,
	    String dictfo) {
	this.texthi = texthi;
	this.textal = textal;
	this.texttr = texttr;
	this.textfo = textfo;
	this.cite = cite;
	this.href = href;
	this.keyhi = keyhi;
	this.keyal = keyal;
	this.keytr = keytr;
	this.keyfo = keyfo;
	this.dicthi = dicthi;
	this.dictal = dictal;
	this.dicttr = dicttr;
	this.dictfo = dictfo;
    }

    // Extract information from TierPart.
    public LxInfo(LxPart part) {
	texthi = part.texthi;
	textal = part.textal;
	texttr = part.texttr;
	textfo = part.textfo;
	cite = part.cite;
	href = part.href;
	keyhi = part.keyhi;
	keyal = part.keyal;
	keytr = part.keytr;
	keyfo = part.keyfo;
	dicthi = part.dicthi;
	dictal = part.dictal;
	dicttr = part.dicttr;
	dictfo = part.dictfo;
    }

    // Extract information from Element.
    public LxInfo(Element elem) {
        texthi = getValue(elem.getAttributeNode("texthi"));
        textal = getValue(elem.getAttributeNode("textal"));
        texttr = getValue(elem.getAttributeNode("texttr"));
        textfo = getValue(elem.getAttributeNode("textfo"));
        cite = getValue(elem.getAttributeNode("cite"));
        href = getValue(elem.getAttributeNode("href"));
        keyhi = getValue(elem.getAttributeNode("keyhi"));
        keyal = getValue(elem.getAttributeNode("keyal"));
        keytr = getValue(elem.getAttributeNode("keytr"));
        keyfo = getValue(elem.getAttributeNode("keyfo"));
        dicthi = getValue(elem.getAttributeNode("dicthi"));
        dictal = getValue(elem.getAttributeNode("dictal"));
        dicttr = getValue(elem.getAttributeNode("dicttr"));
        dictfo = getValue(elem.getAttributeNode("dictfo"));
    }

    // Create LxPart out of information.
    public LxPart getLxPart() {
	return new LxPart(
	    texthi,
	    textal,
	    texttr,
	    textfo,
	    cite,
	    href,
	    keyhi,
	    keyal,
	    keytr,
	    keyfo,
	    dicthi,
	    dictal,
	    dicttr,
	    dictfo);
    }

    ///////////////////////////
    // Auxiliaries.

    // Get attribute value, but "" if attribute is null.
    private static String getValue(Attr attr) {
        return attr == null ? "" : attr.getValue();
    }

}
