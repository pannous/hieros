/***************************************************************************/
/*                                                                         */
/*  REScode.java                                                           */
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

// A piece of REScode.
// See description of REScode for more information.

package nederhof.res;

public class REScode {
    // Text direction.
    public int dir;
    // Line size.
    public int size;
    // Groups in code.
    public REScodeGroups groups;

    // After reading, remainder string.
    private String remainder;

    // Read one chunk of REScode in input string.
    public REScode(String code) {
	this(ResValues.DIR_HLR, 1000, null);
	ParseBuffer in = new ParseBuffer(code);
	in.readToNonspace();
	if (in.isEmpty())
	    return;
	if (!readChunk(in))
	    in.readToEnd();
	remainder = in.remainder();
    }

    // Make REScode with given properties.
    public REScode(int dir, int size, REScodeGroups groups) {
	this.dir = dir;
	this.size = size;
	this.groups = groups;
	remainder = "";
    }

    // Make empty REScode, with given properties.
    public REScode(int dir, int size) {
	this(dir, size, null);
    }

    // Make prefix of length at most n of existing REScode.
    // This requires making copies of groups.
    public REScode(REScode code, int n) {
	this(code.dir, code.size);
	if (code.groups == null)
	    this.groups = null;
	else
	    this.groups = code.groups.clone(n);
    }

    // Return direction.
    public int getDir() {
	return dir;
    }

    // No groups?
    public boolean isEmpty() {
	return groups == null;
    }

    // No more than one group?
    public boolean isShort() {
	return groups == null || groups.tl == null;
    }

    // How many groups?
    public int nGroups() {
	int num = 0;
	for (REScodeGroups groups = this.groups; 
		groups != null;
		groups = groups.tl) 
	    num++;
	return num;
    }

    // The string that remains after reading one chunk.
    public String getRemainder() {
	return remainder;
    }

    // Get code that is tail of code after ignoring number of groups.
    public REScode getTail(int nGroups) {
	REScodeGroups groups = this.groups;
	while (nGroups > 0 && groups != null) {
	    nGroups--;
	    groups = groups.tl;
	}
	return new REScode(dir, size, groups);
    }

    // Try read header of REScode, then groups, then 'e'.
    // Return whether successful.
    private boolean readChunk(ParseBuffer in) {
	int oldPos = in.pos;
	int newDir = 0;
	int newSize = 0;
	if (!in.readChar('$') || 
		(newDir = in.readDirection()) < 0 ||
		(newSize = in.readInt()) == Integer.MAX_VALUE) {
	    in.pos = oldPos;
	    in.parseError("Ill-formed REScode header");
	    return false;
	} 
	dir = newDir;
	size = newSize;
	groups = REScodeGroups.read(in);
	if (groups != null && groups.failed)
	    return false;
	if (!in.readSingleChar('e')) {
	    in.parseError("Missing REScode end");
	    return false;
	}
	return true;
    }

    // Convert REScode to string.
    public String toString() {
	return "$ " + dirToString(dir) + " " +
	    size + " " + 
	    (groups != null ? groups.toString(true) : "") + "e ";
    }

    // Convert direction to string in REScode.
    private String dirToString(int dir) {
	switch (dir) {
	    case ResValues.DIR_HRL:
		return "hrl";
	    case ResValues.DIR_VLR:
		return "vlr";
	    case ResValues.DIR_VRL:
		return "vrl";
	    default:
		return "hlr";
	}
    }

    //////////////////////////////////////////////////////////////////////////
    // For RES or REScode.

    // Turn into division.
    public RESorREScodeDivision createRESorREScodeDivision(HieroRenderContext context) {
	return new REScodeDivision(this, context);
    }
}
