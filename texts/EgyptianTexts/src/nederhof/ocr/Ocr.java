package nederhof.ocr;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import nederhof.ocr.admin.*;
import nederhof.ocr.guessing.*;
import nederhof.ocr.images.*;
import nederhof.util.*;

// Frame in which OCR project is displayed.
public abstract class Ocr extends JFrame implements ActionListener {

	// Settings for OCR.
	protected boolean autoSegment;
	protected boolean autoOcr;
	protected boolean autoFormat;

	// Guesser of glyphs.
	protected OcrGuesser guesser;

	// Analyzer of layout.
	protected LayoutAnalyzer analyzer;

	// Formatter of blobs.
	protected BlobFormatter formatter;

	// Properties of project.
	protected Project project = null;

	// Tabs for pages.
	protected JTabbedPane pageTabs;

	// GUIs for pages.
	private Vector<OcrPage> ocrPages = new Vector<OcrPage>();

	// Menu items that we need to enable/disable.
	protected Vector<JComponent> menuItems = new Vector<JComponent>();

	// Auxiliary window: settings window, directory window, help window.
	private SettingsWindow settingsWindow = null;
	private DirectoryChoosingWindow dirWindow = null;
	private JFrame helpWindow = null;

	// Thread doing OCR and related.
	protected OcrProcess process;
	// Thread that creates process.
	protected Thread processMaker;

	// Make window. With directory if present.
	public Ocr(String dir) {
		setJMenuBar(getMenu());
		setTitle(getOcrTitle());
		setSize(Settings.displayWidthInit, Settings.displayHeightInit);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new CloseListener());
		initializeProcess();
		analyzer = createAnalyzer();
		formatter = createFormatter();
		setVisible(true);
		if (dir == null) 
			chooseProject();
		else
			openProject(new File(dir));
		initializeOcrSettings();
	}

	// Without given directory.
	public Ocr() {
		this(null);
	}

	// Title in frame.
	protected String getOcrTitle() {
		return "OCR";
	}

	// Menu in frame.
	protected JMenuBar getMenu() {
		return new Menu(this);
	}

	// Default directory of prototypes.
	protected abstract String getProtoDir();

	// Default beam size.
	protected int getDefaultBeam() {
		return Settings.beam;
	}
	// Default number of candidates returned.
	protected int getDefaultNCandidates() {
		return Settings.nCandidates;
	}

	// Segment detection starts when line is selected.
	protected boolean getDefaultAutoSegment() {
		return Settings.autoSegment;
	}
	// Ocr starts when line is selected.
	protected boolean getDefaultAutoOcr() {
		return Settings.autoOcr;
	}
	// Formatting starts after OCR.
	protected boolean getDefaultAutoFormat() {
		return Settings.autoFormat;
	}

	// Creates guesser of glyphs, with prototypes in directory.
	protected abstract OcrGuesser createGuesser(String protoDir) throws IOException;
	// Creates analyzer of layout.
	protected abstract LayoutAnalyzer createAnalyzer();
	// Creates process to combine glyphs consisting of several components.
	// The default is not to do anything.
	protected GlyphCombiner createCombiner(Line line) {
		return new GlyphCombiner(line);
	}
	// Creates formatter.
	protected abstract BlobFormatter createFormatter();

	// URL of manual page.
	protected abstract String getHelpPage();

	//////////////////////////////////////////////////////////////////////////////
	// Menu at top of window.

	// Button showing status.
	private JButton statusButton = new JButton("");

	// Menu containing quit and edit buttons.
	protected class Menu extends JMenuBar {

		// Distance between buttons.
		protected static final int STRUT_SIZE = 10;

		public Menu(ActionListener lis) {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setBackground(Color.LIGHT_GRAY);
			add(Box.createHorizontalStrut(STRUT_SIZE));
			addFileItems(lis);
			addResourceItems(lis);
			addCustomItems(lis);
			addSettingsItems(lis);
			addHelpItems(lis);
			addStatus();
		}

		protected void addFileItems(ActionListener lis) {
			JMenu fileMenu = new EnabledMenu(
					"<u>F</u>ile", KeyEvent.VK_F);
			JMenuItem openItem = new EnabledMenuItem(lis,
					"<u>O</u>pen", "open", KeyEvent.VK_O);
			JMenuItem closeItem = new EnabledMenuItem(lis,
					"clo<u>S</u>e", "close", KeyEvent.VK_S);
			JMenuItem protoItem = new EnabledMenuItem(lis,
					"protot<u>y</u>pes", "prototypes", KeyEvent.VK_Y);
			JMenuItem exportItem = new EnabledMenuItem(lis,
					"e<u>X</u>port", "export", KeyEvent.VK_X);
			add(fileMenu);
			menuItems.add(fileMenu);
			fileMenu.add(openItem);
			menuItems.add(openItem);
			fileMenu.add(closeItem);
			menuItems.add(closeItem);
			fileMenu.add(protoItem);
			menuItems.add(protoItem);
			fileMenu.add(exportItem);
			menuItems.add(exportItem);
			add(Box.createHorizontalStrut(STRUT_SIZE));
		}

		protected void addResourceItems(ActionListener lis) {
			JMenu resourceMenu = new EnabledMenu(
					"<u>R</u>esource", KeyEvent.VK_R);
			JMenuItem analyzeItem = new EnabledMenuItem(lis,
					"ana<u>L</u>yze page", "analyze", KeyEvent.VK_L);
			JMenuItem analyzeAllItem = new EnabledMenuItem(lis,
					"analyze <u>A</u>ll", "analyze all", KeyEvent.VK_A);
			JMenuItem deleteItem = new EnabledMenuItem(lis,
					"<u>D</u>elete lines", "delete", KeyEvent.VK_D);
			JMenuItem ocrItem = new EnabledMenuItem(lis,
					"O<u>C</u>R", "ocr", KeyEvent.VK_C);
			JMenuItem updateItem = new EnabledMenuItem(lis,
					"<u>U</u>pdate OCR", "update", KeyEvent.VK_U);
			JMenuItem formatItem = new EnabledMenuItem(lis,
					"for<u>M</u>at line", "format", KeyEvent.VK_M);
			JMenuItem formatAllItem = new EnabledMenuItem(lis,
					"forma<u>T</u> lines", "format all", KeyEvent.VK_T);
			JMenuItem upItem = new EnabledMenuItem(lis,
					"line u<u>P</u>", "up", KeyEvent.VK_P);
			JMenuItem downItem = new EnabledMenuItem(lis,
					"line do<u>W</u>n", "down", KeyEvent.VK_W);
			JMenuItem leftItem = new EnabledMenuItem(lis,
					"glyph left", "left", KeyEvent.VK_LEFT);
			JMenuItem rightItem = new EnabledMenuItem(lis,
					"glyph right", "right", KeyEvent.VK_RIGHT);
			JMenuItem zoomItem = new EnabledMenuItem(lis,
					"<u>Z</u>oom", "zoom", KeyEvent.VK_Z);
			JMenuItem combineItem = new EnabledMenuItem(lis,
					"com<u>B</u>ine", "combine", KeyEvent.VK_B);
			add(resourceMenu);
			menuItems.add(resourceMenu);
			resourceMenu.add(analyzeItem);
			menuItems.add(analyzeItem);
			resourceMenu.add(analyzeAllItem);
			menuItems.add(analyzeAllItem);
			resourceMenu.add(deleteItem);
			menuItems.add(deleteItem);
			resourceMenu.add(ocrItem);
			menuItems.add(ocrItem);
			resourceMenu.add(updateItem);
			menuItems.add(updateItem);
			resourceMenu.add(formatItem);
			menuItems.add(formatItem);
			resourceMenu.add(formatAllItem);
			menuItems.add(formatAllItem);
			resourceMenu.add(upItem);
			menuItems.add(upItem);
			resourceMenu.add(downItem);
			menuItems.add(downItem);
			resourceMenu.add(leftItem);
			menuItems.add(leftItem);
			resourceMenu.add(rightItem);
			menuItems.add(rightItem);
			resourceMenu.add(zoomItem);
			menuItems.add(zoomItem);
			resourceMenu.add(combineItem);
			menuItems.add(combineItem);
			add(Box.createHorizontalStrut(STRUT_SIZE));
		}

		protected void addCustomItems(ActionListener lis) {
			// subclass may override
		}

		protected void addSettingsItems(ActionListener lis) {
			ClickButton settingsItem = new ClickButton(lis,
					"settin<u>G</u>s", "settings", KeyEvent.VK_G);
			add(settingsItem);
			menuItems.add(settingsItem);
			add(Box.createHorizontalStrut(STRUT_SIZE));
		}

		protected void addHelpItems(ActionListener lis) {
			ClickButton helpItem = new ClickButton(lis,
					"<u>H</u>elp", "help", KeyEvent.VK_H);
			add(helpItem);
			menuItems.add(helpItem);
			add(Box.createHorizontalStrut(STRUT_SIZE));
		}

		protected void addStatus() {
			final int BSIZE = 5;
			statusButton.setBorder(new EmptyBorder(BSIZE,BSIZE,BSIZE,BSIZE));
			statusButton.setBackground(Color.LIGHT_GRAY);
			statusButton.setFocusable(false);
			add(statusButton);
			add(Box.createHorizontalGlue());
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

	//////////////////////////////////////////////////////
	// Actions.

	// Actions belonging to buttons.
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("open")) {
			if (trySave(true)) 
				chooseProject();
		} else if (e.getActionCommand().equals("close")) {
			if (trySave(true))
				dispose();
		} else if (e.getActionCommand().equals("prototypes")) {
			protoAdmin();
		} else if (e.getActionCommand().equals("export")) {
			export();
		} else if (e.getActionCommand().equals("settings")) {
			if (settingsWindow == null) 
				settingsWindow = new ConnectedSettingsWindow();
			settingsWindow.setVisible(true);
		} else if (e.getActionCommand().equals("help")) {
			if (helpWindow == null) {
				URL url = FileAux.fromBase(getHelpPage());
				helpWindow = new HTMLWindow("OCR manual", url);
			}
			helpWindow.setVisible(true);
		} else if (e.getActionCommand().matches("analyze all")) {
			if (!linesExist() || userConfirmsLoss("Remove existing lines?"))
				allPageAction(e);
		} else if (e.getActionCommand().matches("analyze")) {
			OcrPage p = currentPage();
			if (p != null &&
					(p.getPage().lines.isEmpty() || 
					 userConfirmsLoss("Remove existing lines?")))
				p.actionPerformed(e);
		} else if (e.getActionCommand().matches("delete")) {
			OcrPage p = currentPage();
			if (p != null &&
					(p.getPage().lines.isEmpty() ||
					 userConfirmsLoss("Remove lines?")))
				p.actionPerformed(e);
		} else if (e.getActionCommand().matches("ocr")) {
			if (settingsWindow == null) 
				settingsWindow = new ConnectedSettingsWindow();
			settingsWindow.toggleAutoOcr();
		} else
			pageAction(e);
	}

	// Send action to all pages.
	private void allPageAction(ActionEvent e) {
		if (pageTabs == null)
			return;
		int totalTabs = pageTabs.getTabCount();
		for (int i = 0; i < totalTabs; i++) {
			Component comp = pageTabs.getComponentAt(i);
			if (comp instanceof OcrPage) {
				OcrPage page = (OcrPage) comp;
				page.actionPerformed(e);
			}
		}
	}

	// Refresh all pages.
	private void refreshPages() {
		if (pageTabs == null)
			return;
		int totalTabs = pageTabs.getTabCount();
		for (int i = 0; i < totalTabs; i++) {
			Component comp = pageTabs.getComponentAt(i);
			if (comp instanceof OcrPage) {
				OcrPage page = (OcrPage) comp;
				page.setLines();
			}
		}
	}

	// Are there lines in any page?
	private boolean linesExist() {
		if (pageTabs == null)
			return false;
		int totalTabs = pageTabs.getTabCount();
		for (int i = 0; i < totalTabs; i++) {
			Component comp = pageTabs.getComponentAt(i);
			if (comp instanceof OcrPage) {
				OcrPage page = (OcrPage) comp;
				if (!page.getPage().lines.isEmpty())
					return true;
			}
		}
		return false;
	}

	// Send action to selected page.
	private void pageAction(ActionEvent e) {
		OcrPage p = currentPage();
		if (p != null)
			p.actionPerformed(e);
	}

	// Return page of tab. Or null if none.
	private OcrPage currentPage() {
		if (pageTabs == null)
			return null;
		Component select = pageTabs.getSelectedComponent();
		if (select instanceof OcrPage) 
			return (OcrPage) select;
		else
			return null;
	}

	//////////////////////////////////////////////////////
	// Process.

	// We want construction of process to run concurrently with potential
	// selection of OCR project.
	private void initializeProcess() {
		processMaker = new Thread(new ProcessConstructor());
		processMaker.start();
	}

	// Process that constructs process.
	private class ProcessConstructor implements Runnable {
		public void run() {
			try {
				guesser = createGuesser(getProtoDir());
				guesser.setBeam(getDefaultBeam());
				guesser.setCandidates(getDefaultNCandidates());
				process = new OcrProcess(guesser, formatter);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(Ocr.this,
						"Cannot start OCR process : " + e.getMessage(),
						"Thread error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
	}

	// Get guesser, but wait if it has not been constructed yet.
	private OcrGuesser getGuesser() {
		try {
			processMaker.join();
		} catch (InterruptedException e) {
			// ignore
		}
		return guesser;
	}

	// Get process, but wait if it has not been constructed yet.
	private OcrProcess getProcess() {
		try {
			processMaker.join();
		} catch (InterruptedException e) {
			// ignore
		}
		return process;
	}

	//////////////////////////////////////////////////////
	// Prototype admin.

	// Let prototype admin import glyphs from this.
	// If there is no current project, just start prototype admin.
	// Make sure all glyphs are saved.
	private void protoAdmin() {
		trySave(false);
		File dir = getGuesser().getPrototypeDir();
		createOcrAdmin(dir, project);
	}

	// Subclass to implement.
	protected abstract OcrAdmin createOcrAdmin(File dir, Project project);

	protected void reloadGuesser() throws IOException {
		showEmphasizedStatus("Reloading prototypes");
		setWait(true);
		int beam = getGuesser().getBeam();
		int candidates = getGuesser().getCandidates();
		guesser = createGuesser(getGuesser().getPrototypeDir().getAbsolutePath());
		guesser.setBeam(beam);
		guesser.setCandidates(candidates);
		getProcess().setGuesser(guesser);
		showStatus("");
		setWait(false);
	}

	//////////////////////////////////////////////////////
	// Settings.

	// Initialize settings of OCR.
	private void initializeOcrSettings() {
		autoSegment = getDefaultAutoSegment();
		autoOcr = getDefaultAutoOcr();
		autoFormat = getDefaultAutoFormat();
	}

	// Connect settings window to values.
	private class ConnectedSettingsWindow extends SettingsWindow {
		public File getPrototypeDir() {
			return getGuesser().getPrototypeDir();
		}
		public void setPrototypeDir(String dir) {
			try {
				guesser = createGuesser(dir);
				getProcess().setGuesser(guesser);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this,
						"Cannot open prototypes: " + e.getMessage(),
						"Reading error", JOptionPane.ERROR_MESSAGE);
			}
		}
		public boolean getDefaultAutoSegment() {
			return Ocr.this.getDefaultAutoSegment();
		}
		public boolean getAutoSegment() {
			return autoSegment;
		}
		public void setAutoSegment(boolean b) {
			autoSegment = b;
		}
		public boolean getDefaultAutoOcr() {
			return Ocr.this.getDefaultAutoOcr();
		}
		public boolean getAutoOcr() {
			return autoOcr;
		}
		public void setAutoOcr(boolean b) {
			boolean old = autoOcr;
			autoOcr = b;
			if (!old && b)
				refreshPages();
		}
		public boolean getDefaultAutoFormat() {
			return Ocr.this.getDefaultAutoFormat();
		}
		public boolean getAutoFormat() {
			return autoFormat;
		}
		public void setAutoFormat(boolean b) {
			autoFormat = b;
		}
		public int getDefaultBeam() {
			return Ocr.this.getDefaultBeam();
		}
		public int getBeam() {
			return getGuesser().getBeam();
		}
		public void setBeam(int n) {
			getGuesser().setBeam(n);
		}
		public int getDefaultNCandidates() {
			return Ocr.this.getDefaultNCandidates();
		}
		public int getNCandidates() {
			return getGuesser().getCandidates();
		}
		public void setNCandidates(int n) {
			getGuesser().setCandidates(n);
		}
	}

	//////////////////////////////////////////////////////
	// Fill project.

	private void openProject(File dir) {
		showEmphasizedStatus("Loading project");
		setWait(true);
		try {
			getProcess().clear();
			project = createProject(dir);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this,
					"Could not open project: " + e.getMessage(),
					"Reading error", JOptionPane.ERROR_MESSAGE);
			project = null;
			showStatus("");
			setWait(false);
			return;
		}
		Container content = getContentPane();
		content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
		content.removeAll();
		pageTabs = new JTabbedPane(JTabbedPane.TOP);
		content.add(pageTabs);
		for (String name : project.images.keySet()) {
			BinaryImage im = project.images.get(name);
			Page page = project.pages.get(name);
			OcrPage ocrPage = createPage(im, page);
			ocrPages.add(ocrPage);
			pageTabs.addTab(name, ocrPage);
		}
		showStatus("");
		setWait(false);
	}

	// Subclass should implement.
	protected abstract Project createProject(File dir) throws IOException;

	// Subclass should implement.
	protected abstract OcrPage createPage(BinaryImage im, Page page);

	// Set wait cursor.
	protected void setWait(boolean wait) {
		if (wait)
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
		else
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	// Make file selector to open existing project or create new project.
	private void chooseProject() {
		allowEdits(false);
		if (dirWindow == null) {
			dirWindow =
				new DirectoryChoosingWindow() {
					protected void choose(File file) {
						openProject(file);
						allowEdits(true);
					}
					public void exit() {
						allowEdits(true);
					}
				};
			if (project != null)
				dirWindow.setCurrentDirectory(project.dir());
			else
				dirWindow.setCurrentDirectory(new File("."));
		}
		dirWindow.setVisible(true);
	}

	///////////////////////////////////////////////////
	// Enabling/disabling of buttons.

	// Enable editing?
	protected void allowEdits(boolean allow) {
		super.setEnabled(allow);
		for (JComponent item : menuItems)
			item.setEnabled(allow);
		if (pageTabs != null) {
			pageTabs.setEnabled(allow);
			int totalTabs = pageTabs.getTabCount();
			for (int i = 0; i < totalTabs; i++) {
				Component comp = pageTabs.getComponentAt(i);
				if (comp instanceof OcrPage) {
					OcrPage page = (OcrPage) comp;
					page.setEnabled(allow);
				}
			}
		}
		repaint();
	}

	///////////////////////////////////////////////////////
	// Glyph choosing menu.
	
    // Send chosen name to appropriate page.
    protected void sendNameToPage(String name) {
        if (pageTabs == null)
            return;
        Component select = pageTabs.getSelectedComponent();
        if (select instanceof OcrPage) {
            OcrPage selectedPage = (OcrPage) select;
            selectedPage.sendNameToLine(name);
        }
    }

	///////////////////////////////////////////////////
	// Closing.

	// For stand-alone, exit upon window close.
	// (Resource leak in JFileChooser forces us to make this distinction.)
	private boolean standAlone = false;

	// Set whether editor is stand alone.
	public void setStandAlone(boolean standAlone) {
		this.standAlone = standAlone;
	}

	// Kill all windows, and exit.
	// Do not proceed if there are open text windows.
	public void dispose() {
		getProcess().quit();
		if (dirWindow != null)
			dirWindow.dispose();
		if (settingsWindow != null)
			settingsWindow.dispose();
		if (helpWindow != null)
			helpWindow.dispose();
		for (OcrPage ocrPage : ocrPages)
			ocrPage.dispose();
		super.dispose();
		if (standAlone)
			System.exit(0);
	}

	// Try saving. Report if not successful. 
	// Only ask when needed.
	// Return whether continue.
	private boolean trySave(boolean ask) {
		if (project != null) {
			try {
				showEmphasizedStatus("Saving project");
				project.save();
				showStatus("");
			} catch (IOException e) {
				showStatus("");
				if (ask)
					return userConfirmsLoss("Could not save project. Want to close anyway?");
			}
		}
		return true;
	}

	// Listen if window to be closed or iconified.
	// Open quit, save.
	private class CloseListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			if (trySave(true))
				dispose();
		}
		public void windowIconified(WindowEvent e) {
			setState(Frame.ICONIFIED);
		}
		public void windowDeiconified(WindowEvent e) {
			setState(Frame.NORMAL);
		}
	}

	// Ask user whether loss of data is intended.
	private boolean userConfirmsLoss(String message) {
		Object[] options = {"proceed", "cancel"};
		int answer = JOptionPane.showOptionDialog(this, message,
				"warning: impending loss of data",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, options, options[1]);
		return answer == 0;
	}

	///////////////////////////////////////////////////////
	// Exporting.

	protected abstract void export();

}
