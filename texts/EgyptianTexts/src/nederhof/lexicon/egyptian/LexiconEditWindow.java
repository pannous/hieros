package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.res.*;
import nederhof.util.*;
import nederhof.util.gui.*;
import nederhof.interlinear.frame.*;

// Window with lexicon editor.
public class LexiconEditWindow extends JFrame implements ActionListener {

    private LexiconEditor editor;

    // There may be auxiliary help window.
    protected JFrame helpWindow = null;

    public LexiconEditWindow() {
	setTitle("Lexicon Editor");
	setSize(Settings.EditorWidth, Settings.EditorHeight);
	setJMenuBar(new Menu());
	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	addWindowListener(new CloseListener());
	try {
	    editor = new LexiconEditor();
	    getContentPane().add(editor);
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(null,
		    "Cannot open lexicon: " + e.getMessage(), "Error",
		    JOptionPane.ERROR_MESSAGE);
	}
	setVisible(true);
    }

    /////////////////////////////////////////////
    // Menu.

    // Distance between buttons.
    protected static final int STRUT_SIZE = 10;

    // Items in menu that may be disabled/enabled.
    protected final JMenu fileMenu = new EnabledMenu(
            "<u>F</u>ile", KeyEvent.VK_F);
    protected final JMenuItem fileCloseItem = new EnabledMenuItem(this,
            "clo<u>S</u>e", "close", KeyEvent.VK_S);
    protected final JMenu editMenu = new EnabledMenu(
            "<u>E</u>dit", KeyEvent.VK_E);
    protected final JMenuItem lemmaItem = new EnabledMenuItem(this,
            "new <u>L</u>emma", "lemma", KeyEvent.VK_L);

    // Menu containing quit and edit buttons.
    protected class Menu extends JMenuBar {
        public Menu() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setBackground(Color.LIGHT_GRAY);

            // file
            add(Box.createHorizontalStrut(STRUT_SIZE));
            fileMenu.add(fileCloseItem);
            add(fileMenu);

            // edit
            add(Box.createHorizontalStrut(STRUT_SIZE));
            editMenu.add(lemmaItem);
            add(editMenu);

            // help
            add(Box.createHorizontalStrut(STRUT_SIZE));
            add(new ClickButton(LexiconEditWindow.this, "<u>H</u>elp", "help", KeyEvent.VK_H));
        }
    }

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("lemma")) {
	    editor.createLemma();
	} else if (e.getActionCommand().equals("close")) {
	    close();
	} else if (e.getActionCommand().equals("help")) {
            if (helpWindow == null) {
                URL url = FileAux.fromBase("data/help/lexicon/editor.html");
                helpWindow = new HTMLWindow("Text viewer manual", url);
            }
            helpWindow.setVisible(true);
	}
    }

    //////////////////////////////////////////////////////////
    // Closing.

    // Listen if window to be closed or iconified.
    private class CloseListener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
	    close();
        }
        public void windowIconified(WindowEvent e) {
            setState(Frame.ICONIFIED);
        }
        public void windowDeiconified(WindowEvent e) {
            setState(Frame.NORMAL);
        }
    }

    private void close() {
	if (editor != null)
	    editor.dispose();
	if (helpWindow != null)
	    helpWindow.dispose();
	dispose();
    }

    //////////////////////////////////////////////////////////
    // Main.

    public static void main(String[] args) {
	new LexiconEditWindow();
    }

}
