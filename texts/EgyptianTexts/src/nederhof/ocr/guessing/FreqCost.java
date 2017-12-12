package nederhof.ocr.guessing;

import nederhof.ocr.images.*;
import nederhof.util.math.*;

// Cost according to relative frequency.
public class FreqCost {

	// One probability for each image.
	protected float[] relFreqs;
	// How many images added?
	protected int nImages = 0;

	// Constructor.
	public FreqCost(int nImagesMax) {
		relFreqs = new float[nImagesMax];
	}

	// Number of images.
	public int size() {
		return nImages;
	}

	// Add value for next image.
	public void add(float p) {
		relFreqs[nImages++] = p;
	}

	// Cost for value.
	public float getCost(int i) {
		return (float) NegLogProb.to(relFreqs[i]);
	}

}

