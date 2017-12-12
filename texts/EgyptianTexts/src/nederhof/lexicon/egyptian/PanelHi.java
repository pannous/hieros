package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

// Hieroglyphic in dictionary. Panel thereof.
public class PanelHi extends PanelText {
    // Info.
    public DictHi dictHi;

    // Constructor.
    public PanelHi(DictHi dictHi) {
	super(dictHi.hi);
        this.dictHi = dictHi;
	// TODO
    }

    public LexRecord getSelection(LexRecord lex) {
        lex.appendHi(dictHi.hi);
        return lex;
    }

}
