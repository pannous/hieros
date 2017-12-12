package nederhof.util.fsa;

import java.util.*;

import nederhof.util.math.*;

// Compute backward values in FSA.
public class FsaBackward<S extends Comparable,L> extends TreeMap<S,Double> {

    // For which FSA.
    private Fsa<S,L> fsa;

    // Constructor.
    public FsaBackward(Fsa<S,L> fsa) {
        this.fsa = fsa;
        ArrayList<S> sort = new ArrayList(new FsaTopologicalSort(fsa));
		for (int i = sort.size()-1; i >= 0; i--) {
			S state = sort.get(i);
			double sum = fsa.getFinalStates().contains(state) ? NegLogProb.ONE : NegLogProb.ZERO;
			for (FsaTrans<S,L> trans : fsa.fromTransitions(state)) {
				double prod = NegLogProb.mult(trans.weight(), get(trans.toState()));
				sum = NegLogProb.add(sum, prod);
			}   
			put(state, sum);
		}
    }   

	// Total probability of all paths.
	public double sum() {
		return get(fsa.getInitialState());
	}
}
