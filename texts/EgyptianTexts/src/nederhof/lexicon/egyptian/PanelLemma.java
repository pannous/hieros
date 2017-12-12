package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import nederhof.interlinear.egyptian.*;

// Lemma in dictionary. Panel thereof.
public class PanelLemma extends JPanel implements ActionListener {
    public DictLemma dictLemma;
    public JButton pos;
    public JButton keyhi;
    public JButton keyal;
    public JButton keytr;
    public JButton keyfo;
    public JButton keyco;
    public Vector<PanelMeaning> pMeanings = new Vector<PanelMeaning>();

    private JPanel header = new JPanel();

    public PanelLemma(DictLemma dictLemma) {
	this.dictLemma = dictLemma;
	pos = new JButton(dictLemma.pos);
	keyhi = new JButton(dictLemma.keyhi);
	keyal = new JButton(dictLemma.keyal);
	keytr = new JButton(dictLemma.keytr);
	keyfo = new JButton(dictLemma.keyfo);
	keyco = new JButton(dictLemma.keyco);

	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	setOpaque(true);
	addHeader();
	addDef();
	propagateListener();
	setFocus(false);
    }

    // Header of lemma.
    private void addHeader() {
	JLabel alLabel = new JLabel(TransHelper.toUnicode(dictLemma.keyal));
	alLabel.setFont(new Font("Serif", Font.ITALIC, 14));
	JLabel trLabel = new JLabel(dictLemma.keytr);
	trLabel.setFont(new Font("Serif", Font.PLAIN, 14));
	JLabel foLabel = new JLabel(dictLemma.keyfo);
	foLabel.setFont(new Font("Serif", Font.BOLD, 14));
	header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
	header.setOpaque(true);
	header.add(Box.createHorizontalStrut(5));
	header.add(alLabel);
	header.add(Box.createHorizontalStrut(10));
	header.add(trLabel);
	header.add(Box.createHorizontalStrut(10));
	header.add(foLabel);
	if (!dictLemma.keyco.equals("")) {
	    JLabel coLabel = new JLabel("(" + dictLemma.keyco + ")");
	    coLabel.setFont(new Font("Serif", Font.PLAIN, 14));
	    header.add(Box.createHorizontalStrut(10));
	    header.add(coLabel);
	}
	header.add(Box.createHorizontalGlue());
	header.addMouseListener(new MouseAdapter() {
	    public void mouseEntered(MouseEvent e) {
		header.setBackground(Settings.hoverColor);
	    }
	    public void mouseExited(MouseEvent e) {
		PanelLemma.this.resetFocus();
	    }
	    public void mouseClicked(MouseEvent e) {
	    	if (e.getButton() == MouseEvent.BUTTON1 && !e.isControlDown())
		    PanelLemma.this.toggleFocus();
		else
		    PanelLemma.this.edit();
	    }
	});
	add(header);
    }

    private void addDef() {
	for (DictMeaning meaning : dictLemma.meanings) {
	    PanelMeaning pMeaning = new PanelMeaning(meaning);
	    add(pMeaning);
	    pMeanings.add(pMeaning);
	}
    }

    ////////////////////////////////////////////////////////////////
    // Events.

    private void propagateListener() {
	for (PanelMeaning meaning : pMeanings) 
	    meaning.propagateListener(this);
    }
    
    // Click on some element of lemma.
    public void actionPerformed(ActionEvent e) {
	if (focus && e.getSource() == this)
	    askFocus(null);
	else {
	    askFocus(this);
	    if (e.getSource() instanceof Component)
		selectElements((Component) e.getSource());
	}
    }

    public void clearFocus() {
	setFocus(false);
	for (PanelMeaning meaning : pMeanings) 
	    meaning.clearFocus();
    }

    public void selectElements(Component elem) {
	PanelMeaning selected = null;
	for (PanelMeaning meaning : pMeanings) 
	    if (meaning.hasElement(elem)) 
		selected = meaning;
	for (PanelMeaning meaning : pMeanings)
	    if (meaning != selected)
		meaning.clearFocus();
	if (selected != null)
	    selected.selectElements(elem);
	LexRecord lex = getSelection();
	if (lex != null)
	    reportSelection(lex);
	else
	    reportSelection(freshRecord());
    }

    // Get focussed elements in lemma. Or null if selection incomplete.
    public LexRecord getSelection() {
	LexRecord lex = freshRecord();
	for (PanelMeaning meaning : pMeanings)
	    if (meaning.getFocus()) {
		return meaning.getSelection(lex);
	    }
	return lex;
    }

    public LexRecord freshRecord() {
	return new LexRecord(
		dictLemma.keyhi,
		dictLemma.keyal,
		dictLemma.keytr,
		dictLemma.keyfo,
		dictLemma.keyco);
    }

    private boolean focus = false;
    public void setFocus(boolean focus) {
	this.focus = focus;
	resetFocus();
    }
    public void resetFocus() {
	setBackground(focus ? Settings.focusBackColor : Settings.defaultBackColor);
	header.setBackground(focus ? Settings.focusBackColor : Settings.defaultBackColor);
	repaint();
    }

    public void toggleFocus() {
	if (focus) 
	    askFocus(null);
	else {
	    askFocus(this);
	    reportSelection(freshRecord());
	}
    }

    // Caller to override.
    public void askFocus(PanelLemma pLem) {
    }

    // Caller to override.
    public void edit() {
    }

    // Report selection. Caller to override.
    public void reportSelection(LexRecord lex) {
    }

    /////////////////////////////////////////////////////////////
    // Appearance.

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
