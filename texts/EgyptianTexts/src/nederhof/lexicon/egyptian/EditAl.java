package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import nederhof.interlinear.egyptian.*;

// Transliteration in dictionary. Edit panel thereof.
public class EditAl extends EditText {

    // Constructor.
    public EditAl(DictAl dictAl, 
	    DocumentListener docListener, ActionListener acListener) {
	super("al", dictAl.al, 10, docListener, acListener);
    }
    public EditAl(DocumentListener docListener, ActionListener acListener) {
	this(new DictAl(""), docListener, acListener);
    }

    public DictUsePart getValue() {
	return new DictAl(textField.getText());
    }

}

