package nederhof.ocr.images.distance;

import java.util.*;

import nederhof.ocr.images.*;
import nederhof.util.math.*;

// Computes relevant values of FFT for image.
public class FftEvaluator {

	// Size (height and width) of image.
	// Should be power of 2.
	private int n;

	// FFT.
	private FFT fft;

	// Highest frequency (rank) to be taken into account.
	private int highestFreqRank = Integer.MAX_VALUE;
	private void setHighestFreqRank(int i) {
		this.highestFreqRank = i;
	}

	// Weight given to amplitude of lowest frequency.
	private float lowestAmplWeight = 2;
	private void setLowestAmplWeight(float w) {
		this.lowestAmplWeight = w;
	}

	// Weight given to amplitude of highest frequency.
	private float highestAmplWeight = 1;
	private void setHighestAmplWeight(float w) {
		this.highestAmplWeight = w;
	}

	// Weight given to phase of lowest frequency.
	private float lowestPhaseWeight = 2;
	private void setLowestPhaseWeight(float w) {
		this.lowestPhaseWeight = w;
	}

	// Weight given to phase of highest frequency.
	private float highestPhaseWeight = 1;
	private void setHighestPhaseWeight(float w) {
		this.highestPhaseWeight = w;
	}

	// Number of relevant values.
	private int nVals;

	// Weights of relevant values.
	private float[] weights;

	// Create. With power of 2.
	public FftEvaluator(int n, int maxRank) {
		this.n = n;
		fft = new FFT(n);
		setHighestFreqRank(maxRank);
		computeWeights();
	}

	public float[] getValues(BinaryImage im) {
		return getValues(BinaryImage.scale(im.array(), n, n));
	}

	// Get relevant FFT values from image.
	// The image should be n * n.
	private float[] getValues(boolean[][] image) {
		float[] re = new float[n*n];
		float[] im = new float[n*n];
		getSquare(image, re, im);
		fft.transform2D(re, im, true);
		return getRelevantValues(re, im);
	}

	// From square image (size n), make real and imaginary part, 
	// as one large array.
	private void getSquare(boolean[][] b, float[] real, float[] imag) {
		for (int x = 0; x < n; x++)
			for (int y = 0; y < n; y++) {
				int i = index(x, y);
				real[i] = b[x][y] ? 1 : 0;
				imag[i] = 0;
			}
	}

	// Index in image, as one large array.
	private int index(int x, int y) {
		return y * n + x;
	}

	// Compute weights.
	private void computeWeights() {
		Vector<Float> ws = new Vector<Float>();
		for (int x = 0; x < n; x++)
			for (int y = 0; y < n; y++) {
				if (criticalReal(x, y) && frequencyRank(x, y) <= highestFreqRank) {
					ws.add(realWeight(x, y));
				} 
				if (criticalImag(x, y) && frequencyRank(x, y) <= highestFreqRank) {
					ws.add(imagWeight(x, y));
				}
			}
		nVals = ws.size();
		weights = new float[nVals];
		for (int i = 0; i < nVals; i++)
			weights[i] = ws.get(i);

	}

	// Get relevant FFT values of image.
	private float[] getRelevantValues(float[] re, float[] im) {
		float[] vals = new float[nVals];
		int i = 0;
		for (int x = 0; x < n; x++)
			for (int y = 0; y < n; y++) {
				if (criticalReal(x, y) && frequencyRank(x, y) <= highestFreqRank) {
					vals[i] = re[index(x, y)];
					i++;
				} 
				if (criticalImag(x, y) && frequencyRank(x, y) <= highestFreqRank) {
					vals[i] = im[index(x, y)];
					i++;
				}
			}
		return vals;
	}

	// Multiply weights, for two images.
	public float diff(float[] vals1, float[] vals2) {
		float d = 0;
		for (int i = 0; i < nVals; i++)
			d += Math.abs(vals1[i] - vals2[i]) * weights[i];
		return d;
	}

	// Critical if not duplicate of other value.
	private boolean criticalReal(int x, int y) {
		if (x == 0 && y == 0)
			return true;
		else if (x == 0)
			return y <= n/2;
		else if (y == 0)
			return x <= n/2;
		else if (x == n/2)
			return y <= n/2;
		else if (y == n/2)
			return x <= n/2;
		else
			return y < n/2;
	}
	private boolean criticalImag(int x, int y) {
		if (x == 0 && y == 0)
			return false;
		else if (x == 0)
			return y < n/2;
		else if (y == 0)
			return x < n/2;
		else if (x == n/2)
			return y < n/2;
		else if (y == n/2)
			return x < n/2;
		else
			return y < n/2;
	}

	// The weight of the value at (x,y).
	private float realWeight(int x, int y) {
		int highest = highestFreqRank == Integer.MAX_VALUE ? n : highestFreqRank;
		int rank = frequencyRank(x,y);
		if (rank <= highestFreqRank)
			return lowestAmplWeight +
				rank * (highestAmplWeight - lowestAmplWeight) / highest;
		else
			return 0;
	}
	private float imagWeight(int x, int y) {
		int highest = highestFreqRank == Integer.MAX_VALUE ? n : highestFreqRank;
		int rank = frequencyRank(x,y);
		if (rank <= highestFreqRank)
			return lowestPhaseWeight +
				rank * (highestPhaseWeight - lowestPhaseWeight) / highest;
		else
			return 0;
	}

	// The rank is higher for higher frequencies.
	private int frequencyRank(int x, int y) {
		int xRank = x < n/2 ? x : n-x;
		int yRank = y < n/2 ? y : n-y;
		return xRank + yRank;
	}

}
