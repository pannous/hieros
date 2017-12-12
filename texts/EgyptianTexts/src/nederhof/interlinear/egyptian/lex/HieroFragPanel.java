package nederhof.interlinear.egyptian.lex;

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

// Panel with hieroglyphic.
public class HieroFragPanel extends JPanel {

    // For hieroglyphic.
    private HieroRenderContext hieroContext;
    private ParsingContext parsingContext;

    // Margin above and below hieroglyphic.
    private int marginHor = 3;
    // Margin before and after hieroglyphic.
    private int marginVert = 10;
    // Formatted RES.
    private FormatFragment frag;
    // Dimensions of hieroglyphic.
    private int width;
    private int height;

    // Constructor.
    public HieroFragPanel(String res,
		HieroRenderContext hieroContext, ParsingContext parsingContext) {
        this.hieroContext = hieroContext;
        this.parsingContext = parsingContext;
	addMouseListener(new ClickListener());
        setBackground(Color.WHITE);
        setRes(res);
    }

    // Size.
    public Dimension getPreferredSize() {
	return new Dimension(width, height);
    }
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    // Set formatted hieroglyphic.
    private void setRes(String res) {
        ResFragment parsed = ResFragment.parse(res, parsingContext);
        frag = new FormatFragment(parsed, hieroContext);
        if (frag.nGroups() > 0) {
            width = frag.width() + 2 * marginVert;
            height = frag.height() + 2 * marginHor;
        } else {
            width = 10; 
            height = 10;
        }
        revalidate();
    }
    // Get formatted hieroglyphic.
    public FormatFragment getRes() {
	return frag;
    }

    // Paint hieroglyphic if there is any.
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        frag.write(g2, marginVert, marginHor);
    }

    // Listen to clicks on hieroglyphs.
    private class ClickListener extends MouseInputAdapter {
	public void mousePressed(MouseEvent event) {
	    int clickX = event.getX() - marginVert;
	    int clickY = event.getY() - marginHor;
	    if (event.getButton() == MouseEvent.BUTTON1 &&
		    !event.isControlDown()) {
		findHieroClicked(clickX, clickY);
	    }
	}
    }

    // Find rectangle of glyph with click.
    private void findHieroClicked(int x, int y) {
	Vector<Rectangle> rects = frag.glyphRectangles();
	for (int i = 0; i < rects.size(); i++) {
	    Rectangle rect = rects.get(i);
	    if (rect.contains(x, y)) {
		reportClicked(i);
		break;
	    }
	}
    }

    ////////////////////////////////////////////////////
    // Communication with caller.

    // Report i-th sign clicked.
    public void reportClicked(int i) {
    }

}
