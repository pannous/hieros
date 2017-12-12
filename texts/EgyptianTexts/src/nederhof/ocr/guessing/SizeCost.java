package nederhof.ocr.guessing;

import nederhof.ocr.images.*;
import nederhof.util.math.*;

// Cost of match or mismatch according to normal distribution of height
// (normalized to unit).
public class SizeCost extends NormalCost {

	// Constructor.
	public SizeCost(int nImagesMax) {
		super(nImagesMax);
	}

	// Get value for new image.
	public float getValue(float relativeHeight) {
		return relativeHeight;
	}
}

