/***************************************************************************/
/*                                                                         */
/*  ITextDraw.java                                                         */
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

// Wrapped iText drawing methods, in generalized framework.

package nederhof.align;

import java.awt.*;
import java.io.*;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfContentByte;

import nederhof.res.*;
import nederhof.res.format.*;

class ITextDraw implements GeneralDraw {

    // For mapping font code to iText font.
    private ITextFontMapper mapper;

    // Surface on which to write, low y is up, high y is down.
    private PdfContentByte surface;

    // Start y position of paragraph.
    private float startY;

    // Size of page.
    private float width;
    private float height;

    // Same surface as above, but Graphics2D.
    private Graphics2D graphics;

    public ITextDraw(ITextFontMapper m, PdfContentByte cb, 
	    float y, float width, float height) {
	mapper = m;
	surface = cb;
	startY = y;
	this.width = width;
	this.height = height;
    }

    public void drawString(int font, Color color, String s, float x, float y) {
	if (s.equals(""))
	    return;
	BaseColor bcolor = new BaseColor(color.getRed(),
		color.getGreen(), color.getBlue(), color.getAlpha());
	BaseFont f = mapper.getBaseFont(font);
	float size = mapper.getFontSize(font);
	surface.setFontAndSize(f, size);
	surface.setColorFill(bcolor);
	surface.setTextMatrix(x, startY-y);
	surface.showTextKerned(s);
    }

    // Before drawing graphics, suspend text part.
    public void drawHiero(FormatFragment hiero, float x, float y) {
	surface.endText();
	surface.saveState();
	/* old
	graphics = surface.createGraphics(width, height, 
		mapper.getHieroFontMapper());
		*/
	graphics = new PdfGraphics2D(surface, width, height, 
		mapper.getHieroFontMapper());
	graphics.translate(Math.round(x), Math.round(height-startY+y));
	// hiero.write(graphics, 0, 0);
	graphics.dispose();
	surface.restoreState();
	surface.beginText();
    }

    // Before drawing line, suspend text part.
    public void fillRect(Color color, float x, float y, 
	    float width, float height) {
	BaseColor bcolor = new BaseColor(color.getRed(),
		color.getGreen(), color.getBlue(), color.getAlpha());
	surface.endText();
	surface.setColorFill(bcolor);
	surface.rectangle(x, startY-y-height, width, height);
	surface.fill();
	surface.beginText();
    }

    public void fillOval(Color color, float x, float y,
	    float width, float height) {
	BaseColor bcolor = new BaseColor(color.getRed(),
		color.getGreen(), color.getBlue(), color.getAlpha());
	surface.endText();
	surface.setColorFill(bcolor);
	surface.ellipse(x, startY-y-height, 
		x + width, startY-y);
	surface.fill();
	surface.beginText();
    }

    // Draw button with hyperlink.
    public void drawUrlButton(float llx, float lly, float urx, float ury,
	    float x, float y,
	    int font, String text, String url) {
	BaseFont f = mapper.getBaseFont(font);
	float size = mapper.getFontSize(font);
	surface.endText();
	surface.drawButton(llx, startY-lly, urx, startY-ury, "", f, size);
	PdfAction jump = new PdfAction(url);
	surface.setAction(jump, llx, startY-lly, urx, startY-ury);
	surface.beginText();
	surface.setFontAndSize(f, size);
	surface.setColorFill(BaseColor.BLACK);
	surface.setTextMatrix(x, startY-y);
	surface.showTextKerned(text);
    }

    // Same as above, but for local link.
    public void drawLocalButton(float llx, float lly, float urx, float ury,
	                float x, float y,
			int font, String text, String local) {
	BaseFont f = mapper.getBaseFont(font);
	float size = mapper.getFontSize(font);
	surface.endText();
	surface.drawButton(llx, startY-lly, urx, startY-ury, "", f, size);
	surface.localGoto(local, llx, startY-lly, urx, startY-ury);
	surface.beginText();
	surface.setFontAndSize(f, size);
	surface.setColorFill(BaseColor.BLACK);
	surface.setTextMatrix(x, startY-y);
	surface.showTextKerned(text);
    }
}
