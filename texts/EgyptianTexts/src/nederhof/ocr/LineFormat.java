package nederhof.ocr;

// Formatted part of a line.
public abstract class LineFormat {

	// Value.
	private String val;

	// Constructor.
	public LineFormat(String val) {
		this.val = val;
	}

	public String getVal() {
		return val;
	}

	public void setVal(String val) {
		this.val = val;
	}

	public abstract LineFormat copy();

}
