package nederhof.util.math;

import java.util.*;

public class NormalDistribution {

	private double mean = 0;
	private double variance = 0;

	// Used when variance is 0;
	private double fallbackVariance = 0;

	// Constructor.
	public NormalDistribution(double mean, double variance) {
		this.mean = mean;
		this.variance = variance;
	}
	// With fallbackVariance.
	public NormalDistribution(double mean, double variance, double fallbackVariance) {
		this(mean, variance);
		this.fallbackVariance = fallbackVariance;
	}
	// Default.
	public NormalDistribution() {
		this(0, 0.5);
	}

	// Constructor with training data.
	public NormalDistribution(Vector<Double> data) {
		if (data.size() < 1) 
			return;
		double sum = 0;
		for (int i = 0; i < data.size(); i++)
			sum += data.get(i);
		mean = sum / data.size();
		double diffSum = 0;
		for (int i = 0; i < data.size(); i++)
			diffSum += (data.get(i) - mean) * (data.get(i) - mean);
		variance = diffSum / data.size();
	}
	// With training data and fallbackVariance.
	public NormalDistribution(Vector<Double> data, double fallbackVariance) {
		this(data);
		this.fallbackVariance = fallbackVariance;
	}

	public void setMean(double mean) {
		this.mean = mean;
	}
	public void setVariance(double variance) {
		this.variance = variance;
	}
	public void setFallbackVariance(double fallbackVariance) {
		this.fallbackVariance = fallbackVariance;
	}
	public double getMean() {
		return mean;
	}
	public double getVariance() {
		return variance;
	}
	public double getFallbackVariance() {
		return fallbackVariance;
	}

	public double density(double val) {
		double var = variance > 0 ? variance : fallbackVariance;
		if (var > 0)
			return 1 / Math.sqrt(2 * Math.PI * var) * Math.exp( -1 / (2 * var) * (val - mean) * (val - mean) );
		else
			return val == mean ? Double.MAX_VALUE : 0;
	}


	public String toString() {
		return "mean=" + mean  + ";sigma^2=" + variance;
	}

	/////////////////////////// 
	// Testing.
	
	public static void main(String[] args) {
		Vector<Double> vals = new Vector<Double>();
		vals.add(-1.0);
		vals.add(1.2);
		vals.add(1.3);
		NormalDistribution n = new NormalDistribution(vals);
		System.out.println(n);
		double pSum = 0;
		double step = 0.1;
		for (double v = -10; v < 10; v += step)
			pSum += n.density(v) * step;
		System.out.println("sum " + pSum);
	}

}
