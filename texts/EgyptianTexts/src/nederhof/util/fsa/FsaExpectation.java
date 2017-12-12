package nederhof.util.fsa;

import java.util.*;

import nederhof.util.math.*;

// Computes expected number of times transition is used,
// and expected number of times state is traversed.
public class FsaExpectation<S extends Comparable,L> {

	// Transitions coupled to expected count.
	private List<FsaTrans<S,L>> transExps = new LinkedList<FsaTrans<S,L>>();

	// States mapped to expected count.
	private Map<S,Double> stateExp = new TreeMap<S,Double>();

	public FsaExpectation(Fsa<S,L> fsa) {
		FsaForward<S,Double> forward = new FsaForward(fsa);
		FsaBackward<S,Double> backward = new FsaBackward(fsa);
		double total = backward.sum();
		for (S state : fsa.getStates()) {
			double fore = forward.get(state);
			double back = backward.get(state);
			for (FsaTrans<S,L> trans : fsa.fromTransitions(state)) {
				double transBack = backward.get(trans.toState());
				double transProd = NegLogProb.mult(trans.weight(),
						NegLogProb.mult(fore, transBack));
				double transProdNorm = NegLogProb.div(transProd, total);
				FsaTrans<S,L> transExp = new FsaTrans(trans, transProdNorm);
				transExps.add(transExp);
			}
			double stateProd = NegLogProb.mult(fore, back);
			double stateProdNorm = NegLogProb.div(stateProd, total);
			stateExp.put(state, stateProdNorm);
		}
	}

	// Get transitions with expected count as weight.
	public List<FsaTrans<S,L>> transCounts() {
		return transExps;
	}
	// Get expected count of state.
	public double stateCount(S state) {
		return stateExp.get(state);
	}

}
