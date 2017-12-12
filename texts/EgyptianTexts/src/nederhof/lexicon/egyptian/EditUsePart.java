package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

// Part of use in dictionary. Edit panel thereof.
public interface EditUsePart {

    public JButton focusButton();
    public DictUsePart getValue();

    public boolean getFocus();
    public boolean containsFocus();
    public void clearFocus();
    public void findFocus(Object source);

    /////////////////////////////////////////////
    // Manipulation.

    public void addUse();
    public void addElem(EditUsePart added);
    public void deleteElem();

}
