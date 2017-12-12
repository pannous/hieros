package nederhof.util.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import nederhof.util.*;

// Button that keeps (extra) size and can be switched on and off.
public class ToggleButton extends EnabledButton {

    int extra = 5;

    public ToggleButton(ActionListener lis, String action) {
	super(lis, action);
    }

    public Dimension getPreferredSize() {
	Dimension min = super.getMinimumSize();
	return new Dimension(min.width+extra, min.height+extra);
    }

    public Dimension getMinimumSize() {
	Dimension min = super.getMinimumSize();
	return new Dimension(min.width+extra, min.height+extra);
    }

    public void switchBorder(boolean status) {
	if (status)
	    setBorder(BorderFactory.createLoweredBevelBorder());
	else
	    setBorder(BorderFactory.createRaisedBevelBorder());
    }

}
