/***************************************************************************/
/*                                                                         */
/*  LegendPlace.java                                                       */
/*                                                                         */
/*  Copyright (c) 2009 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of PhilologEG, and may only be */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Part of legend dealing with place.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

abstract class LegendPlace extends LegendParam implements ItemListener {

    // Old value.
    private String old;
    // Last returned value.
    private String lastReturned;

    // Grid of buttons of places, including c for center.
    JRadioButton ts = new JRadioButton("ts");
    JRadioButton t = new JRadioButton("t");
    JRadioButton te = new JRadioButton("te");
    JRadioButton s = new JRadioButton("s");
    JRadioButton c = new JRadioButton("-");
    JRadioButton e = new JRadioButton("e");
    JRadioButton bs = new JRadioButton("bs");
    JRadioButton b = new JRadioButton("b");
    JRadioButton be = new JRadioButton("be");

    public LegendPlace(String old) {
	this.old = old;
	this.lastReturned = old;
	setLayout(new GridLayout(3, 3));

	add(ts);
	add(t);
	add(te);
	add(s);
	add(c);
	add(e);
	add(bs);
	add(b);
	add(be);

	ButtonGroup group = new ButtonGroup();
	group.add(ts);
	group.add(t);
	group.add(te);
	group.add(s);
	group.add(c);
	group.add(e);
	group.add(bs);
	group.add(b);
	group.add(be);

	ts.addItemListener(this);
	t.addItemListener(this);
	te.addItemListener(this);
	s.addItemListener(this);
	c.addItemListener(this);
	e.addItemListener(this);
	bs.addItemListener(this);
	b.addItemListener(this);
	be.addItemListener(this);

	setValue(old);
    }

    // Set to value.
    private void setValue(String v) {
	if (v.equals("ts"))
	    ts.setSelected(true);
	else if (v.equals("t"))
	    t.setSelected(true);
	else if (v.equals("te"))
	    te.setSelected(true);
	else if (v.equals("s"))
	    s.setSelected(true);
	else if (v.equals("e"))
	    e.setSelected(true);
	else if (v.equals("bs"))
	    bs.setSelected(true);
	else if (v.equals("b"))
	    b.setSelected(true);
	else if (v.equals("be"))
	    be.setSelected(true);
	else
	    c.setSelected(true);
    }

    // Get value.
    private String getValue() {
	if (ts.isSelected())
	    return "ts";
	else if (t.isSelected())
	    return "t";
	else if (te.isSelected())
	    return "te";
	else if (s.isSelected())
	    return "s";
	else if (e.isSelected())
	    return "e";
	else if (bs.isSelected())
	    return "bs";
	else if (b.isSelected())
	    return "b";
	else if (be.isSelected())
	    return "be";
	else
	    return "";
    }

    // Clear to blank.
    public void clear() {
	setValue("");
	processMaybeChanged(getValue());
    }

    // Reset to old value.
    public void resetValue() {
        setValue(old);
        processMaybeChanged(getValue());
    }

    // Value is selected.
    public void itemStateChanged(ItemEvent e) {
	if (e.getStateChange() == ItemEvent.SELECTED)
	    processMaybeChanged(getValue());
    }

    private void processMaybeChanged(String other) {
        if (!other.equals(lastReturned)) {
            lastReturned = other;
            processChanged(other);
        }
    }

    // Process that value is changed.
    abstract protected void processChanged(String v);

}
