package nederhof.ocr.guessing;

import nederhof.ocr.images.*;
import nederhof.ocr.images.distance.*;

// FFT cost for OCR guesser.
public class FftCost {

	// For computing FFT.
	private FftEvaluator fft;

	// Size of images.
    // Must be power of 2.
    private static final int FFT_SIZE = 16;
    // Maximum frequency rank considered.
    private static final int FFT_MAX = 4;

	// One array of FFT values for each image.
	private float[][] values;
	// How many images added?
	private int nImages = 0;

	// Constructor.
	public FftCost(int nImagesMax) {
		this(nImagesMax, FFT_SIZE, FFT_MAX);
	}
	public FftCost(int nImagesMax, int imageSize, int max) {
		fft = new FftEvaluator(imageSize, max);
		values = new float[nImagesMax][];
	}

	public int size() {
		return nImages;
	} 

	// Add values for next image.
	public void addImage(BinaryImage im) {
		values[nImages++] = getValues(im);
	}

	// Get values for new image.
	public float[] getValues(BinaryImage im) {
		return fft.getValues(im);
	}

	// Compare new values to old values.
	public float getCost(float[] vals, int i) {
		return fft.diff(vals, values[i]);
	}

}

