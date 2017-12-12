package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import nederhof.interlinear.egyptian.*;

// Transliteration in dictionary. Edit panel thereof.
public class EditFo extends EditText {

    // Constructor.
    public EditFo(DictFo dictFo, 
	    DocumentListener docListener, ActionListener acListener) {
	super("fo", dictFo.fo, 8, docListener, acListener);
    }
    public EditFo(DocumentListener docListener, ActionListener acListener) {
        this(new DictFo(""), docListener, acListener);
    }

    public DictUsePart getValue() {
        return new DictFo(textField.getText());
    }

}

