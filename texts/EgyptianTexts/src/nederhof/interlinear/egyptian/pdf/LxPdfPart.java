/***************************************************************************/
/*                                                                         */
/*  LxPdfPart.java                                                         */
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

// Lexical entry as part of tier.

package nederhof.interlinear.egyptian.pdf;

import java.awt.*;
import java.util.*;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.*;

import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.res.*;
import nederhof.res.format.*;

public class LxPdfPart extends EgyptianTierPdfPart {

    // Parsing context for hieroglyphic.
    private HieroRenderContext hieroContext =
	new HieroRenderContext(20); // fontsize arbitrary
    private ParsingContext parsingContext =
	new ParsingContext(hieroContext, true);

    public String texthi;
    public String textal;
    public String texttr;
    public String textfo;
    public String cite;
    public String href;
    public String keyhi;
    public String keyal;
    public String keytr;
    public String keyfo;
    public String dicthi;
    public String dictal;
    public String dicttr;
    public String dictfo;

    // Parsed.
    public ResFragment texthiParsed;
    public ResFragment keyhiParsed;
    public ResFragment dicthiParsed;
    // Formatted.
    public FormatFragment texthiFormat;
    public FormatFragment keyhiFormat;
    public FormatFragment dicthiFormat;

    // Should do formatting again?
    private boolean redo = true;

    // Transliteration, split into lower/upper case.
    public Vector textalParts;
    public Vector keyalParts;
    public Vector dictalParts;

    public LxPdfPart(
	    String texthi,
	    String textal,
	    String texttr,
	    String textfo,
	    String cite,
	    String href,
	    String keyhi,
	    String keyal,
	    String keytr,
	    String keyfo,
	    String dicthi,
	    String dictal,
	    String dicttr,
	    String dictfo) {
	this.texthi = texthi;
	this.textal = textal;
	this.texttr = texttr;
	this.textfo = textfo;
	this.cite = cite;
	this.href = href;
	this.keyhi = keyhi;
	this.keyal = keyal;
	this.keytr = keytr;
	this.keyfo = keyfo;
	this.dicthi = dicthi;
	this.dictal = dictal;
	this.dicttr = dicttr;
	this.dictfo = dictfo;
	if (!texthi.matches("\\s*")) 
	    texthiParsed = ResFragment.parse(texthi, parsingContext);
	if (!keyhi.matches("\\s*")) 
	    keyhiParsed = ResFragment.parse(keyhi, parsingContext);
	if (!dicthi.matches("\\s*")) 
	    dicthiParsed = ResFragment.parse(dicthi, parsingContext);
	if (!textal.matches("\\s*")) 
	    textalParts = TransHelper.lowerUpperParts(textal);
	if (!keyal.matches("\\s*")) 
	    keyalParts = TransHelper.lowerUpperParts(keyal);
	if (!dictal.matches("\\s*")) 
	    dictalParts = TransHelper.lowerUpperParts(dictal);
    }

    // If hieroglyphic not formatted with latest context, then do again.
    private void ensureFormatted() {
	if (redo) {
	    if (texthiParsed != null)
		texthiFormat = new FormatFragment(texthiParsed, context());
	    if (keyhiParsed != null)
		keyhiFormat = new FormatFragment(keyhiParsed, context());
	    if (dicthiParsed != null)
		dicthiFormat = new FormatFragment(dicthiParsed, context());
	    redo = false;
	}
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
        if (!breakable(i))
            return Penalties.maxPenalty;
        else
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
	ensureFormatted();
	if (i == j)
	    return 0;
	else 
	    return abbrev() ? abbrevWidth() : fullWidth();
    }

    // Advance from position i to position j.
    // i < nSymbols, j <= nSymbols.
    public float advance(int i, int j) {
	ensureFormatted();
	if (j == nSymbols()) 
	    return (abbrev() ? abbrevWidth() : fullWidth()) + sep();
	else
	    return dist(i, j);
    }

    // Assume starts with space.
    public boolean hasLeadSpace() {
	return true;
    }

    // Spaces at beginning.
    public float leadSpaceAdvance() {
	return egyptUpperFont().getWidthPointKerned(" ", egyptUpperSize());
    }

    // Font metrics.
    public float leading() {
	return lead();
    }
    public float ascent() {
	return egyptUpperFont().getFontDescriptor(BaseFont.ASCENT, egyptUpperSize());
    }
    public float descent() {
	ensureFormatted();
	return Math.max(0, height() - ascent());
    }

    // Height.
    private float height() {
	if (abbrev())
	    return heightAbbrev();
	else
	    return heightFull();
    }

    // Draw.
    public void draw(int i, int j, float x, float y, PdfContentByte surface) {
	ensureFormatted();
	if (abbrev())
	    drawAbbrev(x, y, surface);
	else
	    drawFull(x, y, surface);
    }

    // Draw full form, return height. Only draw if graphics is non-null.
    // Otherwise, this is 'dry-run' to determine height.
    private float drawFull(float x, float y, PdfContentByte surface) {
	y += ascent();
	BaseColor lineColor = BaseColor.DARK_GRAY;
	float contentWidth = rowWidth();
	float thickness = thickness();
	float margin = margin();
	float lineWidth = contentWidth + 2 * (thickness + margin);
	float contentX = x + thickness + margin;
	if (surface != null && thickness > 0) 
	    drawLine(lineColor, x, y - thickness, 
		    lineWidth, thickness, surface);
	float height = thickness;
	float heightLeading = height + margin;
	boolean hasText = false;
	boolean hasCiteRef = false;
	boolean hasKey = false;
	boolean hasDict = false;
	if (texthiFormat != null) {
	    height = heightLeading;
	    if (surface != null) 
		drawHi(texthiFormat, contentX, y - height, surface);
	    height += texthiFormat.height() / context().resolution();
	    heightLeading = height + leadingHi(texthiFormat);
	    hasText = true;
	}
	if (textalParts != null) {
	    height = heightLeading + egyptUpperAscent();
	    if (surface != null) 
		drawAl(textalParts, contentX, y - height, surface);
	    height += egyptUpperDescent();
	    heightLeading = height + egyptUpperLeading();
	    hasText = true;
	}
	if (!texttr.matches("\\s*")) {
	    height = heightLeading + latinAscent();
	    if (surface != null) 
		drawLatin(texttr, contentX, y - height, surface);
	    height += latinDescent();
	    heightLeading = height + latinLeading();
	    hasText = true;
	}
	if (!textfo.matches("\\s*")) {
	    height = heightLeading + italicAscent();
	    if (surface != null) 
		drawItalic(textfo, contentX, y - height, surface);
	    height += italicDescent();
	    heightLeading = height + italicLeading();
	    hasText = true;
	}
	if (hasText) {
	    height += margin;
	    if (surface != null && thickness > 0) 
		drawLine(lineColor, x, y - height - thickness, 
			lineWidth, thickness, surface);
	    height += thickness;
	    heightLeading = height + margin;
	}
	if (!cite.matches("\\s*")) {
	    height = heightLeading + latinAscent();
	    if (surface != null) 
		drawLatin(cite, contentX, y - height, surface);
	    height += latinDescent();
	    heightLeading = height + latinLeading();
	    hasCiteRef = true;
	}
	if (hasCiteRef) {
            height += margin;
            if (surface != null && thickness > 0) 
		drawLine(lineColor, x, y - height - thickness, 
			lineWidth, thickness, surface);
            height += thickness;
            heightLeading = height + margin;
	}
	if (keyhiFormat != null) {
            height = heightLeading;
            if (surface != null) 
		drawHi(keyhiFormat, contentX, y - height, surface);
            height += keyhiFormat.height() / context().resolution();
            heightLeading = height + leadingHi(keyhiFormat);
            hasKey = true;
	}
	if (keyalParts != null) {
            height = heightLeading + egyptUpperAscent();
            if (surface != null) 
		drawAl(keyalParts, contentX, y - height, surface);
            height += egyptUpperDescent();
            heightLeading = height + egyptUpperLeading();
            hasKey = true;
	}
	if (!keytr.matches("\\s*")) {
            height = heightLeading + latinAscent();
            if (surface != null) 
		drawLatin(keytr, contentX, y - height, surface);
            height += latinDescent();
            heightLeading = height + latinLeading();
            hasKey = true;
	}
	if (!keyfo.matches("\\s*")) {
            height = heightLeading + italicAscent();
            if (surface != null) 
		drawItalic(keyfo, contentX, y - height, surface);
            height += italicDescent();
            heightLeading = height + italicLeading();
            hasKey = true;
	}
	if (hasKey) {
            height += margin;
            if (surface != null && thickness > 0) 
		drawLine(lineColor, x, y - height - thickness, 
			lineWidth, thickness, surface);
            height += thickness;
            heightLeading = height + margin;
        }
	if (dicthiFormat != null) {
            height = heightLeading;
            if (surface != null) 
		drawHi(dicthiFormat, contentX, y - height, surface);
            height += dicthiFormat.height() / context().resolution();
            heightLeading = height + leadingHi(dicthiFormat);
            hasKey = true;
	}
	if (dictalParts != null) {
            height = heightLeading + egyptUpperAscent();
            if (surface != null) 
		drawAl(dictalParts, contentX, y - height, surface);
            height += egyptUpperDescent();
            heightLeading = height + egyptUpperLeading();
            hasDict = true;
	}
	if (!dicttr.matches("\\s*")) {
            height = heightLeading + latinAscent();
            if (surface != null) 
		drawLatin(dicttr, contentX, y - height, surface);
            height += latinDescent();
            heightLeading = height + latinLeading();
            hasDict = true;
	}
	if (!dictfo.matches("\\s*")) {
            height = heightLeading + italicAscent();
            if (surface != null) 
		drawItalic(dictfo, contentX, y - height, surface);
            height += italicDescent();
            heightLeading = height + italicLeading();
            hasDict = true;
	}
	if (hasDict || (!hasText && !hasCiteRef && !hasKey && !hasDict)) {
            height += margin;
            if (surface != null && thickness > 0) 
		drawLine(lineColor, x, y - height - thickness, 
			lineWidth, thickness, surface);
            height += thickness;
        }
	if (surface != null && thickness > 0) {
	    drawLine(lineColor, x, y - height, thickness, height, surface);
	    drawLine(lineColor, x + 2 * margin + contentWidth + thickness, 
		    y - height, 
		    thickness, height, surface);
	    if (!href.matches("\\s*")) {
		PdfAction jump = new PdfAction(href);
		surface.setAction(jump, x, y - height,
			x + 2 * (margin + thickness) + contentWidth, y);
	    }
	}
	return height;
    }
    private float heightFull() {
	return drawFull(0, 0, null);
    }

    // Draw abbreviated.
    private float drawAbbrev(float x, float y, PdfContentByte surface) {
	if (textalParts != null) {
	    if (surface != null) {
		drawAl(textalParts, x, y, surface);
		if (!href.matches("\\s*")) {
		    PdfAction jump = new PdfAction(href);
		    surface.setAction(jump, x, y - descent(),
			    x + abbrevWidth(), y + ascent());
		}
	    }
	    return egyptUpperAscent() + egyptUpperDescent();
	} else
	    return 0;
    }
    private float heightAbbrev() {
	return drawAbbrev(0, 0, null);
    }

    ///////////////////////////////////////////
    // Drawing in fonts.

    // Draw transliteration.
    // If surface is null, then this is dry run.
    private float drawAl(Vector parts, float x, float y, PdfContentByte surface) {
	float width = 0;
	if (parts != null) 
	    for (int i = 0; i < parts.size(); i++) {
		Object[] pair = (Object[]) parts.get(i);
		String kind = (String) pair[0];
		String info = (String) pair[1];
		if (kind.equals("translower")) {
		    if (surface != null) {
			surface.setFontAndSize(egyptLowerFont(), egyptLowerSize());
			surface.setColorFill(BaseColor.BLACK);
			surface.setTextMatrix(x + width, y);
			surface.showTextKerned(info);
		    }
		    width += egyptLowerFont().getWidthPointKerned(info, egyptLowerSize());
		} else {
		    if (surface != null) {
			surface.setFontAndSize(egyptUpperFont(), egyptUpperSize());
			surface.setColorFill(BaseColor.BLACK);
			surface.setTextMatrix(x + width, y);
			surface.showTextKerned(info);
		    }
		    width += egyptUpperFont().getWidthPointKerned(info, egyptUpperSize());
		}
	    }
	return width;
    }
    private float widthAl(Vector parts) {
	return drawAl(parts, 0, 0, null);
    }

    // Draw latin.
    private void drawLatin(String  s, float x, float y, PdfContentByte surface) {
	surface.setFontAndSize(latinFont(), latinSize());
	surface.setColorFill(BaseColor.BLACK);
	surface.setTextMatrix(x, y);
	surface.showTextKerned(s);
    }
    // Draw italic.
    private void drawItalic(String  s, float x, float y, PdfContentByte surface) {
	surface.setFontAndSize(italicFont(), italicSize());
	surface.setColorFill(BaseColor.BLACK);
	surface.setTextMatrix(x, y);
	surface.showTextKerned(s);
    }

    // Draw hiero.
    private void drawHi(FormatFragment formatted, 
	    float x, float y, PdfContentByte surface) {
	renderParams.surface.endText();
	renderParams.surface.saveState();
	Graphics2D graphics =
	    new PdfGraphics2D(surface,
		    renderParams.pageWidth,
		    renderParams.pageHeight + renderParams.bottomMargin,
		    context().pdfMapper());
	graphics.translate(Math.round(x),
		Math.round(renderParams.pageHeight + renderParams.bottomMargin
		    - y));
	formatted.write(graphics, 0, 0);
	graphics.dispose();
	renderParams.surface.restoreState();
	renderParams.surface.beginText();
    }

    // Draw line.
    private void drawLine(BaseColor color, float x, float y, 
	    float width, float height, PdfContentByte surface) {
	surface.endText();
	surface.setColorFill(color);
	surface.rectangle(x, y, width, height);
	surface.fill();
	surface.beginText();
    }

    ///////////////////////////////////
    // Auxiliary.

    // Width of full entry. Take maximum.
    private float rowWidth() {
	float max = 0;
	if (texthiFormat != null)
	    max = Math.max(max, texthiFormat.width() / context().resolution());
	max = Math.max(max, widthAl(textalParts));
	max = Math.max(max, latinFont().getWidthPointKerned(texttr, latinSize()));
	max = Math.max(max, italicFont().getWidthPointKerned(textfo, italicSize()));
	if (keyhiFormat != null)
	    max = Math.max(max, keyhiFormat.width() / context().resolution());
	max = Math.max(max, widthAl(keyalParts));
	max = Math.max(max, latinFont().getWidthPointKerned(keytr, latinSize()));
	max = Math.max(max, italicFont().getWidthPointKerned(keyfo, italicSize()));
	max = Math.max(max, latinFont().getWidthPointKerned(cite, latinSize()));
	if (dicthiFormat != null)
	    max = Math.max(max, dicthiFormat.width() / context().resolution());
	max = Math.max(max, widthAl(dictalParts));
	max = Math.max(max, latinFont().getWidthPointKerned(dicttr, latinSize()));
	max = Math.max(max, italicFont().getWidthPointKerned(dictfo, italicSize()));
	return max;
    }

    // Width of full entry.
    private float fullWidth() {
	return rowWidth() + 2 * (thickness() + margin());
    }

    // Width of abbreviated entry.
    private float abbrevWidth() {
	if (textalParts != null)
	    return widthAl(textalParts);
	else
	    return 0;
    }

    // Ascent of hieroglyphic.
    private float leadingHi(FormatFragment format) {
	return format.height() * 0.1f / context().resolution();
    }

    // Leading of latin, italic, upper egyptian.
    private float latinLeading() {
	return latinSize() * renderParams.leadingFactor;
    }
    private float italicLeading() {
	return italicSize() * renderParams.leadingFactor;
    }
    private float egyptUpperLeading() {
	return egyptUpperSize() * renderParams.leadingFactor;
    }

    // Ascent of latin, italic, upper egyptian.
    private float latinAscent() {
	return latinFont().getFontDescriptor(BaseFont.ASCENT, latinSize());
    }
    private float italicAscent() {
	return italicFont().getFontDescriptor(BaseFont.ASCENT, italicSize());
    }
    private float egyptUpperAscent() {
	return egyptUpperFont().getFontDescriptor(BaseFont.ASCENT, egyptUpperSize());
    }

    // Descent of latin, italic, upper egyptian.
    private float latinDescent() {
	return - latinFont().getFontDescriptor(BaseFont.DESCENT, latinSize());
    }
    private float italicDescent() {
	return - italicFont().getFontDescriptor(BaseFont.DESCENT, italicSize());
    }
    private float egyptUpperDescent() {
	return - egyptUpperFont().getFontDescriptor(BaseFont.DESCENT, egyptUpperSize());
    }

    ///////////////////////////////////
    // Parameters.

    // Is abbreviated?
    private boolean abbrev() {
	return renderParams.lxAbbreviated;
    }

    // Thickness of lines.
    private float thickness() {
	return renderParams.lxLineThickness;
    }

    // Margin.
    private float margin() {
	return renderParams.lxInnerMargin;
    }

    // Sep between entries.
    private float sep() {
	return renderParams.lxSep;
    }

    // Leading.
    private float lead() {
	return renderParams.lxLeading;
    }

    // Context for hieroglyphic.
    public HieroRenderContext context() {
	return renderParams.hieroContext;
    }

    // Fonts.
    protected BaseFont latinFont() {
	return renderParams.latinFont;
    }
    protected BaseFont italicFont() {
	return renderParams.italicFont;
    }
    protected BaseFont egyptLowerFont() {
	return renderParams.egyptLowerFont;
    }
    protected BaseFont egyptUpperFont() {
	return renderParams.egyptUpperFont;
    }
    // Sizes.
    protected float latinSize() {
	return renderParams.latinSize;
    }
    protected float italicSize() {
	return renderParams.italicSize;
    }
    protected float egyptLowerSize() {
	return renderParams.egyptLowerSize;
    }
    protected float egyptUpperSize() {
	return renderParams.egyptUpperSize;
    }

}
