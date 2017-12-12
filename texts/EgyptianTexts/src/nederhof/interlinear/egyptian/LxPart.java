/***************************************************************************/
/*                                                                         */
/*  LxPart.java                                                            */
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

package nederhof.interlinear.egyptian;

import java.awt.*;
import java.util.*;

import nederhof.interlinear.*;
import nederhof.res.*;
import nederhof.res.format.*;

public class LxPart extends EgyptianTierAwtPart {

    // Parsing context for hieroglyphic.
    private static final HieroRenderContext hieroContext =
	new HieroRenderContext(20); // fontsize arbitrary
    private static final ParsingContext parsingContext =
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

    // Optional ID, for manual alignment links.
    public String id = "";

    // The context used to format the last time.
    // null if none.
    private HieroRenderContext lastHieroContext;
    // Parsed.
    private ResFragment texthiParsed;
    private ResFragment keyhiParsed;
    private ResFragment dicthiParsed;
    // Formatted.
    private FormatFragment texthiFormat;
    private FormatFragment keyhiFormat;
    private FormatFragment dicthiFormat;

    // Transliteration, split into lower/upper case.
    public Vector textalParts;
    public Vector keyalParts;
    public Vector dictalParts;

    public LxPart(
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
	this(texthi, textal, texttr, textfo, 
		cite, href,
		keyhi, keyal, keytr, keyfo,
		dicthi, dictal, dicttr, dictfo, "");
    }
    public LxPart(
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
	    String dictfo,
	    String id) {
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
	this.id = id;
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
    public LxPart(LxInfo info) {
	this(info, "");
    }
    public LxPart(LxInfo info, String id) {
	this(info.texthi, info.textal, info.texttr, info.textfo,
		info.cite, info.href,
		info.keyhi, info.keyal, info.keytr, info.keyfo,
		info.dicthi, info.dictal, info.dicttr, info.dictfo,
		id);
    }
    public LxPart() {
	this("", "", "", "",
		"", "",
		"", "", "", "",
		"", "", "", "");
    }
    public LxPart(String id) {
	this("", "", "", "",
		"", "",
		"", "", "", "",
		"", "", "", "", id);
    }

    // If hieroglyphic not formatted with latest context, then do again.
    private void ensureFormatted() {
	if (lastHieroContext != context()) {
	    lastHieroContext = context();
	    if (texthiParsed != null)
		texthiFormat = new FormatFragment(texthiParsed, lastHieroContext);
	    if (keyhiParsed != null)
		keyhiFormat = new FormatFragment(keyhiParsed, lastHieroContext);
	    if (dicthiParsed != null)
		dicthiFormat = new FormatFragment(dicthiParsed, lastHieroContext);
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
	return egyptUpperMetrics().stringWidth(" ");
    }

    // Font metrics.
    public float leading() {
	return lead();
    }
    public float ascent() {
	return egyptUpperMetrics().getAscent();
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
    public void draw(int i, int j, int x, int y, Graphics2D g) {
	if (i != j) {
	    ensureFormatted();
	    if (abbrev())
		drawAbbrev(x, y, g);
	    else
		drawFull(x, y, g);
	    if (!highlightsAfter.isEmpty()) {
		g.setColor(Color.BLUE);
		g.fillRect(x + Math.round(advance(i, j)) - highlightBarWidth, 
			y - Math.round(ascent()), 
			highlightBarWidth, Math.round(height()));
	    }
	}
    }

    public int getPos(int i, int j, int x, int y) {
        if (i != j && x >= 0 && x < advance(i, j))
            return 0;
        else
            return -1;
    }

    // Draw full form, return height. Only draw if graphics is non-null.
    // Otherwise, this is 'dry-run' to determine height.
    private float drawFull(int x, int y, Graphics2D g) {
	y -= Math.round(ascent());
	Color lineColor = highlights.isEmpty() ? Color.DARK_GRAY : Color.BLUE;
	int contentWidth = Math.round(rowWidth());
	int thickness = Math.round(thickness());
	int margin = Math.round(margin());
	int lineWidth = contentWidth + 2 * (thickness + margin);
	int contentX = x + thickness + margin;
	if (g != null) {
	    g.setColor(lineColor);
	    g.fillRect(x, y, lineWidth, thickness);
	}
	int height = thickness;
	int heightLeading = height + margin;
	boolean hasText = false;
	boolean hasCiteRef = false;
	boolean hasKey = false;
	boolean hasDict = false;
	if (texthiFormat != null) {
	    height = heightLeading;
	    if (g != null) 
		texthiFormat.write(g, contentX, y + height);
	    height += texthiFormat.height();
	    heightLeading = height + Math.round(leadingHi(texthiFormat));
	    hasText = true;
	}
	if (textalParts != null) {
	    height = heightLeading + egyptUpperMetrics().getAscent();
	    if (g != null) 
		drawAl(textalParts, contentX, y + height, g);
	    height += egyptUpperMetrics().getDescent();
	    heightLeading = height + egyptUpperMetrics().getLeading();
	    hasText = true;
	}
	if (!texttr.matches("\\s*")) {
	    height = heightLeading + latinMetrics().getAscent();
	    if (g != null) 
		drawLatin(texttr, contentX, y + height, g);
	    height += latinMetrics().getDescent();
	    heightLeading = height + latinMetrics().getLeading();
	    hasText = true;
	}
	if (!textfo.matches("\\s*")) {
	    height = heightLeading + italicMetrics().getAscent();
	    if (g != null) 
		drawItalic(textfo, contentX, y + height, g);
	    height += italicMetrics().getDescent();
	    heightLeading = height + italicMetrics().getLeading();
	    hasText = true;
	}
	if (hasText) {
	    height += margin;
	    if (g != null) {
		g.setColor(lineColor);
		g.fillRect(x, y + height, lineWidth, thickness);
	    }
	    height += thickness;
	    heightLeading = height + margin;
	}
	if (!cite.matches("\\s*")) {
	    height = heightLeading + latinMetrics().getAscent();
	    if (g != null) 
		drawLatin(cite, contentX, y + height, g);
	    height += latinMetrics().getDescent();
	    heightLeading = height + latinMetrics().getLeading();
	    hasCiteRef = true;
	}
	if (hasCiteRef) {
            height += margin;
            if (g != null) {
		g.setColor(lineColor);
		g.fillRect(x, y + height, lineWidth, thickness);
            }
            height += thickness;
            heightLeading = height + margin;
	}
	if (keyhiFormat != null) {
            height = heightLeading;
            if (g != null) 
		keyhiFormat.write(g, contentX, y + height);
            height += keyhiFormat.height();
            heightLeading = height + Math.round(leadingHi(keyhiFormat));
            hasKey = true;
	}
	if (keyalParts != null) {
            height = heightLeading + egyptUpperMetrics().getAscent();
            if (g != null) 
		drawAl(keyalParts, contentX, y + height, g);
            height += egyptUpperMetrics().getDescent();
            heightLeading = height + egyptUpperMetrics().getLeading();
            hasKey = true;
	}
	if (!keytr.matches("\\s*")) {
            height = heightLeading + latinMetrics().getAscent();
            if (g != null) 
		drawLatin(keytr, contentX, y + height, g);
            height += latinMetrics().getDescent();
            heightLeading = height + latinMetrics().getLeading();
            hasKey = true;
	}
	if (!keyfo.matches("\\s*")) {
            height = heightLeading + italicMetrics().getAscent();
            if (g != null) 
		drawItalic(keyfo, contentX, y + height, g);
            height += italicMetrics().getDescent();
            heightLeading = height + italicMetrics().getLeading();
            hasKey = true;
	}
	if (hasKey) {
            height += margin;
            if (g != null) {
		g.setColor(lineColor);
		g.fillRect(x, y + height, lineWidth, thickness);
            }
            height += thickness;
            heightLeading = height + margin;
        }
	if (dicthiFormat != null) {
            height = heightLeading;
            if (g != null) 
		dicthiFormat.write(g, contentX, y + height);
            height += dicthiFormat.height();
            heightLeading = height + Math.round(leadingHi(dicthiFormat));
            hasKey = true;
	}
	if (dictalParts != null) {
            height = heightLeading + egyptUpperMetrics().getAscent();
            if (g != null) 
		drawAl(dictalParts, contentX, y + height, g);
            height += egyptUpperMetrics().getDescent();
            heightLeading = height + egyptUpperMetrics().getLeading();
            hasDict = true;
	}
	if (!dicttr.matches("\\s*")) {
            height = heightLeading + latinMetrics().getAscent();
            if (g != null) 
		drawLatin(dicttr, contentX, y + height, g);
            height += latinMetrics().getDescent();
            heightLeading = height + latinMetrics().getLeading();
            hasDict = true;
	}
	if (!dictfo.matches("\\s*")) {
            height = heightLeading + italicMetrics().getAscent();
            if (g != null) 
		drawItalic(dictfo, contentX, y + height, g);
            height += italicMetrics().getDescent();
            heightLeading = height + italicMetrics().getLeading();
            hasDict = true;
	}
	if (hasDict || (!hasText && !hasCiteRef && !hasKey && !hasDict)) {
            height += margin;
            if (g != null) {
		g.setColor(lineColor);
		g.fillRect(x, y + height, lineWidth, thickness);
            }
            height += thickness;
        }
	if (g != null) {
	    g.setColor(lineColor);
	    g.fillRect(x, y, thickness, height);
	    g.fillRect(x + 2 * margin + contentWidth + thickness, y, thickness, height);
	}
	return height;
    }
    private float heightFull() {
	return drawFull(0, 0, null);
    }

    // Draw abbreviated.
    private float drawAbbrev(int x, int y, Graphics2D g) {
	if (textalParts != null) {
	    if (g != null)
		drawAl(textalParts, x, y, g);
	    return egyptUpperMetrics().getAscent()
		+ egyptUpperMetrics().getDescent();
	} else
	    return 0;
    }
    private float heightAbbrev() {
	return drawAbbrev(0, 0, null);
    }

    ///////////////////////////////////////////
    // Drawing in fonts.

    // Draw transliteration.
    // If graphics is null, then this is dry run.
    private float drawAl(Vector parts, int x, int y, Graphics2D g) {
	float width = 0;
	if (parts != null) 
	    for (int i = 0; i < parts.size(); i++) {
		Object[] pair = (Object[]) parts.get(i);
		String kind = (String) pair[0];
		String info = (String) pair[1];
		if (kind.equals("translower")) {
		    if (g != null) {
			g.setFont(egyptLowerFont());
			g.setColor(textColor());
			g.drawString(info, x + width, y);
		    }
		    width += egyptLowerMetrics().stringWidth(info);
		} else {
		    if (g != null) {
			g.setFont(egyptUpperFont());
			g.setColor(textColor());
			g.drawString(info, x + width, y);
		    }
		    width += egyptUpperMetrics().stringWidth(info);
		}
	    }
	return width;
    }
    private float widthAl(Vector parts) {
	return drawAl(parts, 0, 0, null);
    }

    // Draw latin.
    private void drawLatin(String  s, int x, int y, Graphics2D g) {
	g.setFont(latinFont());
	g.setColor(textColor());
	g.drawString(s, x, y);
    }
    // Draw italic.
    private void drawItalic(String  s, int x, int y, Graphics2D g) {
	g.setFont(italicFont());
	g.setColor(textColor());
	g.drawString(s, x, y);
    }

    private Color textColor() {
	return highlights.isEmpty() ? Color.BLACK : Color.BLUE;
    }

    ///////////////////////////////////
    // Auxiliary.

    // Width of full entry. Take maximum.
    private float rowWidth() {
	float max = 0;
	if (texthiFormat != null)
	    max = Math.max(max, texthiFormat.width());
	max = Math.max(max, widthAl(textalParts));
	max = Math.max(max, latinMetrics().stringWidth(texttr));
	max = Math.max(max, italicMetrics().stringWidth(textfo));
	if (keyhiFormat != null)
	    max = Math.max(max, keyhiFormat.width());
	max = Math.max(max, widthAl(keyalParts));
	max = Math.max(max, latinMetrics().stringWidth(keytr));
	max = Math.max(max, italicMetrics().stringWidth(keyfo));
	max = Math.max(max, latinMetrics().stringWidth(cite));
	if (dicthiFormat != null)
	    max = Math.max(max, dicthiFormat.width());
	max = Math.max(max, widthAl(dictalParts));
	max = Math.max(max, latinMetrics().stringWidth(dicttr));
	max = Math.max(max, italicMetrics().stringWidth(dictfo));
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
	return format.height() * 0.1f;
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
    protected Font latinFont() {
	return renderParams.latinFont;
    }
    protected Font italicFont() {
	return renderParams.italicFont;
    }
    protected Font egyptLowerFont() {
	return renderParams.egyptLowerFont;
    }
    protected Font egyptUpperFont() {
	return renderParams.egyptUpperFont;
    }
    // Metrics.
    protected FontMetrics latinMetrics() {
	return renderParams.latinFontMetrics;
    }
    protected FontMetrics italicMetrics() {
	return renderParams.italicFontMetrics;
    }
    protected FontMetrics egyptLowerMetrics() {
	return renderParams.egyptLowerFontMetrics;
    }
    protected FontMetrics egyptUpperMetrics() {
	return renderParams.egyptUpperFontMetrics;
    }

}
