package nederhof.alignment.egyptian;

import java.io.*;
import java.util.*;

import nederhof.egyptian.trans.*;
import nederhof.util.*;
import nederhof.util.fsa.*;
import nederhof.util.ngram.*;

// Computes automaton of all readings of hieroglyphs.
public class BaselineAutomaton {

    // Sign table only created once.
    protected static String signTableLocation = "data/ortho/functions.xml";
    protected static SignTable signTable = null;

    // The hieroglyphic.
    protected String[] hiero;

    // Agenda for states, which are positions in hieroglyphic.
    protected Stack<Integer> agenda = new Stack<Integer>();

    // The automaton containing all paths.
    protected Fsa<Integer,List<Function>> fsa;

    // The (unique) final state, or null if it hasn't been created.
    protected Integer finalState = null;

    // Unigram model.
    private NGram<Function> gram;

    protected BaselineAutomaton() {
        makeTable();
    }

    public BaselineAutomaton(String[] hiero, NGram<Function> gram) {
        this();
	this.gram = gram;
	this.hiero = hiero;
        create();
    }

    protected void makeTable() {
        if (signTable == null) {
            try {
                signTable = new SignTable(signTableLocation);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    // Create the automaton by iteration.
    protected void create() {
	int initial = 0;
        fsa = new Fsa<Integer,List<Function>>(initial);
        agenda.push(initial);
        while (!agenda.empty())
            createFrom(agenda.pop());
	// print();
	// Normalization seems to worsen scores.
	// (new FsaNormalizer<Integer,List<Function>>()).normalize(fsa);
    }

    // Create new transitions from state.
    private void createFrom(int state) {
        if (state >= hiero.length) {
	    fsa.addFinal(state);
        } else {
	    createFrom(state, 0, signTable.getRoot());
	    for (int nSigns = 1; nSigns <= 2; nSigns++)
		for (int nRepeats = 1; nRepeats <= 2; nRepeats++)
		    if (signsRepeated(state, nSigns, nRepeats))
			applyMult(state, nSigns, nRepeats);
	    String[] nextSign = new String[1];
	    nextSign[0] = hiero[state];
	    Function functionSp = new FunctionSpurious(nextSign);
	    apply(state, 1, functionSp);
	    if (signTable.isNumeral(hiero[state]))
		createNumber(state);
	}
    }

    private void createNumber(Integer state) {
        NumberConfig config = new NumberConfig();
        int n = 0;
        while (state + n < hiero.length &&
                    signTable.isNumeral(hiero[state + n])) {
            String hi = hiero[state + n];
            int num = signTable.getNumeral(hi);
            NumberConfig nextConfig = config.apply(hi, num);
            if (nextConfig == null)
                break;
            else
                config = nextConfig;
            n++;
        }
        String[] nextSigns = new String[n];
	for (int i = 0; i < n; i++)
	    nextSigns[i] = hiero[state + i];
        Function function = new FunctionNum(nextSigns, config.getTransLow());
        apply(state, n, function);
    }

    // Create new transitions by traversing sign table, having read n signs.
    private void createFrom(Integer state, int n, TrieList<Function> node) {
        for (Function function : node.get())
            apply(state, n, function);
        if (state + n < hiero.length) {
            String nextSign = hiero[state + n];
            if (node.hasNext(nextSign))
                createFrom(state, n+1, node.next(nextSign));
        }
    }

    // Are X signs repeated Y times?
    private boolean signsRepeated(Integer state, int nSigns, int nRepeats) {
        if (nSigns > state || nSigns * nRepeats > hiero.length - state)
            return false;
        for (int i = 0; i < nSigns; i++)
            for (int j = 0; j < nRepeats; j++)
                if (!hiero[state-nSigns+i].equals(hiero[state+nSigns*j+i]))
                    return false;
        return true;
    }

    // A repetition of nsigns signs, nrepeats times.
    private void applyMult(Integer state, int nSigns, int nRepeats) {
        String[] nextHiero = new String[nSigns * nRepeats];
	for (int i = 0; i < nSigns * nRepeats; i++)
	    nextHiero[i] = hiero[state + i];
        Function functionFemPlur = new FunctionMultFemPlur(nextHiero, nRepeats+1);
        apply(state, nextHiero.length, functionFemPlur);
        Function functionSem = new FunctionMultSem(nextHiero, nRepeats+1);
        apply(state, nextHiero.length, functionSem);
        if (nRepeats == 1) {
            Function functionWj = new FunctionMultSuffix(nextHiero, nRepeats+1, new TransLow("wj"));
            apply(state, nextHiero.length, functionWj);
            Function functionj = new FunctionMultSuffix(nextHiero, nRepeats+1, new TransLow("j"));
            apply(state, nextHiero.length, functionj);
        } else if (nRepeats == 2) {
            Function functionW = new FunctionMultSuffix(nextHiero, nRepeats+1, new TransLow("w"));
            apply(state, nextHiero.length, functionW);
        }
    }

    // Apply function corresponding to n signs.
    private void apply(Integer state, int n, Function function) {
	List<Function> functions = new LinkedList<Function>();
	functions.add(function);
	addTrans(state, n, functions);
    }

    // Add new transition, for list of functions, corresponding to n signs.
    private void addTrans(Integer state, int n, List<Function> functions) {
        double weight = 0;
        for (Function function : functions) 
            weight += weight(function);
        Integer nextState = state + n;
        boolean isNew = fsa.addTrans(state, functions, nextState, weight);
        if (isNew)
            agenda.push(nextState);
    }

    protected double weight(Function fun) {
	final double almostInfinity = Double.MAX_VALUE / 1000;
	double prob = gram.probGT(new Function[0], fun);
	if (prob > 0)
	    return -Math.log(prob);
	else
	    return almostInfinity;
    }

    public boolean success() {
        return !fsa.getFinalStates().isEmpty();
    }

    // Get best list of functions.
    public List<Function> getBest() {
        List<Function> bestFunctions = new LinkedList<Function>();
        List<FsaTrans<Integer,List<Function>>> bestPath = 
			(new FsaShortestPath(fsa)).shortestPath();
        for (FsaTrans<Integer,List<Function>> trans : bestPath)
            bestFunctions.addAll(trans.label());
        return bestFunctions;
    }

    // Print automaton (for testing).
    public void print() {
        for (Integer state : fsa.getStates()) {
            System.out.println(state);
            for (FsaTrans<Integer,List<Function>> trans : fsa.fromTransitions(state)) {
                System.out.print("-->[" + trans.weight() + "]");
                for (Function function : trans.label())
                    System.out.print(function + " ");
                System.out.println("    " + trans.toState());
            }
        }
        for (Integer state : fsa.getFinalStates())
            System.out.println("FINAL " + state);
    }

}
