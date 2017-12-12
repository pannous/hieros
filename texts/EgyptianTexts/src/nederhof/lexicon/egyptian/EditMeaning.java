package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

// Meaning in lemma. Edit panel thereof.
public class EditMeaning extends JPanel {

    private DocumentListener docListener;
    private ActionListener acListener;

    private JTextField rankField = new JTextField(4);
    private JButton rankButton = new JButton("rank:");

    private JPanel leftPanel = new JPanel();
    private JPanel rankPanel = new JPanel();
    private JPanel usesPanel = new JPanel();

    public EditMeaning(DictMeaning dictMeaning, 
	    DocumentListener docListener, ActionListener acListener) {
	this.docListener = docListener;
	this.acListener = acListener;
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	setOpaque(true);
	addRank(dictMeaning.rank, docListener, acListener);
	addParts(dictMeaning, docListener, acListener);
	add(Box.createHorizontalGlue());
	setFocus(false);
    }
    public EditMeaning(DocumentListener docListener, ActionListener acListener) {
	this(new DictMeaning(), docListener, acListener);
    }
    public JButton focusButton() {
	return rankButton;
    }

    private void addRank(String rank, 
	    DocumentListener docListener, ActionListener acListener) {
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        rankPanel.setLayout(new BoxLayout(rankPanel, BoxLayout.X_AXIS));
        rankField.setText(rank);
	rankField.setMaximumSize(rankField.getPreferredSize());
        rankField.setOpaque(true);
	rankField.getDocument().addDocumentListener(docListener);
	rankPanel.add(Box.createHorizontalStrut(5));
	rankButton.addActionListener(acListener);
        rankPanel.add(rankButton);
        rankPanel.add(rankField);
	leftPanel.add(rankPanel);
	leftPanel.add(Box.createVerticalGlue());
        add(leftPanel);
    }

    private void addParts(DictMeaning dictMeaning, 
	    DocumentListener docListener, ActionListener acListener) {
        usesPanel.setLayout(new BoxLayout(usesPanel, BoxLayout.Y_AXIS));
        for (DictUse use : dictMeaning.uses) {
            EditUse eUse = new EditUse(use, docListener, acListener);
            usesPanel.add(eUse);
        }
        add(usesPanel);
    }

    public DictMeaning getValue() {
        Vector<DictUse> uses = new Vector<DictUse>();
        Component[] children = usesPanel.getComponents();
        for (int i = 0; i < children.length; i++) {
            Component child = children[i];
            if (child instanceof EditUse) {
		DictUse use = ((EditUse) child).getValue();
		if (!use.isEmpty())
		    uses.add(use);
	    }
        }
        return new DictMeaning(rankField.getText(), uses);
    }

    /////////////////////////////////////////////////////////////
    // Focus.

    private boolean focus = false;
    public void setFocus(boolean focus) {
        this.focus = focus;
        resetFocus();
    }
    public boolean getFocus() {
        return focus;
    }
    public boolean containsFocus() {
        boolean f = focus;
        Component[] children = usesPanel.getComponents();
        for (int i = 0; i < children.length; i++) {
            Component child = children[i];
            if (child instanceof EditUse)
                f = f || ((EditUse) child).containsFocus();
        }
        return f;
    }

    public void resetFocus() {
	rankButton.setBackground(focus ? Settings.focusBackColor : Settings.defaultBackColor);
        repaint();
    }

    public void clearFocus() {
        setFocus(false);
        Component[] children = usesPanel.getComponents();
        for (int i = 0; i < children.length; i++) {
            Component child = children[i];
            if (child instanceof EditUse)
                ((EditUse) child).clearFocus();
	}
    }

    public void findFocus(Object source) {
	setFocus(!getFocus() && (source == rankButton));
	Component[] children = usesPanel.getComponents();
	for (int i = 0; i < children.length; i++) {
	    Component child = children[i];
	    if (child instanceof EditUse)
		((EditUse) child).findFocus(source);
	}
    }

    /////////////////////////////////////////////
    // Manipulation.

    // Add use.
    public void addUse() {
	if (getFocus()) {
	    EditUse added = new EditUse(docListener, acListener);
	    usesPanel.add(added, 0);
	    makeChangedAndFocus(added.focusButton());
	} else {
	    Component[] children = usesPanel.getComponents();
	    for (int i = 0; i < children.length; i++) {
		Component child = children[i];
		if (child instanceof EditUse) {
		    EditUse use = (EditUse) child;
		    if (use.getFocus()) {
			EditUse added = new EditUse(docListener, acListener);
			usesPanel.add(added, i+1);
			makeChangedAndFocus(added.focusButton());
			break;
		    } else if (use.containsFocus()) {
			use.addUse();
			break;
		    }
		}
	    }
	}
    }

    // Add some element.
    public void addElem(EditUsePart part) {
        Component[] children = usesPanel.getComponents();
        for (int i = 0; i < children.length; i++) {
            Component child = children[i];
            if (child instanceof EditUse) {
		EditUse use = (EditUse) child;
		if (use.containsFocus()) {
		    use.addElem(part);
		    break;
		}
	    }
        }
    }

    // Delete some element.
    public void deleteElem() {
        Component[] children = usesPanel.getComponents();
        for (int i = 0; i < children.length; i++) {
            Component child = children[i];
            if (child instanceof EditUse) {
		EditUse use = (EditUse) child;
		if (use.getFocus()) {
		    usesPanel.remove(child);
		    makeChangedAndFocus(focusButton());
		    break;
		} else if (use.containsFocus()) {
		    use.deleteElem();
		    break;
		}
	    }
        }
    }

    private void makeChanged() {
	acListener.actionPerformed(new ActionEvent(this, 0, "update"));
    }
    private void makeChangedAndFocus(Object source) {
	acListener.actionPerformed(new ActionEvent(source, 0, "update and focus"));
    }

    /////////////////////////////////////////////////////////////
    // Appearance.

    public Dimension getMaximumSize() {
        Dimension pref = super.getPreferredSize();
        Dimension max = super.getMaximumSize();
        return new Dimension(max.width, pref.height);
    }
    public Dimension getPreferredSize() {
        Dimension pref = super.getPreferredSize();
        return new Dimension(pref.width, pref.height);
    }

}
