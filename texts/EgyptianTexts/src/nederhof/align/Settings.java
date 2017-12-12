/***************************************************************************/
/*                                                                         */
/*  Settings.java                                                          */
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

// Various constants.

package nederhof.align;

import java.awt.*;

import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;

import nederhof.fonts.*;

class Settings {

    // Names of files where egyptological fonts can be found.
    // (Usually in directory nederhof/fonts.)
    public static final String egyptFontFilePlain = "data/fonts/Umsch_s.ttf";
    public static final String egyptFontFileBold = "data/fonts/Umsch_sb.ttf";
    public static final String egyptFontFileIt = "data/fonts/Umsch_i.ttf";
    public static final String egyptFontFileBoldIt = "data/fonts/Umsch_ib.ttf";
    public static final String egyptFontName = "data/fonts/Umschrift_TTn";
    // Mapping from ASCII to transliteration characters.
    public static final String egyptMapFile = "data/fonts/jungemapping.txt";

    // Initial dimensions of entire screen.
    public static final int displayWidthInit = 700;
    public static final int displayHeightInit = 800;
    // Same for resource window.
    public static final int preambleWidthInit = 600;
    public static final int preambleHeightInit = 700;
    // Same for HTML window.
    public static final int htmlWidthInit = 600;
    public static final int htmlHeightInit = 700;

    // Margins left and right of the actual text.
    // Space between version label and text.
    // Vertical space between paragraphs and between lines within paragraphs.
    // Vertical space between lines of footnotes.
    // All lines to be made of uniform ascent w.r.t. points.
    // Footnotes collected at end.
    public static final int leftMarginDefault = 15;
    public static final int rightMarginDefault = 20;
    public static final int colSepDefault = 5;
    public static final int parSepDefault = 30;
    public static final int lineSepDefault = 15;
    public static final int footnoteLineSepDefault = 2;
    public static final boolean uniformAscentDefault = false;
    public static final boolean collectNotesDefault = false;

    // The amount that footnote markers are to be raised above baseline,
    // as factor of ascent.
    public static final float footRaiseDefault = 0.6f;

    // Options for rendering lexical entries.
    public static final int lxSepDefault = 10;
    public static final int lxLeadingDefault = 10;
    public static final int lxInnerMarginDefault = 5;
    public static final int lxLineThicknessDefault = 2;
    public static final boolean lxAbbreviatedDefault = false;

    // Options for rendering text.
    public static final String latinFontNameDefault = "SansSerif";
    public static final Integer latinFontStyleDefault = FontTypeRenderer.plainInt;
    public static final Integer latinFontSizeDefault = new Integer(14);
    public static final Integer egyptFontStyleDefault = FontTypeRenderer.plainInt;
    public static final Integer egyptFontSizeDefault = new Integer(20);
    public static final Integer hieroFontSizeDefault = new Integer(28);
    public static final Integer footFontSizeReductionDefault = new Integer(85);

    // Colors.
    public static final Color pointColorDefault = Color.RED;
    public static final Color labelColorDefault = Color.MAGENTA;
    public static final Color noteColorDefault = Color.BLUE;

    // Preamble.
    public static final int preambleLeftMargin = 5;
    public static final int preambleRightMargin = 10;
    public static final int preambleTopMargin = 10;
    public static final int preambleBottomMargin = 10;
    public static final int preambleParSep = 6;
    public static final int headerFontStyle = Font.BOLD;
    public static final float header1Increase = 1.8f;
    public static final float header2Increase = 1.4f;
    public static final float header3Increase = 1.2f;
    public static final float header1PreSep = 0.3f;
    public static final float header1PostSep = 0.2f;
    public static final float header2PreSep = 0.3f;
    public static final float header2PostSep = 0.2f;
    public static final float header3PreSep = 0.3f;
    public static final float header3PostSep = 0.2f;

    ////////////////////////////////////////////////////////////////////////
    // Export of PDF

    // Additional TrueType fonts for export to PDF.
    // These are intended for scripts like Polish, with symbols not in 
    // standard PDF fonts.
    // The files are usually in nederhof/fonts.
    // Ideally, there are 4 different files: plain, italic, bold, bold italic.
    public static final ExternalFont[] externalFonts = 
    { new ExternalFont("QuasiCourier", 
	    "qcr/qcrr.ttf", "qcr/qcrri.ttf", "qcr/qcrb.ttf", "qcr/qcrbi.ttf"),
	new ExternalFont("QuasiBookman", 
	    "qbk/qbkr.ttf", "qbk/qbkri.ttf", "qbk/qbkb.ttf", "qbk/qbkbi.ttf") };

    // Path to directory where PDF files are to be saved.
    // Is null when default directory is home directory.
    public static final String pdfSavePath = null;

    // PDF page format.
    public static final com.itextpdf.text.Rectangle pdfPageSizeDefault = PageSize.A4;
    public static final int pdfTopMarginDefault = 36;
    public static final int pdfLeftMarginDefault = 36;
    public static final int pdfBottomMarginDefault = 60;
    public static final int pdfRightMarginDefault = 36;

    // As above.
    public static final int pdfColSepDefault = 5;
    public static final int pdfParSepDefault = 15;
    public static final int pdfLineSepDefault = 5;
    public static final int pdfFootnoteLineSepDefault = 2;
    public static final boolean pdfUniformAscentDefault = false;
    public static final boolean pdfCollectNotesDefault = false;

    // The amount that footnote markers are to be raised above baseline,
    // as factor of ascent.
    public static final float pdfFootRaiseDefault = 0.6f;

    // As above.
    public static final int pdfLxSepDefault = 10;
    public static final int pdfLxLeadingDefault = 10;
    public static final int pdfLxInnerMarginDefault = 5;
    public static final int pdfLxLineThicknessDefault = 1;
    public static final boolean pdfLxAbbreviatedDefault = false;

    // As above.
    // The font name may be an external font name, like externalFonts[0].getName().
    public static final String pdfLatinFontNameDefault = 
	    com.itextpdf.text.FontFactory.HELVETICA;
    public static final Integer pdfLatinFontSizeDefault = new Integer(12);
    public static final Integer pdfEgyptFontStyleDefault = FontTypeRenderer.plainInt;
    public static final Integer pdfEgyptFontSizeDefault = new Integer(16);
    public static final Integer pdfHieroFontSizeDefault = new Integer(24);
    public static final Integer pdfFootFontSizeReductionDefault = new Integer(85);

    // If hieroglyphic printed as pixels, how many pixels per point.
    public static final Integer pdfHieroResolutionDefault = new Integer(4);

    // Use of color in PDF file.
    public static final boolean pdfColorDefault = false;
    // Colors.
    public static final Color pdfPointColorDefault = Color.RED;
    public static final Color pdfLabelColorDefault = Color.MAGENTA;
    public static final Color pdfNoteColorDefault = Color.BLUE;

    // Page numbers.
    public static final String pdfPageNumberFontNameDefault = 
	    com.itextpdf.text.FontFactory.COURIER;
    public static final int pdfPageNumberFontSizeDefault = 14;

    // Preamble.
    // The style must be one of NORMAL, BOLD, ITALIC, BOLDITALIC.
    public static final float pdfPreambleParSep = 3.0f;
    public static final int pdfHeaderFontStyle = com.itextpdf.text.Font.BOLD;
    public static final float pdfHeader1Increase = 1.8f;
    public static final float pdfHeader2Increase = 1.4f;
    public static final float pdfHeader3Increase = 1.2f;
}
