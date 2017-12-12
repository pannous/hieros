/***************************************************************************/
/*                                                                         */
/*  EtcPart.java                                                           */
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

// Etc part of tier. This functions as invisible symbol, so
// a position can be tied to this. The width is zero however.

package nederhof.interlinear.egyptian;

import java.awt.*;

import nederhof.interlinear.*;

class EtcPart extends EgyptianTierAwtPart {

    public static final String marker = "etc.";

    public EtcPart() {
    }

    // How many symbols.
    public int nSymbols() {
	return 1;
    }

    // Is position breakable?
    public boolean breakable(int i) {
	return true;
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
	if (i == j || !edit)
	    return 0;
	else
	    return metrics().stringWidth(marker);
    }

    // Advance from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float advance(int i, int j) {
	if (j == nSymbols() && edit)
	    return metrics().stringWidth(marker + " ");
	else
	    return 0;
    }

    // Font metrics.
    public float leading() {
	if (edit)
	    return metrics().getLeading();
	else
	    return 0;
    }
    public float ascent() {
	if (!edit)
	    return 0;
	else if (renderParams.uniformAscent)
	    return metrics().getAscent() +
		CoordPart.ascent(renderParams.footLatinFontMetrics);
	else
	    return metrics().getAscent();
    }
    public float descent() {
	if (!edit)
	    return 0;
	else
	    return metrics().getDescent();
    }

    // Ascent without coordinate markers.
    public float exclusiveAscent() {
	return metrics().getAscent();
    }

    // Draw is nothing here. 
    public void draw(int i, int j, int x, int y, Graphics2D g) {
	if (edit && i != j) {
	    g.setFont(font());
	    if (highlights.isEmpty())
		g.setColor(Color.GREEN);
	    else
		g.setColor(Color.BLUE);
	    g.drawString(marker, x, y);
	    if (!highlightsAfter.isEmpty()) {
		g.setColor(Color.BLUE);
		g.fillRect(x + Math.round(advance(i, j)),
		       	y - Math.round(ascent()),
			highlightBarWidth, Math.round(ascent() + descent()));
	    }
	}
    }

    // Font to be used in edit mode.
    protected Font font() {
	return renderParams.italicFont;
    }
    protected FontMetrics metrics() {
	return renderParams.italicFontMetrics;
    }

    public int getPos(int i, int j, int x, int y) {
	if (i != j && x >= 0 && x < advance(i, j)) 
	    return 0;
	else
	    return -1;
    }

    // Is not content.
    public boolean isContent() {
	return edit;
    }

}
