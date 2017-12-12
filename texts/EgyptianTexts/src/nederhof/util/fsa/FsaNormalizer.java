package nederhof.util.fsa;

import java.util.*;

import nederhof.util.math.*;

// Assuming weights are negative log probabilities, normalize FSA.
public class FsaNormalizer<S extends Comparable,L> {

	public FsaNormalizer() {
	}

	public void normalize(Fsa<S,L> aut) {
		Set<S> states = aut.getStates();
		for (S state : states)
			normalize(aut, state);
	}

	public void normalize(Fsa<S,L> aut, S state) {
		double sum = NegLogProb.ZERO;
		List<FsaTrans<S,L>> transs = aut.fromTransitions(state);
		for (FsaTrans<S,L> trans : transs) 
			sum = NegLogProb.add(sum, trans.weight());
		for (FsaTrans<S,L> trans : transs) {
			double p = NegLogProb.div(trans.weight(), sum);
			trans.setWeight(p);
		}
	}

}
