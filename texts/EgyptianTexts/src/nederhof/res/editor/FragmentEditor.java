/***************************************************************************/
/*                                                                         */
/*  FragmentEditor.java                                                    */
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

// Window for editing single RES fragment.

package nederhof.res.editor;

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

import nederhof.res.*;
import nederhof.util.*;
import nederhof.util.xml.*;

public abstract class FragmentEditor extends JFrame 
	implements KeyListener, ActionListener {

    // Context for parsing. 
    private IParsingContext context = new ParsingContext(true);

    // Should XML be escaped?
    private boolean escapeXML;

    // Main panel, containing preview and tree.
    private FragmentPanel fragmentPanel;

    // The listener to closing. To be passed to auxiliary frames.
    private CloseListener closeListener = new CloseListener();


    // Auxiliary window: help window.
    private JFrame helpWindow = null;

    // Create editor window, with file at beginning.
    public FragmentEditor(String res, boolean warningsIgnored,
	    boolean escapeXML,
	    int previewHieroFontSize, int treeHieroFontSize) {
        setTitle("RES Fragment Editor");
        setJMenuBar(getMenu(previewHieroFontSize, treeHieroFontSize));
        setSize(Settings.fragmentDisplayWidthInit, Settings.fragmentDisplayHeightInit);

	context.setIgnoreWarnings(warningsIgnored);
	this.escapeXML = escapeXML;
	if (escapeXML) 
	    res = XmlAux.unescape(res);
	ResFragment parsed = parseRes(res);
	if (parsed == null) {
	    dispose();
	    return;
	}

	fragmentPanel = new FragmentPanel(parsed, this, closeListener,
		previewHieroFontSize, treeHieroFontSize) {
	    protected GlyphChooser getChooserWindow() {
		return getGlyphChooserWindow();
	    }
	    protected void enableUndo(boolean b) {
		undoItem.setEnabled(b);
	    }
	    protected void enableRedo(boolean b) {
		redoItem.setEnabled(b);
	    }
	};

        getContentPane().add(fragmentPanel);

        addKeyListener(this);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(closeListener);
	addComponentListener(new MoveListener());
	addFocusListener(new FocusGainListener());

        setVisible(true);
	Point thisLocation = getLocation();
	Point legendLocation =
	    new Point(thisLocation.x + getWidth(),  thisLocation.y);
	fragmentPanel.initialize(legendLocation);
    }
    // As above, but warnings not igored.
    public FragmentEditor(String res, boolean escapeXML,
	    int previewHieroFontSize, int treeHieroFontSize) {
	this(res, false, escapeXML, previewHieroFontSize, treeHieroFontSize);
    }

    private ResFragment parseRes(String text) {
	nederhof.res.parser p = new nederhof.res.parser(text, context);
        Object result = null;
        try {
            result = p.parse().value;
        } catch (Exception e) {
            result = null;
        }
	if (context.nErrors() > 0) {
	    JOptionPane.showMessageDialog(this, context.error(0),
		    "Parsing error", JOptionPane.ERROR_MESSAGE);
	    int pos = context.errorPos(0);
	    if (pos >= 0)
		error(pos);
	    else
		cancel();
	    return null;
	}
        if (result == null || !(result instanceof ResFragment)) {
	    cancel();
            return null;
	} else
            return (ResFragment) result;
    }

    //////////////////////////////////////////////////////////////////////////////
    // Buttons at top.

    // Items in menu that may be disabled/enabled.
    private final JButton saveItem =
	new ClickButton(this, "clo<u>S</u>e", "save", KeyEvent.VK_S);
    private final JMenuItem undoItem = new MItem(this,
            "<u>U</u>ndo", "undo", KeyEvent.VK_U);
    private final JMenuItem redoItem = new MItem(this,
            "<u>R</u>edo", "redo", KeyEvent.VK_R);
    private final JMenuItem clearItem = new MItem(this,
            "<u>C</u>lear", "clear", KeyEvent.VK_C);
    private final JMenuItem normalizeItem = new MItem(this,
            "<u>N</u>ormalize", "normalize", KeyEvent.VK_N);
    private final JMenuItem flattenItem = new MItem(this,
            "f<u>L</u>atten", "flatten", KeyEvent.VK_L);
    private final JMenuItem swapItem = new MItem(this,
            "s<u>W</u>ap", "swap", KeyEvent.VK_W);
    private final JButton helpItem =
	new ClickButton(this, "<u>H</u>elp", "help", KeyEvent.VK_H);

    // Menus for selecting font sizes.
    private JMenu previewMenu;
    private JMenu treeMenu;

    // Limits to font sizes.
    private final int MIN_FONT_SIZE = 40;
    private final int MAX_FONT_SIZE = 70;

    // Menu.
    private JMenuBar getMenu(int previewHieroFontSize, int treeHieroFontSize) {
        final int STRUT_SIZE = 10;
        JMenuBar box = new JMenuBar();
        box.setLayout(new BoxLayout(box, BoxLayout.X_AXIS));
        box.setBackground(Color.LIGHT_GRAY);
        box.add(Box.createHorizontalStrut(STRUT_SIZE));

	// save
	box.add(saveItem);
	saveItem.setFocusable(false);
	box.add(Box.createHorizontalStrut(STRUT_SIZE));

	// edit
	JMenu editMenu = new JMenu("<html><u>E</u>dit</html>");
	editMenu.setMnemonic(KeyEvent.VK_E);
	editMenu.setBackground(Color.LIGHT_GRAY);
	box.add(editMenu);
        // undo
        // redo
        // clear
        // normalize
        // flatten
        // swap
	editMenu.add(undoItem);
	editMenu.add(redoItem);
	editMenu.add(clearItem);
	editMenu.add(normalizeItem);
	editMenu.add(flattenItem);
	editMenu.add(swapItem);
	box.add(Box.createHorizontalStrut(STRUT_SIZE));

	// Preview font size.
	previewMenu = new JMenu("<html><u>P</u>review</html>");
	previewMenu.setMnemonic(KeyEvent.VK_P);
	previewMenu.setBackground(Color.LIGHT_GRAY);
	box.add(previewMenu);
	for (int i = MIN_FONT_SIZE; i <= MAX_FONT_SIZE; i += 2) 
	    previewMenu.add(new MItem(this, "" + i + " Pt", "P" + i));
	selectFromMenu(previewMenu, "P" + previewHieroFontSize);
	box.add(Box.createHorizontalStrut(STRUT_SIZE));

	// Tree font size.
	treeMenu = new JMenu("<html><u>T</u>ree</html>");
	treeMenu.setMnemonic(KeyEvent.VK_T);
	treeMenu.setBackground(Color.LIGHT_GRAY);
	box.add(treeMenu);
	for (int i = MIN_FONT_SIZE; i <= MAX_FONT_SIZE; i += 2) 
	    treeMenu.add(new MItem(this, "" + i + " pt", "T" + i));
	selectFromMenu(treeMenu, "T" + treeHieroFontSize);
	box.add(Box.createHorizontalStrut(STRUT_SIZE));

	box.add(helpItem);
	helpItem.setFocusable(false);

        box.add(Box.createHorizontalGlue());
        return box;
    }

    // Select one item from menu.
    private static void selectFromMenu(JMenu menu, String action) {
	for (int i = 0; i < menu.getItemCount(); i++) {
	    JMenuItem item = menu.getItem(i);
	    if (item.getActionCommand().equals(action))
		item.setBackground(Color.GRAY);
	    else
		item.setBackground(Color.LIGHT_GRAY);
	}
    }

    // Customized menu item.
    private static class MItem extends JMenuItem {

        // Label of item.
        private String label;

        // Constructor.
        public MItem(ActionListener lis, String label, String action, int mnem) {
            this.label = label;
            setEnabled(true);
            setBackground(Color.LIGHT_GRAY);
            setActionCommand(action);
            setMnemonic(mnem);
            setAccelerator(GuiAux.shortcut(mnem));
            addActionListener(lis);
        }

	// Constructor without mnemonic.
	public MItem(ActionListener lis, String label, String action) {
            this.label = label;
            setEnabled(true);
            setBackground(Color.LIGHT_GRAY);
            setActionCommand(action);
            addActionListener(lis);
	}

        // Make gray if not enabled.
        public void setEnabled(boolean b) {
            super.setEnabled(b);
            if (b)
                setText("<html>" + label + "</html>");
            else
                setText("<html><font color=\"gray\">" + label + "</font></html>");
        }
    }

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("save")) {
	    String escaped = fragmentPanel.contents();
	    if (escapeXML)
		escaped = XmlAux.escape(fragmentPanel.contents());
	    receive(escaped);
	    dispose();
	} else if (e.getActionCommand().equals("quit")) {
	    if (!fragmentPanel.modified() ||
		    editlossConfirmed()) {
		cancel();
		dispose();
	    }
	} else if (e.getActionCommand().equals("undo")) {
	    fragmentPanel.undo();
	} else if (e.getActionCommand().equals("redo")) {
	    fragmentPanel.redo();
	} else if (e.getActionCommand().equals("clear")) {
	    fragmentPanel.clear();
	} else if (e.getActionCommand().equals("normalize")) {
	    fragmentPanel.normalize();
	} else if (e.getActionCommand().equals("flatten")) {
	    fragmentPanel.flatten();
	} else if (e.getActionCommand().equals("swap")) {
	    fragmentPanel.swap();
	} else if (e.getActionCommand().matches("P[0-9]+")) {
	    try {
		int size = 
		    Integer.parseInt(e.getActionCommand().substring(1));
		fragmentPanel.setPreviewFontSize(size);
		selectFromMenu(previewMenu, e.getActionCommand());
	    } catch (NumberFormatException ex) {
		// ignore
	    }
	} else if (e.getActionCommand().matches("T[0-9]+")) {
	    try {
		int size = 
		    Integer.parseInt(e.getActionCommand().substring(1));
		fragmentPanel.setTreeFontSize(size);
		selectFromMenu(treeMenu, e.getActionCommand());
	    } catch (NumberFormatException ex) {
		// ignore
	    }
	} else if (e.getActionCommand().matches("help")) {
	    if (helpWindow == null) {
		URL url = FileAux.fromBase("data/help/res/fragment_editor.html");
		helpWindow = new HTMLWindow("OCR manual", url);
	    }
	    helpWindow.setVisible(true);
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
    // Let legend disappear with iconification of main window.
    private class CloseListener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
	    cancel();
            dispose();
        }
        public void windowIconified(WindowEvent e) {
	    setState(Frame.ICONIFIED);
            fragmentPanel.setVisible(false);
        }
        public void windowDeiconified(WindowEvent e) {
	    setState(Frame.NORMAL);
            fragmentPanel.setVisible(true);
        }
    }

    // Listen if window moved. Move legend with it.
    private class MoveListener extends ComponentAdapter {
	int NONE = Integer.MAX_VALUE;
	int xOld = NONE;
	int yOld = NONE;
	public void componentMoved(ComponentEvent e) {
	    if (e.getComponent() != FragmentEditor.this)
		return;
	    int xNew = getLocation().x;
	    int yNew = getLocation().y;
	    int xMin = xNew + getWidth();
	    int yMin = yNew + getHeight();
	    if (xOld != NONE && yOld != NONE) 
		fragmentPanel.moveLegend(xNew-xOld, yNew-yOld, xMin, yMin);
	    xOld = xNew;
	    yOld = yNew;
	}
    }

    // Listen if window gains focus.
    private class FocusGainListener extends FocusAdapter {
	public void focusGained(FocusEvent e) {
	    fragmentPanel.showLegend();
	}
    }

    // Dispose is also to dispose of auxiliary windows.
    public void dispose() {
	if (fragmentPanel != null)
	    fragmentPanel.dispose();
	if (glyphChooser != null)
	    glyphChooser.dispose();
	if (helpWindow != null)
	    helpWindow.dispose();
	super.dispose();
    }

    ///////////////////////////////////////////////////////////////
    // Processing keyboard input.

    // The keys are passed on to the fragment panel.
    public void keyTyped(KeyEvent e) {
	fragmentPanel.keyTyped(e);
    }

    // The arrows are passed on to the fragment panel.
    public void keyPressed(KeyEvent e) {
	fragmentPanel.keyPressed(e);
    }

    // Ignored.
    public void keyReleased(KeyEvent e) {
        // ignored
    }

    ///////////////////////////////////////////////////////////////
    // Interface to user. Methods to be overridden.

    // Cached chooser of glyphs. To be created only once.
    private GlyphChooser glyphChooser;

    // Get chooser window. Override e.g. if to be created only once for
    // application.
    protected GlyphChooser getGlyphChooserWindow() {
	if (glyphChooser == null) 
	    glyphChooser = new GlyphChooser() {
		protected void receive(String name) {
		    fragmentPanel.receiveGlyph(name);
		}
	    };
	return glyphChooser;
    }

    // Receive edited RES.
    protected abstract void receive(String out);

    // Cancel editing.
    protected abstract void cancel();

    // Premature finish due to syntax error.
    protected abstract void error(int pos);

    //////////////////////////////////////////////////////////////////

    // For testing.
    public static void main(String[] args) {
	String res = "[hrl]![blue]A1![red]*B2*C3-cartouche(C3[mirror]-A1)![blue]-![red]insert(![green]S1,T2![black])-inb()";
	new FragmentEditor(res, true,
		Settings.previewHieroFontSize, Settings.treeHieroFontSize) {
	    protected void receive(String out) {
		System.out.println("out:\n" + out);
	    }
	    protected void cancel() {
		System.out.println("editing cancelled");
	    }
	    protected void error(int pos) {
		System.out.println("error at " + pos);
	    }
	};
    }

}
