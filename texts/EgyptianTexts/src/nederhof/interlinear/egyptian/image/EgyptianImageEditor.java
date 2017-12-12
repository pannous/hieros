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

// Editing images and hieroglyphic text tied to images.
public class EgyptianImageEditor extends ResourceEditor {

    // Auxiliary class for manipulating resource.
    private ImageResourceManipulator manipulator;

    // For hieroglyphic. (Unique instances.)
    protected static HieroRenderContext hieroContext = null;
    protected static ParsingContext parsingContext = null;

    // Auxiliary frame for choosing hieroglyphs.
    private GlyphChooser chooser = null;

    // Panel with buttons for hieroglyphs.
    private HieroPanel hieroPanel;

    // Tabs for pages, one for each image.
    private JTabbedPane pageTabs;

    // Images in tabs. Cached.
    private TreeMap<String,ImagePage> pages = new TreeMap<String,ImagePage>();

    // The component of the current tab.
    private Component currentTab = null;

    // Constructor.
    public EgyptianImageEditor(EgyptianImage resource, int currentSegment) {
	super(resource, currentSegment);
	setSize(Settings.displayWidthInit, Settings.displayHeightInit);
	extendMenu();
	setWait(true, "initializing");
	Container content = getContentPane();
	content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

	if (hieroContext == null) { // do only once
	    hieroContext = new HieroRenderContext(Settings.textHieroFontSize, true);
	    parsingContext = new ParsingContext(hieroContext, true);
	}
	manipulator = new ConnectedManipulator(resource, currentSegment);

	addHieroPanel();
	addImageTabs();
	setWait(false);
	addListeners();
	setVisible(true);
	allowEditing(true);
	repaint();
	hieroPanel.scrollToFocus();
    }

    private class ConnectedManipulator extends ImageResourceManipulator {
	public ConnectedManipulator(EgyptianImage resource, int currentSegment) {
	    super(resource, currentSegment);
	}
	public void recordChange() {
	    EgyptianImageEditor.this.recordChange();
	}
	public void showFocus() {
	    EgyptianImageEditor.this.showFocus();
	}
	public void unshowFocus() {
	    EgyptianImageEditor.this.unshowFocus();
	}
	public void scrollToFocus() {
	    EgyptianImageEditor.this.scrollToFocus();
	}
	public void changeFocus() {
	    EgyptianImageEditor.this.changeFocus();
	}
	public void refresh() {
	    EgyptianImageEditor.this.refresh();
	}
	public void refresh(int i, ImageSign sign) {
	    EgyptianImageEditor.this.refresh(i, sign);
	}
	public void refresh(ImageSign sign) {
	    EgyptianImageEditor.this.refresh(sign);
	}
	public void removeButton(int i) {
	    EgyptianImageEditor.this.removeButton(i);
	}
	public void addButton(int i, ImageSign sign) {
	    EgyptianImageEditor.this.addButton(i, sign);
	}
    }

    // Get the name of the editor.
    public String getName() {
	return "Egyptian Text/Image Editor";
    }

    // Delegate to auxiliary class to keep track of
    // current segment (overrides superclass).
    public int getCurrentSegment() {
	return manipulator.current();
    }

    //////////////////////////////////////////////////////////////////////////////
    // Menu.

    // Button showing status.
    private JButton statusButton = new JButton("");

    // Add more buttons to menu.
    private void extendMenu() {
	JMenuItem importItem = new EnabledMenuItem(this,
	    "<u>I</u>mport", "import", KeyEvent.VK_I);
	JMenu resourceMenu = new EnabledMenu(
	    "<u>R</u>esource", KeyEvent.VK_R);
	JMenuItem imageItem = new EnabledMenuItem(this,
	    "<u>A</u>ppend image", "append", KeyEvent.VK_A);
	JMenuItem cutItem = new EnabledMenuItem(this,
	    "<u>C</u>ut image", "cut", KeyEvent.VK_C);
	JMenuItem leftItem = new EnabledMenuItem(this,
	    "left glyph", "left", KeyEvent.VK_LEFT);
	JMenuItem rightItem = new EnabledMenuItem(this,
	    "right glyph", "right", KeyEvent.VK_RIGHT);
	JMenuItem newItem = new EnabledMenuItem(this,
	    "<u>N</u>ew glyph", "new", KeyEvent.VK_N);
	JMenuItem initialItem = new EnabledMenuItem(this,
	    "new initial glyph", "initial", KeyEvent.VK_0);
	JMenuItem otherItem = new EnabledMenuItem(this,
	    "<u>O</u>ther glyph", "other", KeyEvent.VK_O);
	JMenuItem deleteItem = new EnabledMenuItem(this,
	    "<u>D</u>elete glyph", "delete", KeyEvent.VK_D);
	JMenuItem swapItem = new EnabledMenuItem(this,
	    "s<u>W</u>ap glyphs", "swap", KeyEvent.VK_W);
	JMenu modeMenu = new EnabledMenu(
	    "<u>M</u>ode", KeyEvent.VK_M);
	JMenuItem colorItem = new EnabledMenuItem(this,
	    "co<u>L</u>or", "color", KeyEvent.VK_L);
	JMenuItem bilevelItem = new EnabledMenuItem(this,
	    "<u>B</u>ilevel", "bilevel", KeyEvent.VK_B);
	JMenuItem moveItem = new EnabledMenuItem(this,
	    "mo<u>V</u>e", "move", KeyEvent.VK_V);
	JMenuItem rectItem = new EnabledMenuItem(this,
	    "r<u>E</u>ctangle", "rect", KeyEvent.VK_E);
	JMenuItem polyItem = new EnabledMenuItem(this,
	    "<u>P</u>oly", "poly", KeyEvent.VK_P);
	JMenuItem taggingItem = new EnabledMenuItem(this,
	    "tagging", "tagging", KeyEvent.VK_X);
	JMenuItem componentsItem = new EnabledMenuItem(this,
	    "componen<u>T</u>s", "show", KeyEvent.VK_T);
	JMenuItem hlrItem = new EnabledMenuItem(this,
	    "hlr", "hlr", KeyEvent.VK_1);
	JMenuItem hrlItem = new EnabledMenuItem(this,
	    "hrl", "hrl", KeyEvent.VK_2);
	JMenuItem vlrItem = new EnabledMenuItem(this,
	    "vlr", "vlr", KeyEvent.VK_3);
	JMenuItem vrlItem = new EnabledMenuItem(this,
	    "vrl", "vrl", KeyEvent.VK_4);
	JMenuItem upItem = new EnabledMenuItem(this,
	    "previous rectangle", "up", KeyEvent.VK_UP);
	JMenuItem downItem = new EnabledMenuItem(this,
	    "next rectangle", "down", KeyEvent.VK_DOWN);
	JMenuItem acceptItem = new EnabledMenuItem(this,
	    "o<u>K</u>ay", "accept", KeyEvent.VK_K);
	ClickButton settingsItem = new ClickButton(this,
            "settin<u>G</u>s", "settings", KeyEvent.VK_G);

	fileMenu.add(importItem);

	// resource
	menu.add(Box.createHorizontalStrut(STRUT_SIZE));
	menu.add(resourceMenu);
	resourceMenu.add(imageItem);
	resourceMenu.add(cutItem);
	resourceMenu.addSeparator();
	resourceMenu.add(leftItem);
	resourceMenu.add(rightItem);
	resourceMenu.add(newItem);
	resourceMenu.add(initialItem);
	resourceMenu.add(otherItem);
	resourceMenu.add(deleteItem);
	resourceMenu.add(swapItem);
	resourceMenu.addSeparator();
	resourceMenu.add(colorItem);
	resourceMenu.add(bilevelItem);
	resourceMenu.add(moveItem);
	resourceMenu.add(rectItem);
	resourceMenu.add(polyItem);
	resourceMenu.add(taggingItem);
	resourceMenu.add(componentsItem);
	resourceMenu.add(hlrItem);
	resourceMenu.add(hrlItem);
	resourceMenu.add(vlrItem);
	resourceMenu.add(vrlItem);
	resourceMenu.addSeparator();
	resourceMenu.add(upItem);
	resourceMenu.add(downItem);
	resourceMenu.add(acceptItem);

	// modes
	menu.add(Box.createHorizontalStrut(STRUT_SIZE));
	menu.add(modeMenu);
	modeMenu.add(colorItem);
	modeMenu.add(bilevelItem);
	modeMenu.add(moveItem);
	modeMenu.add(rectItem);
	modeMenu.add(polyItem);
	modeMenu.add(taggingItem);
	modeMenu.add(componentsItem);
	modeMenu.add(hlrItem);
	modeMenu.add(hrlItem);
	modeMenu.add(vlrItem);
	modeMenu.add(vrlItem);
	resourceMenu.addSeparator();
	modeMenu.add(upItem);
	modeMenu.add(downItem);
	modeMenu.add(acceptItem);

	// settings
	menu.add(Box.createHorizontalStrut(STRUT_SIZE));
	menu.add(settingsItem);

	variableJComponents.add(importItem);
	variableJComponents.add(resourceMenu);
	variableJComponents.add(imageItem);
	variableJComponents.add(cutItem);
	variableJComponents.add(leftItem);
	variableJComponents.add(rightItem);
	variableJComponents.add(newItem);
	variableJComponents.add(initialItem);
	variableJComponents.add(otherItem);
	variableJComponents.add(deleteItem);
	variableJComponents.add(swapItem);

	// help
	menu.add(Box.createHorizontalStrut(STRUT_SIZE));
	menu.add(new ClickButton(this,
		    "<u>H</u>elp", "help", KeyEvent.VK_H));

	// status
	final int BSIZE = 5;
	statusButton.setBorder(new EmptyBorder(BSIZE,BSIZE,BSIZE,BSIZE));
	statusButton.setBackground(Color.LIGHT_GRAY);
	statusButton.setFocusable(false);
	menu.add(Box.createHorizontalStrut(STRUT_SIZE));
	menu.add(statusButton);
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
    // Hiero panel.

    // Adding panel with glyphs.
    private void addHieroPanel() {
	Container content = getContentPane();
	hieroPanel = new ConnectedHieroPanel();
	content.add(hieroPanel);
    }

    private class ConnectedHieroPanel extends HieroPanel {
	public ConnectedHieroPanel() {
	    super(hieroContext, parsingContext, manipulator);
	}
	public void openMenu() {
	    findGlyph();
	}
    }

    // Propagate change of focus to panel (panel may not exist yet).
    public void showFocus() {
	if (hieroPanel != null)
	    hieroPanel.showFocus();
    }

    // Take away visible focus.
    public void unshowFocus() {
	if (hieroPanel != null)
	    hieroPanel.unshowFocus();
    }

    // Scroll to focus.
    public void scrollToFocus() {
	if (hieroPanel != null)
	    hieroPanel.scrollToFocus();
    }

    // Refresh (after edit to resource).
    public void refresh() {
	if (hieroPanel != null)
	    hieroPanel.refresh();
    }
    // Refresh (after edit to signs).
    public void refresh(int i, ImageSign sign) {
	if (hieroPanel != null)
	    hieroPanel.refresh(i, sign);
    }
    // Refresh current sign only.
    public void refresh(ImageSign sign) {
	if (hieroPanel != null)
	    hieroPanel.refresh(sign);
    }
    // Remove buttons for sign.
    public void removeButton(int i) {
	if (hieroPanel != null)
	    hieroPanel.removeButton(i);
    }
    // Add buttons for sign.
    public void addButton(int i, ImageSign sign) {
	if (hieroPanel != null)
	    hieroPanel.addButton(i, sign);
    }

    ///////////////////////////////////////////////////
    // Tabbed pane.

    // Make pages in tabs.
    private void addImageTabs() {
	Container content = getContentPane();
	pageTabs = new JTabbedPane();
	content.add(pageTabs);
	fillImageTabs();
    }

    // Fill with tabs anew. Use existing components if possible.
    private void fillImageTabs() {
	int oldTab = currentTab();
	pageTabs.removeAll();
	ImageIndex index = new ImageIndex();
	pageTabs.addTab("index", index);
	for (int i = 0; i < manipulator.nImages(); i++) {
	    String imageFile = manipulator.imagePath(i);
	    ImagePage page = pages.get(imageFile);
	    if (page == null) {
		try {
		    ConnectedImagePage imagePage = 
			new ConnectedImagePage(imageFile, manipulator);
		    pages.put(imageFile, imagePage);
		    pageTabs.addTab("" + (i+1), imagePage);
		} catch (IOException e) {
		    ErrorImagePage errorPage =
			new ErrorImagePage(imageFile, e.getMessage());
		    pages.put(imageFile, errorPage);
		    pageTabs.addTab("" + (i+1), errorPage);
		}
	    } else 
		pageTabs.addTab("" + (i+1), (Component) page);
	}
	if (oldTab+1 >= 0 && oldTab+1 < pageTabs.getTabCount())
	    pageTabs.setSelectedIndex(oldTab+1);
	pageTabs.revalidate();
    }

    // Remove image. Also dispose of component.
    private void removeImage(int i) {
	if (manipulator.imageHasPlaces(i) &&
		!userConfirmsLoss("Remove image and its places?"))
	    return;
	String imageFile = manipulator.imagePath(i);
	if (imageFile == null)
	    return;
	ImagePage page = pages.get(imageFile);
	manipulator.cutImage(i);
	if (page != null) {
	    page.dispose();
	    pages.remove(imageFile);
	}
	fillImageTabs();
	hieroPanel.reshowPlaces();
    }

    // List with all images.
    private class ImageIndex extends JTextPane {
	public ImageIndex() {
	    setEditable(false);
	    setContentType("text/html");
	    fill();
	}
	// Fill index with image addresses.
	private void fill() {
	    StringBuffer buf = new StringBuffer("<html><body>");
	    if (manipulator.nImages() == 0)
		buf.append("No images imported");
	    else {
		buf.append("Imported images: <ul>");
		for (int i = 0; i < manipulator.nImages(); i++) 
		    buf.append("<li>" + manipulator.imagePath(i) + "</li>");
		buf.append("</ul>");
	    }
	    buf.append("</body></html>");
	    setText(buf.toString());
	}
    }

    private class ConnectedImagePage extends ZoomImagePageSettings {
	public ConnectedImagePage(String imageFile, ImageResourceManipulator manipulator) 
	    		throws IOException {
	    super(readImageWithCheck(new File(imageFile)), manipulator);
	}
    }

    // Read from file with extra check.
    private BufferedImage readImageWithCheck(File file) throws IOException {
	BufferedImage im = ImageIO.read(file);
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

    // Get number of current tab.
    private int currentTab() {
	return pageTabs.getSelectedIndex();
    }

    // Return page of current tab. Or null if none.
    private ZoomImagePageSettings currentPage() {
        if (pageTabs == null)
            return null;
        Component select = pageTabs.getSelectedComponent();
        if (select instanceof ZoomImagePageSettings)
            return (ZoomImagePageSettings) select;
        else
            return null;
    }

    // Upon change of tabs, exit old tab and enter new.
    private void changeTabs() {
	Component selected = pageTabs.getSelectedComponent();
	if (selected == currentTab)
	    return;
	if (currentTab != null && currentTab instanceof ZoomImagePageSettings)
	    ((ZoomImagePageSettings) currentTab).showPage(false, this);
	currentTab = selected;
	if (currentTab instanceof ZoomImagePageSettings) {
	    ZoomImagePageSettings zoomPage = (ZoomImagePageSettings) currentTab;
	    zoomPage.showPage(true, this);
	    zoomPage.setNumber(pageTabs.getSelectedIndex() - 1);
	    zoomPage.changeFocus();
	}
    }

    // Change focus in appropriate image.
    private void changeFocus() {
        ZoomImagePageSettings p = currentPage();
        if (p != null)
            p.changeFocus();
    }

    /////////////////////////////////////////////////////
    // Listeners.

    private void addListeners() {
	// Listen to change of image.
	pageTabs.addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
			changeTabs();
                    }
                });
            }
        });
    }

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("import")) {
	    importHiero();
        } else if (e.getActionCommand().equals("append")) {
	    importImage(currentTab());
        } else if (e.getActionCommand().equals("cut")) {
	    removeImage(currentTab()-1);
        } else if (e.getActionCommand().equals("left")) {
	    manipulator.left();
        } else if (e.getActionCommand().equals("right")) {
	    manipulator.right();
        } else if (e.getActionCommand().equals("new")) {
	    setWait(true, "adding");
	    manipulator.addSign();
	    setWait(false);
	    findGlyph();
        } else if (e.getActionCommand().equals("initial")) {
	    setWait(true, "adding");
	    manipulator.addInitialSign();
	    setWait(false);
	    findGlyph();
        } else if (e.getActionCommand().equals("other")) {
	    findGlyph();
        } else if (e.getActionCommand().equals("delete")) {
	    setWait(true, "deleting");
	    manipulator.removeSign();
	    setWait(false);
        } else if (e.getActionCommand().equals("swap")) {
	    setWait(true, "swapping");
	    manipulator.swapSigns();
	    setWait(false);
        } else if (e.getActionCommand().equals("help")) {
	    openHelp();
        } else if (e.getActionCommand().equals("color") ||
		e.getActionCommand().equals("bilevel") ||
		e.getActionCommand().equals("move") ||
		e.getActionCommand().equals("rect") ||
		e.getActionCommand().equals("poly") ||
		e.getActionCommand().equals("tagging") ||
		e.getActionCommand().equals("show") ||
		e.getActionCommand().equals("hlr") ||
		e.getActionCommand().equals("hrl") ||
		e.getActionCommand().equals("vlr") ||
		e.getActionCommand().equals("vrl") ||
		e.getActionCommand().equals("up") ||
		e.getActionCommand().equals("down") ||
		e.getActionCommand().equals("accept")) {
	    pageAction(e);
        } else if (e.getActionCommand().equals("settings")) {
	    showPage();
	} else
	    super.actionPerformed(e);
    }

    // Send action to selected page.
    private void pageAction(ActionEvent e) {
        ZoomImagePageSettings p = currentPage();
        if (p != null)
            p.actionPerformed(e);
    }

    // Show settings of selected page.
    private void showPage() {
        ZoomImagePageSettings p = currentPage();
        if (p != null)
            p.showPage(true, this);
    }

    ///////////////////////////////////////////////////
    // Including new image.

    // Previous directory where image was found, if any.
    private File imageDir = null;

    /**
     * Get file name and import hieroglyphic from it.
     */
    private void importImage(final int current) {
        allowEditing(false);
        FileChoosingWindow chooser = new FileChoosingWindow(
                "image", new String[] { "jpg", "jpeg", "png", "bmp", "gif" }) {
            public void choose(final File file) {
                dispose();
		setWait(true, "including");
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			incorporateImage(current, file);
			imageDir = file.getParentFile();
			setWait(false);
			allowEditing(true);
		    }
		});
            }

            public void exit() {
                dispose();
                allowEditing(true);
            }
        };
	if (imageDir != null) 
	    chooser.setCurrentDirectory(imageDir);
	else {
	    File textFile = new File(resource.getLocation());
	    chooser.setCurrentDirectory(textFile.getParentFile());
	}
    }

    // Append image behind current image.
    private void incorporateImage(int current, File file) {
	manipulator.appendImage(current, file);
	fillImageTabs();
    }

    ///////////////////////////////////////////////////
    // Help.

    /**
     * Open help window if not already open.
     */
    private void openHelp() {
        if (helpWindow == null) {
            URL url = FileAux.fromBase("data/help/text/image_edit.html");
            helpWindow = new HTMLWindow("Text/image editor manual", url);
        }
        helpWindow.setVisible(true);
    }

    ///////////////////////////////////////////////////
    // Closing.

    // Number of changes since last time.
    private int nChanges = 0;
    // Changes to next save.
    private final int maxChanges = 50;

    // Record number of changes. Save every so often.
    public void recordChange() {
	super.recordChange();
	nChanges++;
	if (nChanges > maxChanges) {
	    trySave();
	    nChanges = 0;
	}
    }

    // Kill all windows, and exit.
    public void dispose() {
        super.dispose();
        if (chooser != null)
            chooser.dispose();
        for (ImagePage page : pages.values())
            page.dispose();
    }

    // Save the information displayed on GUI to the resource.
    public void saveToResource() {
	// unused, as all changes are saved immediately.
    }

    ///////////////////////////////////////////////////////
    // Glyph choosing menu.

    // Activate glyph chooser.
    public void findGlyph() {
        if (chooser == null)
            chooser = new GlyphChooser() {
                protected void receive(String name) {
                    hieroPanel.acceptName(name);
                }
                protected void receiveNothing() {
                    hieroPanel.acceptName(null);
                }
            };
        chooser.setVisible(true);
    }

    ///////////////////////////////////////////////////
    // Appearance.

    // Overrides superclass.
    public void allowEditing(boolean allow) {
        super.allowEditing(allow);
    }

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

    /////////////////////////////////////////////
    // Import of data.

    /**
     * Get file name and import hieroglyphic from it.
     */
    private void importHiero() {
        allowEditing(false);
        FileChoosingWindow chooser = new FileChoosingWindow(
                "hieroglyphic resource", new String[] { "xml", "txt" }) {
            public void choose(final File file) {
                dispose();
		setWait(true, "importing");
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			incorporateHiero(file);
			setWait(false);
			allowEditing(true);
		    }
		});
            }

            public void exit() {
                dispose();
                allowEditing(true);
            }
        };
        File textFile = new File(resource.getLocation());
        chooser.setCurrentDirectory(textFile.getParentFile());
    }

   /**
     * Put hieroglyphic from file in current segment.
     * 
     * @param file
     */
    private void incorporateHiero(File file) {
        try {
            if (file.exists()) {
                EgyptianResource hieroResource = new EgyptianResource(
                        file.getPath());
                manipulator.incorporateHiero(hieroResource);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not read:\n" + e.getMessage(), "Reading error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    ///////////////////////////////////////////////////////
    // For testing.

    public static void main(String[] args) {
	EgyptianImageEditor editor = new EgyptianImageEditor(null, 0);
    }

}
