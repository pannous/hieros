package nederhof.ocr.hiero;

import nederhof.ocr.*;

// Formatted part of a line, consisting of line number.
public class NumFormat extends LineFormat {

	// Constructor.
	public NumFormat(String val) {
		super(val);
	}

	public LineFormat copy() {
		return new NumFormat(getVal());
	}

}
