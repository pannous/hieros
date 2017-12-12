package nederhof.ocr.hiero;

import java.awt.*;
import java.util.*;

import nederhof.ocr.*;
import nederhof.ocr.images.*;

public abstract class HieroOcrPage extends OcrPage {

	// Constructor.
	public HieroOcrPage(BinaryImage im, Page page) {
		super(im, page);
	}

	// Communication to caller.
	protected abstract void findGlyph();
	protected abstract void findExtra();

	// OcrLine connected to this.
	private class ConnectedLine extends OcrLine {
		public ConnectedLine(Line line, double subScale) {
			super(im, line, subScale);
		}
		protected OcrProcess getProcess() {
			return HieroOcrPage.this.getProcess();
		}
		protected LayoutAnalyzer getAnalyzer() {
			return HieroOcrPage.this.getAnalyzer();
		}
		protected GlyphCombiner createCombiner(Line line) {
			return HieroOcrPage.this.createCombiner(line);
		}
		protected BlobFormatter getFormatter() {
			return HieroOcrPage.this.getFormatter();
		}
		protected boolean getAutoSegment() {
			return HieroOcrPage.this.getAutoSegment();
		}
		protected boolean getAutoOcr() {
			return HieroOcrPage.this.getAutoOcr();
		}
		protected boolean getAutoFormat() {
			return HieroOcrPage.this.getAutoFormat();
		}
		protected void allowEdits(boolean allow) {
			HieroOcrPage.this.allowEdits(allow);
		}
		protected void setWait(boolean wait) {
			HieroOcrPage.this.setWait(wait);
		}
		protected void setFocus(Line line) {
			focusLine = line;
			setLines();
		}
		protected Vector<Blob> orphans() {
			return page.orphanGlyphs;
		}
	}
	// OcrLineFocus connected to this.
	private class ConnectedLineFocus extends HieroOcrLineFocus {
		public ConnectedLineFocus(Line line, double subScale) {
			super(im, line, subScale);
		}
		protected OcrProcess getProcess() {
			return HieroOcrPage.this.getProcess();
		}
		protected LayoutAnalyzer getAnalyzer() {
			return HieroOcrPage.this.getAnalyzer();
		}
		protected GlyphCombiner createCombiner(Line line) {
			return HieroOcrPage.this.createCombiner(line);
		}
		protected BlobFormatter getFormatter() {
			return HieroOcrPage.this.getFormatter();
		}
		protected boolean getAutoSegment() {
			return HieroOcrPage.this.getAutoSegment();
		}
		protected boolean getAutoOcr() {
			return HieroOcrPage.this.getAutoOcr();
		}
		protected boolean getAutoFormat() {
			return HieroOcrPage.this.getAutoFormat();
		}
		protected void setFocus(Line line) {
			focusLine = line;
			setLines();
		}
		protected void findGlyph() {
			HieroOcrPage.this.findGlyph();
		}
		protected void findExtra() {
			HieroOcrPage.this.findExtra();
		}
		protected void removeGlyph(Blob glyph) {
			HieroOcrPage.this.removeGlyph(glyph);
		}
		protected void allowEdits(boolean allow) {
			HieroOcrPage.this.allowEdits(allow);
		}
		protected void setWait(boolean wait) {
			HieroOcrPage.this.setWait(wait);
		}
		protected Vector<Blob> orphans() {
			return page.orphanGlyphs;
		}
	}

	protected OcrLine createOcrLine(Line line, double subScale) {
		return new ConnectedLine(line, subScale);
	}
	protected OcrLineFocus createOcrLineFocus(Line line, double subScale) {
		return new ConnectedLineFocus(line, subScale);
	}

    /////////////////////////////////////////////////////
    // Specific to hieroglyphs.
	
    // Direction is derived from shape (landscape/portrait).
    protected String dir(Polygon poly) {
        Rectangle rect = poly.getBounds();
        if (rect.width >= rect.height)
            return "hlr";
        else
            return "vlr";
    }

}
