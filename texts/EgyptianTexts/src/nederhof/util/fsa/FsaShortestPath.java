package nederhof.util.fsa;

import java.util.*;

import nederhof.util.math.*;

// Computes shortest path of FSA.
public class FsaShortestPath<S extends Comparable,L> {

	// For which FSA.
	private Fsa<S,L> fsa;

	// Constructor.
	public FsaShortestPath(Fsa<S,L> fsa) {
		this.fsa = fsa;
	}

    // State associated with length of shortest path.
    private class StateAndWeight implements Comparable<StateAndWeight> {
        public S state;
        public double weight;
        public FsaTrans<S,L> previousTrans = null;
        public StateAndWeight(S state, double weight) {
            this.state = state;
            this.weight = weight;
        }
        public int compareTo(StateAndWeight other) {
            return Double.compare(weight, other.weight);
        }
    }

    // Get shortest path.
    public List<FsaTrans<S,L>> shortestPath() {
        Map<S,StateAndWeight> extended = new TreeMap<S,StateAndWeight>();
        PriorityQueue<StateAndWeight> agenda = new PriorityQueue<StateAndWeight>();
        extendState(fsa.getInitialState(), 0.0, extended, agenda);
        for (S state : fsa.getStates()) 
            extendState(state, Double.MAX_VALUE, extended, agenda);
        while (!agenda.isEmpty()) {
            StateAndWeight min = agenda.remove();
            if (fsa.getFinalStates().contains(min.state))
                return getPath(min, extended);
            for (FsaTrans<S,L> trans : fsa.fromTransitions(min.state)) {
                S toState = trans.toState();
                StateAndWeight toExtended = extended.get(toState);
                double toWeight = NegLogProb.mult(min.weight, trans.weight());
                if (toWeight < toExtended.weight) {
                    boolean removed = agenda.remove(toExtended);
                    if (removed) {
                        toExtended.weight = toWeight;
                        toExtended.previousTrans = trans;
                        agenda.add(toExtended);
                    }
                }
            }
        }
        return new LinkedList<FsaTrans<S,L>>();
    }

    // Add extended object to state.
    private void extendState(S state, double val,
            Map<S,StateAndWeight> extended, PriorityQueue<StateAndWeight> agenda) {
        if (extended.get(state) != null)
            return;
        StateAndWeight stateAndWeight = new StateAndWeight(state, val);
		extended.put(state, stateAndWeight);
        agenda.add(stateAndWeight);
    }

    // Do backtracing of shortest path.
    private List<FsaTrans<S,L>> getPath(StateAndWeight state, Map<S,StateAndWeight> extended) {
		LinkedList<FsaTrans<S,L>> path = new LinkedList<FsaTrans<S,L>>();
        while (state.previousTrans != null) {
            path.addFirst(state.previousTrans);
            state = extended.get(state.previousTrans.fromState());
        }
        return path;
    }

}
