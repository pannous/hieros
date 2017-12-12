/***************************************************************************/
/*                                                                         */
/*  Footnote.java                                                          */
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

// Part of tier containing footnote.

package nederhof.interlinear;

import java.util.*;

public abstract class Footnote implements Comparable {

    // Footnote marker.
    private int marker = 0;
    // Set marker.
    public void setMarker(int marker) {
	this.marker = marker;
    }
    // Get marker as number.
    public int getMarker() {
	return marker;
    }
    // As string.
    public String makeMarker() {
	return "" + marker;
    }

    // Footnote text, as vector of tierparts.
    protected Vector<TierPart> text;
    // Get text.
    public Vector<TierPart> text() {
	return text;
    }

    // Symbol in following tierpart to which footnotemarker
    // is connected. If none, then -1.
    private int symbol = -1;
    // Get symbol.
    public int symbol() {
	return symbol;
    }

    public Footnote(Vector<TierPart> text, int symbol) {
	this.text = text;
	this.symbol = symbol;
    }

    // Footnote text, as tier.
    public abstract Tier getTier();

    // Pending notes are put in sets, so ordered.
    public int compareTo(Object o) {
        if (o instanceof Footnote) {
            Footnote other = (Footnote) o;
            if (marker < other.marker)
                return -1;
            else if (marker >= other.marker)
                return 1;
            else
                return 0;
        } else
            return 1;
    }

}
