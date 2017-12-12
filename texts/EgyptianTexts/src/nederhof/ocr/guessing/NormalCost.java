package nederhof.ocr.guessing;

import nederhof.ocr.images.*;
import nederhof.util.math.*;

// Cost of match or mismatch according to normal distribution.
public class NormalCost {

	// One distribution for each image.
	protected NormalDistribution[] normals;
	// How many images added?
	protected int nImages = 0;

	// Constructor.
	public NormalCost(int nImagesMax) {
		normals = new NormalDistribution[nImagesMax];
	}

	// Number of images.
	public int size() {
		return nImages;
	}

	// Add values for next image.
	public void add(NormalDistribution normal) {
		normals[nImages++] = normal;
	}

	// Cost for value.
	public float getCost(float value, int i) {
		double density = normals[i].density(value);
		if (density < Float.MIN_VALUE)
			return Float.MAX_VALUE;
		else
			return (float) NegLogProb.to(normals[i].density(value));
	}

}

