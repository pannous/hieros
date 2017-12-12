package nederhof.util.collections;

import java.util.*;

// Priority queue with maximum number of elements. Weights are floats.
public class BeamQueue<E> {

	// The queue.
	private TreeSet<WeightedElem<E>> queue = new TreeSet<WeightedElem<E>>(); 

	// Beam size.
	private int beam;

	// Upper limit. Beyond this values are ignored.
	private float upper = Float.MAX_VALUE;

	public BeamQueue(int beam) {
		this.beam = beam;
	}
	// Without pruning.
	public BeamQueue() {
		this(Integer.MAX_VALUE);
	}

    // Add, but no more than beam size items can be added.
    // If new item too weighty, do not add.
    // Set new upper weight to last element if beam reached.
    public void add(E elem , float weight) {
        if (weight <= upper) {
            queue.add(new WeightedElem<E>(elem, weight));
            if (queue.size() > beam)
                queue.remove(queue.last());
            if (queue.size() == beam)
                upper = queue.last().weight();
        }
    }
	public void add(WeightedElem<E> weighted) {
		add(weighted.elem(), weighted.weight());
	}

	// Return best elements.
    public Vector<E> best() {
        Vector<E> best = new Vector<E>();
        for (WeightedElem<E> item : queue)
            best.add(item.elem());
        return best;
    }

	// Return best elements and their weights.
	public Vector<WeightedElem<E>> bestWeighted() {
		return new Vector<WeightedElem<E>>(queue);
	}

}
