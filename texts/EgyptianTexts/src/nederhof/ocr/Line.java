package nederhof.ocr;

import java.awt.*;
import java.util.*;

// Records about line in OCR.

public class Line extends OcrProcessTask {

	// Name of page in which it occurs.
	private String page;

	// Bottom Y coordinate.
	private int y;

	// Direction of text. Can be: hlr, hrl, vlr, vrl.
	public String dir;

	// Delimiting surface on page.
	public Polygon polygon;

	// The glyphs in this line.
	public Vector<Blob> glyphs = new Vector<Blob>();

	// The formatted elements.
	public Vector<LineFormat> formatted = new Vector<LineFormat>();

	// Constructor.
	public Line(String dir, Polygon polygon, String page) {
		this.dir = dir;
		this.polygon = polygon;
		this.page = page;
		Rectangle rect = polygon.getBounds();
		this.y = rect.y + rect.height;
	}

	// Get bottom of line.
	public int y() {
		return y;
	}

	// Get name of page.
	public String page() {
		return page;
	}

	// Safe access routine to glyphs, making sure no
	// obsolete ones are returned.
	public Vector<Blob> aliveGlyphs() {
		Vector<Blob> bs = new Vector<Blob>();
		for (Blob b : glyphs) 
			if (!b.isObsolete())
				bs.add(b);
		return bs;
	}

}
