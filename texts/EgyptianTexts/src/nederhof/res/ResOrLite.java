/***************************************************************************/
/*                                                                         */
/*  ResOrLite.java                                                         */
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

// A piece of RES or REScode.

package nederhof.res;

public abstract class ResOrLite {

    // Make RES or REScode.
    public static ResOrLite createResOrLite(String s) {
	if (isResLite(s))
	    /* TODO */
	    // return REScode(s);
	    return null;
	else
	    /* TODO */
	    // return ResFragment.parse(s, context);
	    return null;
    }

    // Make division for RES or REScode.
    // TODO
    /*
    public abstract RESorREScodeDivision createRESorREScodeDivision(
	    HieroRenderContext context);
	    */

    // Is RESlite, i.e. starts with $ ?
    public static boolean isResLite(String code) {
        return code.matches("(?s)\\s*\\$.*");
    }

    // May be RES, i.e. not empty and not starts with $ ?
    public static boolean isRes(String code) {
        return !code.matches("(?s)\\s*") &&
            !code.matches("(?s)\\s*\\$.*");
    }

}
