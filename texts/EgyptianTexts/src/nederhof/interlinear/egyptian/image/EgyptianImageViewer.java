package nederhof.interlinear.egyptian.image;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.frame.*;
import nederhof.res.*;
import nederhof.res.editor.*;
import nederhof.util.*;
import nederhof.util.gui.*;

// Viewing images and hieroglyphic text tied to images.
public class EgyptianImageViewer extends ResourceViewer
	implements ActionListener {

    // Last version when resource refreshed. Do not unnecessarily refresh.
    private int lastVersion;

    // For accessing resource.
    private ImageResourceManipulator manipulator;

    // Tabs for pages, one for each image.
    private JTabbedPane pageTabs = new JTabbedPane();

    // Images in tabs. 
    private Vector<ImagePage> pages = new Vector<ImagePage>();

    // Constructor.
    public EgyptianImageViewer(EgyptianImage resource) {
	setSize(Settings.displayWidthInit, Settings.displayHeightInit);
	setTitle("Egyptian Text/Image Viewer");
	setJMenuBar(new Menu());
	setWait(true, "initializing");
	Container content = getContentPane();
	content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

	this.resource = resource;
	lastVersion = resource.getVersion();
	manipulator = new ConnectedManipulator(resource, -1);
	content.add(pageTabs);
	fillImageTabs();
	setWait(false);

	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	addWindowListener(new CloseListener());
	setVisible(true);
	repaint();
    }

    private class ConnectedManipulator extends ImageResourceManipulator {
	public ConnectedManipulator(EgyptianImage resource, int currentSegment) {
	    super(resource, currentSegment);
	}
	public void changeFocus() {
	    EgyptianImageViewer.this.changeFocus();
	}
    }

    // Delegate to auxiliary class to keep track of
    // current segment.
    public int getCurrentSegment() {
	return manipulator.current();
    }

    // Refresh content, after (possibly) there were modifications.
    public void refresh() {
	if (resource.getVersion() != lastVersion) {
	    setWait(true, "reinitializing");
	    fillImageTabs();
	    setWait(false);
	    lastVersion = resource.getVersion();
	}
    }

    //////////////////////////////////////////////////////////////////////////////
    // Menu.

    // Distance between buttons.
    protected static final int STRUT_SIZE = 10;

    // Button showing status.
    private JButton statusButton = new JButton("");

    protected class Menu extends JMenuBar {
        public Menu() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setBackground(Color.LIGHT_GRAY);
            add(Box.createHorizontalStrut(STRUT_SIZE));

	    ClickButton fileCloseItem = new ClickButton(EgyptianImageViewer.this,
		"clo<u>S</u>e", "close", KeyEvent.VK_S);
	    JMenu resourceMenu = new EnabledMenu(
		"<u>R</u>esource", KeyEvent.VK_R);
	    JMenuItem leftItem = new EnabledMenuItem(EgyptianImageViewer.this,
		"left glyph", "left", KeyEvent.VK_LEFT);
	    JMenuItem rightItem = new EnabledMenuItem(EgyptianImageViewer.this,
		"right glyph", "right", KeyEvent.VK_RIGHT);
	    JMenuItem clearItem = new EnabledMenuItem(EgyptianImageViewer.this,
		"<u>C</u>lear", "clear", KeyEvent.VK_C);
	    ClickButton helpItem = new ClickButton(EgyptianImageViewer.this,
		"<u>H</u>elp", "help", KeyEvent.VK_H);

	    add(fileCloseItem);

	    // resource
	    add(Box.createHorizontalStrut(STRUT_SIZE));
	    add(resourceMenu);
	    resourceMenu.add(leftItem);
	    resourceMenu.add(rightItem);
	    resourceMenu.add(clearItem);

	    // help
	    add(Box.createHorizontalStrut(STRUT_SIZE));
	    add(helpItem);

	    // status
	    final int BSIZE = 5;
	    statusButton.setBorder(new EmptyBorder(BSIZE,BSIZE,BSIZE,BSIZE));
	    statusButton.setBackground(Color.LIGHT_GRAY);
	    statusButton.setFocusable(false);
	    add(Box.createHorizontalStrut(STRUT_SIZE));
	    add(statusButton);
        }
    }

    // Show message in menu.
    private void showStatus(String message) {
        showStatus(message, "gray");
    }
    // Show emphasized message in menu.
    private void showEmphasizedStatus(String message) {
        showStatus(message, "blue");
    }
    // Same but for error.
    private void showErrorStatus(String message) {
        showStatus(message, "red");
    }
    // Show message in menu in color.
    private void showStatus(String message, String color) {
        statusButton.setText("<html><font color=\"" + color + "\">" +
                message + "</font></html>");
        statusButton.setMaximumSize(statusButton.getPreferredSize());
    }

    ///////////////////////////////////////////////////
    // Tabbed pane.

    // Fill with tabs.
    private void fillImageTabs() {
	pageTabs.removeAll();
	pages.clear();
	for (int i = 0; i < manipulator.nImages(); i++) {
	    String imageFile = manipulator.imagePath(i);
	    try {
		ConnectedImagePage imagePage = 
		    new ConnectedImagePage(imageFile, manipulator);
		imagePage.setNumber(i);
		pages.add(imagePage);
		pageTabs.addTab("" + (i+1), imagePage);
	    } catch (IOException e) {
		ErrorImagePage errorPage =
		    new ErrorImagePage(imageFile, e.getMessage());
		pages.add(errorPage);
		pageTabs.addTab("" + (i+1), errorPage);
	    }
	}
	pageTabs.revalidate();
    }

    private class ConnectedImagePage extends ZoomImagePage {
	public ConnectedImagePage(String imageFile, ImageResourceManipulator manipulator) 
	    		throws IOException {
	    super(readImageWithCheck(imageFile), manipulator);
	}
    }

    // Read from file with extra check.
    private BufferedImage readImageWithCheck(String fileName) throws IOException {
	URL url = FileAux.fromBase(fileName);
        if (url == null)
            throw new IOException("Reading failed of: " + fileName);
        BufferedImage im = ImageIO.read(url);
        if (im == null)
            throw new IOException("Reading failed. Is image type acceptable?");
        return im;
    }

    // Placeholder for tab with image, when image couldn't be loaded.
    private class ErrorImagePage extends JTextPane implements ImagePage {
	public ErrorImagePage(String fileName, String message) {
	    setEditable(false);
	    setContentType("text/html");
	    fill(fileName, message);
	}
	private void fill(String fileName, String message) {
	    StringBuffer buf = new StringBuffer("<html><body>");
	    buf.append("<p>Image " + fileName + " could not be loaded:</p>");
	    buf.append("<p><i>" + message + "</i></p>");
	    buf.append("</body></html>");
	    setText(buf.toString());
	}
	public void dispose() { }
    }

    // Return page of current tab. Or null if none.
    private ZoomImagePage currentPage() {
        if (pageTabs == null)
            return null;
        Component select = pageTabs.getSelectedComponent();
        if (select instanceof ZoomImagePage)
            return (ZoomImagePage) select;
        else
            return null;
    }

    // Change focus in images.
    private void changeFocus() {
	for (ImagePage p : pages) 
	    if (p instanceof ZoomImagePage)
		((ZoomImagePage) p).changeFocus();
	flipPageToFocus();
	if (manipulator != null)
	    reportFocus(manipulator.current());
    }

    // Set focus to sign.
    public void setFocus(int i) {
	manipulator.setCurrent(i);
	for (ImagePage p : pages) 
	    if (p instanceof ZoomImagePage)
		((ZoomImagePage) p).showFocusRect();
	flipPageToFocus();
    }
    // Switch to page that has focus sign, if not current page.
    private void flipPageToFocus() {
	if (currentPage() == null || !currentPage().hasFocusRect())
	    for (ImagePage p : pages)
		if (p instanceof ZoomImagePage) {
		    ZoomImagePage page = (ZoomImagePage) p;
		    if (page.hasFocusRect()) {
			pageTabs.setSelectedComponent(page);
		    }
		}
    }
    // Set focus to no sign.
    public void setNoFocus() {
	manipulator.setNoCurrent();
    }

    /////////////////////////////////////////////////////
    // Listeners.

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("close")) {
	    setState(Frame.ICONIFIED);
        } else if (e.getActionCommand().equals("left")) {
	    manipulator.left();
	    if (currentPage() != null)
		currentPage().showFocusRect();
        } else if (e.getActionCommand().equals("right")) {
	    manipulator.right();
	    if (currentPage() != null)
		currentPage().showFocusRect();
        } else if (e.getActionCommand().equals("clear")) {
	    setNoFocus();
        } else if (e.getActionCommand().equals("help")) {
	    openHelp();
	} 
    }

    // Send action to selected page.
    private void pageAction(ActionEvent e) {
        ZoomImagePage p = currentPage();
        if (p != null)
            p.actionPerformed(e);
    }

    ///////////////////////////////////////////////////
    // Help.

    // There may be auxiliary help window.
    protected JFrame helpWindow = null;

    /**
     * Open help window if not already open.
     */
    private void openHelp() {
        if (helpWindow == null) {
            URL url = FileAux.fromBase("data/help/text/image_view.html");
            helpWindow = new HTMLWindow("Text/image viewer manual", url);
        }
        helpWindow.setVisible(true);
    }

    ///////////////////////////////////////////////////
    // Closing.

    // Kill all windows, and exit.
    public void dispose() {
        super.dispose();
        if (helpWindow != null)
            helpWindow.dispose();
        for (ImagePage page : pages)
            page.dispose();
    }

    // Listen if window to be closed or iconified.
    private class CloseListener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
	    setState(Frame.ICONIFIED);
        }
        public void windowIconified(WindowEvent e) {
            setState(Frame.ICONIFIED);
        }
        public void windowDeiconified(WindowEvent e) {
            setState(Frame.NORMAL);
        }
    }

    ///////////////////////////////////////////////////
    // Appearance.

    // Set wait cursor with message.
    public void setWait(boolean wait, String message) {
        if (wait) {
	    showEmphasizedStatus(message);
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
	} else {
	    showStatus(message);
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
    }
    // Set wait cursor without message.
    public void setWait(boolean wait) {
	setWait(wait, "");
    }

    ///////////////////////////////////////////////////////
    // For testing.

    public static void main(String[] args) {
	try {
	    EgyptianImage resource = new EgyptianImage("corpus/resources/urkIV-001Im.xml");
	    EgyptianImageViewer viewer = new EgyptianImageViewer(resource);
	    for (int i = 0; i < viewer.manipulator.nSigns(); i++) {
		viewer.setFocus(i);
		// Thread.sleep(2);
	    }
	    viewer.refresh();
	    viewer.refresh();
	    viewer.refresh();
	    viewer.refresh();
	    viewer.refresh();
	    viewer.refresh();
	    viewer.refresh();
	    viewer.refresh();
	    viewer.refresh();
	    viewer.refresh();
	    viewer.refresh();
	    viewer.refresh();
	    viewer.refresh();
	    viewer.refresh();
	    viewer.refresh();
	    viewer.refresh();
	    viewer.refresh();
	    viewer.refresh();
	} catch (IOException e) {
	    System.err.println("error: " + e.getClass().getName());
	    System.err.println(e.getMessage());
	}
    }

}
