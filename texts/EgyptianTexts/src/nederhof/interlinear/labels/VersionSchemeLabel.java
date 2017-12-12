/***************************************************************************/
/*                                                                         */
/*  VersionSchemeLabel.java                                                */
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

// Combination of version, scheme and label with the scheme.

package nederhof.interlinear.labels;

import java.util.*;

public class VersionSchemeLabel implements Comparable {

    public String version;
    public String scheme;
    public String label;

    public VersionSchemeLabel(String version, String scheme, String label) {
	this.version = version;
	this.scheme = scheme;
	this.label = label;
    }

    public int compareTo(Object o) {
	if (o instanceof VersionSchemeLabel) {
	    VersionSchemeLabel other = (VersionSchemeLabel) o;
	    if (version.compareTo(other.version) != 0)
		return version.compareTo(other.version);
	    else if (scheme.compareTo(other.scheme) != 0)
		return scheme.compareTo(other.scheme);
	    else
		return label.compareTo(other.label);
	} else
	    return 1;
    }

    public String toString() {
	return "" + version + " " + scheme + " " + label;
    }

}
