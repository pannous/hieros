/***************************************************************************/
/*                                                                         */
/*  PdfRenderParameters.java                                               */
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

// Parameters for rendering PDF.

package nederhof.interlinear.frame.pdf;

import java.awt.*;
import java.io.*;
import java.util.*;

import com.itextpdf.text.Document;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import nederhof.fonts.*;
import nederhof.interlinear.*;
import nederhof.interlinear.frame.*;

public abstract class PdfRenderParameters extends RenderParameters {

    // Directory where saved.
    public String savePath = ".";
    // Target file,
    public File file;
    // Header.
    public String header;

    public void setSavePath(String path) {
	savePath = path;
    }

    public void setNames(String baseName, String name) {
	File dir = new File(savePath);
	file = new File(dir, baseName + ".pdf");
	header = name;
    }

    public com.itextpdf.text.Rectangle pageSize;
    public int topMargin;
    public int bottomMargin;
    public String latinFontName;
    public int latinFontStyle;
    public int latinFontSize;
    public boolean color;

    protected void setDefaults() {
	super.setDefaults();
	pageSize = pageSizeDefault();
	topMargin = topMarginDefault();
	bottomMargin = bottomMarginDefault();
	latinFontName = latinFontNameDefault();
	latinFontStyle = latinFontStyleDefault();
	latinFontSize = latinFontSizeDefault();
	color = colorDefault();
    }

    // As in RenderParameters:
    protected int leftMarginDefault() {
        return Settings.pdfLeftMarginDefault;
    }
    protected int rightMarginDefault() {
        return Settings.pdfRightMarginDefault;
    }
    protected int colSepDefault() {
        return Settings.pdfColSepDefault;
    }
    protected int sectionSepDefault() {
        return Settings.pdfSectionSepDefault;
    }
    protected int lineSepDefault() {
        return Settings.pdfLineSepDefault;
    }
    protected int footnoteLineSepDefault() {
        return Settings.pdfFootnoteLineSepDefault;
    }
    protected boolean uniformAscentDefault() {
        return Settings.pdfUniformAscentDefault;
    }
    protected boolean collectNotesDefault() {
        return Settings.pdfCollectNotesDefault;
    }
    protected int footFontSizeReductionDefault() {
        return Settings.pdfFootFontSizeReductionDefault;
    }
    public Color footnoteMarkerColor() {
	return Settings.pdfFootnoteMarkerColorDefault;
    }
    public Color hyperlinkColor() {
	return Settings.pdfHyperlinkColorDefault;
    }

    // Specific to PDF.
    public ExternalFont[] externalFonts() {
	return Settings.externalFonts;
    }
    public com.itextpdf.text.Rectangle pageSizeDefault() {
	return Settings.pdfPageSizeDefault;
    }
    public int topMarginDefault() {
	return Settings.pdfTopMarginDefault;
    }
    public int bottomMarginDefault() {
	return Settings.pdfBottomMarginDefault;
    }
    public float footRaiseDefault() {
	return Settings.pdfFootRaiseDefault;
    }
    public String latinFontNameDefault() {
	return Settings.pdfLatinFontNameDefault;
    }
    public int latinFontStyleDefault() {
	return Settings.pdfLatinFontStyleDefault;
    }
    public int latinFontSizeDefault() {
	return Settings.pdfLatinFontSizeDefault;
    }
    public boolean colorDefault() {
	return Settings.pdfColorDefault;
    }
    public String pageNumberFontNameDefault() {
	return Settings.pdfPageNumberFontNameDefault;
    }
    public int pageNumberFontSizeDefault() {
	return Settings.pdfPageNumberFontSizeDefault;
    }
    public float preambleParSep() {
	return Settings.pdfPreambleParSep;
    }
    public int headerFontStyle() {
	return Settings.pdfHeaderFontStyle;
    }
    public float header1Increase() {
	return Settings.pdfHeader1Increase;
    }
    public float header2Increase() {
	return Settings.pdfHeader2Increase;
    }
    public float header3Increase() {
	return Settings.pdfHeader3Increase;
    }

    public abstract void computeFonts();

    // Ordinary font.
    public BaseFont plainFont;
    // Ordinary font size.
    public float plainSize;

    // Bold.
    public BaseFont boldFont;
    public float boldSize;

    // Tier part for title.
    public abstract TierPdfPart getTitlePdfPart(String title);
    // Tier part for name of resource.
    public abstract TierPdfPart getNamePdfPart(String name);

    /////////////////////////////////////////////////////////
    // Intermediate results of writing to PDF file.

    // The PDF document and the writer.
    public Document doc;
    public PdfWriter writer;
    public PdfContentByte surface;

    // Dimensions of pages.
    public float pageWidth;
    public float pageHeight;

    // Position on current page.
    public float pageY;

    //////////////////////////////////////////
    // Defined by subclass.

    // Get authors of viewed resources.
    // Null if no such information available.
    public String getAuthors(Vector resources) {
	return null;
    }

}
