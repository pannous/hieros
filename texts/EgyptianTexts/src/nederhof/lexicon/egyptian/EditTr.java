package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import nederhof.interlinear.egyptian.*;

// Translation in dictionary. Edit panel thereof.
public class EditTr extends EditText {

    // Constructor.
    public EditTr(DictTr dictTr, 
	    DocumentListener docListener, ActionListener acListener) {
	super("tr", dictTr.tr, 15, docListener, acListener);
    }
    public EditTr(DocumentListener docListener, ActionListener acListener) {
        this(new DictTr(""), docListener, acListener);
    }

    public DictUsePart getValue() {
        return new DictTr(textField.getText());
    }

}

