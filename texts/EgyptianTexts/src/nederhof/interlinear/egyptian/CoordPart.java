/***************************************************************************/
/*                                                                         */
/*  CoordPart.java                                                         */
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

// Coordinate part of tier.

package nederhof.interlinear.egyptian;

import java.awt.*;

import nederhof.interlinear.*;

public class CoordPart extends EgyptianTierAwtPart {

    // The id.
    public String id;

    public CoordPart(String id) {
	this.id = id;
    }

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
	else 
	    return metrics().stringWidth(id);
    }

    // Advance from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float advance(int i, int j) {
	if (j == nSymbols()) 
	    return 3 * lineWidth() +
		(next == null ?
		 metrics().stringWidth(" ") :
		 next.leadSpaceAdvance());
	else
	    return dist(i, j);
    }

    // Dimensions.
    public float leading() {
	return metrics().getLeading();
    }
    public float ascent() {
	return lineHeight() + 
	    metrics().getAscent() + metrics().getDescent() + metrics().getLeading();
    }
    public float descent() {
	return 0;
    }

    // For external use, how much would ascent be?
    public static float ascent(FontMetrics metrics) {
	return metrics.getAscent() + metrics.getDescent() + metrics.getLeading();
    }

    public void draw(int i, int j, int x, int y, Graphics2D g) {
        if (i != j) {
            g.setFont(font());
	    if (highlights.isEmpty())
		g.setColor(Color.RED);
	    else
		g.setColor(Color.BLUE);
            g.drawString(id, x,
		    y - lineHeight() - metrics().getDescent() - metrics().getLeading());
	    g.fillRect(x + lineWidth(), y-lineHeight(), lineWidth(), lineHeight());
	    if (!highlightsAfter.isEmpty()) {
		g.setColor(Color.BLUE);
		g.fillRect(x + Math.round(advance(i, j)), y-lineHeight(), 
			highlightBarWidth, lineHeight());
	    }
        }
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

    // Dimensions of dividing line.
    private int lineWidth() {
	return 2;
    }
    private int lineHeight() {
	if (next == null)
	    return Math.min(
		Math.round(renderParams.latinFontMetrics.getAscent()),
		Math.round(renderParams.egyptLowerFontMetrics.getAscent()));
	else 
	    return Math.round(next.exclusiveAscent());
    }

    // Font for label.
    protected Font font() {
	return renderParams.footLatinFont;
    }

    // Metrics for label.
    protected FontMetrics metrics() {
	return renderParams.footLatinFontMetrics;
    }

}
