/***************************************************************************/
/*                                                                         */
/*  NoteEditor.java                                                        */
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

package nederhof.interlinear.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import nederhof.interlinear.frame.*;
import nederhof.util.*;

public abstract class NoteEditor extends JFrame
	implements ActionListener, DocumentListener, EditChainElement {

    // Styled text.
    private StyledTextPane styledPane;

    // Has it changed?
    private boolean changed = false;

    // The listener to closing. 
    private CloseListener closeListener = new CloseListener();

    // Panel elements.
    protected Vector panelElements = new Vector();

    // Text elements.
    protected Vector textElements = new Vector();

    public NoteEditor(Vector parts) {
	setTitle("Footnote editor");
	setJMenuBar(getMenu());
	setSize(Settings.footnoteEditorWidth, Settings.footnoteEditorHeight);
	getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));

        styledPane = new StyledTextPane(Settings.inputTextFontName,
                Settings.inputTextFontSize) {
            public void stateChanged(ChangeEvent e) {
                changed = true;
            }
        };
        styledPane.getDocument().addDocumentListener(NoteEditor.this);
	NoteEditPopup popup = new NoteEditPopup();
	popup.setUsers(styledPane, NoteEditor.this); 
	styledPane.addMouseListener(popup);

        getContentPane().add(styledPane);
        putValue(parts);

        panelElements.add(styledPane);
	textElements.add(styledPane);
	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	addWindowListener(closeListener);
	setVisible(true);
    }

    /////////////////////////////////////////////
    // Menu.

    // Items in menu that may be disabled/enabled.
    private final JButton saveItem = 
	new ClickButton(this, "clo<u>S</u>e", "save", KeyEvent.VK_S);

    // Menu.
    private JMenuBar getMenu() {
        final int STRUT_SIZE = 10;
        JMenuBar box = new JMenuBar();
        box.setLayout(new BoxLayout(box, BoxLayout.X_AXIS));
        box.setBackground(Color.LIGHT_GRAY);
        box.add(Box.createHorizontalStrut(STRUT_SIZE));
        box.add(saveItem);
        box.add(Box.createHorizontalGlue());
        return box;
    }

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("save")) {
	    if (changed)
		receive(getValue());
	    else
		cancel();
            dispose();
	}
    }

    // Listen if window to be closed or iconified.
    // Let legend disappear with iconification of main window.
    private class CloseListener extends WindowAdapter {
	public void windowClosing(WindowEvent e) {
	    if (userConfirmsLoss("Do you want to proceed and discard any edits?")) {
		cancel();
		dispose();
	    }
	}
    }

    // Ask user whether loss of data is intended.
    private boolean userConfirmsLoss(String message) {
        Object[] options = {"proceed", "cancel"};
        int answer = JOptionPane.showOptionDialog(this, message,
                "warning: impending loss of data",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[1]);
        return answer == 0;
    }

    //////////////////////////////////////////////////////
    // Values.

    // Put string in text. The parts form a single paragraph.
    public void putValue(Vector parts) {
        Vector pars = new Vector();
        pars.add(parts);
        styledPane.setParagraphs(pars);
        changed = false;
    }

    // Retrieve value. These are paragraphs. The parts have to be merged.
    public Vector getValue() {
        Vector pars = styledPane.extractParagraphs();
        Vector parts = new Vector();
        for (int i = 0; i < pars.size(); i++) {
            Vector par = (Vector) pars.get(i);
            parts.addAll(par);
        }
        return parts;
    }

    // Any change is recorded.
    public void changedUpdate(DocumentEvent e) {
        changed = true;
    }
    public void insertUpdate(DocumentEvent e) {
        changed = true;
    }
    public void removeUpdate(DocumentEvent e) {
        changed = true;
    }

    protected abstract void receive(Vector out);

    protected abstract void cancel();

    /////////////////////////////////////////////
    // Appearance.

    // Make border title gray if not editing.
    public void allowEditing(boolean allow) {
	saveItem.setEnabled(allow);
        for (int i = 0; i < panelElements.size(); i++) {
            JComponent comp = (JComponent) panelElements.get(i);
            comp.setBackground(backColor(allow));
        }
        for (int i = 0; i < textElements.size(); i++) {
            JComponent comp = (JComponent) textElements.get(i);
            comp.setEnabled(allow);
        }
        styledPane.setEnabled(allow);
    }

    // Backgroup color.
    private Color backColor(boolean allow) {
        return Color.WHITE;
    }

}
