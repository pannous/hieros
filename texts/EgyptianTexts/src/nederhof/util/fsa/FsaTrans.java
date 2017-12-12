package nederhof.util.fsa;

import java.util.*;

// Transition of finite automaton.
// The weights are assumed to be negative log of probabilities.
public class FsaTrans<S extends Comparable,L> {

	private S fromState;
	private L label;
	private S toState;
	private double weight = 0;

	// Constructor.
	public FsaTrans(S fromState, L label, S toState) {
		this.fromState = fromState;
		this.label = label;
		this.toState = toState;
	}
	// Constructor with weight.
	public FsaTrans(S fromState, L label, S toState, double weight) {
		this(fromState, label, toState);
		this.weight = weight;
	}
	// Copy constructor with weight.
	public FsaTrans(FsaTrans<S,L> trans, double weight) {
		this(trans.fromState, trans.label, trans.toState, weight);
	}

	public S fromState() {
		return fromState;
	}
	public L label() {
		return label;
	}
	public S toState() {
		return toState;
	}
	public double weight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

}
