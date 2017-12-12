package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

// Translation in dictionary. Panel thereof.
public class PanelTr extends PanelText { 
    // Info.
    public DictTr dictTr;

    // Constructor.
    public PanelTr(DictTr dictTr) {
	super(dictTr.tr);
        this.dictTr = dictTr;
	setFont(new Font("Serif", Font.PLAIN, 14));
    }

    public LexRecord getSelection(LexRecord lex) {
        lex.appendTr(dictTr.tr);
        return lex;
    }

}
