/***************************************************************************/
/*                                                                         */
/*  PageNumberer.java                                                      */
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

// Puts page numbers in PDF file.

package nederhof.interlinear.frame.pdf;

import com.itextpdf.text.BaseColor;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.Anchor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
// import com.itextpdf.text.HeaderFooter; // not used?
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.RomanList;
import com.itextpdf.text.factories.RomanNumberFactory;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfDestination;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfPageLabels;
import com.itextpdf.text.pdf.PdfWriter;

public class PageNumberer extends PdfPageEventHelper {

    private boolean romanNumbers;

    // To be printed at Roman numbers, or Arabic?
    public PageNumberer(boolean roman) {
	romanNumbers = roman;
    }

    // Change to roman.
    public void setRoman(boolean roman) {
	romanNumbers = roman;
    }

    // Print page number in middle of lower margin.
    // May be Roman or Arabic.
    public void onEndPage(PdfWriter writer, Document doc) {
	String page;
	if (romanNumbers)
	    page = RomanNumberFactory.getString(doc.getPageNumber());
	else
	    page = Integer.toString(doc.getPageNumber());
	PdfContentByte surface = writer.getDirectContent();
	BaseFont font = FontFactory.getFont(
		Settings.pdfPageNumberFontNameDefault).getBaseFont();
	Rectangle rect = doc.getPageSize();
	float fontSize = Settings.pdfPageNumberFontSizeDefault;
	float width = font.getWidthPointKerned(page, fontSize);
	float x = (rect.getWidth() -
		width - doc.leftMargin() - doc.rightMargin()) / 2 + doc.leftMargin();
	float height = font.getAscentPoint(page, fontSize) -
	    font.getDescentPoint(page, fontSize);
	float y = (doc.bottomMargin() - height) * 0.66f;
	y = Math.abs(y);
	y -= font.getDescentPoint(page, fontSize);
	surface.beginText();
	surface.setFontAndSize(font, fontSize);
	surface.setColorFill(BaseColor.BLACK);
	surface.setTextMatrix(x, y);
	surface.showTextKerned(page);
	surface.endText();
    }
}
