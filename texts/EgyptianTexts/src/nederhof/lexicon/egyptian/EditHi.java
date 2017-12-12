package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import nederhof.interlinear.egyptian.*;

// Hieroglyphic in dictionary. Edit panel thereof.
public class EditHi extends EditText {

    // Constructor.
    public EditHi(DictHi dictHi, 
	    DocumentListener docListener, ActionListener acListener) {
	super("hi", dictHi.hi, 10, docListener, acListener);
    }
    public EditHi(DocumentListener docListener, ActionListener acListener) {
	this(new DictHi(""), docListener, acListener);
    }

    public DictUsePart getValue() {
        return new DictHi(textField.getText());
    }

}

