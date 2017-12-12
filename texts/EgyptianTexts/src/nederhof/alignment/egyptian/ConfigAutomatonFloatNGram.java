package nederhof.alignment.egyptian;

import java.io.*;
import java.util.*;

import nederhof.egyptian.trans.*;
import nederhof.util.*;
import nederhof.util.fsa.*;
import nederhof.util.math.*;
import nederhof.util.ngram.*;

// As ConfigAutomaton, but uses N-gram model.
public class ConfigAutomatonFloatNGram extends ConfigAutomaton {

	// N-gram model.
	private FloatNGram<Function> gram;

	public ConfigAutomatonFloatNGram(String[] hiero, TransLow trans, FloatNGram<Function> gram) {
		this.gram = gram;
		this.historySize = gram.getN()-1;
		create(hiero, trans);
	}

	// Weight by N-gram.
	protected double weight(Function[] history, Function fun) {
		double prob = gram.prob(history, fun);
		return NegLogProb.to(prob);
	}

	// Weight of jumping to final state.
	protected double weightEnd(Function[] history) {
		return NegLogProb.to(gram.prob(history));
	}

}
