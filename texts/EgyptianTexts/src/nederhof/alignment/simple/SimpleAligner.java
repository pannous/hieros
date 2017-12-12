/***************************************************************************/
/*                                                                         */
/*  SimpleAligner.java                                                     */
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

// Aligner of phrases, looking only at lengths of phrases.

package nederhof.alignment.simple;

import java.util.*;

import nederhof.alignment.*;

public abstract class SimpleAligner {

    // What is probability of beginning of phrase in source text
    // to match beginning of phrase in target text.
    public static double samePhraseProb = 0.95;
    public static double otherPhraseProb = 1 - samePhraseProb;

    // We consider up to three source phrases matched to 
    // target phrases where second and third source phrases
    // do not match the beginning of a target phrase.
    public static int stepLimit = 3;

    // Beam width.
    public static int beam = 10;

    // Source text and target text.
    private int[] inText;
    private int[] outText;

    // Precedences.
    // For position in source text, the minimum position in target text
    // that should be aligned to it. And vice versa.
    private int[] inPrec;
    private int[] outPrec;

    // Estimator of how words map to (multiple) words.
    private WordMappingEstimator estim;

    // The top-most configuration per source phase.
    private Vector[] agenda;

    // The arguments are arrays of lengths of phrases.
    public SimpleAligner(int[] inText, int[] outText,
	    int[] inPrec, int[] outPrec) {
	this.inText = inText;
	this.outText = outText;
	this.inPrec = inPrec;
	this.outPrec = outPrec;
	estim = new WordMappingEstimator(textLength(inText), textLength(outText));
	agenda = new Vector[inText.length + 1];
	for (int in = 0; in < agenda.length; in++)
	    agenda[in] = new Vector();
	addConfig(0, 0, 0, null);
	for (int in = 0; in < inText.length; in++) 
	    makeNewConfigs(in);
	Vector finals = agenda[inText.length];
	for (int i = 0; i < finals.size(); i++) {
	    Configuration last = (Configuration) finals.get(i);
	    if (last.out == outText.length)
		traceBack(last);
	}
    }

    // Length of text is sum of lengths of phrases.
    private static int textLength(int[] text) {
	int sum = 0;
	for (int i = 0; i < text.length; i++) 
	    sum += text[i];
	return sum;
    }

    // Add configuration to agenda. Make sure similar configuration
    // with worse penalty is eliminated. Order by penalty.
    // Do not allow to be larger than beam width.
    private void addConfig(int in, int out, double penalty, Configuration prev) {
	Configuration config = new Configuration(in, out, penalty, prev);
	Vector configs = agenda[in];
	for (int i = 0; i < configs.size(); i++) {
	    Configuration old = (Configuration) configs.get(i);
	    if (old.similar(config)) {
		if (old.penalty < penalty) 
		    return;
		else {
		    configs.remove(i);
		    break;
		}
	    }
	}
	for (int i = 0; i <= configs.size(); i++) {
	    if (i == configs.size()) {
		if (configs.size() < beam - 1) 
		    configs.add(config);
		return;
	    } else {
		Configuration old = (Configuration) configs.get(i);
		if (penalty < old.penalty) {
		    configs.add(i, config);
		    if (configs.size() >= beam)
			configs.remove(configs.size() - 1);
		    return;
		}
	    }
	}
    }

    // Make configurations, by jumping across configurations.
    private void makeNewConfigs(int in) {
	Vector configs = agenda[in];
	for (int i = 0; i < configs.size(); i++) {
	    Configuration config = (Configuration) configs.get(i);
	    int out = config.out;
	    for (int stepIn = 1; stepIn <= stepLimit; stepIn++) 
		if (in + stepIn <= inText.length) 
		    for (int stepOut = 1; stepOut <= stepLimit; stepOut++)
			if (out + stepOut <= outText.length)
			    makeNewConfig(config, stepIn, stepOut);
	}
    }

    // Make new configuration by taking stepIn phrases and stepOut phrases
    // in input and output.
    // The probability is determined such that the start of the first phrases
    // in input and output match, but the others don't.
    private void makeNewConfig(Configuration config, int stepIn, int stepOut) {
	int in = config.in;
	int out = config.out;
	if (in + stepIn < inPrec.length && 
			out + stepOut < inPrec[in + stepIn] ||
		out + stepOut < outPrec.length && 
			in + stepIn < outPrec[out + stepOut])
	    return; // if not consistent with precedence then abort
	int longPhraseIn = 0;
	int longPhraseOut = 0;
	for (int i = 0; i < stepIn; i++) 
	    longPhraseIn += inText[in + i];
	for (int i = 0; i < stepOut; i++) 
	    longPhraseOut += outText[out + i];
	int notAgree = (stepIn - 1) + (stepOut - 1);
	int agree = longPhraseOut - notAgree;
	try {
	    double prob = 
		AlignMath.phraseMappingProb(longPhraseIn, longPhraseOut,
			estim.oneToOneProb,
			estim.oneToTwoProb,
			estim.oneToZeroProb) *
		Math.pow(samePhraseProb, agree) * 
		Math.pow(otherPhraseProb, notAgree);
	    double penalty = - Math.log(prob) / Math.log(2);
	    addConfig(in + stepIn, out + stepOut, config.penalty + penalty, config);
	} catch (ArithmeticException e) {
	    // ignore in case of overflow
	}
    }

    // Trace back history from last configuration.
    private void traceBack(Configuration config) {
	if (config.prev != null) {
	    traceBack(config.prev);
	    matchPhrases(config.prev.in, config.prev.out);
	}
    }

    // Configuration consisting of positions in two texts, penalty and
    // the one it is derived from. 
    private class Configuration {
	public int in;
	public int out;
	public double penalty;
	public Configuration prev;

	public Configuration(int in, int out, double penalty, Configuration prev) {
	    this.in = in;
	    this.out = out;
	    this.penalty = penalty;
	    this.prev = prev;
	}

	// Two configurations are simlar if they have same positions.
	// The comparison is always with same first position.
	public boolean similar(Configuration other) {
	    return other.out == out;
	}
    }

    //////////////////////////////////////////////////////////
    // Estimation of probabilities of mapping word to zero, one or two
    // words.

    private static class WordMappingEstimator {
	// Defaults:
	// What is the probability that a phrase of length N corresponds to
	// a phrase of length M, assuming probabilities that
	// 1) one word is mapped to one word
	// 2) one word is mapped to nothing
	// 3) one word is mapped to two words
	public double oneToOneProb = 0.8;
	public double oneToZeroProb = 0.1;
	public double oneToTwoProb = 0.1;

	// We want to have oneToZeroProb and oneToTwoProb both >= 0.1,
	// so we estimate distinguishing two cases.
	// With inLength < outLength we take oneToZeroProb = 0.1 and have:
	// inLength * (1 - oneToTwoProb - 0.1 + 2 * oneToTwoProb) = outLength
	// So: oneToTwoProb = outLength / inLength - 0.9.
	// If we demand oneToOneProb >= 0.1, then
	// 1 - 0.1 - (outLength / inLength - 0.9) >= 0.1, or
	// outLength / inLength >= 1.7
	// With outLength < inLength we take oneToTwoProb = 0.1 and have:
	// inLength * (1 - oneToZeroProb - 0.1 + 2 * 0.1) = outLength
	// So: oneToZeroProb = 1.1 - outLength / inLength;
	// If we demand oneToOneProb >= 0.1, then
	// 1 - 0.1 - (1.1 - outLength / inLength) >= 0.1, or
	// outLength / inLength >= 0.3
	public WordMappingEstimator(int inLength, int outLength) {
	    double ratio = 1.0 * outLength / inLength;
	    if (outLength > inLength) {
		if (ratio <= 1.7) {
		    oneToZeroProb = 0.1;
		    oneToTwoProb = ratio - 0.9;
		    oneToOneProb = 1 - oneToZeroProb - oneToTwoProb;
		}
	    } else if (inLength > outLength) {
		if (ratio >= 0.3) {
		    oneToTwoProb = 0.1;
		    oneToZeroProb = 1.1 - ratio;
		    oneToOneProb = 1 - oneToTwoProb - oneToZeroProb;
		}
	    }
	}
    }

    //////////////////////////////////////////////////////////
    // Connection to caller.

    // Information back to caller about matching phrase
    // numbers.
    public abstract void matchPhrases(int inNum, int outNum);

    ///////////////////////////////////////////////
    // Testing.
    public static void main(String[] args) {
	int[] inText = new int[] {1, 2, 3};
	int[] outText = new int[] {1, 1, 2, 4};
	int[] inPrec = new int[] {0, 0, 0};
	int[] outPrec = new int[] {0, 0, 0, 0};
	new SimpleAligner(inText, outText, inPrec, outPrec) {
	    public void matchPhrases(int inNum, int outNum) {
		System.out.println("" + inNum + " " + outNum);
	    }
	};
    }

}
