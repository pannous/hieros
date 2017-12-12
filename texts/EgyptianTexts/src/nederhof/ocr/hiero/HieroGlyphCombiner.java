package nederhof.ocr.hiero;

import java.awt.*;
import java.io.*;
import java.util.*;

import nederhof.ocr.*;
import nederhof.ocr.admin.*;
import nederhof.ocr.guessing.*;
import nederhof.ocr.hiero.admin.*;
import nederhof.ocr.images.*;
import nederhof.util.*;

// Combines parts of glyphs in one line. 
public class HieroGlyphCombiner extends GlyphCombiner {

	public HieroGlyphCombiner(Line line) {
		super(line);
	}

	// Find part of glyphs. Find possibly related ones.
	// Try combining them to get better OCR scores.
	// Also correct numbers within circle-like glyphs.
	public void combineLine(OcrGuesser guesser) {
		// unit computed only once
		int unit = -1;
		Vector<Blob> glyphs = line.aliveGlyphs();
		for (int i = 0; i < glyphs.size(); i++) {
			Blob b = glyphs.get(i);
			if (b.isObsolete())
				continue;
			String name = nameOf(b);
			if (!name.matches(".*\\[part\\]") && b.getName().equals(""))
				name = secondNameOf(b);
			if (name.matches(".*\\[part\\]")) 
				unit = mergeWithOtherParts(guesser, glyphs, i, b, name, unit);
			else {
				name = nameOf(b);
				if (name.matches("[0-9]")) 
					avoidCircledGlyphs(glyphs, b);
			}
		}
	}

	// Get name of page.
	public String page() {
		return line.page();
	}

	// Y coordinate.
	public int y() {
		return line.y();
	}

	// From i-th glyph with 'part' name, 
	// try to merge with connected parts.
	private int mergeWithOtherParts(OcrGuesser guesser,
			Vector<Blob> glyphs, int i, Blob b, String name,
			int unit) {
		Vector<Blob> others = new Vector<Blob>();
		others.add(b);
		for (int j = i+1; j < glyphs.size(); j++) {
			Blob other = glyphs.get(j);
			if (other.isObsolete())
				continue;
			String otherName = nameOf(other);
			if (!otherName.equals(name) && other.getName().equals(""))
				otherName = secondNameOf(other);
			if (otherName.equals(name)) {
				if (unit < 0) {
					LayoutAnalyzer analyzer = new HieroLayoutAnalyzer();
					unit = analyzer.predictUnitSize(glyphs);
				}
				if (isClose(other, others, unit)) 
					others.add(other);
			}
		}
		if (others.size() > 1) {
			Blob merged = merge(others);
			String plainName = name.replaceAll("\\[part\\]", "");
			int correction = 5; // not sure why this is needed
			if (score(guesser, merged, plainName) < score(guesser, others, name) * correction ||
					plainName.equals("shade") && others.size() > 3) {
				merged.setName(plainName);
				replace(b, others, merged);
			} else
				avoidPart(b);
		} else
			avoidPart(b);
		return unit;
	}

	// We want to avoid hieroglyph looking like circles to be chosen when
	// it is really circled line number.
	private void avoidCircledGlyphs(Vector<Blob> glyphs, Blob b) {
		for (int j = 0; j < glyphs.size(); j++) {
			Blob other = glyphs.get(j);
			if (other != b && other.includes(b)) 
				for (int k = 0; k < NonHiero.circleLike.length; k++) 
					if (nameOf(other).equals(NonHiero.circleLike[k])) {
						other.setName("circle");
						return;
					}
		}
	}

	// Get current name.
	private String nameOf(Blob b) {
		if (!b.getName().equals(""))
			return b.getName();
		else if (b.getGuessed() != null && b.getGuessed().size() > 0)
			return b.getGuessed().get(0);
		else
			return "";
	}

	// Get second name.
	private String secondNameOf(Blob b) {
		if (!b.getName().equals("")) {
			if (b.getGuessed() != null && b.getGuessed().size() > 0)
				return b.getGuessed().get(0);
			else
				return "";
		} else if (b.getGuessed() != null && b.getGuessed().size() > 1)
			return b.getGuessed().get(1);
		else
			return "";
	}

	// Blobs are closer than part of unit.
	private boolean isClose(Blob b, Vector<Blob> bs, int unit) {
		for (Blob b2 : bs) 
			if (isClose(b, b2, unit))
				return true;
		return false;
	}
	private boolean isClose(Blob b1, Blob b2, int unit) {
		int dist = unit/5;
		return
			b1.x() < b2.x() + b2.width() + dist && b2.x() < b1.x() + b1.width() + dist &&
			b1.y() < b2.y() + b2.height() + dist && b2.y() < b1.y() + b1.height() + dist;
	}

	// Merge blobs into one.
	private Blob merge(Vector<Blob> others) {
		int xMin = Integer.MAX_VALUE;
		int xMax = 0;
		int yMin = Integer.MAX_VALUE;
		int yMax = 0;
		for (Blob b : others) {
			xMin = Math.min(xMin, b.x());
			xMax = Math.max(xMax, b.x() + b.width());
			yMin = Math.min(yMin, b.y());
			yMax = Math.max(yMax, b.y() + b.height());
		}
		Blob merged = new Blob(xMin, yMin, xMax-xMin, yMax-yMin);
		for (Blob b : others) 
			BinaryImage.superimpose(merged.imSafe(),
					b.imSafe(), b.x() - merged.x(), b.y() - merged.y());
		return merged;
	}

	// Give best score of guesser for blob and Gardiner name,
	// normalized by (proxy of) surface. (It seems not needed if distortion
	// model itself normalizes surface.)
	private int score(OcrGuesser guesser, Blob b, String name) {
		int score = guesser.score(b.imSafe(), name);
		// int surface = b.imSafe().width() * b.imSafe().height();
		int surface = 1;
		return score / surface;
	}

	// Give sum of scores, normalized by (proxy of) surface, of all glyphs.
	// Normalization doesn't seem to be needed.
	private int score(OcrGuesser guesser, Vector<Blob> blobs, String name) {
		int scores = 0;
		int surfaces = 1;
		for (Blob b : blobs) {
			scores += guesser.score(b.imSafe(), name);
			// surfaces += b.imSafe().width() * b.imSafe().height();
			// surfaces += 1;
		}
		return scores / surfaces;
	}

	// Replace parts by single.
	private void replace(Blob first, Vector<Blob> others, Blob merged) {
		for (Blob other : others) {
			if (other != first) {
				other.remove();
				other.setObsolete();
			}
		}
		first.copyProperties(merged);
	}

	// Avoid the 'part' interpretation of the blob, in favour of another.
	private void avoidPart(Blob b) {
		if (b.getGuessed() != null) 
			for (int i = 0; i < b.getGuessed().size(); i++) {
				String other = b.getGuessed().get(i);
				if (!other.matches(".*\\[part\\]")) {
					b.setName(other);
					return;
				}
			}
	}

}
