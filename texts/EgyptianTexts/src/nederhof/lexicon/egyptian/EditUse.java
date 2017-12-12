package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

// Use in dictionary. Edit panel thereof.
public class EditUse extends JPanel {

    private DocumentListener docListener;
    private ActionListener acListener;

    // Button preceding fields.
    protected JButton useButton = new JButton("use:");

    // Constructor.
    public EditUse(DictUse dictUse, 
	    DocumentListener docListener, ActionListener acListener) {
	this.docListener = docListener;
	this.acListener = acListener;
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	useButton.addActionListener(acListener);
	add(useButton);
	addParts(dictUse, docListener, acListener);
	setFocus(false);
    }
    public EditUse(DocumentListener docListener, ActionListener acListener) {
	this(new DictUse(), docListener, acListener);
    }
    public JButton focusButton() {
	return useButton;
    }

    private void addParts(DictUse dictUse, 
	    DocumentListener docListener, ActionListener acListener) {
        for (DictUsePart part : dictUse.parts) {
            if (part instanceof DictHi) {
                EditHi pHi = new EditHi((DictHi) part, docListener, acListener);
                add(pHi);
            } else if (part instanceof DictAl) {
                EditAl pAl = new EditAl((DictAl) part, docListener, acListener);
                add(pAl);
            } else if (part instanceof DictTr) {
                EditTr pTr = new EditTr((DictTr) part, docListener, acListener);
                add(pTr);
            } else if (part instanceof DictFo) {
                EditFo pFo = new EditFo((DictFo) part, docListener, acListener);
                add(pFo);
            } else if (part instanceof DictCo) {
                EditCo pCo = new EditCo((DictCo) part, docListener, acListener);
                add(pCo);
            } else if (part instanceof DictAlt) {
                EditAlt pAlt = new EditAlt((DictAlt) part, docListener, acListener);
                add(pAlt);
            } else if (part instanceof DictOpt) {
                EditOpt pOpt = new EditOpt((DictOpt) part, docListener, acListener);
                add(pOpt);
            }
        }
        add(Box.createHorizontalGlue());
    }

    public DictUse getValue() {
	Vector<DictUsePart> parts = new Vector<DictUsePart>();
	Component[] children = getComponents();
	for (int i = 0; i < children.length; i++) {
	    Component child = children[i];
	    if (child instanceof EditUsePart) {
		DictUsePart part = ((EditUsePart) child).getValue();
		if (!part.isEmpty())
		    parts.add(part);
	    }
	}
	return new DictUse(parts);
    }

    /////////////////////////////////////////////////////////////
    // Focus.

    // Not used.
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
        Component[] children = getComponents();
        for (int i = 0; i < children.length; i++) {
            Component child = children[i];
            if (child instanceof EditUsePart)
                f = f || ((EditUsePart) child).containsFocus();
        }
        return f;
    }

    public void resetFocus() {
        useButton.setBackground(focus ? Settings.focusBackColor : Settings.defaultBackColor);
        repaint();
    }

    public void clearFocus() {
        setFocus(false);
        Component[] children = getComponents();
        for (int i = 0; i < children.length; i++) {
            Component child = children[i];
            if (child instanceof EditUsePart)
                ((EditUsePart) child).clearFocus();
        }
    }

    public void findFocus(Object source) {
	setFocus(!getFocus() && (source == useButton));
	Component[] children = getComponents();
	for (int i = 0; i < children.length; i++) {
	    Component child = children[i];
	    if (child instanceof EditUsePart)
		((EditUsePart) child).findFocus(source);
	}
    }

    /////////////////////////////////////////////
    // Manipulation.

    // Add use.
    public void addUse() {
        Component[] children = getComponents();
        for (int i = 0; i < children.length; i++) {
            Component child = children[i];
            if (child instanceof EditUsePart) {
		EditUsePart part = (EditUsePart) child;
		if (part.containsFocus()) {
		    part.addUse();
		    break;
		}
	    }
        }
    }

    // Add some element.
    public void addElem(EditUsePart added) {
	if (getFocus()) {
	    add((JPanel) added, 1);
	    makeChangedAndFocus(added.focusButton());
	} else {
	    Component[] children = getComponents();
	    for (int i = 0; i < children.length; i++) {
		Component child = children[i];
		if (child instanceof EditUsePart) {
		    EditUsePart part = (EditUsePart) child;
		    if (part.getFocus()) {
			add((JPanel) added, i+1);
			makeChangedAndFocus(added.focusButton());
			break;
		    } else if (part.containsFocus()) {
			part.addElem(added);
			break;
		    }
		}
	    }
	}
    }

    // Delete some element.
    public void deleteElem() {
        Component[] children = getComponents();
        for (int i = 0; i < children.length; i++) {
            Component child = children[i];
            if (child instanceof EditUsePart) {
		EditUsePart part = (EditUsePart) child;
		if (part.getFocus()) {
		    remove(child);
		    makeChangedAndFocus(focusButton());
		    break;
		} else if (part.containsFocus()) {
		    part.deleteElem();
		    break;
		}
	    }
        }
    }

    private void makeChangedAndFocus(Object source) {
        acListener.actionPerformed(new ActionEvent(source, 0, "update and focus"));
    }

}
