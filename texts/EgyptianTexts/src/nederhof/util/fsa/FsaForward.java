package nederhof.util.fsa;

import java.util.*;

import nederhof.util.math.*;

// Compute forward values in FSA.
public class FsaForward<S extends Comparable,L> extends TreeMap<S,Double> {

	// For which FSA.
	private Fsa<S,L> fsa;

	// Constructor.
	public FsaForward(Fsa<S,L> fsa) {
		this.fsa = fsa;
		List<S> sort = new FsaTopologicalSort(fsa);
		for (S state : sort) {
			double sum = state == fsa.getInitialState() ? NegLogProb.ONE : NegLogProb.ZERO;
			for (FsaTrans<S,L> trans : fsa.toTransitions(state)) {
				double prod = NegLogProb.mult(trans.weight(), get(trans.fromState()));
				sum = NegLogProb.add(sum, prod);
			}
			put(state, sum);
		}
	}

	// Total probability of all paths.
	public double sum() {
		double sum = NegLogProb.ZERO;
		for (S state : fsa.getFinalStates())
			sum = NegLogProb.add(sum, get(state));
		return sum;
	}
}
