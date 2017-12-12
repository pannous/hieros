package nederhof.ocr;

import java.util.*;

// Records about page in OCR.
public class Page {

	// Name (usually number) of page.
	public String name;

	// The lines.
	public Vector<Line> lines = new Vector<Line>();

	// Glyphs with confirmed names not belonging 
	// to any lines. Will not be saved to file.
	public Vector<Blob> orphanGlyphs = new Vector<Blob>();

	// Constructor.
	public Page(String name) {
		this.name = name;
	}

}
