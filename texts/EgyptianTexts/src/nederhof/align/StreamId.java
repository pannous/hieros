/***************************************************************************/
/*                                                                         */
/*  StreamId.java                                                          */
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

// A stream is uniquely determined by file number, version and scheme,
// and stream type.
// Mainly used for hashing.

package nederhof.align;

final class StreamId {
    private int file;
    private String version;
    private String scheme;
    private int type;

    // Constructor.
    public StreamId(int file, String version, String scheme, int type) {
	this.file = file;
	this.version = version;
	this.scheme = scheme;
	this.type = type;
    }

    public int getFile() {
	return file;
    }

    public String getVersion() {
	return version;
    }

    public String getScheme() {
	return scheme;
    }

    public int getType() {
	return type;
    }

    // As above, but more explicit.
    public String getTypeName() {
	switch (type) {
	    case RenderContext.HIERO_FONT:
		return "hieroglyphic";
	    case RenderContext.EGYPT_FONT:
		return "transliteration";
	    case RenderContext.LATIN_FONT:
		return "translation";
	    case RenderContext.LX:
		return "lexical entries";
	    default:
		return "unidentified";
	}
    }

    public boolean equals(Object o) {
	StreamId other = (StreamId) o;
	return file == other.file &&
	    version.equals(other.version) &&
	    scheme.equals(other.scheme) &&
	    type == other.type;
    }

    public int hashCode() {
	String all = version + scheme + type + file;
	return all.hashCode();
    }
}
