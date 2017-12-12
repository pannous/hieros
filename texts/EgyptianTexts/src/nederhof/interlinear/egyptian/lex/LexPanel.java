package nederhof.interlinear.egyptian.lex;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.frame.*;
import nederhof.res.*;
import nederhof.res.format.*;
import nederhof.res.editor.*;
import nederhof.util.*;
import nederhof.util.gui.*;

// Button for lexical entry.
public class LexPanel extends JPanel 
		implements DocumentListener, ChangeListener {

    // Parent GUI element containing this.
    private EditChainElement parent;

    // The lexical entry.
    private LxPart lx;

    // Fields.
    private JPanel textPanel = new JPanel();
    private HieroButton texthi;
    private JTextField textal = new JTextField(20);
    private JTextField texttr = new SpecialTextField(20);
    private JTextField textfo = new SpecialTextField(20);
    private JPanel keyPanel = new JPanel();
    private JTextField cite = new JTextField(20);
    private JTextField href = new JTextField(20);
    private HieroButton keyhi;
    private JTextField keyal = new JTextField(20);
    private JTextField keytr = new SpecialTextField(20);
    private JTextField keyfo = new SpecialTextField(20);
    private JPanel dictPanel = new JPanel();
    private HieroButton dicthi;
    private JTextField dictal = new JTextField(20);
    private JTextField dicttr = new SpecialTextField(20);
    private JTextField dictfo = new SpecialTextField(20);

    // Font to be used for text fields.
    private static Font textInputPlain = new Font(Settings.inputTextFontName,
            Font.PLAIN,
            Settings.inputTextFontSize);
    private static Font textInputItalic = new Font(Settings.inputTextFontName,
            Font.ITALIC,
            Settings.inputTextFontSize);
    private static Font textInputBold = new Font(Settings.inputTextFontName,
            Font.BOLD,
            Settings.inputTextFontSize);

    // Border size.
    private int border = 5;

    // Panel elements.
    protected Vector panelElements = new Vector();

    // Text elements.
    protected Vector textElements = new Vector();

    // Borders of sections.
    private TitledBorder textTitle =
        BorderFactory.createTitledBorder(
                new LineBorder(Color.GRAY, 2), "text");
    private TitledBorder keyTitle =
        BorderFactory.createTitledBorder(
                new LineBorder(Color.GRAY, 2), "key");
    private TitledBorder dictTitle =
        BorderFactory.createTitledBorder(
                new LineBorder(Color.GRAY, 2), "dict");

    public LexPanel(EditChainElement parent, LxPart lx) {
	this.parent = parent;
	putValue(lx);
	textPanel.setBorder(
		BorderFactory.createCompoundBorder(textTitle,
		    BorderFactory.createEmptyBorder(0,5,5,5)));
	keyPanel.setBorder(
		BorderFactory.createCompoundBorder(keyTitle,
		    BorderFactory.createEmptyBorder(0,5,5,5)));
	dictPanel.setBorder(
		BorderFactory.createCompoundBorder(dictTitle,
		    BorderFactory.createEmptyBorder(0,5,5,5)));

        textPanel.setLayout(new SpringLayout());
        textPanel.add(new RecordedLabel("hi"));
        textPanel.add(texthi);
        textPanel.add(new RecordedLabel("al"));
        textPanel.add(textal);
        textPanel.add(new RecordedLabel("tr"));
        textPanel.add(texttr);
        textPanel.add(new RecordedLabel("fo"));
        textPanel.add(textfo);
        SpringUtilities.makeCompactGrid(textPanel, 4, 2, 5, 5, 5, 5);

        keyPanel.setLayout(new SpringLayout());
        keyPanel.add(new RecordedLabel("cite"));
        keyPanel.add(cite);
        keyPanel.add(new RecordedLabel("href"));
        keyPanel.add(href);
        keyPanel.add(new RecordedLabel("hi"));
        keyPanel.add(keyhi);
        keyPanel.add(new RecordedLabel("al"));
        keyPanel.add(keyal);
        keyPanel.add(new RecordedLabel("tr"));
        keyPanel.add(keytr);
        keyPanel.add(new RecordedLabel("fo"));
        keyPanel.add(keyfo);
        SpringUtilities.makeCompactGrid(keyPanel, 6, 2, 5, 5, 5, 5);

        dictPanel.setLayout(new SpringLayout());
        dictPanel.add(new RecordedLabel("hi"));
        dictPanel.add(dicthi);
        dictPanel.add(new RecordedLabel("al"));
        dictPanel.add(dictal);
        dictPanel.add(new RecordedLabel("tr"));
        dictPanel.add(dicttr);
        dictPanel.add(new RecordedLabel("fo"));
        dictPanel.add(dictfo);
        SpringUtilities.makeCompactGrid(dictPanel, 4, 2, 5, 5, 5, 5);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(textPanel);
        add(keyPanel);
        add(dictPanel);

        prepareTextField(textal, textInputItalic);
        prepareTextField(texttr, textInputPlain);
        prepareTextField(textfo, textInputBold);
        prepareTextField(cite, textInputPlain);
        prepareTextField(href, textInputPlain);
        prepareTextField(keyal, textInputItalic);
        prepareTextField(keytr, textInputPlain);
        prepareTextField(keyfo, textInputBold);
        prepareTextField(dictal, textInputItalic);
        prepareTextField(dicttr, textInputPlain);
        prepareTextField(dictfo, textInputBold);
        setFocus(false);
	addMouseListener(new MouseClicker());
    }

    // Do some operations on text elements: set listener, set font, add to
    // text elements.
    private void prepareTextField(JTextField field, Font font) {
        field.getDocument().addDocumentListener(LexPanel.this);
        field.setFont(font);
        textElements.add(field);
    }

    //////////////////////////////////////////////////////
    // Values.

    // Put string in text.
    public void putValue(LxPart lx) {
	this.lx = lx;
	if (texthi != null)
	    textElements.remove(texthi);
	if (keyhi != null)
	    textElements.remove(keyhi);
	if (dicthi != null)
	    textElements.remove(dicthi);
        texthi = makeHieroButton(lx.texthi);
        textal.setText(lx.textal);
        texttr.setText(lx.texttr);
        textfo.setText(lx.textfo);
        cite.setText(lx.cite);
        href.setText(lx.href);
        keyhi = makeHieroButton(lx.keyhi);
        keyal.setText(lx.keyal);
        keytr.setText(lx.keytr);
        keyfo.setText(lx.keyfo);
        dicthi = makeHieroButton(lx.dicthi);
        dictal.setText(lx.dictal);
        dicttr.setText(lx.dicttr);
        dictfo.setText(lx.dictfo);
        textElements.add(texthi);
        textElements.add(keyhi);
        textElements.add(dicthi);
    }

    private HieroButton makeHieroButton(String res) {
        return new HieroButton(res, this) {
            protected void startWait() {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
            }
            protected void endWait() {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            protected void takeFocus() {
            }
            protected void stopEditing() {
                allowEditing(false);
            }
            protected void resumeEditing() {
                allowEditing(true);
            }
        };
    }

    // Any change is recorded.
    public void changedUpdate(DocumentEvent e) {
	copyText();
    }
    public void insertUpdate(DocumentEvent e) {
	copyText();
    }
    public void removeUpdate(DocumentEvent e) {
	copyText();
    }
    public void stateChanged(ChangeEvent e) {
	copyHiero();
    }

    // Copy text fields back to lexical element.
    private void copyText() {
	LxInfo info = new LxInfo(lx);
	info.textal = textal.getText();
	info.texttr = texttr.getText();
	info.textfo = textfo.getText();
	info.cite = cite.getText();
	info.href = href.getText();
	info.keyal = keyal.getText();
	info.keytr = keytr.getText();
	info.keyfo = keyfo.getText();
	info.dictal = dictal.getText();
	info.dicttr = dicttr.getText();
	info.dictfo = dictfo.getText();
	updateSegment(info);
    }

    // Copy hieroglyphic.
    private void copyHiero() {
	LxInfo info = new LxInfo(lx);
	info.texthi = texthi.getRes();
	info.keyhi = keyhi.getRes();
	info.dicthi = dicthi.getRes();
	updateSegment(info);
    }

    /////////////////////////////////////////////
    // Listener.

    private class MouseClicker extends MouseAdapter {
	public void mouseClicked(MouseEvent e) {
	    if (!hasFocus)
		askFocus(lx);
	}
    }

    /////////////////////////////////////////////
    // Appearance.

    private boolean hasFocus = false;

    public void setFocus(boolean b) {
	hasFocus = b;
	texthi.setEnabled(b);
	textal.setEditable(b);
	texttr.setEditable(b);
	texttr.setEditable(b);
	textfo.setEditable(b);
	cite.setEditable(b);
	href.setEditable(b);
	keyhi.setEnabled(b);
	keyal.setEditable(b);
	keytr.setEditable(b);
	keytr.setEditable(b);
	keyfo.setEditable(b);
	dicthi.setEnabled(b);
	dictal.setEditable(b);
	dicttr.setEditable(b);
	dicttr.setEditable(b);
	dictfo.setEditable(b);
        if (b) {
            setBorder(BorderFactory.createLineBorder(Color.BLUE, border));
	} else {
            setBorder(BorderFactory.createLineBorder(Color.WHITE, border));
	}
        repaint();
    }

    // Make border title gray if not editing.
    public void allowEditing(boolean allow) {
	parent.allowEditing(allow);
        for (int i = 0; i < panelElements.size(); i++) {
            JComponent comp = (JComponent) panelElements.get(i);
            comp.setBackground(backColor(allow));
        }
        for (int i = 0; i < textElements.size(); i++) {
            JComponent comp = (JComponent) textElements.get(i);
            comp.setEnabled(allow);
        }
        textTitle.setTitleColor(allow ? Color.BLACK : Color.GRAY);
        keyTitle.setTitleColor(allow ? Color.BLACK : Color.GRAY);
        dictTitle.setTitleColor(allow ? Color.BLACK : Color.GRAY);
	redoFormatting();
    }

    // Adapt to size change of hieroglyphic.
    private void redoFormatting() {
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    textPanel.invalidate();
		    keyPanel.invalidate();
		    dictPanel.invalidate();
		    invalidate();
		    revalidate();
		    reportResized();
		}
	});
    }

    // Backgroup color.
    private Color backColor(boolean allow) {
        return Color.WHITE;
    }

    // Label to be treated as text element.
    private class RecordedLabel extends JLabel {
        public RecordedLabel(String lab) {
            super(lab);
            textElements.add(this);
        }
    }

    // Size.
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    ////////////////////////////////////////////
    // Communication to caller.

    // Caller to override.
    public void updateSegment(LxInfo info) {
    }

    // Caller to override.
    public void reportResized() {
    }

    // Caller to override.
    public void askFocus(LxPart lx) {
    }

}
