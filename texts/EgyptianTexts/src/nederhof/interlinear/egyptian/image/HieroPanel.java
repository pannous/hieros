package nederhof.interlinear.egyptian.image;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import nederhof.res.*;
import nederhof.res.editor.*;
import nederhof.res.format.*;
import nederhof.util.*;
import nederhof.util.gui.*;

// Panel with buttons for hieroglyphic.
public class HieroPanel extends JPanel {

    // Auxiliary class for manipulating resource.
    private ImageResourceManipulator manipulator;

    // For hieroglyphic.
    private HieroRenderContext hieroContext;
    private ParsingContext parsingContext;

    // One button for each hieroglyph.
    private JPanel buttonPanel;
    // Scroller thereof.
    private SimpleHorScroller scroller;

    // Button that holds focus.
    private ConnectedHieroButton focusButton;

    // Constructor.
    public HieroPanel(HieroRenderContext hieroContext, ParsingContext parsingContext,
	    ImageResourceManipulator manipulator) {
	this.hieroContext = hieroContext;
	this.parsingContext = parsingContext;
	this.manipulator = manipulator;
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	buttonPanel = new JPanel();
	buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
	scroller = new SimpleHorScroller(buttonPanel);
	add(scroller);
	refresh();
    }

    public Dimension getMinimumSize() {
	Dimension min = super.getMinimumSize();
	Dimension pref = super.getPreferredSize();
	return new Dimension(min.width, pref.height);
    }
    public Dimension getPreferredSize() {
	Dimension pref = super.getPreferredSize();
	return new Dimension(pref.width, pref.height);
    }
    public Dimension getMaximumSize() {
	Dimension max = super.getMaximumSize();
	Dimension pref = super.getPreferredSize();
	return new Dimension(max.width, pref.height);
    }

    /////////////////////////////////////////////////////
    // Auxiliary classes.

    private class ConnectedHieroButton extends HieroButton {
	public ConnectedHieroButton(ImageSign sign) {
	    super(sign, hieroContext, parsingContext);
	}
	public void askFocus(ImageSign sign) {
	    HieroPanel.this.askFocus(sign);
	}
	public void openMenu() {
	    HieroPanel.this.openMenu();
	}
    }

    /////////////////////////////////////////////////////
    // Communication back to caller.

    // Open menu for choosing new sign for focus.
    public void openMenu() {
	// caller should override
    }

    /////////////////////////////////////////////////////
    // Signs in panel and their appearance.

    // Complete (re)filling with signs.
    public void refresh() {
	buttonPanel.removeAll();
	focusButton = null;
	for (int i = 0; i < manipulator.nSigns(); i++) {
	    buttonPanel.add(new ConnectedHieroButton(manipulator.sign(i)));
	}
	buttonPanel.add(Box.createHorizontalGlue());
	showFocus();
	revalidate();
	repaint();
    }
    public void refresh(int i, ImageSign sign) {
	if (i >= 0 && i < buttonPanel.getComponents().length) {
	    ConnectedHieroButton but = (ConnectedHieroButton)
		buttonPanel.getComponent(i);
	    but.reset(sign);
	    but.revalidate();
	    repaint();
	}
    }
    public void refresh(ImageSign sign) {
	if (focusButton != null) {
	    focusButton.reset(sign);
	    focusButton.revalidate();
	    repaint();
	    scrollToFocusLater();
	}
    }

    // Remove sign.
    public void removeButton(int i) {
	if (i >= 0 && i < buttonPanel.getComponents().length) {
	    buttonPanel.remove(i);
	    showFocus();
	    revalidate();
	    repaint();
	}
    }

    // Add sign.
    public void addButton(int i, ImageSign sign) {
	if (i >= 0 && i <= buttonPanel.getComponents().length) {
	    unshowFocus();
	    buttonPanel.add(new ConnectedHieroButton(sign), i);
	    showFocus();
	    revalidate();
	    repaint();
	    scrollToFocusLater();
	}
    }

    // Bring focus to button containing sign.
    public void askFocus(ImageSign sign) {
	manipulator.setCurrent(sign);
    }

    // Change sign of focus.
    public void acceptName(String name) {
	if (name != null) 
	    manipulator.updateSign(name);
    }

    // Add sign at i-th position.
    public void addHiero(int i) {
	boolean done = manipulator.insertSign(i);
	if (done)
	    openMenu();
    }

    // Show again the places of signs.
    public void reshowPlaces() {
	Component[] buttons = buttonPanel.getComponents();
	for (int i = 0; i < buttons.length; i++)
	    if (buttons[i] instanceof ConnectedHieroButton)
		((ConnectedHieroButton) buttons[i]).showPlaces();
    }

    // Show focus.
    public void showFocus() {
	int current = manipulator.current();
	if (current >= 0 && current < buttonPanel.getComponents().length) {
	    ConnectedHieroButton but = (ConnectedHieroButton)
		buttonPanel.getComponents()[current];
	    but.setFocus(true);
	    focusButton = but;
	}
    }
    // No longer show focus.
    public void unshowFocus() {
	if (focusButton != null)
	    focusButton.setFocus(false);
	focusButton = null;
    }
    // Scroll to focus.
    public void scrollToFocus() {
	if (focusButton != null)
	    scrollTo(focusButton);
    }
    // Scroll to focus later.
    public void scrollToFocusLater() {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		scrollToFocus();
	    }
	});
    }

    private void scrollTo(Component comp) {
	// scroller.scrollRectToVisible(comp.getBounds());
	int x = comp.getLocation().x;
	scroller.getHorizontalScrollBar().setValue(
		Math.max(0, x - 4 * getWidth() / 10));
    }

    /////////////////////////////////////////////////////
    // Auxiliary GUI.

    // Simple scroll panes.
    protected static class SimpleHorScroller extends JScrollPane {
        public SimpleHorScroller(JComponent pane) {
            super(pane,
                    JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            setFocusable(false);
            getHorizontalScrollBar().setUnitIncrement(10);
        }
    }
}
