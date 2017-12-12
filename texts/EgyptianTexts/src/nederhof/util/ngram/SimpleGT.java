package nederhof.util.ngram;

import java.util.*;

// Simple Good-Turing smoothing.
public class SimpleGT {
    private final double CONFID_FACTOR = 1.96;

    private int nRows;
    private int[] r;
    private int[] n;

    private int bigN = 0;
    private double pZero;
    private double bigNPrime;
    private double slope;
    private double intercept;
    private double[] z;
    private double[] logR;
    private double[] logZ;
    private double[] rStar;
    private double[] p;

    // Does Simple Good-Turing, using a mapping from keys to values.
    public SimpleGT(TreeMap<Integer,Integer> map) {
	nRows = map.size();
	r = new int[nRows];
	n = new int[nRows];
	int i = 0;
	for (int key : map.keySet()) {
	    r[i] = key;
	    n[i] = map.get(key);
	    i++;
	}

	logR = new double[nRows];
	logZ = new double[nRows];
	z = new double[nRows];
	rStar = new double[nRows];
	p = new double[nRows];

	for (int j = 0; j < nRows; j++) 
	    bigN += r[j] * n[j];

	int nextN = row(1);
	pZero = (nextN < 0) ? 0 : n[nextN] / (double) bigN;

	for (int j = 0; j < nRows; j++) {
	    int m = (j == 0) ? 0 : r[j - 1];
	    double k = (j == nRows - 1) ? 2.0 * r[j] - m : r[j + 1];
	    z[j] = 2 * n[j] / (k - m);
	    logR[j] = Math.log(r[j]);
	    logZ[j] = Math.log(z[j]);
	}

	findBestFit();

	boolean indiffValsSeen = false;
	for (int j = 0; j < nRows; j++) {
	    double y = (r[j] + 1) * smoothed(r[j] + 1) / smoothed(r[j]);

	    if (row(r[j] + 1) < 0)
		indiffValsSeen = true;
	    if (!indiffValsSeen) {
		nextN = n[row(r[j] + 1)];
		double x = (r[j] + 1) * nextN / (double) n[j];
		if (Math.abs(x - y) <= CONFID_FACTOR
			* Math.sqrt(sq(r[j] + 1.0) * nextN
			    / (sq((double) n[j]))
			    * (1 + nextN / (double) n[j])))
		    indiffValsSeen = true;
		else
		    rStar[j] = x;
	    }

	    if (indiffValsSeen)
		rStar[j] = y;
	}

	bigNPrime = 0.0;
	for (int j = 0; j < nRows; ++j)
	    bigNPrime += n[j] * rStar[j];
	for (int j = 0; j < nRows; ++j)
	    p[j] = (1 - pZero) * rStar[j] / bigNPrime;
    }

    // Mapping from frequencies to smoothed probabilities.
    public TreeMap<Integer,Double> getProbs() {
	TreeMap<Integer,Double> probs = new TreeMap<Integer,Double>();
	for (int i = 0; i < nRows; i++) 
	    probs.put(r[i], p[i]);
	probs.put(0, pZero);
	return probs;
    }

    private double smoothed(int i) {
	return Math.exp(intercept + slope * Math.log(i));
    }

    private double sq(double x) {
	return x * x;
    }

    private void findBestFit() {
	double XYs = 0; 
	double Xsquares = 0;
	double meanX = 0;
	double meanY = 0;

	for (int j = 0; j < nRows; j++) {
	    meanX += logR[j];
	    meanY += logZ[j];
	}

	meanX /= nRows;
	meanY /= nRows;

	for (int j = 0; j < nRows; j++) {
	    XYs += (logR[j] - meanX) * (logZ[j] - meanY);
	    Xsquares += sq(logR[j] - meanX);
	}

	slope = XYs / Xsquares;
	intercept = meanY - slope * meanX;
    }

    private int row(int freq) {
	int i = 0;
	while (i < nRows && r[i] < freq)
	    i++;
	return (i < nRows && r[i] == freq) ? i : -1;
    }

}

