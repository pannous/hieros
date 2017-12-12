package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

// Use in dictionary. Panel thereof.
public class PanelUse extends JPanel {
    // Info.
    public DictUse dictUse;

    public Vector<PanelUsePart> pUseParts = new Vector<PanelUsePart>();

    // Constructor.
    public PanelUse(DictUse dictUse) {
	this.dictUse = dictUse;

	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	addParts();
	setFocus(false);
    }

    private void addParts() {
	for (DictUsePart part : dictUse.parts) {
            if (part instanceof DictHi) {
                PanelHi pHi = new PanelHi((DictHi) part);
		add(pHi);
		pUseParts.add(pHi);
            } else if (part instanceof DictAl) {
                PanelAl pAl = new PanelAl((DictAl) part);
		add(pAl);
		pUseParts.add(pAl);
            } else if (part instanceof DictTr) {
                PanelTr pTr = new PanelTr((DictTr) part);
		add(pTr);
		pUseParts.add(pTr);
            } else if (part instanceof DictFo) {
                PanelFo pFo = new PanelFo((DictFo) part);
		add(pFo);
		pUseParts.add(pFo);
            } else if (part instanceof DictCo) {
                PanelCo pCo = new PanelCo((DictCo) part);
		add(pCo);
		pUseParts.add(pCo);
            } else if (part instanceof DictAlt) {
                PanelAlt pAlt = new PanelAlt((DictAlt) part);
		add(pAlt);
		pUseParts.add(pAlt);
            } else if (part instanceof DictOpt) {
                PanelOpt pOpt = new PanelOpt((DictOpt) part);
		add(pOpt);
		pUseParts.add(pOpt);
            }  
	}
	add(Box.createHorizontalGlue());
    }

    ////////////////////////////////////////////////////////////////
    // Events.

    public void propagateListener(ActionListener listener) {
	for (PanelUsePart part : pUseParts) 
	    part.propagateListener(listener);
    }

    public void clearFocus() {
	setFocus(false);
	for (PanelUsePart part : pUseParts) 
	    part.clearFocus();
    }

    public boolean hasElement(Component elem) {
	for (PanelUsePart part : pUseParts) 
	    if (part.hasElement(elem))
		return true;
	return false;
    }

    public void selectElements(Component elem) {
	setFocus(true);
	for (PanelUsePart part : pUseParts) 
	    part.selectElements(elem);
    }

    public LexRecord getSelection(LexRecord lex) {
	for (PanelUsePart part : pUseParts) 
	    lex = part.getSelection(lex);
	return lex;
    }

    // Not used.
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

}
