package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import nederhof.interlinear.egyptian.*;

// Textual element. Edit panel thereof.
public abstract class EditText extends JPanel implements EditUsePart {

    private DocumentListener docListener;
    private ActionListener acListener;

    // Field to be edited.
    protected JTextField textField = new JTextField();
    // Button preceding field.
    protected JButton nameButton;

    // Constructor.
    public EditText(String name, String s, int width, 
	    DocumentListener docListener, ActionListener acListener) {
	this.docListener = docListener;
	this.acListener = acListener;
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	textField.setText(s);
	textField.setColumns(width);
	textField.getDocument().addDocumentListener(docListener);
	nameButton = new JButton(name + ":");
	nameButton.addActionListener(acListener);
	add(nameButton);
	add(textField);
	setMaximumSize(getPreferredSize());
	setFocus(false);
    }

    public JButton focusButton() {
	return nameButton;
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
        return focus;
    }

    public void resetFocus() {
        nameButton.setBackground(focus ? Settings.focusBackColor : Settings.defaultBackColor);
        repaint();
    }

    public void clearFocus() {
        setFocus(false);
    }

    public void findFocus(Object source) {
	setFocus(!getFocus() && (source == nameButton));
    }

    /////////////////////////////////////////////
    // Manipulation.

    // Add use.
    public void addUse() {
	// cannot happen
    }

    // Add some element.
    public void addElem(EditUsePart added) {
        // cannot happen
    }

    // Delete some element.
    public void deleteElem() {
	// cannot happen
    }

}
