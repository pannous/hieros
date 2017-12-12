package nederhof.interlinear.egyptian.lex;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.frame.*;
import nederhof.res.*;
import nederhof.res.operations.*;

// Contains hieroglyphic.
public class HierosPanel extends JPanel {

    // For manipulating resource.
    private LexicoManipulator manipulator;

    // For hieroglyphic. (Unique instances.)
    protected static HieroRenderContext hieroContext =
	new HieroRenderContext(Settings.textHieroFontSize, true);
    protected static ParsingContext parsingContext = 
	new ParsingContext(hieroContext, true);

    // One subpanel for each RES fragment.
    private JPanel fragmentPanel;
    // Scroller thereof.
    private SimpleHorScroller scroller;

    // Constructor.
    public HierosPanel(LexicoManipulator manipulator) {
        this.manipulator = manipulator;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));        
	fragmentPanel = new JPanel();
        fragmentPanel.setLayout(new BoxLayout(fragmentPanel, BoxLayout.X_AXIS));
        scroller = new SimpleHorScroller(fragmentPanel);
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
    // Fragments in panel and their appearance.

    // Complete (re)filling with entries.
    public void refresh() {
        fragmentPanel.removeAll();
        for (int i = 0; i < manipulator.nFragments(); i++) {
	    String frag = manipulator.fragment(i);
            fragmentPanel.add(new ConnectedHiPanel(frag));
        }
        fragmentPanel.add(Box.createHorizontalGlue());
        revalidate();
        repaint();
    }

    private class ConnectedHiPanel extends HieroFragPanel {
	public ConnectedHiPanel(String hi) {
	    super(hi, hieroContext, parsingContext);
	}
	public void reportClicked(int i) {
	    splitOffToClicked(this, i);
	}
    }

    ////////////////////////////////////////////////////////////
    // Manipulation.

    private void splitOffToClicked(ConnectedHiPanel clickedPanel, 
	    int clickedGlyph) {
	ResComposer comp = new ResComposer();
	NormalizerRemoveWhite whiteRemover = new NormalizerRemoveWhite();
	ResFragment prefFrag = new ResFragment();
	int prefLen = 0;
	Component[] comps = fragmentPanel.getComponents();
	for (int i = 0; i < comps.length && comps[i] != clickedPanel; i++) {
	    prefLen++;
	    if (comps[i] instanceof ConnectedHiPanel) {
		ConnectedHiPanel prefPanel = (ConnectedHiPanel) comps[i];
		prefFrag = comp.append(prefFrag, prefPanel.getRes());
	    }
	}
	if (prefLen < comps.length && comps[prefLen] instanceof ConnectedHiPanel) {
	    ConnectedHiPanel clicked = (ConnectedHiPanel) comps[prefLen];
	    ResFragment prefix = clicked.getRes().prefixGlyphs(clickedGlyph+1);
	    prefix = whiteRemover.normalize(prefix);
	    prefFrag = comp.append(prefFrag, prefix);
	    splitOff(prefFrag, prefLen, clickedGlyph+1);
	}
    }

    ////////////////////////////////////////////////////////////
    // Communication with caller.

    // Caller to override.
    // Offers piece of hieroglyphic. Caller to return
    // whether this is accepted.
    public boolean splitOff(ResFragment prefFrag, int prefPanels, int prefGlyphs) {
	return true;
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
