/***************************************************************************/
/*                                                                         */
/*  TierPos.java                                                           */
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

// Position in tier, consisting of tier and input position.
// The type of the position can also be included, if needed.

package nederhof.interlinear;

public class TierPos implements Comparable {

    public String type;
    public Tier tier;
    public int pos;

    // Without type.
    public TierPos(Tier tier, int pos) {
	this("", tier, pos);
    }

    // With type.
    public TierPos(String type, Tier tier, int pos) {
	this.type = type;
	this.tier = tier;
	this.pos = pos;
    }

    // Equality.
    public boolean equals(Object o) {
	if (o instanceof TierSpan) {
	    TierPos other = (TierPos) o;
	    return 
		other.type.equals(type) &&
		other.tier == tier && 
		other.pos == pos;
	} else
	    return false;
    }

    // Comparison.
    public int compareTo(Object o) {
	if (o instanceof TierSpan) {
	    TierPos other = (TierPos) o;
	    if (type.compareTo(other.type) != 0)
		return type.compareTo(other.type);
	    else if (compareTo(tier.id(), other.tier.id()) != 0)
		return compareTo(tier.id(), other.tier.id());
	    else 
		return compareTo(pos, other.pos);
	} else
	    return 1;
    }

    // Compare two numbers.
    private static int compareTo(int i1, int i2) {
	if (i1 < i2)
	    return -1;
	else if (i1 > i2)
	    return 1;
	else
	    return 0;
    }

    // Unbreakable span around position.
    public TierSpan unbreakableSpan() {
	return tier.unbreakableSpan(pos);
    }

    // For debugging.
    public String toString() {
	return "type=" + type + ", tier.id=" + tier.id() + ", pos=" + pos;
    }

}
