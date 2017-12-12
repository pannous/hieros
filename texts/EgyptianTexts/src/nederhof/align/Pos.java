/***************************************************************************/
/*                                                                         */
/*  Pos.java                                                               */
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

package nederhof.align;

// A position consists of version, scheme and tag.
// Possibly also filenumber if tag starts with @;
// otherwise filenumber is < 0.
// For phrasal positions, unique numbers are generated.
// These numbers are kept as strings and as ints.

final class Pos {
    private static int new_pos = 0;
    private boolean isPhrasal;
    private String version;
    private String scheme;
    private String tag;
    private int num;
    private int file;
    private static final int nonFile = -1;

    // Constructor for non-phrasal positions.
    public Pos(String v, String s, String p, int f) {
	isPhrasal = false;
	version = v;
	scheme = s;
	tag = p;
	num = 0;
	if (tag.startsWith("@"))
	    file = f;
	else
	    file = nonFile;
    }

    // Constructor for phrasal positions.
    public Pos(int f) {
	isPhrasal = true;
	version = "";
	scheme = "";
	tag = Integer.toString(new_pos);
	num = new_pos++;
	file = f;
    }

    public boolean isPhrasal() {
	return isPhrasal;
    }

    public String getVersion() {
	return version;
    }

    public String getScheme() {
	return scheme;
    }

    public String getTag() {
	return tag;
    }

    public int getNum() {
	return num;
    }

    public int getFile() {
	return file;
    }

    public boolean equals(Object o) {
	Pos other = (Pos) o;
	return isPhrasal == other.isPhrasal &&
	    version.equals(other.version) &&
	    scheme.equals(other.scheme) &&
	    tag.equals(other.tag) &&
	    file == other.file;
    }

    public int hashCode() {
	return tag.hashCode();
    }
}
