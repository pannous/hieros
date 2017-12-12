package nederhof.ocr.guessing;

import nederhof.ocr.images.*;

// Cost of match or mismatch of dimensions.
public class DimensionCost {

	// One array of dimensions for each image.
	private float[][] values;
	// How many images added?
	private int nImages = 0;

	// Constructor.
	public DimensionCost(int nImagesMax) {
		values = new float[nImagesMax][];
	}

	public int size() {
		return nImages;
	}

	// Add values for next image.
	public void addImage(BinaryImage im, float h) {
		values[nImages++] = getValues(im, h);
	}

	// Get values for new image.
	// Consist of normalized height (or negative if unknown) and 
	// width/height ratio.
	public float[] getValues(BinaryImage im, float h) {
		float ratio = 1f * im.width() / im.height();
		return new float[] {h, ratio};
	}

    // Maximum ratio difference width versus height.
    private static final float MAX_WIDTH_HEIGHT_DIFF = 2.0f;
    // Ratio beyond which the ratio difference doesn't matter.
    // So two very high but narrow images are always candidates to be matched,
    // irrespective of difference in ratio width/height.
    private static final float WIDTH_HEIGHT_LIMIT = 3f;
    // Maximum and minimum ratio difference height (relative to unit),
    // text occurrence / prototype.
    private static final float MAX_HEIGHT_DIFF = 2.0f;
    private static final float MIN_HEIGHT_DIFF = 0.3f;

	// Compare new values to old values.
	public float getCost(float[] vals, int i) {
		float height = vals[0];
		float ratio = vals[1];
		float oldHeight = values[i][0];
		float oldRatio = values[i][1];
		if (ratio > WIDTH_HEIGHT_LIMIT && oldRatio > WIDTH_HEIGHT_LIMIT ||
                1/ratio > WIDTH_HEIGHT_LIMIT && 1/oldRatio > WIDTH_HEIGHT_LIMIT ||
                    ratio/oldRatio < MAX_WIDTH_HEIGHT_DIFF &&
                    oldRatio/ratio < MAX_WIDTH_HEIGHT_DIFF &&
                    (height < 0 || oldHeight < 0 ||
                    height / oldHeight > MIN_HEIGHT_DIFF &&
                    height / oldHeight < MAX_HEIGHT_DIFF))
			return 0;
		else
			return 1;
	}

}

