/***************************************************************************/
/*                                                                         */
/*  PreamblePars.java                                                      */
/*                                                                         */
/*  Copyright (c) 2006 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// The paragraphs in preamble.

package nederhof.align;

import java.util.*;

class PreamblePars {

    // The paragraphs. Each is LinkedList of Elems.
    private Vector pars = new Vector();

    // Is last paragraph finished?
    private boolean lastIsFinished = true;

    // Add element to paragraph. If previous paragraph was finished,
    // create a new one.
    public void add(Object element) {
	LinkedList par;
	if (lastIsFinished) {
	    par = new LinkedList();
	    pars.add(par);
	    lastIsFinished = false;
	} else
	    par = (LinkedList) pars.elementAt(size()-1);
	par.addLast(element);
    }

    // Make present paragraph finished.
    public void makeFinished() {
	lastIsFinished = true;
    }

    // Normalize spaced in paragraphs.
    public void normalize() {
	for (int i = 0; i < pars.size(); i++) {
	    LinkedList par = getPar(i);
	    StreamSystem.normalizeList(par);
	}
    }

    // How many paragraphs.
    public int size() {
	return pars.size();
    }

    // Get i-th paragraph.
    public LinkedList getPar(int i) {
	return (LinkedList) pars.elementAt(i);
    }

}
