/***************************************************************************/
/*                                                                         */
/*  LocatedTierPartSpan.java                                               */
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

// Part of a span, with location where it is placed.

package nederhof.interlinear;

class LocatedTierPartSpan {

    public TierPart part;
    public int fromPos;
    public int toPos;
    public float location;

    // Part of span between positions, at location.
    public LocatedTierPartSpan(TierPart part,
	    int fromPos, int toPos, float location) {
	this.part = part;
	this.fromPos = fromPos;
	this.toPos = toPos;
	this.location = location;
    }

    // Part of span from position.
    public LocatedTierPartSpan(TierPart part,
	    int fromPos, float location) {
	this(part, fromPos, part.nSymbols(), location);
    }

}
