/***************************************************************************/
/*                                                                         */
/*  TextValidationWindow.java                                              */
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

// Correcting errors in input that is to be validated.

package nederhof.util;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

public class TextValidationWindow extends JFrame implements ActionListener {

    // Default initial dimensions.
    private static final int width = 700;
    private static final int height = 900;

    // Pane containing error.
    private JEditorPane errorPane;
    // Pane containing content.
    private JEditorPane textPane;

    // Validation of text.
    private TextValidation validation;

    // Constructor only to be called if there are errors.
    private TextValidationWindow(TextValidation validation) {
	setTitle("Error");
	setJMenuBar(getMenu());
	setSize(width, height);
	Container content = getContentPane();
	content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

	errorPane = new JEditorPane();
	errorPane.setEditable(false);
	errorPane.setFocusable(false);
	JComponent errorScroll = new JScrollPane(errorPane);
	errorScroll.setPreferredSize(new Dimension(width, height/8));
	content.add(errorScroll);

	textPane = new JEditorPane();
	textPane.setEditable(true);
	textPane.setSelectionColor(Color.yellow);
	textPane.setText(validation.getText());
	JComponent textScroll = new JScrollPane(textPane);
	textScroll.setPreferredSize(new Dimension(width, 7*height/8));
	content.add(textScroll);

	this.validation = validation;
	String error = validation.getError();
	int pos = validation.getErrorPos();
	errorPane.setText(error);
	textPane.setCaretPosition(pos+2);
	textPane.moveCaretPosition(pos);

	addWindowListener(new CloseListener());
	setVisible(true);
	textPane.grabFocus();
    }

    // Menu.
    private JMenuBar getMenu() {
	final int STRUT_SIZE = 10;
	JMenuBar box = new JMenuBar();
	box.setLayout(new BoxLayout(box, BoxLayout.X_AXIS));
	box.setBackground(Color.LIGHT_GRAY);
	box.add(Box.createHorizontalStrut(STRUT_SIZE));

	// cancel
	box.add(new ClickButton(this, "<u>C</u>ancel", 
		    "cancel", KeyEvent.VK_C));
	box.add(Box.createHorizontalStrut(STRUT_SIZE));
	// resume
	box.add(new ClickButton(this, "<u>R</u>esume", 
		    "resume", KeyEvent.VK_R));
	return box;
    }

    // Listen if window to be closed. 
    private class CloseListener extends WindowAdapter {
	public void windowClosing(WindowEvent e) {
	    closeValidation();
	}
    }

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("cancel"))
	    closeValidation();
	else if (e.getActionCommand().equals("resume"))
	    resumeValidation();
    }

    // Abort process prematurely.
    private void closeValidation() {
	validation.abort();
	dispose();
    }

    // With new text, try again.
    private void resumeValidation() {
	validation.setText(textPane.getText());
	if (validation.isValid()) {
	    validation.finish();
	    dispose();
	} else {
	    String error = validation.getError();
	    int pos = validation.getErrorPos();
	    errorPane.setText(error);
	    textPane.setCaretPosition(pos+2);
	    textPane.moveCaretPosition(pos);
	    textPane.grabFocus();
	}
    }

    // TextValidate is a text with methods to validate its correctness.
    // If it is already valid, the process can be finished.
    // Else, an edit window needs to be opened to correct errors.
    // After editing, the process is repeated as often as needed.
    public static void iterateCorrections(TextValidation validation) {
	if (validation.isValid())
	    validation.finish();
	else 
	    new TextValidationWindow(validation);
    }

}
