// Element of orthograph annotation.

package nederhof.interlinear.egyptian;

import java.awt.*;
import java.util.*;

import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.ortho.*;
import nederhof.res.*;
import nederhof.res.format.*;

public class OrthoPart extends EgyptianTierAwtPart {

    // Parsing context.
    public static final HieroRenderContext hieroContext =
        new HieroRenderContext(20); // fontsize arbitrary
    private static final ParsingContext parsingContext =
        new ParsingContext(hieroContext, true);

    // The hieroglyphic.
    public String texthi;
    // The context used to format the last time.
    // null if none.
    private HieroRenderContext lastHieroContext;
    // Parsed.
    public ResFragment texthiParsed;
    // Formatted.
    private FormatFragment texthiFormat;

    // The transliteration.
    public String textal;
    // Transliteration, split into lower/upper case.
    public Vector<Object[]> textalParts;
    // Similarly, but with extra space between each two letters.
    public Vector<Object[]> textalPartsWide;

    // The orthographic elements.
    public Vector<OrthoElem> textortho;

    // ID, for manual alignment links.
    public String id = "";

    // Constructor.
    public OrthoPart(String texthi, String textal, Vector<OrthoElem> textortho) {
	this(texthi, textal, textortho, "");
    }
    public OrthoPart(String texthi, String textal, Vector<OrthoElem> textortho, String id) {
	this.texthi = texthi;
        this.textal = textal;
        this.textortho = new Vector<OrthoElem>(textortho);
	this.id = id;
	OrthoHelper.sort(this.textortho);
        if (!texthi.matches("\\s*"))
            texthiParsed = ResFragment.parse(texthi, parsingContext);
        if (!textal.matches("\\s*")) {
            textalParts = TransHelper.lowerUpperParts(textal);
            textalPartsWide = widen(textalParts);
	}
    }

    // If hieroglyphic not formatted with latest context, then do again.
    private void ensureFormatted() {
        if (lastHieroContext != context()) {
            lastHieroContext = context();
            if (texthiParsed != null) {
                texthiFormat = new FormatFragment(texthiParsed, lastHieroContext);
		int orthoWidth = Math.round(orthoWidth());
		if (orthoWidth > Math.round(texthiFormat.width())) {
		    int pad = orthoWidth - Math.round(texthiFormat.width());
		    texthiFormat = new FormatFragment(texthiParsed, 
			    lastHieroContext, pad);
		}
	    }
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
	int firstRowSep = latinMetrics().getAscent();
	int secondRowSep = 2 * latinMetrics().getAscent();
	// anchors to various elements 
	int hieroY = 0;
	int orthoYtop = 0;
	int orthoYbottom = 0;
	int[] orthoXs = textortho.size() > 0 ? 
	    new int[textortho.size()] : null;
	int alX = 0;
	int alY = 0;
	if (orthoWidth() > widthAl(textalPartsWide))
	    alX = Math.round(orthoWidth() - widthAl(textalPartsWide)) / 2;
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
	if (texthiFormat != null) {
	    hieroY = heightLeading;
	    height = heightLeading;
	    if (g != null)
		texthiFormat.write(g, contentX, y + height);
	    height += texthiFormat.height();
	    heightLeading = height + Math.round(leadingHi(texthiFormat));
	    heightLeading += firstRowSep;
	}
	if (textortho.size() > 0) {
	    orthoYtop = heightLeading;
	    height = heightLeading + latinMetrics().getAscent();
	    if (g != null) {
		int orthoX = contentX;
		int n = 0;
		for (int i = 0; i < textortho.size(); i++) {
		    OrthoElem ortho = textortho.get(i);
		    String name = ortho.name();
		    drawLatin(name, orthoX, y + height, g);
		    orthoXs[n++] =
			orthoX + latinMetrics().stringWidth(name) / 2;
		    orthoX += latinMetrics().stringWidth(name) +
			orthoSep();
		}
	    }
	    height += latinMetrics().getDescent();
	    orthoYbottom = height;
	    heightLeading = height + latinMetrics().getLeading();
	    heightLeading += secondRowSep;
	}
	if (textalPartsWide != null) {
	    alY = heightLeading;
	    height = heightLeading + egyptUpperMetrics().getAscent();
	    if (g != null)
		drawAl(textalPartsWide, contentX + alX, y + height, g);
	    height += egyptUpperMetrics().getDescent();
	    heightLeading = height + egyptUpperMetrics().getLeading();
	}
	height += margin;
	if (g != null) {
	    g.setColor(lineColor);
	    g.fillRect(x, y + height, lineWidth, thickness);
	}
	height += thickness;
	if (g != null) {
	    g.setColor(lineColor);
	    g.fillRect(x, y, thickness, height);
	    g.fillRect(x + 2 * margin + contentWidth + thickness, y, thickness, height);
	    Vector<Rectangle> glyphRects = texthiFormat != null ?
		texthiFormat.glyphRectangles() : new Vector<Rectangle>();
	    for (int i = 0; i < textortho.size(); i++) {
		OrthoElem ortho = textortho.get(i);
		int[] signs = ortho.signs();
		int[] letters = ortho.letters();
		if (signs != null) 
		    for (int j = 0; j < signs.length; j++) {
			if (0 <= signs[j] && signs[j] < glyphRects.size()) {
			    Rectangle rect = glyphRects.get(signs[j]);
			    g.drawLine(contentX + rect.x + rect.width / 2, 
				     y + hieroY + rect.y + rect.height,
				     orthoXs[i], y + orthoYtop);
			}
		    }
		if (letters != null)
		    for (int j = 0; j < letters.length; j++) {
			int xLetter = Math.round(middleLetter(textalPartsWide, letters[j]));
			if (xLetter >= 0)
			    g.drawLine(orthoXs[i], y + orthoYbottom,
				    contentX + alX + xLetter, y + alY);
		    }
	    }
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
    private float drawAl(Vector<Object[]> parts, int x, int y, Graphics2D g) {
        float width = 0;
        if (parts != null)
            for (int i = 0; i < parts.size(); i++) {
                Object[] pair = parts.get(i);
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

    // Get middle of i-th letter.
    private float middleLetter(Vector<Object[]> parts, int pos) {
	pos *= 2; // every letter followed by extra space.
	float width = 0;
	if (parts != null)
	    for (int i = 0; i < parts.size(); i++) {
		Object[] pair = parts.get(i);
		String kind = (String) pair[0];
		String info = (String) pair[1];
		if (kind.equals("translower")) {
		    if (pos < info.length()) {
			String prefix = info.substring(0, pos);
			String letter = info.substring(pos, pos+1);
			return width + egyptLowerMetrics().stringWidth(prefix) +
			    egyptLowerMetrics().stringWidth(letter) / 2;
		    }
		    width += egyptLowerMetrics().stringWidth(info);
		    pos -= info.length();
		} else {
		    if (pos < info.length()) {
			String prefix = info.substring(0, pos);
			String letter = info.substring(pos, pos+1);
			return width + egyptUpperMetrics().stringWidth(prefix) +
			    egyptUpperMetrics().stringWidth(letter) / 2;
		    }
		    width += egyptUpperMetrics().stringWidth(info);
		    pos -= info.length();
		}
	    }
	return 0;
    }

    // Draw latin.
    private void drawLatin(String  s, int x, int y, Graphics2D g) {
        g.setFont(latinFont());
        g.setColor(textColor());
        g.drawString(s, x, y);
    }

    private Color textColor() {
        return highlights.isEmpty() ? Color.BLACK : Color.BLUE;
    }

    ///////////////////////////////////
    // Auxiliary.

    // Space between orthographic elements.
    private float orthoSep() {
	return latinMetrics().stringWidth("  ");
    }

    // Width of full entry. Take maximum of three rows.
    private float rowWidth() {
        float max = 0;
        if (texthiFormat != null)
            max = Math.max(max, texthiFormat.width());
	if (textalPartsWide != null)
	    max = Math.max(max, widthAl(textalPartsWide));
        max = Math.max(max, orthoWidth());
        return max;
    }

    // Width of orthographic elements.
    private float orthoWidth() {
	float width = 0;
	for (int i = 0; i < textortho.size(); i++) {
	    OrthoElem ortho = textortho.get(i);
	    String name = ortho.name();
	    width += latinMetrics().stringWidth(name);
	}
	if (textortho.size() > 0)
	    width += (textortho.size()-1) * orthoSep();
	return width;
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

    // Insert space after each letter, in vector of parts.
    public static Vector<Object[]> widen(Vector parts) {
	Vector wideParts = new Vector();
	for (int j = 0; j < parts.size(); j++) {
	    Object[] pair = (Object[]) parts.get(j);
	    String kind = (String) pair[0];
	    String info = (String) pair[1];
	    StringBuffer b = new StringBuffer();
	    for (int i = 0; i < info.length(); i++) {
		b.append("" + info.charAt(i));
		if (j < parts.size() - 1 || i < info.length() - 1)
		    b.append(" ");
	    }
	    String wideInfo = b.toString();
	    wideParts.add(new Object[] {kind, wideInfo});
	}
	return wideParts;
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
    protected FontMetrics egyptLowerMetrics() {
        return renderParams.egyptLowerFontMetrics;
    }
    protected FontMetrics egyptUpperMetrics() {
        return renderParams.egyptUpperFontMetrics;
    }

}
