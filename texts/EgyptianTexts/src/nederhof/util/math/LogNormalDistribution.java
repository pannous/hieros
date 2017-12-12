package nederhof.util.math;

import java.util.*;

public class LogNormalDistribution extends NormalDistribution {

	public LogNormalDistribution(double mean, double variance) {
		super(mean, variance);
	}
	public LogNormalDistribution(double mean, double variance, double fallbackVariance) {
		super(mean, variance, fallbackVariance);
	}
	public LogNormalDistribution() {
		super(1, 0);
	}

	// Constructor with training data.
	public LogNormalDistribution(Vector<Double> data) {
		super(toLog(data));
	}
	// With training data and fallbackVariance.
	public LogNormalDistribution(Vector<Double> data, double fallbackVariance) {
		super(toLog(data), fallbackVariance);
	}

	public double density(double val) {
		return super.density(toLog(val));
	}

	private static double toLog(double val) {
		return Math.log(val);
	}

	private static Vector<Double> toLog(Vector<Double> data) {
		Vector<Double> logData = new Vector<Double>();
		for (double val : data)
			logData.add(toLog(val));
		return logData;
	}

	///////////////////////////
	// Testing.
	
	public static void main(String[] args) {
		Vector<Double> vals = new Vector<Double>();
		vals.add(1.0);
		vals.add(0.5);
		vals.add(2.0);
		LogNormalDistribution n = new LogNormalDistribution(vals);
		System.out.println(n);
		System.out.println("density 0.0005=" + n.density(0.0005));
		System.out.println("density 0.5=" + n.density(0.5));
		System.out.println("density 1=" + n.density(1));
		System.out.println("density 2=" + n.density(2));
		System.out.println("density 2000=" + n.density(2000));
		double pSum = 0;
		double step = 0.001;
		for (double v = 0.001; v < 100; v += step)
			pSum += n.density(v) * step;
		System.out.println("sum " + pSum);
	}

}
