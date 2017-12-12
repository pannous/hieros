package nederhof.ocr;

import java.awt.*;
import java.io.*;
import java.util.*;

import nederhof.ocr.admin.*;
import nederhof.ocr.guessing.*;
import nederhof.ocr.images.*;
import nederhof.util.*;

// Combines parts of glyphs in one line. 
public class GlyphCombiner extends OcrProcessTask {

	// For which line is the combiner.
	public Line line;

	public GlyphCombiner(Line line) {
		this.line = line;
	}

	// By default does nothing.
	public void combineLine(OcrGuesser guesser) {
	}

	// Get name of page.
	public String page() {
		return line.page();
	}

	// Y coordinate.
	public int y() {
		return line.y();
	}

}
