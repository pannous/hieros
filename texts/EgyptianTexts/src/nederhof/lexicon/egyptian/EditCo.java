package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import nederhof.interlinear.egyptian.*;

// Comment in dictionary. Edit panel thereof.
public class EditCo extends EditText {

    // Constructor.
    public EditCo(DictCo dictCo, 
	    DocumentListener docListener, ActionListener acListener) {
	super("co", dictCo.co, 25, docListener, acListener);
    }
    public EditCo(DocumentListener docListener, ActionListener acListener) {
        this(new DictCo(""), docListener, acListener);
    }

    public DictUsePart getValue() {
        return new DictCo(textField.getText());
    }

}

