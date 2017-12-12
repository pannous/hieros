package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import nederhof.interlinear.egyptian.*;

// Transliteration in dictionary. Panel thereof.
public class PanelAl extends PanelText {
    // Info.
    public DictAl dictAl;

    // Constructor.
    public PanelAl(DictAl dictAl) {
	super(TransHelper.toUnicode(dictAl.al));
        this.dictAl = dictAl;
	setFont(new Font("Serif", Font.ITALIC, 14));
    }

    public LexRecord getSelection(LexRecord lex) {
	lex.appendAl(dictAl.al);
	return lex;
    }
}
