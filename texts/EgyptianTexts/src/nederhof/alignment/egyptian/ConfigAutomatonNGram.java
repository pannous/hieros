package nederhof.alignment.egyptian;

import java.io.*;
import java.util.*;

import nederhof.egyptian.trans.*;
import nederhof.util.*;
import nederhof.util.fsa.*;
import nederhof.util.math.*;
import nederhof.util.ngram.*;

// As ConfigAutomaton, but uses N-gram model.
public class ConfigAutomatonNGram extends ConfigAutomaton {

	// N-gram model.
	private NGram<Function> gram;

	public ConfigAutomatonNGram(String[] hiero, TransLow trans, NGram<Function> gram) {
		this.gram = gram;
		this.historySize = gram.getN()-1;
		create(hiero, trans);
	}

	// Weight by N-gram.
	protected double weight(Function[] history, Function fun) {
		// double prob = gram.probGT(history, fun);
		// double prob = gram.probSimpleGT(history, fun);
		double prob = gram.probKatz(history, fun);
		return NegLogProb.to(prob);
	}

	// Weight of jumping to final state.
	protected double weightEnd(Function[] history) {
		// return NegLogProb.to(gram.probGT(history));
		// return NegLogProb.to(gram.probSimpleGT(history));
		return NegLogProb.to(gram.probKatz(history));
	}

}
