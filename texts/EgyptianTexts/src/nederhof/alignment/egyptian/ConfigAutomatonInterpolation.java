package nederhof.alignment.egyptian;

import java.io.*;
import java.util.*;

import nederhof.egyptian.trans.*;
import nederhof.util.*;
import nederhof.util.fsa.*;
import nederhof.util.math.*;
import nederhof.util.ngram.*;

// As ConfigAutomatonNGram, but interpolated with class-based N-gram model, next to
// unigram model of functions.
public class ConfigAutomatonInterpolation extends ConfigAutomaton {
    // N-gram model for functions.
    private NGram<Function> gram;
    // N-gram model for classes of functions.
    private NGram<String> classGram;
    // One unigram for each class.
    protected TreeMap<String,NGram<Function>> unigrams;

    // The interpolation weight.
    protected static final double classWeight = 0.2;
    protected static final double functionWeight = 1 - classWeight;

    public ConfigAutomatonInterpolation(String[] hiero, TransLow trans,
            NGram<Function> gram, NGram<String> classGram, TreeMap<String,NGram<Function>> unigrams) {
        this.gram = gram;
        this.classGram = classGram;
        this.unigrams = unigrams;
        this.historySize = gram.getN()-1;
        create(hiero, trans);
    }

    // Weight by N-gram, using interpolated model.
    protected double weight(Function[] history, Function fun) {
	double pFunction = gram.probKatz(history, fun);
	double pClass = classBasedProb(history, fun);
	double p = pFunction * functionWeight + pClass * classWeight;
        return NegLogProb.to(p);
    }

    // Class based (or HMM-like) model.
    // P(f | fs) = P(c | cs) * P(f | c).
    protected double classBasedProb(Function[] history, Function fun) {
        String[] classHistory = functionsToClasses(history);
        double classProb = classGram.probKatz(classHistory, fun.name());
        double unigramProb = 1;
        if (unigrams.get(fun.name()) != null)
            unigramProb = unigrams.get(fun.name()).probKatz(fun);
        return classProb * unigramProb;
    }

    // Weight of jumping to final state.
    protected double weightEnd(Function[] history) {
        String[] classHistory = functionsToClasses(history);
 	double pFunction = gram.probKatz(history);
	double pClass = classGram.probKatz(classHistory);
	double p = pFunction * functionWeight + pClass * classWeight;
        return NegLogProb.to(p);
    }

    protected String[] functionsToClasses(Function[] functions) {
        String[] classes = new String[functions.length];
        for (int i = 0; i < functions.length; i++)
            classes[i] = functions[i].name();
        return classes;
    }

}
