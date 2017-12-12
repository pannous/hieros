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
import nederhof.res.format.*;
import nederhof.res.editor.*;
import nederhof.util.*;
import nederhof.util.gui.*;

// Button for hieroglyph.
public abstract class HieroButton extends JButton {

    // The sign occurrence and connected information.
    private ImageSign sign;

    // For hieroglyphic.
    private HieroRenderContext hieroContext;
    private ParsingContext parsingContext;
    private FormatFragment frag;
    private int width;
    private int height;
    // Border size.
    private int border = 5;
    // Margins above and below hieroglyphic.
    private int marginHor = 3;
    // Margin before and after hieroglyphic.
    private int marginVert = 2;

    public HieroButton(ImageSign sign, 
	    HieroRenderContext hieroContext, ParsingContext parsingContext) {
	this.sign = sign;
	this.hieroContext = hieroContext;
	this.parsingContext = parsingContext;
	formatHiero();
	setFocus(false);
	showPlaces();
	addMouseListener(new ClickListener());
	setAlignmentY(0.75f);
    }

    // Set to new sign.
    public void reset(ImageSign sign) {
	this.sign = sign;
	formatHiero();
	showPlaces();
    }

    // Hieroglyphic name.
    private String name() {
	return sign.getName();
    }

    private void formatHiero() {
	ResFragment parsed = ResFragment.parse(name(), parsingContext);
	frag = new FormatFragment(parsed, hieroContext);
	width = frag.width() + 2 * (marginVert + border);
	height = frag.height() + 2 * (marginHor + border);
    }

    // Paint hieroglyphic.
    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	Graphics2D g2 = (Graphics2D) g;
	frag.write(g2, marginVert + border, marginHor + border);
    }

    // Listen to left or right mouse click.
    private class ClickListener extends MouseAdapter {
	public void mouseClicked(MouseEvent e) {
	    if (e.getButton() == MouseEvent.BUTTON1 && !e.isControlDown())
		askFocus(sign);
	    else if (e.getButton() == MouseEvent.BUTTON3 || e.isControlDown()) {
		askFocus(sign);
		openMenu();
	    }
	}
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

    //////////////////////////////////////////////////
    // Appearance.

    // If button has focus, change border.
    public void setFocus(boolean b) {
	if (b)
	    setBorder(BorderFactory.createLineBorder(Color.BLUE, border));
	else
	    setBorder(BorderFactory.createLineBorder(Color.WHITE, border));
	repaint();
    }

    // The number of places associated with sign changes background color.
    public void showPlaces() {
	int nPlaces = sign.getPlaces().size();
	if (nPlaces == 0)
	    setBackground(Color.WHITE);
	else if (nPlaces == 1)
	    setBackground(Color.LIGHT_GRAY);
	else 
	    setBackground(Color.GRAY);
	repaint();
    }

    //////////////////////////////////////////////////
    // Communicate to user.

    // User requests focus to this.
    protected abstract void askFocus(ImageSign sign);

    // User wants menu to change sign.
    protected abstract void openMenu();

}

