/***************************************************************************/
/*                                                                         */
/*  LabeledDoubleLinearFiniteAutomatonState.java                           */
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
// There are additional (induced) transitions.

package nederhof.util;

import java.util.*;

public class LabeledDoubleLinearFiniteAutomatonState 
		extends LabeledLinearFiniteAutomatonState
		implements DoubleLinearFiniteAutomatonState {

    // Induced outgoing transitions.
    private TreeMap inducedOutTransitions = new TreeMap();

    // Constructor.
    public LabeledDoubleLinearFiniteAutomatonState(Comparable label) {
	super(label);
    }

    // Get induced outgoing transitions.
    public TreeMap getInducedOutTransitions() {
	return inducedOutTransitions;
    }

    // Add induced transition.
    public void addInducedTransition(Comparable label,
	    FiniteAutomatonState toState) {
	addTransition(getInducedOutTransitions(), label, toState);
    }

}
