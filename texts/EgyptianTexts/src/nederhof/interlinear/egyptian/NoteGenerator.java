/***************************************************************************/
/*                                                                         */
/*  NoteGenerator.java                                                     */
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

// Generates buttons containing footnotes.

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

public class NoteGenerator implements EditorComponentGenerator {

    // Text to which this is added.
    private StyledTextPane text;

    // User of this generator.
    private EditChainElement parent;

    public NoteGenerator(StyledTextPane text, EditChainElement parent) {
	this.text = text;
	this.parent = parent;
    }

    // Make new hieroglyphic button.
    public Component makeComponent(ChangeListener listener) {
	return new NoteButton(new Vector(), listener);
    }
    public Component makeComponent(Object object, ChangeListener listener) {
	Vector parts = (Vector) object;
	NoteButton b = new NoteButton(parts, listener);
	return b;
    }
    public Object extract(Component comp) {
	NoteButton button = (NoteButton) comp;
	Vector parts = button.parts();
	return parts;
    }

    // Button showing hieroglyphic.
    private class NoteButton extends JButton implements ActionListener {
	// The content.
	private Vector parts;

	// External agent that listens to changes.
	private ChangeListener listener;

        public NoteButton(Vector parts, ChangeListener listener) {
	    this.parts = parts;
	    this.listener = listener;
	    setBackground(Color.WHITE);
	    setForeground(Color.BLUE);
	    setText("note");
	    setBorder(new LineBorder(Color.WHITE, 0));
	    setAlignmentY(1.00f);
            setActionCommand("edit");
            addActionListener(this);
        }

	public Vector parts() {
	    return parts;
	}

	public Dimension getMinimumSize() {
	    return getPreferredSize();
	}
	public Dimension getMaximumSize() {
	    return getPreferredSize();
	}

	// Upon click, edit contents.
        public void actionPerformed(ActionEvent e) {
	    createNoteEditor();
        }

        // Embedded editor of footnote.
        private void createNoteEditor() {
	    parent.allowEditing(false);
            new NoteEditor(parts) {
                protected void receive(Vector out) {
		    listener.stateChanged(new ChangeEvent(this));
                    parent.allowEditing(true);
		    parts = out;
                    text.requestFocus();
                }
                protected void cancel() {
                    parent.allowEditing(true);
                    text.requestFocus();
                }
		protected void makeChanged() {
		}
            };
        }

    }

}
