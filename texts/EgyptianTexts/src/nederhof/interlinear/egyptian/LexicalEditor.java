/***************************************************************************/
/*                                                                         */
/*  LexicalEditor.java                                                     */
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

// Frame in which to edit lexical entry.

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
import nederhof.util.*;

public abstract class LexicalEditor extends JFrame 
	implements ActionListener, DocumentListener,
	ChangeListener {

    // Fields.
    private HieroButton texthi;
    private JTextField textal = new JTextField(20);
    private JTextField texttr = new SpecialTextField(20);
    private JTextField textfo = new SpecialTextField(20);
    private JTextField cite = new JTextField(20);
    private JTextField href = new JTextField(20);
    private HieroButton keyhi;
    private JTextField keyal = new JTextField(20);
    private JTextField keytr = new SpecialTextField(20);
    private JTextField keyfo = new SpecialTextField(20);
    private HieroButton dicthi;
    private JTextField dictal = new JTextField(20);
    private JTextField dicttr = new SpecialTextField(20);
    private JTextField dictfo = new SpecialTextField(20);

    // Font to be used for text fields.
    private static Font textInputFont = new Font(Settings.inputTextFontName, 
	    Font.PLAIN, 
	    Settings.inputTextFontSize);

    // Has entry changed?
    private boolean changed = false;

    // The listener to closing. To be passed to auxiliary frames.
    private CloseListener closeListener = new CloseListener();

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

    public LexicalEditor(LxInfo info) {
	setTitle("Lexical editor");
	setJMenuBar(getMenu());
	putValue(info);

	JPanel textPanel = new JPanel();
	JPanel keyPanel = new JPanel();
	JPanel dictPanel = new JPanel();
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

	Container content = getContentPane();
	content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
	content.add(textPanel);
	content.add(keyPanel);
	content.add(dictPanel);

	textElements.add(texthi);
	prepareTextField(textal);
	prepareTextField(texttr);
	prepareTextField(textfo);
	prepareTextField(cite);
	prepareTextField(href);
	textElements.add(keyhi);
	prepareTextField(keyal);
	prepareTextField(keytr);
	prepareTextField(keyfo);
	textElements.add(dicthi);
	prepareTextField(dictal);
	prepareTextField(dicttr);
	prepareTextField(dictfo);

	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	addWindowListener(closeListener);
	pack();
	setVisible(true);
    }

    // Do some operations on text elements: set listener, set font, add to
    // text elements.
    private void prepareTextField(JTextField field) {
	field.getDocument().addDocumentListener(LexicalEditor.this);
	field.setFont(textInputFont);
	textElements.add(field);
    }

    /////////////////////////////////////////////
    // Menu.

    // Items in menu that may be disabled/enabled.
    private final JButton saveItem = 
	new ClickButton(this, "clo<u>S</u>e", "save", KeyEvent.VK_S);

    // Menu.
    private JMenuBar getMenu() {
        final int STRUT_SIZE = 10;
        JMenuBar box = new JMenuBar();
        box.setLayout(new BoxLayout(box, BoxLayout.X_AXIS));
        box.setBackground(Color.LIGHT_GRAY);
        box.add(Box.createHorizontalStrut(STRUT_SIZE));
        box.add(saveItem);
        box.add(Box.createHorizontalGlue());
        return box;
    }

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("save")) {
            if (changed)
                receive(getValue());
            else
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

    //////////////////////////////////////////////////////
    // Values.

    // Put string in text. The parts form a single paragraph.
    public void putValue(LxInfo info) {
	texthi = makeHieroButton(info.texthi);
	textal.setText(info.textal);
	texttr.setText(info.texttr);
	textfo.setText(info.textfo);
	cite.setText(info.cite);
	href.setText(info.href);
	keyhi = makeHieroButton(info.keyhi);
	keyal.setText(info.keyal);
	keytr.setText(info.keytr);
	keyfo.setText(info.keyfo);
	dicthi = makeHieroButton(info.dicthi);
	dictal.setText(info.dictal);
	dicttr.setText(info.dicttr);
	dictfo.setText(info.dictfo);
        changed = false;
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
                requestFocus();
            }
            protected void stopEditing() {
                allowEditing(false);
            }
            protected void resumeEditing() {
                allowEditing(true);
            }
        };
    }

    // Retrieve value. These are paragraphs. The parts have to be merged.
    public LxInfo getValue() {
	return new LxInfo(
		texthi.getRes(),
		textal.getText(),
		texttr.getText(),
		textfo.getText(),
		cite.getText(),
		href.getText(),
		keyhi.getRes(),
		keyal.getText(),
		keytr.getText(),
		keyfo.getText(),
		dicthi.getRes(),
		dictal.getText(),
		dicttr.getText(),
		dictfo.getText());
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
    public void stateChanged(ChangeEvent e) {
	changed = true;
    }

    protected abstract void receive(LxInfo info);

    protected abstract void cancel();

    /////////////////////////////////////////////
    // Appearance.

    // Make border title gray if not editing.
    public void allowEditing(boolean allow) {
	saveItem.setEnabled(allow);
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
	repaint();
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

}

