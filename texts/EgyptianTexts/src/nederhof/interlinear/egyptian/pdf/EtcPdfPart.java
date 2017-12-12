/***************************************************************************/
/*                                                                         */
/*  EtcPdfPart.java                                                        */
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

package nederhof.interlinear.egyptian.pdf;

import com.itextpdf.text.pdf.*;

import nederhof.interlinear.*;

public class EtcPdfPart extends EgyptianTierPdfPart {

    public EtcPdfPart() {
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
	return 0;
    }

    // Advance from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float advance(int i, int j) {
	return 0;
    }

    // Font metrics.
    public float leading() {
	return 0;
    }
    public float ascent() {
	if (next == null)
	    return 0;
	else
	    return next.ascent();
    }
    public float descent() {
	return 0;
    }

    // Draw is nothing here. 
    public void draw(int i, int j, float x, float y, PdfContentByte surface) {
	// nothing
    }

    // Is not content.
    public boolean isContent() {
	return false;
    }

}
