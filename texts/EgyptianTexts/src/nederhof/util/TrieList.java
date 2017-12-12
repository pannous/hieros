// Trie. Mapping from strings to subtries.
// Each node in trie has list of objects.

package nederhof.util;

import java.util.*;

public class TrieList<E> {

    private LinkedList<E> objects = new LinkedList<E>();

    public void add(E o) {
	objects.add(o);
    }

    public LinkedList<E> get() {
	return objects;
    }

    private TreeMap<String,TrieList<E>> transitions = new TreeMap<String,TrieList<E>>();

    public boolean hasNext(String label) {
	return transitions.get(label) != null;
    }

    public TrieList<E> next(String label) {
	if (transitions.get(label) == null)
	    transitions.put(label, new TrieList<E>());
	return transitions.get(label);
    }

    public TrieList<E> next(String[] labels) {
	TrieList<E> to = this;
	for (int i = 0; i < labels.length; i++) 
	    to = to.next(labels[i]);
	return to;
    }

}
