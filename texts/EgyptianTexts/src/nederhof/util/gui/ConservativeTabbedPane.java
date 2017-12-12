// Pane that has no more than minimum height.

package nederhof.util.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class ConservativeTabbedPane extends JTabbedPane {

    // Minimum height if any (otherwise negative).
    private int minHeight = -1;

    public ConservativeTabbedPane() {
    }

    public ConservativeTabbedPane(int minHeight) {
	this.minHeight = minHeight;
    }

    public Dimension getMinimumSize() {
        Dimension min = super.getMinimumSize();
        int height = Math.max(min.height, minHeight);
        return new Dimension(min.width, height);
    }
    public Dimension getPreferredSize() {
	Dimension min = super.getMinimumSize();
	Dimension pref = super.getPreferredSize();
        int height = Math.max(min.height, minHeight);
	return new Dimension(pref.width, height);
    }
    public Dimension getMaximumSize() {
	Dimension min = super.getMinimumSize();
	Dimension max = super.getMaximumSize();
        int height = Math.max(min.height, minHeight);
	return new Dimension(max.width, height);
    }

}
