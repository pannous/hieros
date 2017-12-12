/***************************************************************************/
/*                                                                         */
/*  CrackFill.java                                                         */
/*                                                                         */
/*  Copyright (c) 2006 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Information to fill cracks between areas of shading
// that might result from rounding-off in REScode.
// To do this, we maintain two arrays for each direction,
// One indicates that shading otherwise ending in a certain position,
// should now be ending in a number of positions further to the right
// (or further below). The other vice versa.

package nederhof.res;

import java.awt.*;

class CrackFill {

    // Text direction.
    private boolean isHorizontal;

    // Size of text.
    private int width;
    private int height;

    // Two arrays each for two directions.
    private int[] horForward;
    private int[] horBackward;
    private int[] vertForward;
    private int[] vertBackward;

    // Size of cracks to fill;
    private int imprecision = 0;

    // Make record for filling of cracks.
    public CrackFill(boolean isHorizontal, int width, int height, 
	    HieroRenderContext context) {
	this.isHorizontal = isHorizontal;
	this.width = width;
	this.height = height;
	if (isHorizontal)
	    newHor();
	else
	    newVert();
	setImprecision(context.emSizePix());
    }

    // Set size of cracks to fill.
    private void setImprecision(int pixelSize) {
	imprecision = (int) (2.0 * pixelSize / 1000.0 + 2.0);
    }

    // Go to new group.
    public void newGroup() {
	if (isHorizontal) 
	    newVert();
	else 
	    newHor();
    }

    // Make new arrays for horizontal.
    private void newHor() {
	horForward = new int[width];
	horBackward = new int[width];
	for (int i = 0; i < width; i++) {
	    horForward[i] = 0;
	    horBackward[i] = 0;
	}
    }

    // Make new arrays for vertical.
    private void newVert() {
	vertForward = new int[height];
	vertBackward = new int[height];
	for (int i = 0; i < height; i++) {
	    vertForward[i] = 0;
	    vertBackward[i] = 0;
	}
    }

    // Change dimensions of shaded rectangle to close cracks.
    // Also make edges straight at boundaries of rectangle.
    public void makeConnect(Rectangle shadeRect) {
	int xMin = extendStart(shadeRect.x, horBackward);
	int xPlus = extendEnd(shadeRect.x + shadeRect.width, horForward);
	int yMin = extendStart(shadeRect.y, vertBackward);
	int yPlus = extendEnd(shadeRect.y + shadeRect.height, vertForward);
	recordStart(shadeRect.x, horForward);
	recordEnd(shadeRect.x + shadeRect.width, horBackward);
	recordStart(shadeRect.y, vertForward);
	recordEnd(shadeRect.y + shadeRect.height, vertBackward);
	if (Math.abs(shadeRect.x) <= imprecision)
	    xMin = shadeRect.x;
	int xMax = shadeRect.x + shadeRect.width;
	if (Math.abs(width - xMax) <= imprecision)
	    xPlus = width - xMax;
	if (Math.abs(shadeRect.y) <= imprecision)
	    yMin = shadeRect.y;
	int yMax = shadeRect.y + shadeRect.height;
	if (Math.abs(height - yMax) <= imprecision)
	    yPlus = height - yMax;
	shadeRect.x -= xMin;
	shadeRect.width += xMin + xPlus;
	shadeRect.y -= yMin;
	shadeRect.height += yMin + yPlus;
    }

    // How much is shade area to be moved back to avoid crack?
    private int extendEnd(int pos, int[] forward) {
	if (pos >= 0 && pos < forward.length)
	    return forward[pos];
	else
	    return 0;
    }

    // How much is shade area to be moved forward to avoid crack?
    private int extendStart(int pos, int[] backward) {
	if (pos >= 0 && pos < backward.length)
	    return backward[pos];
	else
	    return 0;
    }

    // Record allowable moving of next areas.
    private void recordStart(int pos, int[] forward) {
	int min = Math.max(0, pos - imprecision);
	int max = Math.min(forward.length, pos);
	for (int i = min; i < max; i++) 
	    forward[i] = Math.max(forward[i], pos-i);
    }

    // Record allowable moving of next areas.
    private void recordEnd(int pos, int[] backward) {
	int min = Math.max(-1, pos);
	int max = Math.min(backward.length-1, pos + imprecision);
	for (int i = min + 1; i <= max; i++) 
	    backward[i] = Math.max(backward[i], i-pos);
    }

    // For padding, we need to bridge gaps between pos1 and pos2.
    public void recordPadding(int pos1, int pos2) {
	if (isHorizontal) {
	    recordPadding(pos1, pos2, horForward);
	    recordEnd(pos2, horBackward);
	} else {
	    recordPadding(pos1, pos2, vertForward);
	    recordEnd(pos2, vertBackward);
	}
    }
    private void recordPadding(int pos1, int pos2, int[] forward) {
	int min = Math.max(0, pos1 - imprecision);
	int max = Math.min(forward.length, pos2);
	for (int i = min; i < max; i++) 
	    forward[i] = Math.max(forward[i], pos2-i);
    }
}
