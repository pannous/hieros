/***************************************************************************/
/*                                                                         */
/*  ISOlatEntities.java                                                    */
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

package nederhof.align.convert;

// Maps ASCII characters back to the entity names in ISOlat1.

public class ISOlatEntities {

    // Is mentioned in ISOlat1 entities?
    public static boolean isEntity(char c) {
	return 160 <= c && c <= 255;
    }

    // Map char to XML entity.
    public static String charToEntity(char c) {
	switch (c) {
	    case 160: return "&nbsp;";
	    case 161: return "&iexcl;";
	    case 162: return "&cent;";
	    case 163: return "&pound;";
	    case 164: return "&curren;";
	    case 165: return "&yen;";
	    case 166: return "&brvbar;";
	    case 167: return "&sect;";
	    case 168: return "&uml;";
	    case 169: return "&copy;";
	    case 170: return "&ordf;";
	    case 171: return "&laquo;";
	    case 172: return "&not;";
	    case 173: return "&shy;";
	    case 174: return "&reg;";
	    case 175: return "&macr;";
	    case 176: return "&deg;";
	    case 177: return "&plusmn;";
	    case 178: return "&sup2;";
	    case 179: return "&sup3;";
	    case 180: return "&acute;";
	    case 181: return "&micro;";
	    case 182: return "&para;";
	    case 183: return "&middot;";
	    case 184: return "&cedil;";
	    case 185: return "&sup1;";
	    case 186: return "&ordm;";
	    case 187: return "&raquo;";
	    case 188: return "&frac14;";
	    case 189: return "&frac12;";
	    case 190: return "&frac34;";
	    case 191: return "&iquest;";
	    case 192: return "&Agrave;";
	    case 193: return "&Aacute;";
	    case 194: return "&Acirc;";
	    case 195: return "&Atilde;";
	    case 196: return "&Auml;";
	    case 197: return "&Aring;";
	    case 198: return "&AElig;";
	    case 199: return "&Ccedil;";
	    case 200: return "&Egrave;";
	    case 201: return "&Eacute;";
	    case 202: return "&Ecirc;";
	    case 203: return "&Euml;";
	    case 204: return "&Igrave;";
	    case 205: return "&Iacute;";
	    case 206: return "&Icirc;";
	    case 207: return "&Iuml;";
	    case 208: return "&ETH;";
	    case 209: return "&Ntilde;";
	    case 210: return "&Ograve;";
	    case 211: return "&Oacute;";
	    case 212: return "&Ocirc;";
	    case 213: return "&Otilde;";
	    case 214: return "&Ouml;";
	    case 215: return "&times;";
	    case 216: return "&Oslash;";
	    case 217: return "&Ugrave;";
	    case 218: return "&Uacute;";
	    case 219: return "&Ucirc;";
	    case 220: return "&Uuml;";
	    case 221: return "&Yacute;";
	    case 222: return "&THORN;";
	    case 223: return "&szlig;";
	    case 224: return "&agrave;";
	    case 225: return "&aacute;";
	    case 226: return "&acirc;";
	    case 227: return "&atilde;";
	    case 228: return "&auml;";
	    case 229: return "&aring;";
	    case 230: return "&aelig;";
	    case 231: return "&ccedil;";
	    case 232: return "&egrave;";
	    case 233: return "&eacute;";
	    case 234: return "&ecirc;";
	    case 235: return "&euml;";
	    case 236: return "&igrave;";
	    case 237: return "&iacute;";
	    case 238: return "&icirc;";
	    case 239: return "&iuml;";
	    case 240: return "&eth;";
	    case 241: return "&ntilde;";
	    case 242: return "&ograve;";
	    case 243: return "&oacute;";
	    case 244: return "&ocirc;";
	    case 245: return "&otilde;";
	    case 246: return "&ouml;";
	    case 247: return "&divide;";
	    case 248: return "&oslash;";
	    case 249: return "&ugrave;";
	    case 250: return "&uacute;";
	    case 251: return "&ucirc;";
	    case 252: return "&uuml;";
	    case 253: return "&yacute;";
	    case 254: return "&thorn;";
	    case 255: return "&yuml;";
	    default: return String.valueOf(c);
	}
    }

    // Map chars in string to entities.
    public static String stringToEntities(String str) {
	StringBuffer out = new StringBuffer();
	for (int i = 0; i < str.length(); i++) {
	    char thisChar = str.charAt(i);
	    out.append(charToEntity(thisChar));
	}
	return out.toString();
    }
}
