/***************************************************************************/
/*                                                                         */
/*  AlignMath.java                                                         */
/*                                                                         */
/*  Copyright (c) 2009 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of PhilologEG, and may only be */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Auxiliary math methods for alignment.

package nederhof.alignment;

public class AlignMath {

    // What is the probability that a phrase of length N corresponds to
    // a phrase of length M, assuming probabilities that 
    // 1) one word is mapped to one word
    // 2) one word is mapped to nothing
    // 3) one word is mapped to two words
    public static double oneToOneProb = 0.8;
    public static double oneToZeroProb = 0.1;
    public static double oneToTwoProb = 0.1;

    // inLength is length of source phrase (number of words),
    // outLength is length of target phrase.
    // The number i of words mapped to one word cannot be more than
    // min(inLength, outLength)
    // It must also satisfy:
    // 2 * (inLength - i) >= (outLength - i)
    // because in the best case the remaining words of the source are mapped to two
    // words, which must account for the other target words.
    // This means:
    // i <= 2 * inLength - outLength
    // Furthermore, outLength - i must be even.
    public static double phraseMappingProb(int inLength, int outLength,
	    double oneToOneProb, double oneToTwoProb, double oneToZeroProb) 
		throws ArithmeticException {
	int k = Math.min(Math.min(inLength, outLength), 2 * inLength - outLength);
	double sum = 0;
	int start = outLength % 2 == 0 ? 0 : 1;
	for (int i = start; i <= k; i += 2) {
	    sum += 
		Math.pow(oneToOneProb, i) *
		binomialCoefficient(inLength, i) * 
		phraseMappingOtherProb(inLength - i, outLength - i, 
			oneToTwoProb, oneToZeroProb);
	}
	return sum;
    }
    // As above, but with probabilities being default values.
    public static double phraseMappingProb(int inLength, int outLength) 
		throws ArithmeticException {
	return phraseMappingProb(inLength, outLength, 
		oneToOneProb, oneToTwoProb, oneToZeroProb);
    }

    // Probability of length of source phrase mapped to length of target
    // phrase, with only one-to-zero and one-to-two allowed.
    private static double phraseMappingOtherProb(int inLength, int outLength,
	    double oneToTwoProb, double oneToZeroProb) 
		throws ArithmeticException {
	return 
	    Math.pow(oneToTwoProb, outLength / 2) *
	    Math.pow(oneToZeroProb, inLength - outLength / 2) *
	    binomialCoefficient(inLength, outLength / 2);
    }

    // n choose r
    // Exception if not fits within long.
    public static long binomialCoefficient(int n, int r) throws ArithmeticException {
        long t = 1;
        
	r = Math.max(r, n - r);
        
        for (int i = n, j = 1; i > r; i--, j++) {
	    if (Long.MAX_VALUE / i <= t)
		throw new ArithmeticException("overflow");
            t = t * i / j;
        }
        
        return t;
    }

    
    public static void main(String[] args) {
	try {
	    double sum = 0;
	    for (int i = 0; i <= 60; i++) {
		double x = phraseMappingProb(30, i);
		sum += x;
	    }
	} catch (ArithmeticException e) {
	}
    }

}
