// Window for editing single transliteration string.

package nederhof.interlinear.egyptian.ortho;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import nederhof.interlinear.frame.*;
import nederhof.util.*;

public abstract class AlEditorFrame extends JFrame 
	implements ActionListener {

    // The listener to closing. To be passed to auxiliary frames.
    private CloseListener closeListener = new CloseListener();

    // The text editor.
    private AlPlainEditor editor;

    // Original value.
    private String original;

    // Create editor window.
    public AlEditorFrame(String al) {
	setTitle("Transliteration Editor");
	setJMenuBar(getMenu());
	original = al;

	editor = new AlPlainEditor("", al);
	getContentPane().add(editor);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(closeListener);
	int initWidth = 400;
	int initHeight = 200;
	setSize(initWidth, initHeight);
    }

    // Set parent.
    public void setParent(EditChainElement parent) {
	editor.setParent(parent);
	setVisible(true);
    }

    //////////////////////////////////////////////////////////////////////////////
    // Buttons at top.

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

        // save
        box.add(saveItem);
	return box;
    }

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("save")) {
	    String newAl = editor.getString();
            receive(newAl);
	    dispose();
        } 
    }

    // Loss of edits to be confirmed by used.
    private boolean editlossConfirmed() {
        Object[] options = {"proceed", "cancel"};
        int answer = JOptionPane.showOptionDialog(this,
                "Do you want to proceed and discard edits?",
                "warning: impending loss of edits",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[1]);
        return answer == 0;
    }

    // Listen if window to be closed or iconified.
    private class CloseListener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
	    if (editor.getString().equals(original) ||
		    editlossConfirmed()) {
		cancel();
		dispose();
	    }
        }
        public void windowIconified(WindowEvent e) {
            setState(Frame.ICONIFIED);
        }
        public void windowDeiconified(WindowEvent e) {
            setState(Frame.NORMAL);
        }
    }

    ///////////////////////////////////////////////////////////////
    // Interface to user. Methods to be overridden.

    // Receive edited transliteration.
    protected abstract void receive(String al);

    // Cancel editing.
    protected abstract void cancel();
}
