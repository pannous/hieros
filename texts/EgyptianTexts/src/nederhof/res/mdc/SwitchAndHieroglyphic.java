/***************************************************************************/
/*                                                                         */
/*  SwitchAndHieroglyphic.java                                             */
/*                                                                         */
/*  Copyright (c) 2008 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of PhilologEG, and may only be */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

package nederhof.res.mdc;

import java.util.*;

import nederhof.res.*;

// Combines hieroglyphic preceded by a switch.
class SwitchAndHieroglyphic {

    public ResSwitch switchs;
    public ResHieroglyphic hiero;

    public SwitchAndHieroglyphic(ResSwitch switchs) {
	this.switchs = switchs;
	this.hiero = null;
    }

    public SwitchAndHieroglyphic(SwitchAndTopgroup group) {
	this.switchs = group.switchs;
	this.hiero = new ResHieroglyphic(group.group);
    }

    public SwitchAndHieroglyphic add(SwitchAndTopgroup group) {
	if (hiero == null) {
	    switchs = switchs.join(group.switchs);
	    hiero = new ResHieroglyphic(group.group);
	} else
	    hiero.addGroup(group.switchs, group.group);
	return this;
    }

    // Turn into ResHorgroup, and add switch at end.
    public SwitchAndTopgroup horgroup(ResSwitch switchs2) {
	ResTopgroup group;
	if (hiero == null)
	    group = new ResEmptyglyph(0.0f, 0.0f, switchs2);
	else {
	    if (hiero.nGroups() == 1) 
		group = hiero.group(0);
	    else 
		group = ComposeHelper.toHorgroup(
			hiero.groups, hiero.ops, hiero.switches, 1.0f);
	    group = ResComposer.distribute(group,
		    null, 0, 1.0f, Color16.NO_COLOR, null,
		    new Vector(), new Vector(), switchs2);
	}
	return new SwitchAndTopgroup(switchs, group);
    }

    // Put in the form of a fragment.
    public ResFragment res() {
	return new ResFragment(switchs, hiero);
    }

    public String toString() {
	return switchs.toString() + 
	    (hiero == null ? "" : hiero.toString());
    }

}
