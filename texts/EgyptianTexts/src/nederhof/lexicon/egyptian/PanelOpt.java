package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import nederhof.util.gui.*;

// Optional alternatives in dictionary. Panel thereof.
public class PanelOpt extends JPanel implements PanelUsePart {
    // Info.
    public DictOpt dictOpt;

    public Vector<PanelUse> pUses = new Vector<PanelUse>();

    public PanelOpt(DictOpt dictOpt) {
	this.dictOpt = dictOpt;

	setBorder(new DashedBorder());
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	addParts();
	setFocus(false);
    }

    private void addParts() {
	for (DictUse use : dictOpt.uses) {
	    PanelUse pUse = new PanelUse(use);
	    pUses.add(pUse);
	    add(pUse);
	}
    }

    public void propagateListener(ActionListener listener) {
	for (PanelUse use : pUses)
	    use.propagateListener(listener);
    }

    public void clearFocus() {
	setFocus(false);
	for (PanelUse use : pUses)
	    use.clearFocus();
    }

    public boolean hasElement(Component elem) {
	for (PanelUse use : pUses)
	    if (use.hasElement(elem))
		return true;
	return false;
    }

    public void selectElements(Component elem) {
        PanelUse selected = null;
        for (PanelUse use : pUses)
            if (use.hasElement(elem))
                selected = use;
	if (selected != null) {
	    setFocus(true);
	    for (PanelUse use : pUses)
		if (use != selected)
		    use.clearFocus();
            selected.selectElements(elem);
	}
    }

    // An alternative is optional.
    public LexRecord getSelection(LexRecord lex) {
        for (PanelUse use : pUses)
            if (use.getFocus())
                return use.getSelection(lex);
        return lex;
    }

    private boolean focus = false;
    public void setFocus(boolean focus) {
        this.focus = focus;
        resetFocus();
    }
    public boolean getFocus() {
        return focus;
    }
    public void resetFocus() {
        setBackground(focus ? Settings.focusBackColor : Settings.defaultBackColor);
        repaint();
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
