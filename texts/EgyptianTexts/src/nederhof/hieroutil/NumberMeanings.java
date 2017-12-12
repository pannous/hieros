/***************************************************************************/
/*                                                                         */
/*  NumberMeanings.java                                                    */
/*                                                                         */
/*  Copyright (c) 2008 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Processing sequences of numerals to compute number.

package nederhof.hieroutil;

import java.util.*;

import nederhof.util.*;

public class NumberMeanings {

    // Mapping from glyphs to possible numeric meanings.
    private static TreeMap numerals = new TreeMap();

    static {
	numerals.put("Z1", new Integer(1));
	numerals.put("Z4", new Integer(2));
	numerals.put("Z2", new Integer(3));
	numerals.put("V20", new Integer(10));
	numerals.put("V1", new Integer(100));
	numerals.put("M12", new Integer(1000));
	numerals.put("D50", new Integer(10000));
	numerals.put("I8", new Integer(100000));
	numerals.put("C11", new Integer(1000000));
    }

    // Add transitions for numbers.
    // From the outside, this method is to be called with the initial state,
    // and the process is propagated to other states.
    public static void induceNumbers(DoubleLinearFiniteAutomatonState state) {
	while (state != null) {
	    induceNumbers(state, 0, state);
	    state = (DoubleLinearFiniteAutomatonState) state.getNextState();
	}
    }

    // Add transitions for numbers, assuming number has already been read
    // between states.
    private static void induceNumbers(DoubleLinearFiniteAutomatonState fromState,
	    int num, DoubleLinearFiniteAutomatonState state) {
	TreeMap outs = state.getOutTransitions();
	for (Iterator iter = outs.keySet().iterator(); iter.hasNext(); ) {
	    String sign = (String) iter.next();
	    Set toStates = (Set) outs.get(sign);
	    for (Iterator it = toStates.iterator(); it.hasNext(); ) {
		DoubleLinearFiniteAutomatonState toState =
		    (DoubleLinearFiniteAutomatonState) it.next();
		if (numerals.get(sign) != null) {
		    Integer value = (Integer) numerals.get(sign);
		    int v = value.intValue();
		    int sum = num + v;
		    HieroMeaning meaning = new HieroMeaning("num", "" + sum);
		    fromState.addInducedTransition(meaning, toState);
		    induceNumbers(fromState, sum, toState);
		}
	    }
	}
    }

    // Two meanings used in below.
    private static final HieroMeaning dualMeaning = new HieroMeaning("dualis");
    private static final HieroMeaning pluralMeaning = new HieroMeaning("pluralis");

    // Add transitions for repeated occurrences of signs,
    // i.e. dual and plural.
    // From the outside, this method is to be called with the initial state,
    // and the process is propagated to other states.
    // Note: A previous implementation using recursion led to stack overflow for
    // large texts (the Java developers at Sun are idiots).
    public static void induceRepeat(DoubleLinearFiniteAutomatonState state) {
	while (state != null) {
	    induceDual(state);
	    state = (DoubleLinearFiniteAutomatonState) state.getNextState();
	}
    }

    private static void induceDual(DoubleLinearFiniteAutomatonState state) {
	TreeMap outs = state.getOutTransitions();
	for (Iterator iter = outs.keySet().iterator(); iter.hasNext(); ) {
	    String sign = (String) iter.next();
	    Set toStates = (Set) outs.get(sign);
	    for (Iterator it = toStates.iterator(); it.hasNext(); ) {
		DoubleLinearFiniteAutomatonState toState =
		    (DoubleLinearFiniteAutomatonState) it.next();
		induceDual(state, sign, toState);
	    }
	}
    }

    private static void induceDual(DoubleLinearFiniteAutomatonState prevState,
	    String prevSign, DoubleLinearFiniteAutomatonState state) {
	TreeMap outs = state.getOutTransitions();
	for (Iterator iter = outs.keySet().iterator(); iter.hasNext(); ) {
	    String sign = (String) iter.next();
	    Set toStates = (Set) outs.get(sign);
	    for (Iterator it = toStates.iterator(); it.hasNext(); ) {
		DoubleLinearFiniteAutomatonState toState =
		    (DoubleLinearFiniteAutomatonState) it.next();
		if (sign.equals(prevSign)) {
		    state.addInducedTransition(dualMeaning, toState);
		    inducePlural(state, sign, toState);
		}
	    }
	}
    }

    private static void inducePlural(DoubleLinearFiniteAutomatonState prevState,
	    String prevSign, DoubleLinearFiniteAutomatonState state) {
	TreeMap outs = state.getOutTransitions();
	for (Iterator iter = outs.keySet().iterator(); iter.hasNext(); ) {
	    String sign = (String) iter.next();
	    Set toStates = (Set) outs.get(sign);
	    for (Iterator it = toStates.iterator(); it.hasNext(); ) {
		DoubleLinearFiniteAutomatonState toState =
		    (DoubleLinearFiniteAutomatonState) it.next();
		if (sign.equals(prevSign)) 
		    prevState.addInducedTransition(pluralMeaning, toState);
	    }
	}
    }

}
