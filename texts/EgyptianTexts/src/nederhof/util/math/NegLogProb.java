package nederhof.util.math;

import java.util.*;

// Negative log probability.
public class NegLogProb {

	// Largest difference between values that can be represented.
	public static final double PRECISION = -Math.log(Double.MIN_VALUE) - Math.log(3);

	// The zero element as probability. Represented as almost infinity.
	public static final double ZERO = Double.MAX_VALUE / 100000;

	// The one element as probability.
	public static final double ONE = 0;

	public static double to(double p) {
		if (p > 0)
			return -Math.log(p);
		else
			return ZERO;
	}

	public static double from(double m) {
		if (m < PRECISION)
			return Math.exp(-m);
		else
			return 0;
	}

	// Multiply two probabilities that are in minus-log representation.
	public static double mult(double a, double b) {
		if (a == ZERO || b == ZERO)
			return ZERO;
		else
			return a+b;
	}
	public static double div(double a, double b) {
		if (a == ZERO)
			return ZERO;
		else
			return a-b;
	}

	// Add two probabilities that are in minus-log representation.
	// Scale to avoid underflow.
	public static double add(double a, double b) {
		if (a == ZERO)
			return b;
		else if (b == ZERO)
			return a;
		else if (a - b > PRECISION)
			return b;
		else if (b - a > PRECISION)
			return a;
		else {
			double B = Math.min(a, b);
			return -Math.log(Math.exp(-a + B) + Math.exp(-b + B)) + B;
		}
	}

	// Perplexity for input of length n.
	public static double perplexity(double m, int n) {
		return Math.exp(m / n);
	}

	// For testing.
	public static void main(String[] args) {
		double a0 = 0;
		double a1 = 0.00001;
		double a2 = 0.0000000000001;
		double m0 = NegLogProb.to(a0);
		double m1 = NegLogProb.to(a1);
		double m2 = NegLogProb.to(a2);
		double m3 = NegLogProb.add(m1, m2);
		double b0 = NegLogProb.from(m0);
		double b1 = NegLogProb.from(m1);
		double b2 = NegLogProb.from(m2);
		double b3 = NegLogProb.from(m3);
		System.out.println(a1);
		System.out.println(a2);
		System.out.println(b0);
		System.out.println(b1);
		System.out.println(b2);
		System.out.println(b3);
		// Normalization.
		double m6 = NegLogProb.add(m3, m0);
		double m7 = NegLogProb.div(m0, m6);
		double m8 = NegLogProb.div(m1, m6);
		double m9 = NegLogProb.div(m2, m6);
		double b7 = NegLogProb.from(m7);
		double b8 = NegLogProb.from(m8);
		double b9 = NegLogProb.from(m9);
		System.out.println(b7);
		System.out.println(b8);
		System.out.println(b9);
		// Perplexity.
		double m10 = NegLogProb.to(0.333);
		double m11 = NegLogProb.mult(m10, m10);
		double m12 = NegLogProb.mult(m11, m11);
		System.out.println(NegLogProb.perplexity(m12, 4));
	}

}
