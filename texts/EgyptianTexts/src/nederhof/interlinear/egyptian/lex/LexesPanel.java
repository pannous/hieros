package nederhof.interlinear.egyptian.lex;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.frame.*;
import nederhof.res.*;

// Contains the lexical annotations.
public class LexesPanel extends JPanel implements EditChainElement {

    // Parent GUI element containing this.
    private EditChainElement parent;

    // For manipulating resource.
    private LexicoManipulator manipulator;

    // One button for each segment.
    private JPanel buttonPanel;
    // Scroller thereof.
    private SimpleHorScroller scroller;

    // Button that holds focus.
    private ConnectedLexPanel focusButton;

    // Constructor.
    public LexesPanel(EditChainElement parent, LexicoManipulator manipulator) {
        this.parent = parent;
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

    private class ConnectedLexPanel extends LexPanel {
        public ConnectedLexPanel(LxPart part) {
            super(LexesPanel.this, part);
        }
        public void updateSegment(LxInfo info) {
	    LexesPanel.this.manipulator.updateSegment(info);
        }
        public void reportResized() {
            LexesPanel.this.revalidate();
        }
        public void askFocus(LxPart lx) {
            LexesPanel.this.askFocus(lx);
        }
    }

    /////////////////////////////////////////////////////
    // Entries in panel and their appearance.

    // Complete (re)filling with entries.
    public void refresh() {
        buttonPanel.removeAll();
        focusButton = null;
        for (int i = 0; i < manipulator.nSegments(); i++) {
            buttonPanel.add(new ConnectedLexPanel(manipulator.segment(i)));
        }
        buttonPanel.add(Box.createHorizontalGlue());
        showFocus();
        revalidate();
        repaint();
	searchLexicon();
    }
    public void refresh(int i, LxPart lx) {
        if (i >= 0 && i < buttonPanel.getComponents().length) {
	    buttonPanel.remove(i);
	    buttonPanel.add(new ConnectedLexPanel(lx), i);
            // ConnectedLexPanel but = (ConnectedLexPanel) buttonPanel.getComponent(i);
            // but.putValue(lx);
	    showFocus();
	    revalidate();
            repaint();
        }
    }

    // Remove segment.
    public void removeButton(int i) {
        if (i >= 0 && i < buttonPanel.getComponents().length) {
            buttonPanel.remove(i);
            showFocus();
            revalidate();
            repaint();
	    searchLexicon();
        }
    }

    // Add segment.
    public void addButton(int i, LxPart lx) {
        if (i >= 0 && i <= buttonPanel.getComponents().length) {
            unshowFocus();
            buttonPanel.add(new ConnectedLexPanel(lx), i);
            showFocus();
            revalidate();
            repaint();
            scrollToFocusLater();
        }
    }

    // Bring focus to button containing segment.
    public void askFocus(LxPart lx) {
        manipulator.setCurrent(lx);
    }

    // Show focus.
    public void showFocus() {
        int current = manipulator.current();
        if (current >= 0 && current < buttonPanel.getComponents().length) {
            ConnectedLexPanel but = (ConnectedLexPanel)
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

    public void allowEditing(boolean allow) {
	parent.allowEditing(allow);
	setEnabled(allow);
    }

    // Insert search into lexica.
    public void searchLexicon() {
	LxPart lx = manipulator.segment();
	if (lx != null && !lx.textal.equals("")) {
	    searchLexicon(lx.textal);
	}
    }

    // Caller overrides.
    public void searchLexicon(String al) {
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
