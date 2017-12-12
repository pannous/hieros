/***************************************************************************/
/*                                                                         */
/*  ITierPart.java                                                         */
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

// Part of a tier, consisting of one kind of data.
// Position i refer to location after i-th symbol.
// Position 0 precedes first symbol.

package nederhof.interlinear;

import java.util.*;

public interface ITierPart {

    // How many symbols. Not normally including whitespace.
    // There must be at least one symbol in each tier.
    public int nSymbols();

    // Is position breakable?
    // This includes the position nSymbols(),
    // but need not be defined for position 0.
    public boolean breakable(int i);

    // Penalty of line break at breakable position.
    public double penalty(int i);

    // Distance of position j from position i.
    // We look at location of symbol following position j,
    // with text from position i onward.
    // i <= j < nSymbols.
    public float dist(int i, int j);

    // Width from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float width(int i, int j);

    // Advance from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float advance(int i, int j);

    // Font metrics.
    public float leading();
    public float ascent();
    public float descent();

    // Width up to position,
    public float widthTo(int j);

    // Width from position,
    public float widthFrom(int i);

    // Total width.
    public float width();

    // Advance up to position,
    public float advanceTo(int j);

    // Advance from position,
    public float advanceFrom(int i);

    // Total advance.
    public float advance();

    // By situating we mean expansion of e.g. footnotes by
    // specific labels, which needs to be done depending
    // on the situation.
    // For parts that are not footnote markers, nothing is done.
    // We assume that footnote markers and the like are unbreakable.
    public void situate(int i, int j);

    // Get footnotes within boundaries. Most types will not have any.
    public TreeSet getFootnotes(int i, int j);

    // By default, a part is content. (Overridden by coordinates.)
    public boolean isContent();

}
