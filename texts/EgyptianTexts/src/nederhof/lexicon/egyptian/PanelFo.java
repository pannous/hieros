package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

// Form in dictionary. Panel thereof.
public class PanelFo extends PanelText {
    // Info.
    public DictFo dictFo;

    // Constructor.
    public PanelFo(DictFo dictFo) {
	super(dictFo.fo);
        this.dictFo = dictFo;
	setFont(new Font("Serif", Font.BOLD, 14));
    }

    public LexRecord getSelection(LexRecord lex) {
        lex.appendFo(dictFo.fo);
        return lex;
    }

}
