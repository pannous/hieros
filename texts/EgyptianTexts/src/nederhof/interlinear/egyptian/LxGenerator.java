/***************************************************************************/
/*                                                                         */
/*  LxGenerator.java                                                       */
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

// Generates buttons for lexical entries.

package nederhof.interlinear.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import nederhof.interlinear.frame.*;
import nederhof.util.*;

public class LxGenerator implements EditorComponentGenerator {

    // Text to which this is added.
    private StyledTextPane text;

    // User of this generator.
    private EditChainElement parent;

    public LxGenerator(StyledTextPane text, EditChainElement parent) {
        this.text = text;
        this.parent = parent;
    }

    // Make new hieroglyphic button.
    public Component makeComponent(ChangeListener listener) {
	return new LxButton(new LxInfo(), listener);
    }
    public Component makeComponent(Object object, ChangeListener listener) {
	LxInfo info = (LxInfo) object;
	LxButton b = new LxButton(info, listener);
	return b;
    }
    public Object extract(Component comp) {
	LxButton button = (LxButton) comp;
	LxInfo info = button.info();
	return info;
    }

    // Button showing hieroglyphic.
    private class LxButton extends JButton implements ActionListener {
	// Content.
	private LxInfo info;

	// External agent that listens to changes.
	private ChangeListener listener;

        public LxButton(LxInfo info, ChangeListener listener) {
	    this.listener = listener;
	    setBackground(Color.WHITE);
	    setForeground(Color.BLUE);
	    setInfo(info);
	    setAlignmentY(0.75f);
	    setActionCommand("edit");
	    addActionListener(this);
        }

	public LxInfo info() {
	    return info;
	}

	public Dimension getMinimumSize() {
	    return getPreferredSize();
	}
	public Dimension getMaximumSize() {
	    return getPreferredSize();
	}

	// Upon click, edit contents.
        public void actionPerformed(ActionEvent e) {
	    createLxEditor();
        }

        // Embedded editor of entry.
        private void createLxEditor() {
            parent.allowEditing(false);
            new LexicalEditor(info) {
                protected void receive(LxInfo out) {
                    listener.stateChanged(new ChangeEvent(this));
                    parent.allowEditing(true);
		    setInfo(out);
                    text.requestFocus();
                }
                protected void cancel() {
                    parent.allowEditing(true);
                    text.requestFocus();
                }
		/* TODO remove?
                protected void makeChanged() {
                }
		*/
            };
        }

	// Set info.
	private void setInfo(LxInfo info) {
	    this.info = info;
	    if (!info.texttr.matches("\\s*"))
		setText(info.texttr);
	    else if (!info.textal.matches("\\s*"))
		setText(info.textal);
	    else if (!info.dicttr.matches("\\s*"))
		setText(info.dicttr);
	    else if (!info.dictal.matches("\\s*"))
		setText(info.dictal);
	    else
		setText("...");
	}

    }

}
