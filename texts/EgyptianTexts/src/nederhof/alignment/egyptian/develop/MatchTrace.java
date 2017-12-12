/***************************************************************************/
/*                                                                         */
/*  MatchTrace.java                                                        */
/*                                                                         */
/*  Copyright (c) 2009 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of PhilologEG, and may only be */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Trace of matching process. Only for debugging.

package nederhof.alignment.egyptian.develop;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import nederhof.alignment.egyptian.*;
import nederhof.alignment.*;
import nederhof.fonts.*;
import nederhof.res.*;
import nederhof.util.*;

public class MatchTrace extends JFrame implements ActionListener {
    /*

    private JLabel statistics = new JLabel("-");

    // The content, representing the trace.
    private JPanel trace = new JPanel();

    // Size of window.
    private int width = 700;
    private int height = 700;

    // Renderer for hieroglyphic.
    private int hieroFontSize = 30;
    private HieroRenderContext hieroContext = new HieroRenderContext(hieroFontSize);

    public MatchTrace() {
	setTitle("Trace");
	setJMenuBar(new QuitMenu(this));
	setSize(width, height);
	Container content = getContentPane();
	content.setLayout(new BorderLayout());
	content.add(statistics, BorderLayout.NORTH);
	trace.setLayout(new BoxLayout(trace, BoxLayout.Y_AXIS));
	JScrollPane scroll = new JScrollPane(trace);
	content.add(scroll, BorderLayout.CENTER);
	addWindowListener(new Listener());
	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    // Listen if window to be closed. Merely make invisible.
    private class Listener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            setVisible(false);
        }
    }
    */

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("quit"))
            setVisible(false);
    }

    /*
    // Fill window with statistics.
    public void putStatistics(int wordStarts, int found, int notFound, int wrong) {
	statistics.setText("<html>&nbsp;" +
		"total words: <b>" + wordStarts + "</b>; " +
		"found: <font color=green>" + found + "</font>; " +
		"not found: <font color=blue>" + notFound + "</font>; " +
		"wrong: <font color=red>" + wrong + "</font></html>");
    }

    // Put line saying no trace.
    public void putNotFound() {
	JLabel line = new JLabel("<html>" +
		"<font color=\"red\">State not found; glyph part of group?</font>" +
		"</html>");
	trace.add(line);
	trace.validate();
	trace.repaint();
    }

    // Put one line of trace.
    public void putTraceLine(String res, WordMatch word, String action, 
	    double penalty, String allActions, boolean focus) {
	JPanel line = new JPanel();
	line.setLayout(new BoxLayout(line, BoxLayout.X_AXIS));

	JButton hiero = new GlyphLabel(res, focus);
	line.add(hiero);

	DecimalFormat penaltyFormat = new DecimalFormat("#.#");
	String penaltyString;
	if (penalty == 0)
	    penaltyString = "";
	else
	    penaltyString = " [" + penaltyFormat.format(penalty) + "]";
	JButton act = new ActionLabel(action + penaltyString);
	line.add(act);

	String wordString = "-";
	if (word != null)
	    wordString = word.toHtmlString();
	JButton wordLabel = new WordLabel(wordString);
	line.add(wordLabel);

	JButton actionsLabel = new ActionsLabel(allActions);
	line.add(actionsLabel);

	line.add(Box.createHorizontalGlue());
	trace.add(line);
	trace.invalidate();
    }

    // Maximum dimension of glyphs and glyph names in labels.
    private int maxWidthGlyph = 5;
    private int maxHeightGlyph = 5;
    private int maxWidthName = 5;
    private int maxHeightName = 5;

    // Maximum width of word.
    private int maxWidthWord = 5;

    // Maximum width of action and actions.
    private int maxWidthAction = 5;
    private int maxWidthActions = 5;

    // The dimension of the button covered by glyph.
    // An extra factor provides a margin.
    private static final float margin = 0.125f;
    private int glyphPartWidth() {
	return Math.round(maxWidthGlyph * (1 + 2 * margin));
    }
    private int glyphPartHeight() {
	return Math.round(maxHeightGlyph * (1 + 2 * margin));
    }

    // Font for glyph name.
    private static Font textFont = new Font("SansSerif", Font.BOLD, 14);

    // We have to get a graphics from somewhere, so we create a dummy image.
    private static BufferedImage dummyImage =
	new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
    private static Graphics dummyGraphics =
	dummyImage.createGraphics();

    // Label containing glyph.
    private class GlyphLabel extends JButton {
	// Name of glyph, also used as a small piece of RES.
	private String text;

	// Width of string in pixels.
	private int labelWidth(String text) {
	    if (text == null)
		return 0;
	    else
		return dummyGraphics.getFontMetrics(textFont).stringWidth(text);
	}
	// Experience has shown that ascent gives better results than height.
	private int labelHeight(String text) {
	    if (text == null)
		return 0;
	    else
		return dummyGraphics.getFontMetrics(textFont).getAscent();
	}

        public GlyphLabel(String text, boolean focus) {
            this.text = text;
	    if (focus)
		setBackground(Color.LIGHT_GRAY);
	    else
		setBackground(Color.WHITE);
            setFocusable(false);
	    if (text == null)
		return;
            ResDivision div = getDiv();
            maxWidthGlyph = Math.max(maxWidthGlyph, div.getWidthPixels());
            maxHeightGlyph = Math.max(maxHeightGlyph, div.getHeightPixels());
            maxWidthName = Math.max(maxWidthName, labelWidth(text));
            maxHeightName = Math.max(maxHeightName, labelHeight(text));
        }

        // Paint glyph and name of glyph.
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
	    if (text == null)
		return;
            Graphics2D g2 = (Graphics2D) g;
            ResDivision div = getDiv();
            div.write(g2, (int) (maxWidthGlyph * margin),
                    (int) (maxHeightGlyph * margin));
            g2.setFont(textFont);
            g2.setColor(Color.BLACK);
            g2.drawString(text, glyphPartWidth(),
                    glyphPartHeight() * 0.5f + maxHeightName / 2.0f);
        }

        // Extra pixels to separate name from right border.
        public Dimension getPreferredSize() {
            return new Dimension(glyphPartWidth() + maxWidthName + 3,
                    glyphPartHeight());
        }

	public Dimension getMinimumSize() {
            return new Dimension(glyphPartWidth() + maxWidthName + 3,
                    glyphPartHeight());
	}

	public Dimension getMaximumSize() {
            return new Dimension(glyphPartWidth() + maxWidthName + 3,
                    glyphPartHeight());
	}

        // Get division for hieroglyph.
        private ResDivision getDiv() {
            RES res = RES.createRES(text, hieroContext);
            return new ResDivision(res, hieroContext);
        }

    }

    // Label containing word.
    private class WordLabel extends JButton {

	public WordLabel(String text) {
	    super(text);
	    setBackground(Color.WHITE);
	    setFocusable(false);
            maxWidthWord = Math.max(maxWidthWord, super.getPreferredSize().width);
	}

	public Dimension getPreferredSize() {
	    return new Dimension(maxWidthWord, glyphPartHeight());
	}
	public Dimension getMinimumSize() {
	    return new Dimension(maxWidthWord, glyphPartHeight());
	}
	public Dimension getMaximumSize() {
	    return new Dimension(maxWidthWord, glyphPartHeight());
	}
    }

    // Label containing action.
    private class ActionLabel extends JButton {

	public ActionLabel(String text) {
	    super(text);
	    setBackground(Color.WHITE);
	    setFocusable(false);
            maxWidthAction = Math.max(maxWidthAction, super.getPreferredSize().width);
	}

	public Dimension getPreferredSize() {
	    return new Dimension(maxWidthAction, glyphPartHeight());
	}
	public Dimension getMinimumSize() {
	    return new Dimension(maxWidthAction, glyphPartHeight());
	}
	public Dimension getMaximumSize() {
	    return new Dimension(maxWidthAction, glyphPartHeight());
	}
    }

    // Label containing actions.
    private class ActionsLabel extends JButton {

	public ActionsLabel(String text) {
	    super(text);
	    setBackground(Color.WHITE);
	    setFocusable(false);
            maxWidthActions = Math.max(maxWidthActions, super.getPreferredSize().width);
	}

	public Dimension getPreferredSize() {
	    Dimension dim = super.getPreferredSize();
	    return new Dimension(dim.width, glyphPartHeight());
	}
	public Dimension getMinimumSize() {
	    Dimension dim = super.getPreferredSize();
	    return new Dimension(dim.width, glyphPartHeight());
	}
	public Dimension getMaximumSize() {
	    Dimension dim = super.getPreferredSize();
	    return new Dimension(dim.width, glyphPartHeight());
	}
    }

    // Remove trace.
    public void clearTrace() {
	trace.removeAll();
	maxWidthGlyph = 5;
	maxHeightGlyph = 5;
	maxWidthName = 5;
	maxHeightName = 5;
	maxWidthWord = 5;
	maxWidthAction = 5;
	maxWidthActions = 5;
    }
    */
}
