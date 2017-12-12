// Element of image annotation.
// This is a dummy, as this will never be printed in PDF.

package nederhof.interlinear.egyptian.pdf;

import java.awt.*;
import java.util.*;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.*;

import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.egyptian.image.*;
import nederhof.res.*;
import nederhof.res.format.*;

public class ImagePlacePdfPart extends EgyptianTierPdfPart {

    // All information on sign.
    public ImageSign info;

    // Constructor.
    public ImagePlacePdfPart(ImageSign info) {
	this.info = info;
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
	return 0;
    }

    // Advance from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float advance(int i, int j) {
	return 0;
    }

    // Spaces at beginning.
    public float leadSpaceAdvance() {
        return 0;
    }

    // Font metrics.
    public float leading() {
        return 0;
    }
    public float ascent() {
        return 0;
    }
    public float descent() {
        return 0;
    }

    // Draw.
    public void draw(int i, int j, float x, float y, PdfContentByte surface) {
    }

}

