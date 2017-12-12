/***************************************************************************/
/*                                                                         */
/*  LinearDoubleFiniteAutomatonState.java                                  */
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

// State of non-deterministic finite automaton without epsilon
// transitions, which states can be ordered linearly.
// There are two classes of transitions.

package nederhof.util;

import java.util.*;

public interface LinearDoubleFiniteAutomatonState 
	extends DoubleFiniteAutomatonState, Comparable {

    public LinearDoubleFiniteAutomatonState nextState();

}
