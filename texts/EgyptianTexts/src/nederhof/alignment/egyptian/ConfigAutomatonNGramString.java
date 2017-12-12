package nederhof.alignment.egyptian;

import java.io.*;
import java.util.*;

import nederhof.egyptian.trans.*;
import nederhof.util.*;
import nederhof.util.fsa.*;
import nederhof.util.math.*;
import nederhof.util.ngram.*;

// As ConfigAutomatonNGram, but jumps taken together with following function.
public class ConfigAutomatonNGramString extends ConfigAutomaton {

    // N-gram model.
    private NGram<String> gram;

    public ConfigAutomatonNGramString(String[] hiero, TransLow trans, NGram<String> gram) {
	this.gram = gram;
	this.historySize = gram.getN()-1;
        create(hiero, trans);
    }

    // Add new transition, for list of functions, corresponding to n signs.
    protected void addTrans(ComplexConfig state, int n, List<Function> functions) {
        String[] hiero = state.getHiero();
        int hieroPos = state.getHieroPos() + n;
        Function[] history = state.getHistory();
        SimpleConfig config = state;
        double weight = 0;
        weight = weight(history, functions);
        history = ComplexConfig.updateHistoryNoJump(history, functions, historySize);
        for (Function function : functions)
            config = function.apply(config);
        ComplexConfig nextState =
            new ComplexConfig(config, hiero, hieroPos, history);
        boolean isNew = fsa.addTrans(state, functions, nextState, weight);
        if (isNew)
            agenda.push(nextState);
    }

    // Weight by N-gram.
    protected double weight(Function[] history, Function fun) {
	// double prob = gram.probGT(history, fun);
	// double prob = gram.probSimpleGT(history, fun);
	String[] historyString = functionsToStrings(history);
	String funString = fun.toString();
	double prob = gram.probKatz(historyString, funString);
	// System.out.print("" + fun + " given " + history[0] + " ");
	// System.out.println(prob);
	return NegLogProb.to(prob);
    }

    protected double weight(Function[] history, List<Function> functions) {
	String[] historyString = functionsToStrings(history);
	String composedFunString = concat(functions);
	double prob = gram.probKatz(historyString, composedFunString);
	return NegLogProb.to(prob);
    }

    // Weight of jumping to final state.
    protected double weightEnd(Function[] history) {
	// return NegLogProb.to(gram.probGT(history));
	// return NegLogProb.to(gram.probSimpleGT(history));
	String[] historyString = functionsToStrings(history);
	return NegLogProb.to(gram.probKatz(historyString));
    }

    protected String[] functionsToStrings(Function[] functions) {
	List<String> ss = new LinkedList<String>();
	String s = "";
        for (int i = 0; i < functions.length; i++) {
            s += "" + functions[i];
	    // TODO
            if (!(functions[i] instanceof FunctionJump)) {
                ss.add(s);
                s = "";
            }   
        }
	if (!(s.equals("")))
	    ss.add(s);
        String[] strings = new String[ss.size()];
	int j = 0;
	for (String string : ss)
            strings[j++] = string;
        return strings;
    }

    protected String concat(List<Function> functions) {
	String s = "";
	for (Function fun : functions)
	    s += "" + fun;
	return s;
    }

}
