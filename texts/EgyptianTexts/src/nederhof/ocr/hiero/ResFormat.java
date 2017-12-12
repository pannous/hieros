package nederhof.ocr.hiero;

import java.util.*;

import nederhof.ocr.*;

// Formatted part of a line, consisting of RES.
public class ResFormat extends LineFormat {

	// For every position there can be one note.
	private TreeMap<Integer,String> notes = new TreeMap<Integer,String>();

	// Constructor.
	public ResFormat(String val) {
		super(val);
	}

	// Get notes.
	public TreeMap<Integer,String> getNotes() {
		return notes;
	}

	// Set notes.
	public void setNote(int num, String text) {
		notes.put(num, text);
	}
	public void setNotes(TreeMap<Integer,String> notes) {
		this.notes = notes;
	}

	public LineFormat copy() {
		ResFormat format = new ResFormat(getVal());
		format.notes = (TreeMap<Integer,String>) notes.clone();
		return format;
	}

}
