package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

// Part of use in dictionary. Panel thereof.
public interface PanelUsePart {

    public void propagateListener(ActionListener listener);
    public void clearFocus();
    public boolean hasElement(Component elem);
    public void selectElements(Component elem);
    public LexRecord getSelection(LexRecord lex);

}
