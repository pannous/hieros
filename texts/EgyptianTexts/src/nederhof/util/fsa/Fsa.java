package nederhof.util.fsa;

import java.util.*;

import nederhof.util.math.*;

// Finite automaton, with S states and L transition labels.
// The weights are assumed to be negative log of probabilities.
public class Fsa<S extends Comparable,L> {

	// All states.
	private TreeSet<S> states = new TreeSet<S>();

	// Initial state.
	private S initialState;

	// Final states.
	private TreeSet<S> finalStates = new TreeSet<S>();

	// Transitions leaving state.
	private TreeMap<S,List<FsaTrans<S,L>>> fromTransitions = 
		new TreeMap<S,List<FsaTrans<S,L>>>();
	// Transitions entering state.
	private TreeMap<S,List<FsaTrans<S,L>>> toTransitions = 
		new TreeMap<S,List<FsaTrans<S,L>>>();

	// Constructor.
	public Fsa(S initialState) {
		createState(initialState);
		this.initialState = initialState;
	}

	// Add final state.
	public void addFinal(S finalState) {
		createState(finalState);
		finalStates.add(finalState);
	}

	// Return whether is new state.
	private boolean createState(S state) {
		if (states.contains(state))
			return false;
		else {
			states.add(state);
			fromTransitions.put(state, new ArrayList<FsaTrans<S,L>>());
			toTransitions.put(state, new ArrayList<FsaTrans<S,L>>());
			return true;
		} 
	}

	// Add transition. Return whether toState is new.
	public boolean addTrans(S fromState, L label, S toState, double weight) {
		createState(fromState);
		boolean newToState = createState(toState);
		FsaTrans<S,L> trans = new FsaTrans<S,L>(fromState, label, toState, weight);
		fromTransitions.get(fromState).add(trans);
		toTransitions.get(toState).add(trans);
		return newToState;
	}
	// Add transition without weight.
	public boolean addTrans(S fromState, L label, S toState) {
		return addTrans(fromState, label, toState, NegLogProb.ONE);
	}

	// Get states.
	public Set<S> getStates() {
		return states;
	}
	// Get initial state.
	public S getInitialState() {
		return initialState;
	}
	// Get final states.
	public Set<S> getFinalStates() {
		return finalStates;
	}

	// Get outgoing transitions.
	public List<FsaTrans<S,L>> fromTransitions(S fromState) {
		if (fromTransitions.get(fromState) == null)
			return new ArrayList<FsaTrans<S,L>>();
		else
			return fromTransitions.get(fromState);
	}
	// Get incoming transitions.
	public List<FsaTrans<S,L>> toTransitions(S toState) {
		if (toTransitions.get(toState) == null)
			return new ArrayList<FsaTrans<S,L>>();
		else
			return toTransitions.get(toState);
	}

	// Print automaton.
	public void print() {
		for (S state : fromTransitions.keySet()) {
			System.out.println(state);
			for (FsaTrans<S,L> trans : fromTransitions.get(state)) {
				if (trans.weight() > 0)
					System.out.println("--> " + trans.label() + " " + trans.weight());
				else
					System.out.println("--> " + trans.label());
				System.out.println("	" + trans.toState());
			}
		}
		for (S state : finalStates)
			System.out.println("FINAL " + state);
	}

}

