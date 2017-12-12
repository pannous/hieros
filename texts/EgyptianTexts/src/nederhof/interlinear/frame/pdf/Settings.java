/***************************************************************************/
/*                                                                         */
/*  Settings.java                                                          */
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

// Various constants of PDF output.

package nederhof.interlinear.frame.pdf;

import java.awt.*;

import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;

import nederhof.fonts.*;

class Settings {

    // Additional TrueType fonts for export to PDF.
    // These are intended for scripts like Polish, with symbols not in
    // standard PDF fonts.
    // The files are usually in nederhof/fonts.
    // Ideally, there are 4 different files: plain, italic, bold, bold italic.
    public static final ExternalFont[] externalFonts =
    { new ExternalFont("QuasiCourier",
            "data/fonts/qcr/qcrr.ttf", 
	    "data/fonts/qcr/qcrri.ttf", 
	    "data/fonts/qcr/qcrb.ttf", 
	    "data/fonts/qcr/qcrbi.ttf"),
        new ExternalFont("QuasiBookman",
            "data/fonts/qbk/qbkr.ttf", 
	    "data/fonts/qbk/qbkri.ttf", 
	    "data/fonts/qbk/qbkb.ttf", 
	    "data/fonts/qbk/qbkbi.ttf") };

    // PDF page format.
    public static final com.itextpdf.text.Rectangle pdfPageSizeDefault = PageSize.A4;
    public static final int pdfTopMarginDefault = 36;
    public static final int pdfLeftMarginDefault = 36;
    public static final int pdfBottomMarginDefault = 45;
    public static final int pdfRightMarginDefault = 36;

    // As above.
    public static final int pdfColSepDefault = 5;
    public static final int pdfSectionSepDefault = 15;
    public static final int pdfLineSepDefault = 5;
    public static final int pdfFootnoteLineSepDefault = 2;
    public static final boolean pdfUniformAscentDefault = false;
    public static final boolean pdfCollectNotesDefault = false;

    // The amount that footnote markers are to be raised above baseline,
    // as factor of ascent.
    public static final float pdfFootRaiseDefault = 0.6f;
    public static final int pdfFootFontSizeReductionDefault = 85;

    // As above.
    // The font name may be an external font name, like externalFonts[0].getName().
    public static final String pdfLatinFontNameDefault =
            com.itextpdf.text.FontFactory.HELVETICA;
    public static final int pdfLatinFontStyleDefault = Font.PLAIN;
    public static final int pdfLatinFontSizeDefault = 12;

    // Use of color in PDF file.
    public static final boolean pdfColorDefault = true;
    // Colors.
    public static final Color pdfLabelColorDefault = Color.MAGENTA;
    public static final Color pdfCoordColorDefault = Color.RED;
    public static final Color pdfVersionColorDefault = Color.BLUE;
    public static final Color pdfFootnoteMarkerColorDefault = Color.BLUE;
    public static final Color pdfHyperlinkColorDefault = Color.BLUE;

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
