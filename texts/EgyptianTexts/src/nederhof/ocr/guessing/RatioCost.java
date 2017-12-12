package nederhof.ocr.guessing;

import nederhof.ocr.images.*;
import nederhof.util.math.*;

// Cost of match or mismatch according to ratio of width and height.
public class RatioCost extends NormalCost {

	// Constructor.
	public RatioCost(int nImagesMax) {
		super(nImagesMax);
	}

	// Get value for new image.
	public float getValue(BinaryImage im) {
		return 1f * im.width() / im.height();
	}
}

