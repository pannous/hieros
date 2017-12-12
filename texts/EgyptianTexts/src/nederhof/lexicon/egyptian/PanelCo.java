package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

// Comment in dictionary. Panel thereof.
public class PanelCo extends PanelText {
    // Info.
    public DictCo dictCo;

    // Constructor.
    public PanelCo(DictCo dictCo) {
	super("(" + dictCo.co + ")");
        this.dictCo = dictCo;
	setFont(new Font("Serif", Font.PLAIN, 14));
    }

    // Overrides superclass.
    public void selectElements(Component elem) {
	if (elem == this)
	    setFocus(true);
    }

    public LexRecord getSelection(LexRecord lex) {
	if (getFocus())
	    lex.appendCo(dictCo.co);
        return lex;
    }

}
