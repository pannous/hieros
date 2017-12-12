// Element of orthograph annotation.

package nederhof.interlinear.egyptian.pdf;

import java.awt.*;
import java.util.*;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.*;

import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.egyptian.ortho.*;
import nederhof.res.*;
import nederhof.res.format.*;

public class OrthoPdfPart extends EgyptianTierPdfPart {

	// Parsing context for hieroglyphic.
	private HieroRenderContext hieroContext =
		new HieroRenderContext(20); // fontsize arbitrary
	private ParsingContext parsingContext =
		new ParsingContext(hieroContext, true);

	// The hieroglyphic.
	public String texthi;
	// Parsed.
	public ResFragment texthiParsed;
	// Formatted.
	private FormatFragment texthiFormat;

	// The transliteration.
	public String textal;
	// Transliteration, split into lower/upper case.
	public Vector textalParts;
	// Similarly, but with extra space between each two letters.
	public Vector textalPartsWide;

	// The orthographic elements.
	public Vector<OrthoElem> textortho;

	// Should do formatting again?
	private boolean redo = true;

	// Constructor.
	public OrthoPdfPart(String texthi, String textal, Vector<OrthoElem> textortho) {
		this.texthi = texthi;
		this.textal = textal;
		this.textortho = textortho;
		if (!texthi.matches("\\s*"))
			texthiParsed = ResFragment.parse(texthi, parsingContext);
		if (!textal.matches("\\s*")) {
			textalParts = TransHelper.lowerUpperParts(textal);
			textalPartsWide = OrthoPart.widen(textalParts);
		}
	}

	// If hieroglyphic not formatted with latest context, then do again.
	private void ensureFormatted() {
		if (redo) {
			if (texthiParsed != null) {
				texthiFormat = new FormatFragment(texthiParsed, context());
				float hiWidth = texthiFormat.width() / context().resolution();
				float orthoWidth = Math.round(orthoWidth());
				if (orthoWidth > hiWidth) {
					float pad = orthoWidth - hiWidth;
					texthiFormat = new FormatFragment(texthiParsed,
						context(), Math.round(pad * context().resolution()));
				}
			}
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
		float firstRowSep = latinAscent();
		float secondRowSep = 2 * latinAscent();
		float orthoLineSep = 0.2f * latinSize();
		// anchors to various elements
		float hieroY = 0;
		float orthoYtop = 0;
		float orthoYbottom = 0;
		float[] orthoXs = textortho.size() > 0 ?
			new float[textortho.size()] : null;
		float alX = 0;
		float alY = 0;
		if (orthoWidth() > widthAl(textalPartsWide))
			alX = Math.round(orthoWidth() - widthAl(textalPartsWide)) / 2;
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
		if (texthiFormat != null) {
			hieroY = heightLeading + orthoLineSep;
			height = heightLeading;
			if (surface != null) 
				drawHi(texthiFormat, contentX, y - height, surface);
			height += texthiFormat.height() / context().resolution();
			heightLeading = height + leadingHi(texthiFormat);
			heightLeading += firstRowSep;
		}
		if (textortho.size() > 0) {
			orthoYtop = heightLeading - orthoLineSep;
			height = heightLeading + latinAscent();
			if (surface != null) {
				float orthoX = contentX;
				int n = 0;
				for (int i = 0; i < textortho.size(); i++) {
					OrthoElem ortho = (OrthoElem) textortho.get(i);
					String name = ortho.name();
					drawLatin(name, orthoX, y - height, surface);
					orthoXs[n++] =
						orthoX + latinFont().getWidthPointKerned(name, latinSize()) / 2;
					orthoX += latinFont().getWidthPointKerned(name, latinSize()) +
						orthoSep();
				}
			}
			height += latinDescent();
			orthoYbottom = height + orthoLineSep;
			heightLeading = height + latinLeading();
			heightLeading += secondRowSep;
		}
		if (textalPartsWide != null) {
			alY = heightLeading - orthoLineSep;
			height = heightLeading + egyptUpperAscent();
			if (surface != null) 
				drawAl(textalPartsWide, contentX + alX, y - height, surface);
			height += egyptUpperDescent();
			heightLeading = height + egyptUpperLeading();
		}
		height += margin;
		if (surface != null && thickness > 0)
			drawLine(lineColor, x, y - height - thickness, 
					lineWidth, thickness, surface);
		height += thickness;
		if (surface != null && thickness > 0) {
			drawLine(lineColor, x, y - height, thickness, height, surface);
			drawLine(lineColor, x + 2 * margin + contentWidth + thickness, 
					y - height, 
					thickness, height, surface);
		}
		if (surface != null) {
			float orthoLineWidth = 0.5f * thickness();
			Vector glyphRects = texthiFormat != null ?
				texthiFormat.glyphRectangles() : new Vector();
			for (int i = 0; i < textortho.size(); i++) {
				OrthoElem ortho = (OrthoElem) textortho.get(i);
				int[] signs = ortho.signs();
				int[] letters = ortho.letters();
				if (signs != null)
					for (int j = 0; j < signs.length; j++) {
						if (0 <= signs[j] && signs[j] < glyphRects.size()) {
							Rectangle rect = (Rectangle) glyphRects.get(signs[j]);
							float scaledX = rect.x / context().resolution();
							float scaledY = rect.y / context().resolution();
							float scaledWidth = rect.width / context().resolution();
							float scaledHeight = rect.height / context().resolution();
							drawLineBetween(lineColor, orthoLineWidth,
									contentX + scaledX + scaledWidth / 2,
									y - hieroY - scaledY - scaledHeight,
									orthoXs[i], y - orthoYtop, surface);
						}
					}
				if (letters != null)
					for (int j = 0; j < letters.length; j++) {
						int xLetter = Math.round(middleLetter(textalPartsWide, letters[j]));
						if (xLetter >= 0)
							drawLineBetween(lineColor, orthoLineWidth,
								orthoXs[i], y - orthoYbottom,
								contentX + alX + xLetter, y - alY, surface);
					}
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
			if (surface != null) 
				drawAl(textalParts, x, y, surface);
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

	// Get middle of i-th letter.
	private float middleLetter(Vector parts, int pos) {
		pos *= 2; // every letter followed by extra space.
		float width = 0;
		if (parts != null)
			for (int i = 0; i < parts.size(); i++) {
				Object[] pair = (Object[]) parts.get(i);
				String kind = (String) pair[0];
				String info = (String) pair[1];
				if (kind.equals("translower")) {
					if (pos < info.length()) {
						String prefix = info.substring(0, pos);
						String letter = info.substring(pos, pos+1);
						return width + egyptLowerFont().
								getWidthPointKerned(prefix, egyptLowerSize()) +
							egyptLowerFont().getWidthPointKerned(letter, egyptLowerSize()) / 2;
					}
					width += egyptLowerFont().getWidthPointKerned(info, egyptLowerSize());
					pos -= info.length();
				} else {
					if (pos < info.length()) {
						String prefix = info.substring(0, pos);
						String letter = info.substring(pos, pos+1);
						return width + egyptUpperFont().
								getWidthPointKerned(prefix, egyptUpperSize()) +
							egyptUpperFont().getWidthPointKerned(letter, egyptUpperSize()) / 2;
					}
					width += egyptUpperFont().getWidthPointKerned(info, egyptUpperSize());
					pos -= info.length();
				}
			}
		return 0;
	}

	// Draw latin.
	private void drawLatin(String  s, float x, float y, PdfContentByte surface) {
		surface.setFontAndSize(latinFont(), latinSize());
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

	// Draw line between two points.
	private void drawLineBetween(BaseColor color, float width, 
					float x0, float y0, float x1, float y1, PdfContentByte surface) {
		surface.endText();
		surface.setColorFill(color);
		surface.setLineWidth(width);
		surface.setGrayStroke(0.2f); // almost black
		surface.moveTo(x0, y0); 
		surface.lineTo(x1, y1);
		surface.stroke();
		surface.beginText();
	}

	///////////////////////////////////
	// Auxiliary.

	// Space between orthographic elements.
	private float orthoSep() {
		return latinFont().getWidthPointKerned("  ", latinSize());
	}

	// Width of full entry. Take maximum.
	private float rowWidth() {
		float max = 0;
		if (texthiFormat != null)
			max = Math.max(max, texthiFormat.width() / context().resolution());
		if (textalPartsWide != null)
			max = Math.max(max, widthAl(textalPartsWide));
		max = Math.max(max, orthoWidth());
		return max;
	}

	// Width of orthographic elements.
	private float orthoWidth() {
		float width = 0;
		for (int i = 0; i < textortho.size(); i++) {
			OrthoElem ortho = (OrthoElem) textortho.get(i);
			String name = ortho.name();
			width += latinFont().getWidthPointKerned(name, latinSize());
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

	// Ascent of hieroglyphic.
	private float leadingHi(FormatFragment format) {
		return format.height() * 0.1f / context().resolution();
	}

	// Leading of latin, italic, upper egyptian.
	private float latinLeading() {
		return latinSize() * renderParams.leadingFactor;
	}
	private float egyptUpperLeading() {
		return egyptUpperSize() * renderParams.leadingFactor;
	}

	// Ascent of latin, italic, upper egyptian.
	private float latinAscent() {
		return latinFont().getFontDescriptor(BaseFont.ASCENT, latinSize());
	}
	private float egyptUpperAscent() {
		return egyptUpperFont().getFontDescriptor(BaseFont.ASCENT, egyptUpperSize());
	}

	// Descent of latin, italic, upper egyptian.
	private float latinDescent() {
		return - latinFont().getFontDescriptor(BaseFont.DESCENT, latinSize());
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
	protected float egyptLowerSize() {
		return renderParams.egyptLowerSize;
	}
	protected float egyptUpperSize() {
		return renderParams.egyptUpperSize;
	}

}
