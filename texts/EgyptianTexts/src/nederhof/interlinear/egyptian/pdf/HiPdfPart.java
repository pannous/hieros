/***************************************************************************/
/*                                                                         */
/*  HiPdfPart.java                                                         */
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

// Hieroglypic part of tier.

package nederhof.interlinear.egyptian.pdf;

import java.awt.*;
import java.util.*;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.pdf.*;

import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.frame.*;
import nederhof.res.*;
import nederhof.res.format.*;

public class HiPdfPart extends EgyptianTierPdfPart {

    // Parsing context.
    private HieroRenderContext hieroContext = 
	new HieroRenderContext(20); // fontsize arbitrary
    private ParsingContext parsingContext =
	new ParsingContext(hieroContext, true);

    // Hieroglyphic as string.
    public String hi;

    // Footnotes attached.
    private Vector<Footnote> notes = new Vector<Footnote>();
    // Set footnotes.
    public void setNotes(Vector<Footnote> notes) {
	this.notes = notes;
    }

    // Parsed RES.
    public ResFragment parsed;
    // Formatted RES.
    private FormatFragment formatted;
    // Number of symbols therein.
    private int nSymbols;
    // Breakable symbols.
    private boolean[] breakables;

    // Should do again?
    private boolean redo = true;

    public HiPdfPart(String hi, boolean note) {
	this.hi = hi;
	setFootnote(note);
        parsed = ResFragment.parse(hi, parsingContext);
	if (parsed != null) {
	    nSymbols = parsed.nGlyphs();
	    breakables = parsed.glyphAfterPaddable();
	} else {
	    nSymbols = 0;
	    breakables = null;
	}
    }

    private void ensureFormatted() {
	if (redo) {
	    if (parsed != null)
		formatted = new FormatFragment(parsed, context());
	    redo = false;
	}
    }

    // How many symbols.
    // Pretend there is one symbol if none.
    public int nSymbols() {
	if (nSymbols == 0 && parsed.nGroups() > 0)
	    return 1;
	return nSymbols;
    }

    // Is position breakable?
    public boolean breakable(int i) {
	if (nSymbols == 0) 
	    return next == null || next.hasLeadSpace();
	if (i == nSymbols)
	    return next == null || next.hasLeadSpace() ||
		next instanceof CoordPdfPart;
	else if (breakables == null)
	    return false;
	else 
	    return breakables[i];
    }

    // Penalty of line break at breakable position.
    public double penalty(int i) {
	if (!breakable(i))
	    return Penalties.maxPenalty;
	else if (i == nSymbols()) {
	    if (isFootnote())
		return Penalties.spacePenalty;
	    else
		return 0;
	} else
	    return Penalties.hierobreakPenalty;
    }

    // Distance of position j from position i.
    // We look at location of symbol following position j,
    // with text from position i onward.
    // i <= j < nSymbols.
    public float dist(int i, int j) {
	ensureFormatted();
	if (formatted != null) {
	    int groupI = i == 0 ? 0 : formatted.glyphToGroup(i);
	    int groupJ = formatted.glyphToGroup(j);
	    return formatted.widthDist(groupI, groupJ) / context().resolution();
	} else 
	    return 0;
    }

    // Width from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float width(int i, int j) {
	ensureFormatted();
	if (i == j || formatted == null)
	    return 0;
	else {
	    int groupI = i == 0 ? 0 : formatted.glyphToGroup(i);
	    int groupJ = formatted.glyphToGroupAndEmpty(j-1);
	    return formatted.width(groupI, groupJ) / context().resolution();
	}
    }

    // Advance from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float advance(int i, int j) {
	ensureFormatted();
	if (j == nSymbols() && formatted != null) {
	    int groupI = i == 0 ? 0 : formatted.glyphToGroup(i);
	    int groupJ = formatted.glyphToGroup(j);
	    return (formatted.width(groupI, groupJ) +
		context().emToPix(context().fontSep())) / context().resolution();
	} else
	    return dist(i, j);
    }

    // Space at beginning after other element.
    public float leadSpaceAdvance() {
	return context().emToPix(context().fontSep()) / context().resolution();
    }

    // Font metrics.
    public float leading() {
	return ascent() * renderParams.leadingFactor;
    }
    public float ascent() {
	ensureFormatted();
	if (formatted != null)
	    return formatted.height() / context().resolution();
	else
	    return 0;
    }
    public float descent() {
	return 0;
    }

    // Assign markers to footnotes between boundaries.
    // Then place footnotes in hieroglyphic,
    // Avoid work if no footnotes within range.
    public void situate(int i, int j) {
	boolean change = false;
	for (int n = 0; n < notes.size(); n++) {
	    Footnote note = notes.get(n);
	    if (i <= note.symbol() && note.symbol() < j) {
		change = true;
		note.setMarker(renderParams.getMarker());
	    }
	}
	if (change) {
	    parsed = ResFragment.parse(hi, parsingContext);
	    for (int n = 0; n < notes.size(); n++) {
		Footnote note = notes.get(n);
		parsed.addNote(note.symbol(), note.makeMarker(), new Color16("blue"));
	    }
	    redo = true;
	    formatted = new FormatFragment(parsed, context());
	}
    }

    public TreeSet<Footnote> getFootnotes(int i, int j) {
	TreeSet<Footnote> footnotes = new TreeSet<Footnote>();
	for (int n = 0; n < notes.size(); n++) {
	    Footnote note = notes.get(n);
	    if (i <= note.symbol() && note.symbol() < j) 
		footnotes.add(note);
	}
	return footnotes;
    }

    // Draw substring.
    public void draw(int i, int j, float x, float y, PdfContentByte surface) {
	ensureFormatted();
	if (i != j && formatted != null) {
	    int height = formatted.height() / context().resolution();
	    int groupI = i == 0 ? 0 : formatted.glyphToGroup(i);
	    int groupJ = formatted.glyphToGroupAndEmpty(j-1);
	    renderParams.surface.endText();
	    renderParams.surface.saveState();
	    Graphics2D graphics = 
		new PdfGraphics2D(surface,
			renderParams.leftMargin + renderParams.pageWidth + 
				renderParams.rightMargin, 
			renderParams.topMargin + renderParams.pageHeight + 
				renderParams.bottomMargin,
			context().pdfMapper());
	    graphics.translate(Math.round(x),
		    Math.round(renderParams.topMargin +
			renderParams.pageHeight + renderParams.bottomMargin
			- y - height));
	    formatted.write(graphics, groupI, groupJ, 0, 0);
	    graphics.dispose();
	    renderParams.surface.restoreState();
	    renderParams.surface.beginText();
	}
    }

    // Context depends on whether in footnote.
    public HieroRenderContext context() {
	if (isFootnote()) 
	    return renderParams.footHieroContext;
	else
	    return renderParams.hieroContext;
    }

}
