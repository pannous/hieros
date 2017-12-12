// Configuration in matching hieroglyphic against hieroglyphic.

package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.hieroutil.*;
import nederhof.util.*;

public class HiConfiguration implements Comparable {

    // Previous configuration.
    private HiConfiguration prev;

    // Action from which current configuration was obtained
    // from previous.
    private String action;

    // Penalty of last action.
    private double penalty;

    // States in hieroglyphic.
    private LinearFiniteAutomatonState hieroState1;
    private LinearFiniteAutomatonState hieroState2;

    // Constructor for initial configuration.
    public HiConfiguration(LinearFiniteAutomatonState hieroState1,
            LinearFiniteAutomatonState hieroState2) {
        this(null, "", 0, hieroState1, hieroState2);
    }

    // Constructor for subsequent configuration.
    private HiConfiguration(HiConfiguration prev, String action, double penalty,
            LinearFiniteAutomatonState hieroState1,
            LinearFiniteAutomatonState hieroState2) {
        this.prev = prev;
        this.action = action;
        this.penalty = penalty;
        this.hieroState1 = hieroState1;
        this.hieroState2 = hieroState2;
    }

    public HiConfiguration prev() {
        return prev;
    }

    public String action() {
        return action;
    }

    public double penalty() {
        return penalty;
    }

    public LinearFiniteAutomatonState getHieroState1() {
        return hieroState1;
    }

    public LinearFiniteAutomatonState getHieroState2() {
        return hieroState2;
    }

    public boolean isFinal() {
        return hieroState1.isFinal() && hieroState2.isFinal();
    }

    // Go to next configuration.
    public LinkedList nextConfigs() {
        LinkedList configs = new LinkedList();
        deleteHiero(configs);
        insertHiero(configs);
        matchHiero(configs);
        return configs;
    }

    // Delete from first hiero.
    private void deleteHiero(LinkedList configs) {
	TreeMap nexts = hieroState1.getOutTransitions();
	for (Iterator it = nexts.keySet().iterator();
		it.hasNext(); ) {
	    String symbol = (String) it.next();
	    TreeSet states = (TreeSet) nexts.get(symbol);
	    for (Iterator iter = states.iterator();
		    iter.hasNext(); ) {
		LinearFiniteAutomatonState next =
		    (LinearFiniteAutomatonState) iter.next();
		HiConfiguration nextConfig =
		    new HiConfiguration(this, "delete", WordMatch.hieroSkipPenalty,
			    next, hieroState2);
		configs.add(new WeightConfig(WordMatch.hieroSkipPenalty, nextConfig));
	    }
	}
    }
    // Delete from second hiero.
    private void insertHiero(LinkedList configs) {
	TreeMap nexts = hieroState2.getOutTransitions();
	for (Iterator it = nexts.keySet().iterator();
		it.hasNext(); ) {
	    String symbol = (String) it.next();
	    TreeSet states = (TreeSet) nexts.get(symbol);
	    for (Iterator iter = states.iterator();
		    iter.hasNext(); ) {
		LinearFiniteAutomatonState next =
		    (LinearFiniteAutomatonState) iter.next();
		HiConfiguration nextConfig =
		    new HiConfiguration(this, "insert", WordMatch.hieroSkipPenalty,
			    hieroState1, next);
		configs.add(new WeightConfig(WordMatch.hieroSkipPenalty, nextConfig));
	    }
	}
    }

    // Match two identical hieroglyphs.
    private void matchHiero(LinkedList configs) {
	TreeMap nexts1 = hieroState1.getOutTransitions();
	TreeMap nexts2 = hieroState2.getOutTransitions();
	for (Iterator it1 = nexts1.keySet().iterator();
		it1.hasNext(); ) {
	    String symbol1 = (String) it1.next();
	    TreeSet states1 = (TreeSet) nexts1.get(symbol1);
	    for (Iterator it2 = nexts2.keySet().iterator();
		    it2.hasNext(); ) {
		String symbol2 = (String) it2.next();
		TreeSet states2 = (TreeSet) nexts2.get(symbol2);
		if (symbol1.equals(symbol2)) {
		    for (Object next1 : states1) {
			LinearFiniteAutomatonState state1 = (LinearFiniteAutomatonState) next1;
			for (Object next2 : states2) {
			    LinearFiniteAutomatonState state2 = (LinearFiniteAutomatonState) next2;
			    HiConfiguration nextConfig =
				new HiConfiguration(this, "match", 0,
					state1, state2);
			    configs.add(new WeightConfig(0, nextConfig));
			}
		    }
		}
	    }
	}
    }

    public boolean equals(Object o) {
        if (o instanceof HiConfiguration) {
            HiConfiguration other = (HiConfiguration) o;
            return compareTo(other) == 0;
        } else
            return false;
    }

    // Compare to other configuration.
    public int compareTo(Object o) {
        if (o instanceof HiConfiguration) {
            HiConfiguration other = (HiConfiguration) o;
            if (compareTo(this.hieroState1, other.hieroState1) != 0)
                return compareTo(this.hieroState1, other.hieroState1);
	    else 
                return compareTo(this.hieroState2, other.hieroState2);
        } else
            return 1;
    }

    // Compare two objects that may be null. 
    // Null precedes non-null.
    private int compareTo(Comparable o1, Comparable o2) {
        if (o1 == null && o2 == null)
            return 0;
        else if (o1 == null)
            return -1;
        else if (o2 == null)
            return 1;
        else
            return o1.compareTo(o2);
    }

    // Compare booleans.
    private int compareTo(boolean b1, boolean b2) {
        if (b1 == b2)
            return 0;
        else if (!b1)
            return -1;
        else
            return 1;
    }

    // For testing.
    public String toString() {
        return "hiero1=" + hieroState1 + " hiero2=" + hieroState2;
    }

}
