/***************************************************************************/
/*                                                                         */
/*  RESorREScode.java                                                      */
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

// A piece of RES or REScode.

package nederhof.res;

public abstract class RESorREScode {

    // Make RES or REScode.
    public static RESorREScode createRESorREScode(String s, 
	    HieroRenderContext context) {
	/*
	 * TODO remove
	if (ResOrLite.isResLite(s))
	    return new REScode(s);
	else
	    return RES.createRES(s, context);
	    */
	return null;
    }

    // Make division for RES or REScode.
    public abstract RESorREScodeDivision createRESorREScodeDivision(
	    HieroRenderContext context);
}
