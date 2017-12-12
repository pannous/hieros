/***************************************************************************/
/*                                                                         */
/*  StringAux.java                                                         */
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

package nederhof.util;

public class StringAux {

    // Longest common prefix.
    public static String longestCommonPrefix(String s1, String s2) {
	StringBuffer buf = new StringBuffer();
	for (int i = 0; i < Math.min(s1.length(), s2.length()); i++) {
	    if (s1.charAt(i) == s2.charAt(i))
		buf.append(s1.charAt(i));
	    else
		break;
	}
	return buf.toString();
    }

}
