/***************************************************************************/
/*                                                                         */
/*  SwitchAndTopgroup.java                                                 */
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

// Combines topgroup preceded by a switch.
class SwitchAndTopgroup {

    public ResSwitch switchs;
    public ResTopgroup group;

    public SwitchAndTopgroup(ResSwitch switchs, ResTopgroup group) {
	this.switchs = switchs;
	this.group = group;
    }

    public SwitchAndTopgroup(ResTopgroup group) {
	this.switchs = new ResSwitch();
	this.group = group;
    }

    public SwitchAndTopgroup addVert(String maybeArg, ResSwitch switchs2,
	    SwitchAndTopgroup next) {
	ResOp op = new ResOp();
	if (maybeArg.equals(""))
	    ; // no argument
	else if (maybeArg.equals("fit"))
	    op.fit = Boolean.TRUE;
	else
	    System.err.println("ResArg " + maybeArg + " ignored in SwitchAndTopgroup");
	ResSwitch interSwitchs = switchs2.join(next.switchs);
	group = ResComposer.joinVert(group, op, interSwitchs, next.group);
	return this;
    }

    public SwitchAndTopgroup addHor(String maybeArg, ResSwitch switchs2, 
	    SwitchAndTopgroup next) {
	ResOp op = new ResOp();
	if (maybeArg.equals(""))
	    ; // no argument
	else if (maybeArg.equals("fit"))
	    op.fit = Boolean.TRUE;
	else
	    System.err.println("ResArg " + maybeArg + " ignored in SwitchAndTopgroup");
	ResSwitch interSwitchs = switchs2.join(next.switchs);
	group = ResComposer.joinHor(group, op, interSwitchs, next.group);
	return this;
    }

    public SwitchAndTopgroup distribute(Vector shades, ResSwitch switchs2) {
	group = ResComposer.distribute(group, 
		null, 0, 1.0f, Color16.NO_COLOR, null, shades, new Vector(),
		switchs2);
	return this;
    }

}
