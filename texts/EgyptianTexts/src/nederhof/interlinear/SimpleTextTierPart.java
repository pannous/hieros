/***************************************************************************/
/*                                                                         */
/*  SimpleTextTierPart.java                                                */
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

// Tier part consisting of text, with each character having 1 unit width.
// Mainly used for testing.

package nederhof.interlinear;

import java.awt.*;

class SimpleTextTierPart extends TextTierPart {

    public SimpleTextTierPart(String text) {
	super(text);
    }

    public float dist(int i, int j) {
	return originalPos(j) - originalPos(i);
    }

    public float width(int i, int j) {
	int iOrig = originalPos(i);
	int jOrig = 
	    (i < j && followsSpace[j]) ? originalPos(j-1)+1 : originalPos(j);
	return jOrig - iOrig;
    }

    public float advance(int i, int j) {
	return dist(i, j);
    }

    public float leading() {
	return 0;
    }

    public float ascent() {
	return 1;
    }

    public float descent() {
	return 0;
    }

    public void draw(int i, int j, int x, int y, Graphics2D g) {
    }

    // Penalty.
    public double penalty(int i) {
        return i >= nSymbols() ? endPenalty : spacePenalty;
    }

}

