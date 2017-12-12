/***************************************************************************/
/*                                                                         */
/*  Version.java                                                           */
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

// A version consists of version name and numbering scheme.

package nederhof.align;

final class Version {
    private String name;
    private String scheme;

    // Constructor.
    public Version(String name, String scheme) {
	this.name = name;
	this.scheme = scheme;
    }

    public String getName() {
	return name;
    }

    public String getScheme() {
	return scheme;
    }

    public boolean equals(Object o) {
	Version v = (Version) o;
	return name.equals(v.name) && scheme.equals(v.scheme);
    }

    public int hashCode() {
	return name.hashCode();
    }
}
