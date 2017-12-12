/***************************************************************************/
/*                                                                         */
/*  PhraseSeparatorEg.java                                                 */
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

// Marker to separate phrases.

package nederhof.interlinear.egyptian;

import java.awt.*;

import nederhof.interlinear.*;

class PhraseSeparatorEg extends EgyptianTierAwtPart {

    // Size of marker.
    private final int markerSize = 10;

    // How many symbols.
    public int nSymbols() {
        return 1;
    }

    // Is position breakable?
    public boolean breakable(int i) {
        return next == null;
    }

    // Penalty of line break at breakable position.
    public double penalty(int i) {
        return 0;
    }

    // Distance of position j from position i.
    // We look at location of symbol following position j,
    // with text from position i onward.
    // i <= j < nSymbols.
    public float dist(int i, int j) {
        return 0;
    }

    // Width from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float width(int i, int j) {
        if (i == j)
            return 0;
        else {
            return markerSize * 2;
        }
    }

    // Advance from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float advance(int i, int j) {
        if (j == nSymbols())
            return markerSize * 2;
        else
            return dist(i, j);
    }

    // Dimensions.
    public float leading() {
        return markerSize / 2;
    }
    public float ascent() {
        return markerSize;
    }
    public float descent() {
        return markerSize / 2;
    }

    public void draw(int i, int j, int x, int y, Graphics2D g) {
        if (i != j) {
	    if (highlights.isEmpty() && highlightsAfter.isEmpty())
		g.setColor(Color.GREEN);
	    else
		g.setColor(Color.BLUE);
	    g.draw(new Rectangle(x + markerSize / 2, y - markerSize, 
		markerSize, markerSize));
        }
    }

    public int getPos(int i, int j, int x, int y) {
	if (x >= 0 && x < advance(i, j))
	    return 0;
	else
	    return -1;
    }

    // Is content for purposes of editing.
    public boolean isContent() {
        return true;
    }

}
