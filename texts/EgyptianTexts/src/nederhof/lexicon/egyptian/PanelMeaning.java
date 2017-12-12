package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

// Meaning in lemma. Panel thereof.
public class PanelMeaning extends JPanel {
    public DictMeaning dictMeaning;
    public String rank;
    public JButton rankButton;

    private JPanel rankPanel = new JPanel();
    private JPanel usesPanel = new JPanel();
    public Vector<PanelUse> pUses = new Vector<PanelUse>();

    // For non-edit.
    public PanelMeaning(DictMeaning dictMeaning, int rank) {
	this(dictMeaning, "" + rank);
    }
    public PanelMeaning(DictMeaning dictMeaning, String rank) {
	this.dictMeaning = dictMeaning;
	this.rank = rank;

	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	setOpaque(true);
	addRank();
	addParts();
	add(Box.createHorizontalGlue());
	setFocus(false);
    }
    // For editing.
    public PanelMeaning(DictMeaning dictMeaning) {
	this(dictMeaning, dictMeaning.rank);
    }

    private void addRank() {
	rankPanel.setLayout(new BoxLayout(rankPanel, BoxLayout.Y_AXIS));
	rankButton = new JButton(rank);
	rankButton.setMargin(new Insets(0, 10, 0, 0));
	rankButton.setContentAreaFilled(false);
	rankButton.setBorderPainted(false);
	rankButton.setForeground(Color.BLUE);
	rankButton.setOpaque(true);
	rankButton.setFocusable(false);
	rankPanel.add(rankButton);
	rankPanel.add(Box.createVerticalGlue());
	add(rankPanel);
    }

    private void addParts() {
	usesPanel.setLayout(new BoxLayout(usesPanel, BoxLayout.Y_AXIS));
	for (DictUse use : dictMeaning.uses) {
	    PanelUse pUse = new PanelUse(use);
	    pUses.add(pUse);
	    usesPanel.add(pUse);
	}
	add(usesPanel);
    }

    ////////////////////////////////////////////////////////////////
    // Events.

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
	for (PanelUse use : pUses)
	    if (use != selected)
		use.clearFocus();
	if (selected != null) {
	    setFocus(true);
	    selected.selectElements(elem);
	}
    }

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
    public void resetFocus() {
	setBackground(focus ? Settings.focusBackColor : Settings.defaultBackColor);
	rankPanel.setBackground(focus ? Settings.focusBackColor : Settings.defaultBackColor);
	rankButton.setBackground(focus ? Settings.focusBackColor : Settings.defaultBackColor);
	usesPanel.setBackground(focus ? Settings.focusBackColor : Settings.defaultBackColor);
        repaint();
    }
    public boolean getFocus() {
	return focus;
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
