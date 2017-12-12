package nederhof.ocr.hiero;

import java.util.*;

import nederhof.ocr.*;
import nederhof.ocr.admin.*;
import nederhof.ocr.images.*;
import nederhof.ocr.guessing.*;
import nederhof.ocr.hiero.admin.*;
import nederhof.ocr.hiero.parsing.*;
import nederhof.util.*;
import nederhof.util.gui.*;
import nederhof.res.*;
import nederhof.res.editor.*;

public class HieroBlobFormatter implements BlobFormatter {

	// Turn list of blobs into list of lineformats.
	public Vector<LineFormat> toFormats(Vector<Blob> glyphs, String dir) {
		Vector<LineFormat> formats = new Vector<LineFormat>();
		Vector<Blob> hieros = new Vector<Blob>();
		Vector<Blob> nonHieros = new Vector<Blob>();
		LayoutAnalyzer analyzer = new HieroLayoutAnalyzer();
		int unit = analyzer.predictUnitSize(glyphs);
		for (Blob blob : glyphs) {
			if (name(blob) == null)
			   ; // ignore
			else if (includeInResBlobs(name(blob))) {
				if (!nonHieros.isEmpty()) {
					String num = toNum(nonHieros);
					if (!num.equals("")) {
						NumFormat format = new NumFormat(num);
						formats.add(format);
					}
					nonHieros = new Vector<Blob>();
				}
				hieros.add(blob);
			} else if (NonHiero.inLineNumber(name(blob))) {
				if (!hieros.isEmpty()) {
					TreeMap<Integer,String> notes = new TreeMap<Integer,String>();
					ResFormat format = new ResFormat(toRes(hieros, unit, dir, notes));
					format.setNotes(notes);
					formats.add(format);
					hieros = new Vector<Blob>();
				}
				if (name(blob).matches("^[0-9]*$") || name(blob).matches("^circle.*$"))
					nonHieros.add(blob);
			} 
		}
		if (!nonHieros.isEmpty()) {
			String num = toNum(nonHieros);
			if (!num.equals("")) {
				NumFormat format = new NumFormat(num);
				formats.add(format);
			}
		}
		if (!hieros.isEmpty()) {
			TreeMap<Integer,String> notes = new TreeMap<Integer,String>();
			ResFormat format = new ResFormat(toRes(hieros, unit, dir, notes));
			format.setNotes(notes);
			formats.add(format);
		}
		return formats;
	}

	// The confirmed name, or else the first guessed name.
	private String name(Blob b) {
		if (!b.getName().equals(""))
			return b.getName();
		else if (b.getGuessed() != null &&
					b.getGuessed().size() > 0)
			return b.getGuessed().get(0);
		else
			return null;
	}

	private boolean includeInResBlobs(String name) {
		return !NonHiero.isExtra(name) && !name.matches(".*\\[part\\]") ||
			NonHiero.isBox(name) ||
			name.equals("unk") || name.equals("shade");
	}

	// Turns list of hieroglyphs into RES.
	private String toRes(Vector<Blob> glyphs, int unit, String dir,
			TreeMap<Integer,String> notes) {
		SurfaceParser parser = new SurfaceParser(glyphs, unit, dir);
		ResFragment fragment = parser.encoding();
		transferNotes(fragment, notes);
		return fragment.toString();
	}

	// Turns list of nonhieroglyphs into number.
	private String toNum(Vector<Blob> digits) {
		Vector<Blob> sortDigits = (Vector<Blob>) digits.clone();
		Collections.sort(sortDigits, new XComparator());
		Vector<String> digs = new Vector<String>();
		for (Blob blob : sortDigits) {
			digs.add(name(blob));
		}
		return NonHiero.assembleLineNumber(digs);
	}

	// Move notes from glyphs to separate map.
	// More than one note per glyph is removed.
	private void transferNotes(ResFragment fragment, TreeMap<Integer,String> notes) {
		Vector<ResNamedglyph> nameds = fragment.glyphs();
		for (int i = 0; i < nameds.size(); i++) {
			ResNamedglyph named = nameds.get(i);
			if (named.nNotes() > 0) {
				String note = ResNote.stringify(named.note(0).string);
				notes.put(i, note);
				named.notes = new Vector<ResNote>();
			}
		}
	}

	// Comparing glyphs with x position.
	private class XComparator implements Comparator<Blob> {
		public int compare(Blob b1, Blob b2) {
			if (b1.x() < b2.x())
				return -1;
			else if (b1.x() > b2.x())
				return 1;
			else if (b1.y() < b2.y())
				return -1;
			else if (b1.y() > b2.y())
				return 1;
			else
				return 0;
		}
	}

}
