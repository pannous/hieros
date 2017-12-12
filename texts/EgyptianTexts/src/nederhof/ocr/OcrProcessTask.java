package nederhof.ocr;

// Task to be done by thread doing OCR and formatting.
public abstract class OcrProcessTask implements Comparable<OcrProcessTask> {

	// Comparison with other tasks.
	public int compareTo(OcrProcessTask t) {
		return compare(this, t);
	}

	// For comparison of tasks.
	// First we compare page names, then y coordinate on page.
	// GlyphCombiner comes after Line, which comes after Blob.
	public static int compare(OcrProcessTask t1, OcrProcessTask t2) {
		String page1 = t1.page();
		String page2 = t2.page();
		int y1 = t1.y();
		int y2 = t2.y();
		int priority1 = priorityOf(t1);
		int priority2 = priorityOf(t2);
		if (page1.compareTo(page2) != 0)
			return page1.compareTo(page2);
		else if (y1 < y2)
			return -1;
		else if (y1 > y2)
			return 1;
		else if (priority1 < priority2)
			return -1;
		else if (priority1 > priority2)
			return 1;
		else 
			return 0;
	}

	// Get name of relevant page.
	public abstract String page();
	// Get y coordinate.
	public abstract int y();

	// Priorities of different tasks. Lower priority means it has to be done
	// first.
	private static int priorityOf(OcrProcessTask t) {
		if (t instanceof Blob) 
			return 1;
		else if (t instanceof Line)
			return 2;
		else if (t instanceof GlyphCombiner)
			return 3;
		else 
			return 0;
	}
}
