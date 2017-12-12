package nederhof.alignment.egyptian;

import java.io.*;
import java.util.*;

import nederhof.egyptian.trans.*;
import nederhof.util.*;
import nederhof.util.fsa.*;
import nederhof.util.math.*;

// Automaton from string of hieroglyphs and transliteration.
public class ConfigAutomaton {

	// Sign table only created once.
	protected static String signTableLocation = "data/ortho/functions.xml";
	protected static SignTable signTable = null;

	// Use of history for N-grams.
	protected int historySize = 0;

	// Agenda for states.
	protected Stack<ComplexConfig> agenda = new Stack<ComplexConfig>();

	// The automaton containing all paths.
	protected Fsa<ComplexConfig,List<Function>> fsa;

	// The (unique) final state, or null if it hasn't been created.
	protected ComplexConfig finalState = null;

	protected ConfigAutomaton() {
		makeTable();
	}

	public ConfigAutomaton(String[] hiero, TransLow trans) {
		this();
		create(hiero, trans);
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
	protected void create(String[] hiero, TransLow trans) {
		ComplexConfig initial = new ComplexConfig(hiero, historySize);
		initial.setTarget(trans);
		fsa = new Fsa<ComplexConfig,List<Function>>(initial);
		agenda.push(initial);
		while (!agenda.empty()) 
			createFrom(agenda.pop());
		// Normalization seems to worsen scores.
		// (new FsaNormalizer<ComplexConfig,List<Function>>()).normalize(fsa);
	}

	// Create new transitions from state.
	private void createFrom(ComplexConfig state) {
		if (state.canFinish()) {
			int dist = state.getTrans().length() - state.getPos();
			if (dist == 0) {
				double weight = weightEnd(state.getHistory());
				ComplexConfig finalState = getFinalState();
				fsa.addTrans(state, new LinkedList<Function>(), finalState, weight);
			} else {
				List<Function> functions = new LinkedList<Function>();
				Function jump = new FunctionJump(dist);
				functions.add(jump);
				addTrans(state, 0, functions);
			}
		}
		createFrom(state, 0, signTable.getRoot());
		for (TransLow phon : state.nextPhon()) {
			Function function = new FunctionEpsPhon(phon);
			applyWithJumps(state, 0, function);
		}
		for (int nSigns = 1; nSigns <= 2; nSigns++)
			for (int nRepeats = 1; nRepeats <= 2; nRepeats++)
				if (state.signsRepeated(nSigns, nRepeats)) 
					applyMultWithJumps(state, nSigns, nRepeats);
		if (state.getHieroPos() < state.getHieroLength()) {
			String[] nextSign = state.getNextHiero(1);
			Function functionSp = new FunctionSpurious(nextSign);
			applyWithJumps(state, 1, functionSp);
			if (signTable.isNumeral(nextSign[0])) 
				createNumber(state);
		}
	}

	// Get FSA.
	public Fsa<ComplexConfig,List<Function>> getFsa() {
		return fsa;
	}

	// Create final state.
	private ComplexConfig getFinalState() {
		if (finalState == null) {
			finalState = new FinalConfig();
			fsa.addFinal(finalState);
		}
		return finalState;
	}

	private void createNumber(ComplexConfig state) {
		NumberConfig config = new NumberConfig();
		int n = 0;
		while (state.getHieroPos() + n < state.getHieroLength() &&
					signTable.isNumeral(state.getSign(state.getHieroPos() + n))) {
			String hi = state.getSign(state.getHieroPos() + n);
			int num = signTable.getNumeral(hi);
			NumberConfig nextConfig = config.apply(hi, num);
			if (nextConfig == null)
				break;
			else
				config = nextConfig;
			n++;
		}
		String[] nextSigns = state.getNextHiero(n);
		Function function = new FunctionNum(nextSigns, config.getTransLow());
		applyWithJumps(state, n, function);
	}

	// Create new transitions by traversing sign table, having read n signs.
	private void createFrom(ComplexConfig state, int n, TrieList<Function> node) {
		for (Function function : node.get()) 
			applyWithJumps(state, n, function);
		if (state.getHieroPos() + n < state.getHieroLength()) {
			String nextSign = state.getSign(state.getHieroPos() + n);
			if (node.hasNext(nextSign)) 
				createFrom(state, n+1, node.next(nextSign));
		}
	}

	// A repetition of nsigns signs, nrepeats times.
	private void applyMultWithJumps(ComplexConfig state, int nSigns, int nRepeats) {
		String[] nextHiero = state.getNextHiero(nSigns * nRepeats);
		Function functionFemPlur = new FunctionMultFemPlur(nextHiero, nRepeats+1);
		applyWithJumps(state, nextHiero.length, functionFemPlur);
		Function functionSem = new FunctionMultSem(nextHiero, nRepeats+1);
		applyWithJumps(state, nextHiero.length, functionSem);
		if (nRepeats == 1) {
			Function functionWj = new FunctionMultSuffix(nextHiero, nRepeats+1, new TransLow("wj"));
			applyWithJumps(state, nextHiero.length, functionWj);
			Function functionj = new FunctionMultSuffix(nextHiero, nRepeats+1, new TransLow("j"));
			applyWithJumps(state, nextHiero.length, functionj);
		} else if (nRepeats == 2) {
			Function functionW = new FunctionMultSuffix(nextHiero, nRepeats+1, new TransLow("w"));
			applyWithJumps(state, nextHiero.length, functionW);
		}
	}

	// Try to apply if possible, if needed with jump before. This is a
	// function corresponding to n signs.
	private void applyWithJumps(ComplexConfig state, int n, Function function) {
		if (function.applicable(state)) {
			List<Function> functions = new LinkedList<Function>();
			functions.add(function);
			addTrans(state, n, functions);
		} 
		List<Integer> dists = function.jumpApplicable(state);
		for (Integer dist : dists) {
			List<Function> functions = new LinkedList<Function>();
			Function jump = new FunctionJump(dist);
			functions.add(jump);
			functions.add(function);
			addTrans(state, n, functions);
		}
	}

	// Add new transition, for list of functions, corresponding to n signs.
	protected void addTrans(ComplexConfig state, int n, List<Function> functions) {
		String[] hiero = state.getHiero();
		int hieroPos = state.getHieroPos() + n;
		Function[] history = state.getHistory();
		SimpleConfig config = state;
		double weight = 0;
		for (Function function : functions) {
			config = function.apply(config);
			weight += weight(history, function);
			history = ComplexConfig.updateHistory(history, function);
		}
		ComplexConfig nextState = 
			new ComplexConfig(config, hiero, hieroPos, history);
		boolean isNew = fsa.addTrans(state, functions, nextState, weight);
		if (isNew)
			agenda.push(nextState);
	}

	protected double weight(Function[] history, Function fun) {
		return fun.getWeight();
	}

	protected double weight(Function[] history, List<Function> functions) {
		double weight = 0;
		for (Function fun : functions) 
			weight += fun.getWeight();
		return weight;
	}

	protected double weightEnd(Function[] history) {
		return 0;
	}

	public boolean success() {
		return !fsa.getFinalStates().isEmpty();
	}

	// Get best list of functions.
	public List<Function> getBest() {
		List<Function> bestFunctions = new LinkedList<Function>();
		List<FsaTrans<ComplexConfig,List<Function>>> bestPath = 
				(new FsaShortestPath(fsa)).shortestPath();
		for (FsaTrans<ComplexConfig,List<Function>> trans : bestPath)
			bestFunctions.addAll(trans.label());
		return bestFunctions;
	}

	// Print automaton (for testing).
	public void print() {
		for (ComplexConfig state : fsa.getStates()) {
			System.out.println(state);
			for (FsaTrans<ComplexConfig,List<Function>> trans : fsa.fromTransitions(state)) {
				System.out.print("-->[" + NegLogProb.from(trans.weight()) + "]");
				for (Function function : trans.label())
					System.out.print(function + " ");
				System.out.println("	" + trans.toState());
			}
		}
		for (ComplexConfig state : fsa.getFinalStates())
			System.out.println("FINAL " + state);
	}

	// Print best path.
	public void printBest() {
		for (Function function : getBest())
			System.out.println(function);
	}

	// For debugging.
	private void printFunctions(Function[] history) {
		System.err.println("[");
		for (int i = 0; i < history.length; i++) {
			System.err.println(history[i].toString());
		}
		System.err.println("]");
	}

}
