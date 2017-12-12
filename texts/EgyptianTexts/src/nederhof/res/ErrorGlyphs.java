/***************************************************************************/
/*                                                                         */
/*  ErrorGlyphs.java                                                       */
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

// Like Glyphs, but intended for missing glyphs. To be rendered as black box.

package nederhof.res;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

public class ErrorGlyphs implements OptionalGlyphs {

    private int blockSize;
    private int imageType;

    // Create dummy of given dimension (in pixels).
    public ErrorGlyphs(int size, HieroRenderContext context) {
	this.blockSize = Math.max(1, size/4);
	this.imageType = context.imageType();
    }

    // Get dimension.
    public Dimension dimension() {
	return new Dimension(blockSize, blockSize);
    }

    // Area covered by glyph. Pretend nothing for error glyph.
    public Area filling(int x, int y) {
	return new Area();
    }

    // Make black box.
    public void render(TransGraphics graph, int x, int y) {
	graph.fillRect(Color.BLACK, x-(blockSize/2), 
		y-(blockSize/2), blockSize, blockSize);
    }

    // The straight marker is only relevant for text, so ignore here.
    public void renderStraight(TransGraphics graph, int x, int y) {
	render(graph, x, y);
    }

    // Ignore the clipped argument.
    public void render(TransGraphics graph, int x, int y, Area clipped) {
	render(graph, x, y);
    }

    // For error glyphs, there is no edge.
    public int leftEdge() {
	return 0;
    }
    public int rightEdge() {
	return 0;
    }
    public int bottomEdge() {
	return 0;
    }
    public int topEdge() {
	return 0;
    }

}
