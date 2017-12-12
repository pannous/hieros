package nederhof.interlinear.egyptian.image;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;

// Zoomable image, with separate settings window.
public class ZoomImagePageSettings extends ZoomImagePage
	implements ComponentListener, ActionListener {

    // Auxiliary frame for settings.
    private SettingsWindow settingsWindow;

    // Constructor.
    public ZoomImagePageSettings(BufferedImage image, ImageResourceManipulator manipulator) {
	super(image, manipulator);
    }

    // Overrides superclass.
    protected void setInitialMode() {
	mouseMode = MouseMode.MOVE;
	setCursor();
	makeSettings();
    }

    //////////////////////////////////////////////
    // Settings panel.

    // Panel with settings.
    private void makeSettings() {
	settingsWindow = new SettingsWindow() {
	    public void reportBilevel(boolean b) {
		ZoomImagePageSettings.this.setBilevel(b);
	    }
	    public void reportMove() {
		ZoomImagePageSettings.this.setMove();
	    }
	    public void reportRect() {
		ZoomImagePageSettings.this.setRect();
	    }
	    public void reportPoly() {
		ZoomImagePageSettings.this.setPoly();
	    }
	    public void reportTagging() {
		ZoomImagePageSettings.this.setTagging();
	    }
	    public void reportComponents(boolean b) {
		ZoomImagePageSettings.this.setComponents(b);
	    }
	    public void reportDirection(String dir) {
		ZoomImagePageSettings.this.setDirection(dir);
	    }
	    public void reportColorChange(int t, int r, int g, int b) {
		ZoomImagePageSettings.this.changeColor(t, r, g, b);
	    }
	    public void reportComponentChange(int connected, int size) {
		ZoomImagePageSettings.this.changeComponent(connected, size);
	    }
	};
    }

    // Set setting visual mode.
    private void setBilevel(boolean b) {
	if (b) 
	    settingsWindow.reportAll();
	bilevel = b;
	repaint();
    }

    // For navigating with mouse movements.
    private void setMove() {
	mouseMode = MouseMode.MOVE;
	setCursor();
	repaint();
    }
    // For drawing rectangles.
    private void setRect() {
	mouseMode = MouseMode.RECT;
	setCursor();
	repaint();
    }
    // For drawing polygons.
    private void setPoly() {
	mouseMode = MouseMode.POLY;
	setCursor();
	repaint();
    }
    // For tagging areas.
    private void setTagging() {
	mouseMode = MouseMode.TAG;
	setCursor();
	repaint();
    }
    // Set cursor appropriate for mouse mode.
    private void setCursor() {
	if (mouseMode == MouseMode.MOVE)
	    imagePanel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
	else if (mouseMode == MouseMode.RECT)
	    imagePanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	else 
	    imagePanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    // For drawing candidate rectangles or not.
    private void setComponents(boolean b) {
	showComponents = b;
	repaint();
    }

    // Set direction.
    private void setDirection(String dir) {
	direction = dir;
	ComponentFinder finder = new ComponentFinder(componentConnect,
		componentSize);
	components = finder.order(components, direction);
    }

    // Change color.
    private void changeColor(int t, int r, int g, int b) {
	if (t == threshold && r == rWeight && g == gWeight & b == bWeight)
	    return;
	stopBilevelMaker();
	threshold = t;
	rWeight = r;
	gWeight = g;
	bWeight = b;
	bilevelImage = null;
	if (polyDone)
	    findComponents();
	imagePanel.repaint();
    }

    // Change the way components are recognized.
    private void changeComponent(int connected, int size) {
	componentConnect = connected;
	componentSize = size;
	if (polyDone) 
	    findComponents();
	imagePanel.repaint();
    }

    // Upon showing tab, also show settings window.
    public void showPage(boolean b, JFrame parent) {
	settingsWindow.setVisible(b);
	if (b)
	    settingsWindow.setLocationFirstTime(parent);
    }

    ////////////////////////////////////////////
    // Cleaning up.

    // Kill auxiliary windows.
    public void dispose() {
	settingsWindow.dispose();
    }

    //////////////////////////////////////////////
    // Listeners.

    // ComponentListener
    public void componentHidden(ComponentEvent e) {}
    public void componentMoved(ComponentEvent e) {}
    public void componentResized(ComponentEvent e) {
	makeFresh();
    }
    public void componentShown(ComponentEvent e) {}

    // ActionListener
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("color"))
	    settingsWindow.setBilevel(false);
	else if (e.getActionCommand().equals("bilevel"))
	    settingsWindow.setBilevel(true);
	else if (e.getActionCommand().equals("move"))
	    settingsWindow.setMove();
	else if (e.getActionCommand().equals("rect"))
	    settingsWindow.setRect();
	else if (e.getActionCommand().equals("poly"))
	    settingsWindow.setPoly();
	else if (e.getActionCommand().equals("tagging"))
	    settingsWindow.setTagging();
	else if (e.getActionCommand().equals("show"))
	    settingsWindow.toggleComponents();
	else if (e.getActionCommand().equals("hlr") ||
		e.getActionCommand().equals("hrl") ||
		e.getActionCommand().equals("vlr") ||
		e.getActionCommand().equals("vrl"))
	    settingsWindow.setDirection(e.getActionCommand());
	else
	    super.actionPerformed(e);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Appearance.

    // Set cursor.
    protected void setBusy(boolean busy) {
        if (busy) {
            imagePanel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        } else {
            setCursor();
	}
	settingsWindow.setBusy(busy);
    }

    //////////////////////////////////////////////////////////////////////////////

    // For testing.
    public static void main(String[] args) {
	if (args.length == 0) 
	    System.exit(1);

	final String filename = args[0];

	JFrame frame = new JFrame();
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	ZoomImagePageSettings panel = null;
	try {
	    final BufferedImage image = ImageIO.read(new File(filename));
	    panel = new ZoomImagePageSettings(image, null);
	    panel.showPage(true, frame); 
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(null, e.getMessage(), "",
		JOptionPane.ERROR_MESSAGE);
	    System.exit(1);
	}

	frame.getContentPane().add(panel, BorderLayout.CENTER);
	frame.setSize(new Dimension(300, 500));
	frame.setVisible(true);
    }
}
