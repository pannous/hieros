package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.util.gui.*;

// Alternatives in dictionary. Edit panel thereof.
public class EditAlt extends JPanel implements EditUsePart {

    private DocumentListener docListener;
    private ActionListener acListener;

    // Button preceding fields.
    protected JButton altButton = new JButton("alt:");

    public EditAlt(DictAlt dictAlt, 
	    DocumentListener docListener, ActionListener acListener) {
	this.docListener = docListener;
	this.acListener = acListener;
	Border raisedbevel = BorderFactory.createRaisedBevelBorder();
	Border loweredbevel = BorderFactory.createLoweredBevelBorder();
	setBorder(BorderFactory.createCompoundBorder(raisedbevel, loweredbevel));
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	altButton.addActionListener(acListener);
	JPanel header = new JPanel();
	header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
	header.add(altButton);
	header.add(Box.createHorizontalGlue());
	add(header);
	addParts(dictAlt, docListener, acListener);
	setFocus(false);
    }
    public EditAlt(DocumentListener docListener, ActionListener acListener) {
	this(new DictAlt(), docListener, acListener);
    }
    public JButton focusButton() {
	return altButton;
    }

    private void addParts(DictAlt dictAlt, 
	    DocumentListener docListener, ActionListener acListener) {
        for (DictUse use : dictAlt.uses) {
            EditUse pUse = new EditUse(use, docListener, acListener);
            add(pUse);
        }
    }

    public DictUsePart getValue() {
        Vector<DictUse> uses = new Vector<DictUse>();
        Component[] children = getComponents();
        for (int i = 0; i < children.length; i++) {
            Component child = children[i];
            if (child instanceof EditUse) {
		DictUse use = ((EditUse) child).getValue();
		if (!use.isEmpty())
		    uses.add(use);
	    }
        }   
        return new DictOpt(uses);
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
        Component[] children = getComponents();
        for (int i = 0; i < children.length; i++) {
            Component child = children[i];
            if (child instanceof EditUse)
                f = f || ((EditUse) child).containsFocus();
	}
	return f;
    }

    public void resetFocus() {
	altButton.setBackground(focus ? Settings.focusBackColor : Settings.defaultBackColor);
	repaint();
    }

    public void clearFocus() {
	setFocus(false);
        Component[] children = getComponents();
        for (int i = 0; i < children.length; i++) {
            Component child = children[i];
            if (child instanceof EditUse)
                ((EditUse) child).clearFocus();
	}
    }

    public void findFocus(Object source) {
	setFocus(!getFocus() && (source == altButton));
	Component[] children = getComponents();
	for (int i = 0; i < children.length; i++) {
	    Component child = children[i];
	    if (child instanceof EditUse)
		((EditUse) child).findFocus(source);
	}
    }

    /////////////////////////////////////////////
    // Manipulation.

    // Add use element.
    public void addUse() {
        if (getFocus()) {
            EditUse added = new EditUse(docListener, acListener);
            add(added, 1);
            makeChangedAndFocus(added.focusButton());
        } else {
            Component[] children = getComponents();
            for (int i = 0; i < children.length; i++) {
                Component child = children[i];
                if (child instanceof EditUse) {
                    EditUse use = (EditUse) child;
                    if (use.getFocus()) {
                        EditUse added = new EditUse(docListener, acListener);
                        add(added, i+1);
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
    public void addElem(EditUsePart added) {
        Component[] children = getComponents();
        for (int i = 0; i < children.length; i++) {
            Component child = children[i];
            if (child instanceof EditUse) {
                EditUse use = (EditUse) child;
                if (use.containsFocus()) {
                    use.addElem(added);
                    break;
                }
            }
        }
    }

    // Delete some element.
    public void deleteElem() {
        Component[] children = getComponents();
        for (int i = 0; i < children.length; i++) {
            Component child = children[i];
            if (child instanceof EditUse) {
                EditUse use = (EditUse) child;
                if (use.getFocus()) {
                    remove(child);
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
        return new Dimension(pref.width, max.height);
    }
    public Dimension getPreferredSize() {
        Dimension pref = super.getPreferredSize();
        return new Dimension(pref.width, pref.height);
    }

}
