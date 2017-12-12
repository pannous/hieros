package nederhof.util.math;

import java.lang.*;

// FFT for images (2D).
public class FFT {

    // We assume n is power of 2.
    private int n;
    private int logN;

    // Weighting factors.
    private float[] w1;
    private float[] w2;
    private float[] w3;

    // Create instance for problem size n.
    public FFT(int n) {
	this.n = n;
	logN = log2(n);
	if (logN <= 0)
	    throw new ArithmeticException("FFT not on power of 2: " + n);
        w1 = new float[logN];
        w2 = new float[logN];
        w3 = new float[logN];
        int N = 1;
        for (int i = 0; i < logN; i++) {
            N *= 2;
            double angle = -2.0 * Math.PI / N;
            w1[i] = (float) Math.sin(0.5 * angle);
            w2[i] = -2.0f * w1[i] * w1[i];
            w3[i] = (float) Math.sin(angle);
        }
    }

    private void scramble(float[] real, float[] imag) {
        int j = 0;
        for (int i = 0; i < n; i++) {
            if (i > j) {
		// Swap i and j in real and imag.
                float temp1 = real[j];
                real[j] = real[i];
                real[i] = temp1;
                float temp2 = imag[j];
                imag[j] = imag[i];
                imag[i] = temp2;
            }
            int m = n / 2;
            while (j >= m && m >= 2) {
                j -= m;
                m /= 2;
            }
            j += m;
        }
    }

    private void butterflies(int direction, float[] real, float[] imag) {
        int m = 1;
        for (int k = 0; k < logN; k++) {
            int halfM = m;
            m *= 2;
            float wt = direction * w1[k];
            float wpReal = w2[k];
            float wpImag = direction * w3[k];
            float wReal = 1.0f;
            float wImag = 0.0f;
            for (int offset = 0; offset < halfM; offset++) {
                for(int i = offset; i < n; i += m) {
                    int j = i + halfM;
                    float re = real[j];
                    float im = imag[j];
                    float tempReal = (wReal * re) - (wImag * im);
                    float tempImag = (wImag * re) + (wReal * im);
                    real[j] = real[i] - tempReal;
                    real[i] += tempReal;
                    imag[j] = imag[i] - tempImag;
                    imag[i] += tempImag;
                }
                wt = wReal;
                wReal = wt * wpReal - wImag * wpImag + wReal;
                wImag = wImag * wpReal + wt * wpImag + wImag;
            }
        }
        if (direction == -1) {
            float nr = 1.0f / n;
            for (int i = 0; i < n; i++) {
                real[i] *= nr;
                imag[i] *= nr;
            }
        }
    }

    // Apply on image of n by n.
    public void transform2D(float[] real, float[] imag, boolean forward) {
        float[] rtemp = new float[n];
        float[] itemp = new float[n];

        // Rows.
        for (int y = 0; y < n; y++) {
            int offset = y * n;
            System.arraycopy(real, offset, rtemp, 0, n);
            System.arraycopy(imag, offset, itemp, 0, n);
            transform1D(rtemp, itemp, forward);
            System.arraycopy(rtemp, 0, real, offset, n);
            System.arraycopy(itemp, 0, imag, offset, n);
        }

        // Columns.
        for (int x = 0; x < n; x++) {
            int index = x;
            for (int y = 0; y < n; y++) {
                rtemp[y] = real[index];
                itemp[y] = imag[index];
                index += n;
            }
            transform1D(rtemp, itemp, forward);
            index = x;
            for (int y = 0; y < n; y++) {
                real[index] = rtemp[y];
                imag[index] = itemp[y];
                index += n;
            }
        }
    }

    private void transform1D(float[] real, float[] imag, boolean forward) {
	int direction = (forward ? 1 : -1);
        scramble(real, imag);
        butterflies(direction, real, imag);
    }

    // Compute base-2 log of number, or -1 when not power of 2.
    private int log2(int n) {
        int m = 1;
        int log2n = 0;
        while (m < n) {
            m *= 2;
            log2n++;
        }
        return m == n ? log2n : -1;
    }

    // Print values from real or imaginary part of FFT.
    public void print(float[] vals) {
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++)
                System.out.print(vals[x + y * n] + " ");
            System.out.println();
        }
    }

    // Print values from real and imaginary part of FFT.
    public void print(float[] real, float[] imag) {
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++)
                System.out.print("(" + real[x + y * n] + "," +
			imag[x + y * n] + ") ");
            System.out.println();
        }
    }

}
