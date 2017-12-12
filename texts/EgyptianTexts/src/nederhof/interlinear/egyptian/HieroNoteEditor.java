/***************************************************************************/
/*                                                                         */
/*  HieroNoteEditor.java                                                   */
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

// Editor of footnotes in hieroglyphic.

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
import nederhof.res.*;
import nederhof.res.editor.*;
import nederhof.res.format.*;
import nederhof.util.*;

public abstract class HieroNoteEditor extends JFrame 
	implements ActionListener, DocumentListener,
	EditChainElement {

    // Hieroglyphic.
    private ResFragment res;

    // Panel containing hieroglyphic.
    private GlyphSelectPanel hieroPanel;

    // Footnotes.
    private Vector notes;

    // Panel containing notes.
    private JPanel notePanels = new JPanel();

    // Has it changed?
    private boolean changed = false;

    // The listener to closing.
    private CloseListener closeListener = new CloseListener();

    // Panel elements.
    protected Vector panelElements = new Vector();

    // Text elements.
    protected Vector textElements = new Vector();

    public HieroNoteEditor(FormatFragment res, HieroRenderContext context,
	    Vector notes) {
	this.res = res;
	this.notes = notes;
        setTitle("Footnote editor");
        setJMenuBar(getMenu());
        setSize(Settings.hieroNoteEditorWidth, Settings.hieroNoteEditorHeight);
	Container content = getContentPane();
	hieroPanel = new GlyphSelectPanel(new ResFragment(), context) {
	    protected void notifyFocus(int pos) {
		changeFocus(pos);
	    }
	};
	if (res.effectIsH()) {
	    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
	    content.add(new JScrollPane(hieroPanel,
		    JScrollPane.VERTICAL_SCROLLBAR_NEVER,
		    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS) {
		public Dimension getMaximumSize() {
		    return new Dimension(super.getMaximumSize().width,
			getPreferredSize().height);
		}
		public Dimension getMinimumSize() {
		    return new Dimension(super.getMinimumSize().width,
			getPreferredSize().height);
		}
	    });
	} else {
	    content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
	    content.add(new JScrollPane(hieroPanel,
		    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
		public Dimension getMaximumSize() {
		    return new Dimension(getPreferredSize().width,
			super.getMaximumSize().height);
		}
		public Dimension getMinimumSize() {
		    return new Dimension(getPreferredSize().width,
			super.getMinimumSize().height);
		}
	    });
	}

	notePanels.setLayout(new BoxLayout(notePanels, BoxLayout.Y_AXIS));
	notePanels.setBackground(Color.WHITE);
	content.add(new ScrollConservative(notePanels));
	listNotes(-1);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(closeListener);
        setVisible(true);
    }

    // List notes, with highlight for focus.
    // If there is no focus, then negative.
    private void listNotes(int focus) {
	if (changed)
	    retrieveNotes();
	notePanels.removeAll();
	textElements.clear();
	notes = sortNotes(notes);
	hieroPanel.setHiero(notedHiero());
	if (focus >= 0)
	    notePanels.add(new NotePanel(new Vector(), true, focus, 0));
	for (int i = 0; i < notes.size(); i++) {
	    NotePart notePart = (NotePart) notes.get(i);
	    int symb = notePart.symbol();
	    Vector text = notePart.text();
	    notePanels.add(new NotePanel(text, symb == focus, symb, i+1));
	}
	notePanels.add(Box.createVerticalGlue());
	notePanels.revalidate();
    }

    // Styled text editor within pane.
    private class NotePanel extends JPanel {
	// The pane.
	private StyledTextPane styledPane;
	// The symbol.
	private int symbol;

	// Constructor.
	public NotePanel(Vector text, boolean highlight, int symbol, int mark) {
	    this.symbol = symbol;
	    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	    setBackground(Color.WHITE);
	    String titleString = mark >= 0 ? "" + mark : "new";
	    Color borderCol = highlight ? Color.BLUE : Color.GRAY;
	    TitledBorder title =
		BorderFactory.createTitledBorder(
			new LineBorder(borderCol, 2), titleString);
	    setBorder(
		    BorderFactory.createCompoundBorder(title,
			BorderFactory.createEmptyBorder(0,5,5,5)));

	    styledPane = 
		new StyledTextPane(Settings.inputTextFontName,
			Settings.inputTextFontSize) {
		    public void stateChanged(ChangeEvent e) {
			changed = true;
		    }
		};
	    NoteEditPopup popup = new NoteEditPopup();
	    popup.setUsers(styledPane, HieroNoteEditor.this);
	    styledPane.addMouseListener(popup);

	    Vector parts = ParsingHelper.toEdit(text);
	    Vector pars = new Vector();
	    pars.add(parts);
	    styledPane.setParagraphs(pars);
	    styledPane.getDocument().addDocumentListener(HieroNoteEditor.this);
	    add(styledPane);
	    panelElements.add(this);
	    textElements.add(styledPane);
	}

	// Retrieve content.
	// These are paragraphs. The parts have to be merged.
	public Vector getText() {
	    Vector pars = styledPane.extractParagraphs();
	    Vector parts = new Vector();
	    for (int i = 0; i < pars.size(); i++) {
		Vector par = (Vector) pars.get(i);
		parts.addAll(par);
	    }
	    return ParsingHelper.fromEdit(parts);
	}

	// Retrieve symbol.
	public int getSymbol() {
	    return symbol;
	}
	// Change symbol.
	public void setSymbol(int symbol) {
	    this.symbol = symbol;
	    changed = true;
	}

	// Is caret in text?
	public boolean hasCaret() {
	    return styledPane.isFocusOwner();
	}
    }

    // Get panel carrying caret. Return null if none.
    private NotePanel focusCarrier() {
	Component[] children = notePanels.getComponents();
	for (int i = 0; i < children.length; i++) {
	    if (children[i] instanceof NotePanel) {
		NotePanel panel = (NotePanel) children[i];
		if (panel.hasCaret()) {
		    return panel;
		}
	    }
	}
	return null;
    }

    // Move note carrying caret backward.
    private void moveBackward() {
	NotePanel panel = focusCarrier();
	if (panel != null &&
		0 <= panel.getSymbol()-1 && panel.getSymbol()-1 < res.nGlyphs()) {
	    panel.setSymbol(panel.getSymbol()-1);
	    listNotes(panel.getSymbol());
	}
    }

    // Move note carrying caret forward.
    private void moveForward() {
	NotePanel panel = focusCarrier();
	if (panel != null &&
		0 <= panel.getSymbol()+1 && panel.getSymbol()+1 < res.nGlyphs()) {
	    panel.setSymbol(panel.getSymbol()+1);
	    listNotes(panel.getSymbol());
	}
    }

    // Get notes from panels.
    private void retrieveNotes() {
	notes.clear();
	Component[] children = notePanels.getComponents();
	for (int i = 0; i < children.length; i++) {
	    if (children[i] instanceof NotePanel) {
		NotePanel panel = (NotePanel) children[i];
		Vector text = panel.getText();
		if (!text.isEmpty()) {
		    int sym = panel.getSymbol();
		    notes.add(new NotePart(text, sym));
		}
	    }
	}
    }

    /////////////////////////////////////////////
    // Menu.

    // Items in menu that may be disabled/enabled.
    private final JMenu fileMenu = new EnabledMenu(
            "<u>F</u>ile", KeyEvent.VK_F);
    private final JMenuItem leftItem = new EnabledMenuItem(this,
            "left", "left", KeyEvent.VK_COMMA);
    private final JMenuItem rightItem = new EnabledMenuItem(this,
            "right", "right", KeyEvent.VK_PERIOD);
    private final JMenuItem backwardItem = new EnabledMenuItem(this,
            "b<u>A</u>ckward", "backward", KeyEvent.VK_A);
    private final JMenuItem forwardItem = new EnabledMenuItem(this,
            "f<u>O</u>rward", "forward", KeyEvent.VK_O);
    private final JMenuItem saveItem = new EnabledMenuItem(this,
            "clo<u>S</u>e", "save", KeyEvent.VK_S);

    // Menu.
    private JMenuBar getMenu() {
        final int STRUT_SIZE = 10;
        JMenuBar box = new JMenuBar();
        box.setLayout(new BoxLayout(box, BoxLayout.X_AXIS));
        box.setBackground(Color.LIGHT_GRAY);
        box.add(Box.createHorizontalStrut(STRUT_SIZE));
        box.add(fileMenu);
        fileMenu.add(leftItem);
        fileMenu.add(rightItem);
        fileMenu.add(backwardItem);
        fileMenu.add(forwardItem);
        fileMenu.add(saveItem);
        box.add(Box.createHorizontalGlue());
        return box;
    }

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("left")) {
	    hieroPanel.goLeft();
        } else if (e.getActionCommand().equals("right")) {
	    hieroPanel.goRight();
        } else if (e.getActionCommand().equals("backward")) {
	    moveBackward();
        } else if (e.getActionCommand().equals("forward")) {
	    moveForward();
        } else if (e.getActionCommand().equals("save")) {
            if (changed) {
		retrieveNotes();
                receive(notes);
	    } else
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

    /////////////////////////////////////////////
    // Values.

    // User has clicked on position.
    private void changeFocus(int pos) {
	listNotes(pos);
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

    // Connection with caller.
    protected abstract void receive(Vector changedNotes);
    protected abstract void cancel();

    /////////////////////////////////////////////
    // Appearance.

    // Make border title gray if not editing.
    public void allowEditing(boolean allow) {
	fileMenu.setEnabled(allow);
        leftItem.setEnabled(allow);
        rightItem.setEnabled(allow);
        backwardItem.setEnabled(allow);
        forwardItem.setEnabled(allow);
	saveItem.setEnabled(allow);
	hieroPanel.setEnabled(allow);
        for (int i = 0; i < panelElements.size(); i++) {
            JComponent comp = (JComponent) panelElements.get(i);
            comp.setBackground(backColor(allow));
        }
        for (int i = 0; i < textElements.size(); i++) {
            JComponent comp = (JComponent) textElements.get(i);
            comp.setEnabled(allow);
        }
    }

    // Backgroup color.
    private Color backColor(boolean allow) {
        return Color.WHITE;
    }

    //////////////////////////////////////////
    // Auxiliary.

    // Extend hieroglyphic with markers where there are footnotes.
    private ResFragment notedHiero() {
	ResFragment noted = (ResFragment) res.clone();
	TreeSet glyphs = new TreeSet();
	for (int i = 0; i < notes.size(); i++) {
	    NotePart note = (NotePart) notes.get(i);
	    int sym = note.symbol();
	    noted.addNote(sym, "" + (i+1), Color16.BLUE);
	}
	return noted;
    }

    // Sort notes based on glyph name.
    private Vector sortNotes(Vector notes) {
	Vector notesWithIndex = new Vector();
	for (int i = 0; i < notes.size(); i++)
	    notesWithIndex.add(
		    new Object[] {notes.get(i), new Integer(i)});
	Collections.sort(notesWithIndex, new NoteComparator());
	Vector sorted = new Vector();
	for (int i = 0; i < notesWithIndex.size(); i++) {
	    Object[] pair = (Object[]) notesWithIndex.get(i);
	    sorted.add(pair[0]);
	}
	return sorted;
    }

    // Compares notes for ordering.
    private class NoteComparator implements Comparator {
	public int compare(Object o1, Object o2) {
	    if (o1 == null && o2 == null)
		return 0;
	    else if (o1 == null)
		return 1;
	    else if (o2 == null)
		return -1;
	    else {
		Object[] pair1 = (Object[]) o1;
		Object[] pair2 = (Object[]) o2;
		NotePart n1 = (NotePart) pair1[0];
		NotePart n2 = (NotePart) pair2[0];
		Integer i1 = (Integer) pair1[1];
		Integer i2 = (Integer) pair2[1];
		if (n1.symbol() < n2.symbol())
		    return -1;
		else if (n1.symbol() > n2.symbol())
		    return 1;
		else 
		    return i1.compareTo(i2);
	    }
	}

	// Never used.
	public boolean equals(Object o) {
	    return false;
	}
    }

}
