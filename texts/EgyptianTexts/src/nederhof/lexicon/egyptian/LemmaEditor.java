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

// Editor of lemma in Egyptian lexicon.
public class LemmaEditor extends JFrame implements ActionListener,
		DocumentListener, EditChainElement {

    // Current lexicon and original lemma.
    private EgyptianLexicon lex;
    private DictLemma lemma;

    // Editable fields.
    private JTextField posField = new JTextField(5);
    private JTextField keyhiField = new JTextField(10);
    private JTextField keyalField = new JTextField(10);
    private JTextField keytrField = new JTextField(10);
    private JTextField keyfoField = new JTextField(10);
    private JTextField keycoField = new JTextField(10);

    // Auxiliary panels.
    private JPanel header = new NarrowPanel();
    private JPanel def = new JPanel();

    // There may be auxiliary help window.
    protected JFrame helpWindow = null;

    // Is there unsaved edit?
    private boolean changed = false;

    // Is external edit window open?
    private boolean externEdit = false;

    // Constructor.
    public LemmaEditor() {
	setTitle("Lemma Editor");
	setSize(Settings.LemmaEditWidth, Settings.LemmaEditHeight);
	setJMenuBar(new Menu());
	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	Container cont = getContentPane();
	cont.setLayout(new BoxLayout(cont, BoxLayout.Y_AXIS));
	addWindowListener(new CloseListener());
	cont.add(header());
	cont.add(new SimpleScroller(def(), true, true));
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
    protected final JMenuItem meaningItem = new EnabledMenuItem(this,
            "new <u>M</u>eaning", "meaning", KeyEvent.VK_M);
    protected final JMenuItem useItem = new EnabledMenuItem(this,
            "new <u>U</u>se", "use", KeyEvent.VK_U);
    protected final JMenuItem hiItem = new EnabledMenuItem(this,
            "new <u>H</u>i", "hi", KeyEvent.VK_H);
    protected final JMenuItem alItem = new EnabledMenuItem(this,
            "new <u>A</u>l", "al", KeyEvent.VK_A);
    protected final JMenuItem trItem = new EnabledMenuItem(this,
            "new <u>T</u>r", "tr", KeyEvent.VK_T);
    protected final JMenuItem foItem = new EnabledMenuItem(this,
            "new <u>F</u>o", "fo", KeyEvent.VK_F);
    protected final JMenuItem coItem = new EnabledMenuItem(this,
            "new <u>C</u>o", "co", KeyEvent.VK_C);
    protected final JMenuItem altItem = new EnabledMenuItem(this,
            "new a<u>L</u>t", "alt", KeyEvent.VK_L);
    protected final JMenuItem optItem = new EnabledMenuItem(this,
            "new <u>O</u>pt", "opt", KeyEvent.VK_O);
    protected final JMenuItem deleteItem = new EnabledMenuItem(this,
            "<u>D</u>elete element", "delete", KeyEvent.VK_D);
    protected final JMenuItem removeItem = new EnabledMenuItem(this,
            "<u>R</u>emove lemma", "remove", KeyEvent.VK_R);

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
            editMenu.add(meaningItem);
            editMenu.add(useItem);
            editMenu.add(hiItem);
            editMenu.add(alItem);
            editMenu.add(trItem);
            editMenu.add(foItem);
            editMenu.add(coItem);
            editMenu.add(altItem);
            editMenu.add(optItem);
            editMenu.add(deleteItem);
            editMenu.add(removeItem);
	    add(editMenu);

	    // help
	    add(Box.createHorizontalStrut(STRUT_SIZE));
	    add(new ClickButton(LemmaEditor.this, "hel<u>P</u>", "help", KeyEvent.VK_P));
        }
    }

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("close")) {
	    tryClose();
	} else if (e.getActionCommand().equals("update")) {
	    makeChanged();
	} else if (e.getActionCommand().equals("update and focus")) {
	    makeChangedAndFocus(e.getSource());
	} else if (e.getActionCommand().equals("meaning")) {
	    addMeaning();
	} else if (e.getActionCommand().equals("use")) {
	    addUse();
	} else if (e.getActionCommand().equals("hi")) {
	    addHi();
	} else if (e.getActionCommand().equals("al")) {
	    addAl();
	} else if (e.getActionCommand().equals("tr")) {
	    addTr();
	} else if (e.getActionCommand().equals("fo")) {
	    addFo();
	} else if (e.getActionCommand().equals("co")) {
	    addCo();
	} else if (e.getActionCommand().equals("alt")) {
	    addAlt();
	} else if (e.getActionCommand().equals("opt")) {
	    addOpt();
	} else if (e.getActionCommand().equals("delete")) {
	    deleteElem();
	} else if (e.getActionCommand().equals("remove")) {
	    if (userConfirmsLoss("Proceed and remove lemma?")) {
		setEmpty();
		close();
	    }
	} else if (e.getActionCommand().equals("help")) {
	    if (helpWindow == null) {
		URL url = FileAux.fromBase("data/help/lexicon/lemma_editor.html");
		helpWindow = new HTMLWindow("Lemma editor manual", url);
	    }
	    helpWindow.setVisible(true);
	} else {
	    findFocus(e.getSource());
	}
    }

    /////////////////////////////////////////////
    // Content.

    // Set to lemma of lexicon. Return whether possible.
    public boolean set(EgyptianLexicon lex, DictLemma lemma) {
	if (this.lex != null || this.lemma != null)
	    return false;
	this.lex = lex;
	this.lemma = lemma;
	setHeader(lemma);
	setDef(lemma);
	setVisible(true);
	changed = false;
	return true;
    }

    // Set to lexicon, for creating new lemma.
    public boolean set(EgyptianLexicon lex) {
	return set(lex, null);
    }

    // Set all values to empty.
    private void setEmpty() {
	DictLemma empty = new DictLemma();
	setHeader(empty);
	setDef(empty);
	changed = true;
    }

    // Header of lemma.
    private JPanel header() {
	posField.setMaximumSize(posField.getPreferredSize());
	keyhiField.setMaximumSize(keyhiField.getPreferredSize());
	keyalField.setMaximumSize(keyalField.getPreferredSize());
	keytrField.setMaximumSize(keytrField.getPreferredSize());
	keyfoField.setMaximumSize(keyfoField.getPreferredSize());
	keycoField.setMaximumSize(keycoField.getPreferredSize());
	posField.getDocument().addDocumentListener(this);
	keyhiField.getDocument().addDocumentListener(this);
	keyalField.getDocument().addDocumentListener(this);
	keytrField.getDocument().addDocumentListener(this);
	keyfoField.getDocument().addDocumentListener(this);
	keycoField.getDocument().addDocumentListener(this);
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setOpaque(true);
        header.add(Box.createHorizontalStrut(5));
        header.add(new JLabel("pos:"));
        header.add(posField);
        header.add(Box.createHorizontalStrut(10));
        header.add(new JLabel("hi:"));
        header.add(keyhiField);
        header.add(Box.createHorizontalStrut(10));
        header.add(new JLabel("al:"));
        header.add(keyalField);
        header.add(Box.createHorizontalStrut(10));
        header.add(new JLabel("tr:"));
        header.add(keytrField);
        header.add(Box.createHorizontalStrut(10));
        header.add(new JLabel("fo:"));
        header.add(keyfoField);
        header.add(Box.createHorizontalStrut(10));
        header.add(new JLabel("co:"));
        header.add(keycoField);
        header.add(Box.createHorizontalGlue());
	header.setBackground(Color.GRAY);
	return header;
    }

    // Definition.
    private JPanel def() {
	def.setLayout(new BoxLayout(def, BoxLayout.Y_AXIS));
	return def;
    }

    // Fill header of lemma with values.
    private void setHeader(DictLemma lemma) {
	if (lemma == null) {
	    posField.setText("");
	    keyhiField.setText("");
	    keyalField.setText("");
	    keytrField.setText("");
	    keyfoField.setText("");
	    keycoField.setText("");
	} else {
	    posField.setText(lemma.pos);
	    keyhiField.setText(lemma.keyhi);
	    keyalField.setText(lemma.keyal);
	    keytrField.setText(lemma.keytr);
	    keyfoField.setText(lemma.keyfo);
	    keycoField.setText(lemma.keyco);
	}
    }

    // Fill definition.
    private void setDef(DictLemma lemma) {
	def.removeAll();
	if (lemma != null)
	    for (DictMeaning meaning : lemma.meanings) {
		EditMeaning eMeaning = new EditMeaning(meaning, this, this);
		def.add(eMeaning);
	    }
	def.add(Box.createVerticalGlue());
	validate();
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

    // Change requiring repaint.
    public void makeChanged() {
	changed = true;
	validate();
	def.revalidate();
	repaint();
    }
    // Also focus.
    public void makeChangedAndFocus(Object source) {
	makeChanged();
	findFocus(source);
    }

    // Is editing allowed in current window?
    public void allowEditing(boolean b) {
    }

    public DictLemma getValue() {
	Vector<DictMeaning> meanings = new Vector<DictMeaning>();
	Component[] children = def.getComponents();
	for (int i = 0; i < children.length; i++) {
	    Component child = children[i];
	    if (child instanceof EditMeaning) {
		DictMeaning meaning = ((EditMeaning) child).getValue();
		if (!meaning.isEmpty())
		    meanings.add(meaning);
	    }
	}
	return new DictLemma(
		posField.getText(),
		keyhiField.getText(),
		keyalField.getText(),
		keytrField.getText(),
		keyfoField.getText(),
		keycoField.getText(),
		meanings);
    }

    public boolean containsFocus() {
	boolean f = false;
	Component[] children = def.getComponents();
	for (int i = 0; i < children.length; i++) {
	    Component child = children[i];
	    if (child instanceof EditMeaning)
		f = f || ((EditMeaning) child).containsFocus();
	}
	return f;
    }

    public void findFocus(Object source) {
	Component[] children = def.getComponents();
	for (int i = 0; i < children.length; i++) {
	    Component child = children[i];
	    if (child instanceof EditMeaning)
		((EditMeaning) child).findFocus(source);
	}
    }

    /////////////////////////////////////////////
    // Manipulation.

    // Add various elements, after focus if there is any.
    public void addMeaning() {
	EditMeaning added = new EditMeaning(this, this);
	if (!containsFocus()) {
	    def.add(added, 0);
	    makeChangedAndFocus(added.focusButton());
	} else {
	    Component[] children = def.getComponents();
	    for (int i = 0; i < children.length; i++) {
		Component child = children[i];
		if (child instanceof EditMeaning) {
		    EditMeaning meaning = (EditMeaning) child;
		    if (meaning.containsFocus()) {
			def.add(added, i+1);
			makeChangedAndFocus(added.focusButton());
			break;
		    } 
		}
	    }
	}
    }

    // Add some element after focus.
    public void addUse() {
	Component[] children = def.getComponents();
	for (int i = 0; i < children.length; i++) {
	    Component child = children[i];
	    if (child instanceof EditMeaning) {
		EditMeaning meaning = (EditMeaning) child;
		if (meaning.containsFocus()) {
		    meaning.addUse();
		    break;
		} 
	    }
	}
    }

    // Add some element after focus.
    public void addHi() {
	EditHi added = new EditHi(this, this);
	addElem(added);
    }
    public void addAl() {
	EditAl added = new EditAl(this, this);
	addElem(added);
    }
    public void addTr() {
	EditTr added = new EditTr(this, this);
	addElem(added);
    }
    public void addFo() {
	EditFo added = new EditFo(this, this);
	addElem(added);
    }
    public void addCo() {
	EditCo added = new EditCo(this, this);
	addElem(added);
    }
    public void addAlt() {
	EditAlt added = new EditAlt(this, this);
	addElem(added);
    }
    public void addOpt() {
	EditOpt added = new EditOpt(this, this);
	addElem(added);
    }
    public void addElem(EditUsePart part) {
	Component[] children = def.getComponents();
	for (int i = 0; i < children.length; i++) {
	    Component child = children[i];
	    if (child instanceof EditMeaning) {
		EditMeaning meaning = (EditMeaning) child;
		if (meaning.containsFocus()) {
		    meaning.addElem(part);
		    break;
		} 
	    }
	}
    }

    // Delete some element of lemma.
    public void deleteElem() {
	Component[] children = def.getComponents();
	for (int i = 0; i < children.length; i++) {
	    Component child = children[i];
	    if (child instanceof EditMeaning) {
		EditMeaning meaning = (EditMeaning) child;
		if (meaning.getFocus()) {
		    def.remove(child);
		    makeChanged();
		    break;
		} else if (meaning.containsFocus()) {
		    meaning.deleteElem();
		    break;
		}
	    }
	}
    }

    /////////////////////////////////////////////
    // Closing.

    // Listen if window to be closed or iconified.
    private class CloseListener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
	    tryClose();
        }
        public void windowIconified(WindowEvent e) {
            setState(Frame.ICONIFIED);
        }
        public void windowDeiconified(WindowEvent e) {
            setState(Frame.NORMAL);
        }
    }

    // Close window. Also save entry.
    private void tryClose() {
	if (trySave()) {
	    setVisible(false);
	    refresh();
	}
    }

    // Close window and save entry without checking.
    private void close() {
	save();
	setVisible(false);
	refresh();
    }

    // Quit.
    public void dispose() {
	save();
	super.dispose();
	if (helpWindow != null)
	    helpWindow.dispose();
    }

    // Try save.
    private boolean trySave() {
	if (externEdit && !userConfirmsLoss("Do you want to proceed and discard any edits?"))
	    return false;
	save();
	return true;
    }

    // Save current entry.
    private void save() {
	if (changed && lex != null) {
	    DictLemma newLemma = getValue();
	    if (newLemma.isEmpty()) {
		if (lemma != null)
		    lex.removeLemma(lemma);
	    } else {
		if (lemma == null)
		    lex.addLemma(newLemma);
		else
		    lex.replaceLemma(lemma, newLemma);
	    }
	    try {
		lex.save();
	    } catch (IOException e) {
		JOptionPane.showMessageDialog(this,
			"Could not save:\n" + e.getMessage(), "Writing error",
			JOptionPane.ERROR_MESSAGE);
	    }
	}
	lex = null;
	lemma = null;
	def.removeAll();
	changed = false;
	externEdit = false;
    }

    // Ask user whether loss of data is intended.
    protected boolean userConfirmsLoss(String message) {
        Object[] options = {"proceed", "cancel"};
        int answer = JOptionPane.showOptionDialog(this, message,
                "warning: impending loss of data",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[1]);
        return answer == 0;
    }

    // Caller to override. Refreshed search.
    public void refresh() {
    }

    /////////////////////////////////////////////////////////////
    // Appearance.

    private class NarrowPanel extends JPanel {
	public Dimension getMaximumSize() {
	    Dimension pref = super.getPreferredSize();
	    Dimension max = super.getMaximumSize();
	    return new Dimension(max.width, pref.height + 5);
	}
	public Dimension getPreferredSize() {
	    Dimension pref = super.getPreferredSize();
	    return new Dimension(pref.width, pref.height + 5);
	}
    }

}
