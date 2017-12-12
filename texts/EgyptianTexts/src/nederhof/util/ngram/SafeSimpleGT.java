package nederhof.util.ngram;

import java.util.*;

// Simple Good-Turing smoothing, but robust to having a single value, or no
// value at all.
public class SafeSimpleGT extends SimpleGT {

    public SafeSimpleGT(TreeMap<Integer,Integer> map) {
	super(extendedMap(map));
    }

    // If there is no key, add two dummy values.
    // If only key is 1, then add half the value for key 2.
    // If first key is > 1, then add double the value for key 1.
    private static TreeMap<Integer,Integer> extendedMap(TreeMap<Integer,Integer> map) {
	if (map.size() == 0) {
	    TreeMap<Integer,Integer> extended = new TreeMap<Integer,Integer>(map);
	    extended.put(1, 2);
	    extended.put(2, 1);
	    return extended;
	} else if (map.firstKey() != 1) {
	    TreeMap<Integer,Integer> extended = new TreeMap<Integer,Integer>(map);
	    int key = map.firstKey();
	    int val = map.get(key);
	    extended.put(1, val * 2);
	    return extended;
	} else if (map.size() == 1) {
	    TreeMap<Integer,Integer> extended = new TreeMap<Integer,Integer>(map);
	    int key = map.firstKey();
	    int val = map.get(key);
	    extended.put(2, Math.max(1, val / 2));
	    return extended;
	} else
	    return map;
    }

}
