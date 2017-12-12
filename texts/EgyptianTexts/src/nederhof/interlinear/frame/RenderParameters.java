/***************************************************************************/
/*                                                                         */
/*  RenderParameters.java                                                  */
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

// Parameters for rendering interlinear text.

package nederhof.interlinear.frame;

import java.awt.*;
import java.util.*;
import javax.swing.*;

import nederhof.interlinear.*;

public abstract class RenderParameters {

    // Margins left and right of the actual text.
    public int leftMargin;
    public int rightMargin;
    // Space between version label and text.
    public int colSep;
    // Vertical space between sections.
    public int sectionSep;
    // Vertical space between lines within sections.
    public int lineSep;
    // Vertical space between lines of footnotes.
    public int footnoteLineSep;
    // All lines to be made of uniform ascent w.r.t. labels.
    public boolean uniformAscent;
    // Footnotes collected at end.
    public boolean collectNotes;
    // Percentage size reduction.
    public int footFontSizeReduction;

    // Listener to changed in parameters.
    public FormatListener listener;

    public RenderParameters() {
	setDefaults();
    }

    // Set target frame for font metrics.
    // Only relevant for AWT, not for PDF.
    public void setTargetFrame(JFrame frame) {
    }

    public void setListener(FormatListener listener) {
	this.listener = listener;
    }

    protected void setDefaults() {
	leftMargin = leftMarginDefault();
	rightMargin = rightMarginDefault();
	colSep = colSepDefault();
	sectionSep = sectionSepDefault();
	lineSep = lineSepDefault();
	footnoteLineSep = footnoteLineSepDefault();
	uniformAscent = uniformAscentDefault();
	collectNotes = collectNotesDefault();
	footFontSizeReduction = footFontSizeReductionDefault();
    }

    protected int leftMarginDefault() {
	return Settings.leftMarginDefault;
    }
    protected int rightMarginDefault() {
	return Settings.rightMarginDefault;
    }
    protected int colSepDefault() {
	return Settings.colSepDefault;
    }
    protected int sectionSepDefault() {
	return Settings.sectionSepDefault;
    }
    protected int lineSepDefault() {
	return Settings.lineSepDefault;
    }
    protected int footnoteLineSepDefault() {
	return Settings.footnoteLineSepDefault;
    }
    protected boolean uniformAscentDefault() {
	return Settings.uniformAscentDefault;
    }
    protected boolean collectNotesDefault() {
	return Settings.collectNotesDefault;
    }
    protected int footFontSizeReductionDefault() {
	return Settings.footFontSizeReductionDefault;
    }
    public Color footnoteMarkerColor() {
	return Settings.footnoteMarkerColor;
    }

    // To be defined by subclass.
    // Edit above and other parameters by GUI
    public abstract void edit();

    // To be called by subclass.
    protected void reformat() {
	if (listener != null)
	    listener.reformat();
    }

    // Disposing of resources. May be overridden in subclass.
    public void dispose() {
	// nothing
    }

    ////////////////////////////////////////////
    // Footnote markers.

    // Number of footnote counters in use.
    private int marker;

    // Last established footnote counter.
    private int markerFixed;

    // Pending notes.
    private TreeSet<Footnote> pendingNotes;

    // Initiate counters of footnotes.
    // Before formatting of sections. 
    public void initiateSituation() {
        marker = 0;
        markerFixed = 0;
        pendingNotes = new TreeSet<Footnote>();
    }

    // Get next marker.
    public int getMarker() {
        return ++marker;
    }

    // Reset marker to beginning of section.
    public void resetMarker() {
	marker = markerFixed;
    }

    // Add pending note.
    public void addFootnotes(Vector<Footnote> notes) {
	for (int i = 0; i < notes.size(); i++) {
	    Footnote note = notes.get(i);
	    pendingNotes.add(note);
	    markerFixed = Math.max(markerFixed, note.getMarker());
	    marker = markerFixed;
	}
    }

    // Retrieve pending notes.
    public Vector<Footnote> getPendingNotes() {
        Vector<Footnote> notes = new Vector(pendingNotes);
        pendingNotes = new TreeSet<Footnote>();
        return notes;
    }

}
