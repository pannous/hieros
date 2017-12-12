package nederhof.util.collections;

// Element with weight (float).
public class WeightedElem<E> implements Comparable<WeightedElem<E>> {

	private E elem;
	private float weight;

	public WeightedElem(E elem , float weight) {
		this.elem = elem;
		this.weight = weight;
	}

	public E elem() {
		return elem;
	}
	public float weight() {
		return weight;
	}

	public int compareTo(WeightedElem other) {
		if (weight < other.weight)
			return -1;
		else if (weight > other.weight)
			return 1;
		else
			return 0;
	}

}
