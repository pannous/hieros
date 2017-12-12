package nederhof.lexicon.egyptian;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import nederhof.interlinear.egyptian.*;

// Textual element. Panel thereof.
public abstract class PanelText extends JButton implements PanelUsePart {

    private ActionListener listener;

    // Constructor.
    public PanelText(String s) {
	super(s);
        setMargin(new Insets(0, 0, 0, 0));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(true);
	setFocusable(false);
        setFocus(false);
    }

    public void propagateListener(ActionListener listener) {
	this.listener = listener;
    }

    public void processMouseEvent(MouseEvent e) {
        if (e.getID() == MouseEvent.MOUSE_ENTERED) {
            setBackground(Settings.hoverColor);
        } else if (e.getID() == MouseEvent.MOUSE_EXITED) {
            resetFocus();
        } else if (e.getID() == MouseEvent.MOUSE_PRESSED) {
	    listener.actionPerformed(new ActionEvent(this, e.getID(), "button"));
	}
    }

    public void clearFocus() {
	setFocus(false);
    }

    public boolean hasElement(Component elem) {
	return elem == this;
    }

    public void selectElements(Component elem) {
	setFocus(true);
    }

    private boolean focus = false;
    public void setFocus(boolean focus) {
        this.focus = focus;
        resetFocus();
    }
    public void resetFocus() {
        setBackground(focus ? Settings.focusBackColor : Settings.defaultBackColor);
        repaint();
    }

    public boolean getFocus() {
        return focus;
    }

}
