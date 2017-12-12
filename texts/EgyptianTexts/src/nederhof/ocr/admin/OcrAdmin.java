package nederhof.ocr.admin;

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
import javax.xml.parsers.*;
import org.w3c.dom.*;

import nederhof.ocr.*;
import nederhof.ocr.guessing.*;
import nederhof.ocr.images.*;
import nederhof.ocr.images.distance.*;
import nederhof.util.*;
import nederhof.util.gui.*;
import nederhof.util.xml.*;

// Frame in which collections of prototypes of glyphs can be maintained.
public abstract class OcrAdmin extends JFrame implements ActionListener {

	// Name of new directory.
	private final String NEW_DIR = "new";

	// Extension of graphics.
	private final String EXT = "png";

	// Directory containing prototypes.
	private File dir;

	// Ordered list of glyph names (possibly with modifiers) and extras. 
	protected Vector<String> names = null;

	// Have the prototypes changed since creation?
	private boolean hasChanged = false;
	// TODO: set to True in appropriate places.

	// Maps each name to list of prototypes.
	protected HashMap<String, Vector<Prototype>> protos;

	// Auxiliary windows.
	private DirectoryChoosingWindow dirWindow = null;
	private DirectoryChoosingWindow mergeWindow = null;
	private DirectoryChoosingWindow importWindow = null;
	private JFrame helpWindow = null;
	private LogWindow logWindow = null;

	// Panel containing prototypes.
	private JPanel protoPanel = new JPanel();

	// Constructor without directory.
	public OcrAdmin() {
		setJMenuBar(new Menu(this));
		setSize(AdminSettings.displayWidthInit, AdminSettings.displayHeightInit);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new CloseListener());

		Container content = getContentPane();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.add(new SimpleScroller(protoPanel, true, true));

		setVisible(true);
	}
	// Constructor with directory.
	public OcrAdmin(File dir) {
		this();
		setDir(dir);
	}

	// With directory and project from which to import.
	public OcrAdmin(File dir, Project proj) {
		this(dir);
		getProtosFrom(proj);
	}

	// Set to directory.
	public void setDir(File dir) {
		this.dir = dir;
		setTitle(dir.getName());
		refreshDir();
	}

	// Refresh directory. For example, after pruning prototypes.
	private void refreshDir() {
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		repaint();
		compileNames();
		openCollection();
		showCollection();
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		repaint();
	}

	/////////////////////////////////////
	// Determined by subclass.

	// Spawn new administrator.
	protected abstract OcrAdmin makeAdmin(File dir);

	// URL of manual page.
	protected abstract String getHelpPage();

	// From file name (without extension) make glyph name.
	protected abstract String getGlyphName(String fileName);
	// Add index to turn glyph name into file name.
	protected abstract String getFileName(String glyphName, int i);

	// At interface, some glyph names may be abbreviated.
	// This method turns short form into full form.
	// Subclass may override.
	protected String shortToLong(String s) {
		return s;
	}

	protected abstract Project getProject(File file) throws IOException;

	//////////////////////////////////////////////////////////////////////////////
	// Menu at top of window.

	// Items in menu that may be disabled/enabled.
	private final JMenu fileMenu = new EnabledMenu(
			"<u>F</u>ile", KeyEvent.VK_F);
	private final JMenuItem openItem = new EnabledMenuItem(this,
			"<u>O</u>pen", "open", KeyEvent.VK_O);
	private final JMenuItem closeItem = new EnabledMenuItem(this,
			"clo<u>S</u>e", "close", KeyEvent.VK_S);
	private final JMenu editMenu = new EnabledMenu(
			"<u>E</u>dit", KeyEvent.VK_E);
	private final JMenuItem pruneItem = new EnabledMenuItem(this,
			"<u>P</u>rune", "prune", KeyEvent.VK_P);
	private final JMenuItem importItem = new EnabledMenuItem(this,
			"<u>I</u>mport", "import", KeyEvent.VK_I);
	private final JMenuItem mergeItem = new EnabledMenuItem(this,
			"<u>M</u>erge", "merge", KeyEvent.VK_M);
	private final ClickButton helpItem = new ClickButton(this,
			"<u>H</u>elp", "help", KeyEvent.VK_H);

	// Menu containing quit and edit buttons.
	private class Menu extends JMenuBar {

		// Distance between buttons.
		private static final int STRUT_SIZE = 10;

		public Menu(ActionListener lis) {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setBackground(Color.LIGHT_GRAY);
			add(Box.createHorizontalStrut(STRUT_SIZE));

			// file
			add(fileMenu);
			add(Box.createHorizontalStrut(STRUT_SIZE));
			// open, close
			fileMenu.add(openItem);
			fileMenu.add(closeItem);

			// edit
			add(editMenu);
			add(Box.createHorizontalStrut(STRUT_SIZE));
			editMenu.add(pruneItem);
			editMenu.add(importItem);
			editMenu.add(mergeItem);

			// help
			add(helpItem);
			add(Box.createHorizontalStrut(STRUT_SIZE));

			add(Box.createHorizontalGlue());
		}
	}

	// Actions belonging to buttons.
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("open")) {
			chooseCollection();
		} else if (e.getActionCommand().equals("close")) {
			dispose();
		} else if (e.getActionCommand().equals("prune")) {
			choosePrune();
		} else if (e.getActionCommand().equals("import")) {
			getProtos();
		} else if (e.getActionCommand().equals("merge")) {
			mergeProtos();
		} else if (e.getActionCommand().equals("help")) {
			if (helpWindow == null) {
				URL url = FileAux.fromBase(getHelpPage());
				helpWindow = new HTMLWindow("OCR administration manual", url);
			}
			helpWindow.setVisible(true);
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
	public void dispose() {
		if (dirWindow != null)
			dirWindow.dispose();
		if (mergeWindow != null)
			mergeWindow.dispose();
		if (importWindow != null)
			importWindow.dispose();
		if (logWindow != null)
			logWindow.dispose();
		if (helpWindow != null)
			helpWindow.dispose();
		super.dispose();
		if (standAlone)
			System.exit(0);
		if (hasChanged)
			refreshApplication();
	}

	// Listen if window to be closed or iconified.
	// Open quit, save.
	private class CloseListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			dispose();
		}
		public void windowIconified(WindowEvent e) {
			setState(Frame.ICONIFIED);
		}
		public void windowDeiconified(WindowEvent e) {
			setState(Frame.NORMAL);
		}
	}

	public void refreshApplication() {
		// subclass may override
	}

	//////////////////////////////////////////////////////
	// Glyph list.

	// Get ordered list of glyph names.
	protected abstract void compileNames();

	//////////////////////////////////////////////////////
	// Choice of collection.

	// Make file selector to open existing collection.
	private void chooseCollection() {
		if (dirWindow == null) {
			dirWindow =
				new DirectoryChoosingWindow() {
					protected void choose(File file) {
						setCursor(new Cursor(Cursor.WAIT_CURSOR));
						setDir(file);
						setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
					public void exit() {
					}
				};
			if (dir != null)
				dirWindow.setCurrentDirectory(dir);
			else
				dirWindow.setCurrentDirectory(new File("."));
		}
		dirWindow.setVisible(true);
	}

	// Open collection of prototypes.
	private void openCollection() {
		try {
			File[] files = dir.listFiles();
			for (File f : files) {
				if (f.isFile() && FileAux.hasExtension(f.getName(), EXT)) {
					String fileName = FileAux.removeExtension(f.getName(), EXT);
					String glyphName = getGlyphName(fileName);
					BinaryImage im = new BinaryImage(f);
					storeProto(im, f, glyphName);
				}
			}
			hasChanged = false;
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} catch (NullPointerException e) {
			System.err.println("NullPointerException");
			System.err.println(e.getMessage());
			// ignore
		}
	}

	// Store new prototype.
	private void storeProto(BinaryImage im, File file, String name) {
		if (protos.containsKey(name))
			protos.get(name).add(new Prototype(im, file));
	}

	///////////////////////////////////////////////////
	// Panel with prototypes.

	// Put prototypes in panel.
	// Maximum of prototypes determines number of columns.
	private void showCollection() {
		int maxN = 0;
		for (String name : names) 
			maxN = Math.max(maxN, protos.get(name).size());
		protoPanel.removeAll();
		protoPanel.setLayout(new GridLayout(0, maxN + 1));
		for (final String name : names) {
			Vector<Prototype> nameProtos = protos.get(name);
			if (nameProtos.size() > 0) {
				PrintGlyphButton typeButton = getPrintButton(name);
				protoPanel.add(typeButton);
				int w = typeButton.getGlyphWidth();
				int h = typeButton.getGlyphHeight();
				for (final Prototype proto : nameProtos) {
					ScanGlyphButton scanButton = getScanButton(name, proto, w, h);
					protoPanel.add(scanButton);
				}
				for (int i = 0; i < maxN - nameProtos.size(); i++) 
					protoPanel.add(new JPanel());
			}
		}
		validate();
		repaint();
	}

	// Button for printed form.
	protected abstract PrintGlyphButton getPrintButton(String name);

	// Button for scanned form.
	protected abstract ScanGlyphButton getScanButton(String name, Prototype proto, int w, int h);

	///////////////////////////////////////////////////
	// Choice of parameter for pruning.

	// Make menu for pruning.
	private void choosePrune() {
		String s = (String) JOptionPane.showInputDialog(
					this,
					"Pruning number of prototypes down to the most distant\n"
					+ "Enter maximum number (0 -- 10) of prototypes per glyph:",
					"Pruning",
					JOptionPane.QUESTION_MESSAGE);
		if (s == null)
			return;
		final int MAX = 10;
		int limit = MAX;
		try {
			limit = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this,
					"Cannot parse: " + s, "Input error", 
					JOptionPane.ERROR_MESSAGE);
		}
		if (limit >= 0 && limit <= 10) {
			prune(limit);
		} else
			JOptionPane.showMessageDialog(this,
					"Should be number between 0 and 10, was: " + s, 
					"Input error", 
					JOptionPane.ERROR_MESSAGE);
	}

	///////////////////////////////////////////////////
	// Editing of collection.

	// Prune to up to limit prototypes per glyph.
	private void prune(final int limit) {
		Thread t = new PruningThread(limit);
		t.start();
	}

	private class PruningThread extends Thread {
		public int limit;
		public PruningThread(int limit) {
			this.limit = limit;
		}
		public void run() { 
			logWindow = new LogWindow("Pruning in progress");
			prune(limit, logWindow);
			logWindow.dispose();
			refreshDir();
		}

		// Prune.
		public void prune(int limit, LogWindow log) {
			for (String name : names) {
				Vector<Prototype> nameProtos = protos.get(name);
				if (nameProtos.size() > limit) {
					log.addText("Pruning: " + name + "\n");
					try {
						sleep(1);
						if (log.halt())
							return;
					} catch (InterruptedException ex) {}
						OcrAdmin.this.prune(name, limit, nameProtos, log);
				}
			}
		}
	}

	// Prune.
	// As long as there are too many prototypes, remove one that is
	// nearest to any other.
	protected void prune(String name, int limit, Vector<Prototype> protos, LogWindow log) {
		DistortionModel distModel = new IDM();
		float[][] dists = new float[protos.size()][];
		for (int i = 0; i < protos.size(); i++) {
			dists[i] = new float[protos.size()];
			for (int j = i+1; j < protos.size(); j++) {
				try {
					BufferedImage im1 = ImageIO.read(protos.get(i).file);
					BufferedImage im2 = ImageIO.read(protos.get(j).file);
					dists[i][j] = distModel.distort(im1, im2);
				} catch (IOException e) {
					dists[i][j] = Float.MAX_VALUE;
				}
			}
		}
		boolean[] alive = new boolean[protos.size()];
		for (int i = 0; i < protos.size(); i++) 
			alive[i] = true;
		for (int n = 0; n < protos.size() - limit; n++) {
			float[] minDist = new float[protos.size()];
			for (int i = 0; i < protos.size(); i++) 
				minDist[i] = Float.MAX_VALUE;
			for (int i = 0; i < protos.size(); i++) 
				if (alive[i]) 
					for (int j = i+1; j < protos.size(); j++) 
						if (alive[j]) {
							minDist[i] = Math.min(minDist[i], dists[i][j]);
							minDist[j] = Math.min(minDist[j], dists[i][j]);
						}
			int closest = 0;
			float minminDist = Float.MAX_VALUE;
			for (int i = 0; i < protos.size(); i++) 
				if (alive[i] && minDist[i] < minminDist) {
					minminDist = minDist[i];
					closest = i;
				}
			alive[closest] = false;
		}
		try {
			rearrange(name, protos, alive, log);
		} catch (SecurityException e) {
			log.addText(e.getMessage());
		}
	}

	// Remove prototypes that are not alive. Rename in sequence.
	private void rearrange(String name, Vector<Prototype> protos, boolean[] alive, 
			LogWindow log) throws SecurityException {
		File parent = null;
		for (int i = 0; i < alive.length; i++)
			if (!alive[i]) {
				log.addText("Removing: " + protos.get(i).file.getName() + "\n");
				hasChanged = true;
				parent = protos.get(i).file.getParentFile();
			} 
		int alives = 0;
		for (int i = 0; i < alive.length; i++) 
			if (alive[i]) {
				File tmp = new File(parent, "tmp-" + (alives++) + "." + EXT);
				protos.get(i).file.renameTo(tmp);
			} else  {
				protos.get(i).file.delete();
			}
		alives = 0;
		for (int i = 0; i < alive.length; i++)
			if (alive[i]) {
				File tmp1 = new File(parent, "tmp-" + alives + "." + EXT);
				File tmp2 = new File(parent, getFileName(name, alives) + "." + EXT);
				tmp1.renameTo(tmp2);
				alives++;
			}
	}

	///////////////////////////////////////////////////////
	// Manipulation of prototypes.

	// Remove prototype of sign.
	protected void remove(String name, Prototype proto) {
		Vector<Prototype> nameProtos = protos.get(name);
		nameProtos.remove(proto);
		try {
			proto.file.delete();
		} catch (SecurityException e) {
			LogAux.reportError(e.getMessage());
		}
		hasChanged = true;
		repaint();
	}

	// Move prototype to different name.
	protected void move(String name, Prototype proto) {
		String s = (String) JOptionPane.showInputDialog(
					this,
					"Enter glyph name:\n",
					"Moving to different name",
					JOptionPane.QUESTION_MESSAGE);
		if (s == null)
			return;
		s = shortToLong(s.trim());
		if (!protos.containsKey(s)) {
			JOptionPane.showMessageDialog(this,
					"Invalid glyph name: " + s, "Input error", 
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (s.equals(name))
			return;
		File parent = proto.file.getParentFile();
		int i = 0;
		File newFile = freeFile(parent, s);
		try {
			proto.file.renameTo(newFile);
			refreshDir();
		} catch (SecurityException e) {
			LogAux.reportError(e.getMessage());
		}
	}

	///////////////////////////////////////////////////////
	// Import from OCR project.

	// How many prototypes per glyph to be imported.
	private int importLimit;

	// To be called from special constructor.
	private void getProtosFrom(Project project) {
		if (!prepareProtoImport())
			return;
		else {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			importGlyphs(project);
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	// Open dialog, where to get signs.
	private void getProtos() {
		if (!prepareProtoImport())
			return;
		if (importWindow == null) {
			importWindow =
				new DirectoryChoosingWindow() {
					protected void choose(File file) {
						setCursor(new Cursor(Cursor.WAIT_CURSOR));
						importGlyphs(file);
						setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
					public void exit() {
					}
				};
			importWindow.setCurrentDirectory(new File("."));
		}
		importWindow.setVisible(true);
	}

	// Prepare directories for import of prototypes.
	// Also ask user for limit.
	// Return if successful.
	private boolean prepareProtoImport() {
		if (!getNewDir() || !clearNewDir())
			return false;
		String s = (String) JOptionPane.showInputDialog(
					this,
					"How many prototypes should be kept for each sign?\n"
					+ "Enter number (0 -- 10) of prototypes:",
					"Pruning",
					JOptionPane.QUESTION_MESSAGE);
		if (s == null)
			return false;
		final int MAX = 10;
		try {
			importLimit = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this,
					"Cannot parse: " + s, "Input error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	// Make sure there is "new" directory, to contain newly imported
	// prototypes.
	// Return whether successful.
	private boolean getNewDir() {
		if (dir == null) {
			JOptionPane.showMessageDialog(this,
					"Choose directory first", "Input error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		} else {
			try {
				File newDir = new File(dir, NEW_DIR);
				if (newDir.exists() && newDir.isDirectory()) {
					return true;
				} else if (!newDir.exists()) {
					newDir.mkdir();
					return true;
				}
			} catch (SecurityException e) { }
			JOptionPane.showMessageDialog(this,
					"Cannot make directory 'new'", "Input error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	// Remove all existing files from new directory.
	// Return whether successful.
	private boolean clearNewDir() {
		try {
			File newDir = new File(dir, NEW_DIR);
			File[] files = newDir.listFiles();
			for (File f : files) 
				if (f.isFile() && FileAux.hasExtension(f.getName(), EXT)) 
					f.delete();
			return true;
		} catch (SecurityException e) {
			return false;
		}
	}

	// Import signs from an OCR project.
	private void importGlyphs(File file) {
		Project project;
		try {
			project = getProject(file);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this,
					"Could not open project: " + e.getMessage(),
					"Reading error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		importGlyphs(project);
	}
	private void importGlyphs(Project project) {
		HashMap<String, Vector<File>> moreProtos = getGlyphs(project);
		importGlyphs(moreProtos);
	}
	private HashMap<String, Vector<File>> getGlyphs(Project project) {
		HashMap<String, Vector<File>> moreProtos = new HashMap<String, Vector<File>>();
		for (String pageName : project.images.keySet()) {
			Page page = project.pages.get(pageName);
			for (Line line : page.lines) {
				for (Blob blob : line.glyphs) {
					if (blob.isSaved()) {
						String name = blob.getName();
						if (name.equals("") &&
									blob.getGuessed() != null &&
									blob.getGuessed().size() > 0)
							name = blob.getGuessed().get(0);
						if (!name.equals("") && importAllowed(name) &&
								protos.containsKey(name)) {
							if (moreProtos.get(name) == null)
								moreProtos.put(name, new Vector<File>());
							moreProtos.get(name).add(blob.file());
							hasChanged = true;
						}
					}
				}
			}
		}
		return moreProtos;
	}

	// Subclass may override.
	protected boolean importAllowed(String name) {
		return true;
	}

	// Import prototypes to new directory.
	// Take only prefix.
	protected void importGlyphs(HashMap<String, Vector<File>> moreProtos) {
		File newDir = new File(dir, NEW_DIR);
		for (String name : moreProtos.keySet()) {
			Vector<File> moreFiles = moreProtos.get(name);
			for (int i = 0; i < Math.min(moreFiles.size(), importLimit * 3); i++) 
				try {
					File protoFile = moreFiles.get(i);
					File target = new File(newDir, getFileName(name, i) + "." + EXT);
					BufferedImage im = ImageIO.read(protoFile);
					ImageIO.write(im, EXT, target);
					importParts(newDir, im, name);
				} catch (IOException e) { /* ignore */ }
		}
		OcrAdmin newAdmin = makeAdmin(newDir);
		newAdmin.prune(importLimit);
	}

	// Find glyphs consisting of several unconnected parts.
	// Add these separately.
	// Not done by default.
	protected void importParts(File newDir, BufferedImage im, String name) {
		// subclass may override
	}

	///////////////////////////////////////////////////////
	// Merge new prototypes in collection.

	// Merge with other collection.
	private void mergeProtos() {
		if (dir == null) {
			JOptionPane.showMessageDialog(this,
					"Choose current directory first", "Input error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (mergeWindow == null) {
			mergeWindow =
				new DirectoryChoosingWindow() {
					protected void choose(File file) {
						setCursor(new Cursor(Cursor.WAIT_CURSOR));
						mergeDir(file);
						setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
					public void exit() {
					}
				};
			if (dir != null) {
				File newDir = new File(dir, NEW_DIR);
				if (newDir.exists() && 
						newDir.isDirectory() &&
						newDir.listFiles() != null &&
						newDir.listFiles().length > 0) 
					mergeWindow.setCurrentDirectory(newDir);
				else
					mergeWindow.setCurrentDirectory(dir);
			} else
				mergeWindow.setCurrentDirectory(new File("."));
		}
		mergeWindow.setVisible(true);
	}

	// Merge prototypes from two directories.
	private void mergeDir(File otherDir) {
		if (dir == otherDir)
			return;
		File[] files = otherDir.listFiles();
		for (File f : files) {
			if (f.isFile() && FileAux.hasExtension(f.getName(), EXT)) {
				String fileName = FileAux.removeExtension(f.getName(), EXT);
				String name = getGlyphName(fileName);
				File otherName = freeFile(dir, name);
				f.renameTo(otherName);
				hasChanged = true;
			}
		}
		refreshDir();
	}

	// Find free filename in directory.
	protected File freeFile(File dir, String name) {
		File f = null;
		for (int i = 0; true; i++) {
			f = new File(dir, getFileName(name, i) + "." + EXT);
			if (!f.exists())
				break;
		}
		return f;
	}

}

