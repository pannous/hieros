package nederhof.util.gui;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import nederhof.util.*;
import nederhof.util.gui.*;
import nederhof.util.xml.*;

public class LogWindow extends JFrame implements ActionListener {

    // Default initial dimensions.
    private static final int width = 500;
    private static final int height = 600;

    // Text.
    private JTextPane logText = new JTextPane();
    // Scroll.
    private JScrollPane scroll = new JScrollPane(logText);

    // To be interrupted?
    private boolean halt = false;

    // Constructor.
    public LogWindow(String title) {
        setTitle(title);
	setJMenuBar(new Menu(this));
        setSize(width, height);
        Container content = getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        content.add(scroll);
	DefaultCaret caret = (DefaultCaret) logText.getCaret();
	caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	setVisible(true);
    }

    //////////////////////////////////////////////////////////////////////////////
    // Menu at top of window.

    private final JMenuItem haltItem = new EnabledMenuItem(this,
	    "<u>S</u>top", "halt", KeyEvent.VK_S);

    // Menu containing quit and edit buttons.
    private class Menu extends JMenuBar {

	// Distance between buttons.
	private static final int STRUT_SIZE = 10;

	public Menu(ActionListener lis) {
	    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	    setBackground(Color.LIGHT_GRAY);
	    add(Box.createHorizontalStrut(STRUT_SIZE));
	    add(haltItem);
	    add(Box.createHorizontalGlue());
	}
    }

    ////////////////////////////////////
    // Interaction.

    // Add text.
    public void addText(String s) {
	Document doc = logText.getDocument();
	try {
	    doc.insertString(doc.getLength(), s, null);
	} catch (BadLocationException e) {
	    // ignore
	}
	repaint();
    }

    // Process halt.
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("halt")) 
	    halt = true;
    }

    // Halted?
    public boolean halt() {
	return halt;
    }

}
