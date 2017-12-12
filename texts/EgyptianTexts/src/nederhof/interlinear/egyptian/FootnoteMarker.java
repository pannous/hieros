/***************************************************************************/
/*                                                                         */
/*  FootnoteMarker.java                                                    */
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

// Footnote marker. At beginning of footnote.

package nederhof.interlinear.egyptian;

import java.util.*;

import java.awt.*;

import nederhof.interlinear.*;
import nederhof.interlinear.frame.*;

public class FootnoteMarker extends EgyptianTierAwtPart {

    public String marker;

    public FootnoteMarker(String marker) {
	this.marker = marker + " ";
    }

    // How many symbols.
    public int nSymbols() {
        return 1;
    }

    // Is position breakable?
    public boolean breakable(int i) {
        return false;
    }

    // Penalty of line break at breakable position.
    public double penalty(int i) {
	return Penalties.spacePenalty;
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
        else
            return metrics().stringWidth(marker);
    }

    // Advance from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float advance(int i, int j) {
        if (j == nSymbols()) 
            return metrics().stringWidth(marker);
        else
            return dist(i, j);
    }

    // Font metrics.
    public float leading() {
        return metrics().getLeading();
    }
    public float ascent() {
        return (1 + renderParams.raisingFactor) * metrics().getAscent();
    }
    public float descent() {
        return 0;
    }

    public void draw(int i, int j, int x, int y, Graphics2D g) {
        if (i != j) {
            g.setFont(font());
            g.setColor(color());
            g.drawString(marker, x, 
		    y - renderParams.raisingFactor * metrics().getAscent());
        }
    }

    public int getPos(int i, int j, int x, int y) {
        if (i != j && x >= 0 && x < advance(i, j))
            return 0;
        else
            return -1;
    }

    protected Color color() {
	return renderParams.footnoteMarkerColor();
    }

    protected Font font() {
        return renderParams.footLatinFont;
    }

    protected FontMetrics metrics() {
        return renderParams.footLatinFontMetrics;
    }

}
