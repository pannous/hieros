package nederhof.alignment.egyptian;

import java.io.*;
import java.util.*;

import nederhof.egyptian.trans.*;
import nederhof.util.*;
import nederhof.util.fsa.*;
import nederhof.util.math.*;
import nederhof.util.ngram.*;

// As ConfigAutomatonNGram, but uses class-based N-gram model, next to
// unigram model of functions.
public class ConfigAutomatonClassGram extends ConfigAutomaton {

    // N-gram model for functions.
    private NGram<Function> gram;
    // N-gram model for classes of functions.
    private NGram<String> classGram;
    // One unigram for each class.
    protected TreeMap<String,NGram<Function>> unigrams;

    public ConfigAutomatonClassGram(String[] hiero, TransLow trans, 
	    NGram<Function> gram, NGram<String> classGram, TreeMap<String,NGram<Function>> unigrams) {
	this.gram = gram;
	this.classGram = classGram;
	this.unigrams = unigrams;
	this.historySize = gram.getN()-1;
        create(hiero, trans);
    }

    // Weight by N-gram, using class-based model.
    protected double weight(Function[] history, Function fun) {
	return NegLogProb.to(classBasedProb(history, fun));
    }

    // Class based (or HMM-like) model.
    // P(f | fs) = P(c | cs) * P(f | c).
    protected double classBasedProb(Function[] history, Function fun) {
	String[] classHistory = functionsToClasses(history);
	// double classProb = classGram.probGT(classHistory, fun.name());
	// double classProb = classGram.probSimpleGT(classHistory, fun.name());
	double classProb = classGram.probKatz(classHistory, fun.name());
	double unigramProb = 1;
	if (unigrams.get(fun.name()) != null) 
	    // unigramProb = unigrams.get(fun.name()).probGT(fun);
	    // unigramProb = unigrams.get(fun.name()).probSimpleGT(fun);
	    unigramProb = unigrams.get(fun.name()).probKatz(fun);
	return classProb * unigramProb;
    }

    // Weight of jumping to final state.
    protected double weightEnd(Function[] history) {
	String[] classHistory = functionsToClasses(history);
	// return NegLogProb.to(classGram.probGT(classHistory));
	// return NegLogProb.to(classGram.probSimpleGT(classHistory));
	return NegLogProb.to(classGram.probKatz(classHistory));
    }

    protected String[] functionsToClasses(Function[] functions) {
	String[] classes = new String[functions.length];
	for (int i = 0; i < functions.length; i++)
	    classes[i] = functions[i].name();
	return classes;
    }

}
