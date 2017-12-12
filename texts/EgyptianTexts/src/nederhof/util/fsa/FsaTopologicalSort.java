package nederhof.util.fsa;

import java.util.*;

import nederhof.util.math.*;

// Computes topological sort of states of FSA. Assuming DAG.
public class FsaTopologicalSort<S extends Comparable,L> extends LinkedList<S> {

	// The FSA.
	private Fsa<S,L> fsa;

	// States that are marked.
	private TreeSet<S> permanent = new TreeSet<S>();
	private TreeSet<S> temporary = new TreeSet<S>();

	// Is cyclic?
	private boolean cyclic = false;

	// Constructor.
	public FsaTopologicalSort(Fsa<S,L> fsa) {
		this.fsa = fsa;
		visit(fsa.getInitialState());
	}

	// Do depth-first search.
	private void visit(S state) {
		if (temporary.contains(state)) 
			cyclic = true;
		else if (!permanent.contains(state)) {
			temporary.add(state);
			for (FsaTrans<S,L> trans : fsa.fromTransitions(state))
				visit(trans.toState());
			permanent.add(state);
			temporary.remove(state);
			addFirst(state);
		}
	}

	public boolean isCyclic() {
		return cyclic;
	}

}

