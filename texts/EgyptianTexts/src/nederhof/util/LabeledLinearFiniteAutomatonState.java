/***************************************************************************/
/*                                                                         */
/*  LabeledLinearFiniteAutomatonState.java                                 */
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

// State in linear finite automaton, with ordering determined by label.

package nederhof.util;

import java.util.*;

public class LabeledLinearFiniteAutomatonState 
		implements LinearFiniteAutomatonState {

    // Label of state.
    private Comparable label;

    // previous state in linear order.
    private LinearFiniteAutomatonState prevState = null;

    // Next state in linear order.
    private LinearFiniteAutomatonState nextState = null;

    // Outgoing transitions.
    private TreeMap outTransitions = new TreeMap();

    // Is final.
    private boolean isFinal = false;

    // Constructor.
    public LabeledLinearFiniteAutomatonState(Comparable label) {
	this.label = label;
    }

    // Get label.
    public Comparable getLabel() {
	return label;
    }

    // Set next state.
    public void setNextState(LabeledLinearFiniteAutomatonState nextState) {
	this.nextState = nextState;
	nextState.prevState = this;
    }

    // Get previous state.
    public LinearFiniteAutomatonState getPrevState() {
	return prevState;
    }

    // Get next state.
    public LinearFiniteAutomatonState getNextState() {
	return nextState;
    }

    // Get outgoing transitions.
    public TreeMap getOutTransitions() {
	return outTransitions;
    }

    // Add transition with label.
    public void addTransition(Comparable label,
	    FiniteAutomatonState toState) {
	addTransition(getOutTransitions(), label, toState);
    }

    // Generic adding transition to set.
    protected static void addTransition(TreeMap outs, 
	    Comparable label, FiniteAutomatonState toState) {
	if (outs.get(label) == null) 
	    outs.put(label, new TreeSet());
	Set toStates = (Set) outs.get(label);
	toStates.add(toState);
    }

    // Set whether final.
    public void setFinal(boolean isFinal) {
	this.isFinal = isFinal;
    }

    // Is final?
    public boolean isFinal() {
	return isFinal;
    }

    // Comparison with label.
    public int compareTo(Object o) {
	if (o instanceof LabeledLinearFiniteAutomatonState) {
	    LabeledLinearFiniteAutomatonState other = 
		(LabeledLinearFiniteAutomatonState) o;
	    return label.compareTo(other.label);
	} else
	    return 1;
    }

    // String is label.
    public String toString() {
	return "" + label;
    }

}
