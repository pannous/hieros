/***************************************************************************/
/*                                                                         */
/*  HieroMeaning.java                                                      */
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

// Meaning of sign, or sequence of signs.

package nederhof.hieroutil;

public class HieroMeaning implements Comparable {

    // Begin and end markers of word.
    public static final String beginMarker = ">";
    public static final String endMarker = "<";

    // Type can be e.g. "phon", "ideo", or "det".
    private String type;

    // Phonetic pattern. Can be NULL for type=det, etc.
    private String phonetic;

    // Probability. May be ignored in some applications.
    private double prob;

    public HieroMeaning(String type, String phonetic) {
	this.type = type;
	this.phonetic = phonetic;
	this.prob = 1;
    }

    // Constructor when only type is applicable.
    public HieroMeaning(String type) {
	this(type, null);
    }

    public String getType() {
	return type;
    }

    public String getPhonetic() {
	return phonetic;
    }

    public int compareTo(Object o) {
	if (o instanceof HieroMeaning) {
	    HieroMeaning other = (HieroMeaning) o;
	    if (type.equals(other.type)) {
		if (phonetic == null && other.phonetic != null)
		    return -1;
		else if (phonetic != null && other.phonetic == null)
		    return 1;
		else if (phonetic == null && other.phonetic == null)
		    return 0;
		else
		    return phonetic.compareTo(other.phonetic);
	    } else
		return type.compareTo(other.type);
	} else
	    return 1;
    }

    public boolean equals(Object o) {
	return compareTo(o) == 0;
    }

    // For debugging.
    public String toString() {
	return "type=" + type + " phon=" + phonetic;
    }

}
