/***************************************************************************/
/*                                                                         */
/*  CorpusViewer.java                                                      */
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

// Frame in which to view/edit a corpus.

package nederhof.corpus.frame;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.alignment.*;
import nederhof.corpus.*;
import nederhof.interlinear.*;
import nederhof.interlinear.frame.*;
import nederhof.interlinear.frame.pdf.*;
import nederhof.util.*;

public abstract class CorpusViewer extends JFrame 
		implements ActionListener, DocumentListener, ChangeListener {

	// The type of corpus. Define in subclass.
	protected abstract String type();

	// Default directory for corpus. To be overridden in subclass.
	protected String getDefaultCorpusDir() {
		return Settings.defaultCorpusDir;
	}
	// Default directory for texts. To be overridden in subclass.
	protected String getDefaultTextDir() {
		return Settings.defaultTextDir;
	}

	// The corpus under investigation.
	protected Corpus corpus;

	// Whether unsaved editing done on index of current corpus.
	protected boolean changed = false;

	// File being selected.
	private boolean fileSelection = false;

	// Pane containing indices and text being edited.
	private JTabbedPane tabbed = new JTabbedPane(JTabbedPane.TOP);

	// Panel in which properties can be edited.
	private JPanel propertiesPanel;

	// Auxiliary window: file window, directory window, help window.
	private FileChoosingWindow fileWindow = null;
	private DirectoryChoosingWindow dirWindow = null;
	private JFrame helpWindow = null;

	// The windows for editing text.
	protected Vector textEditors = new Vector();

	// The windows for viewing text.
	protected Vector textViewers = new Vector();

	// Containing window for changing options for rendering
	// interlinear form.
	protected RenderParameters renderParameters;

	// Make window.
	public CorpusViewer(String location, boolean visible) {
		setJMenuBar(new Menu(this));
		setSize(Settings.displayWidthInit, Settings.displayHeightInit);
		Container content = getContentPane();
		content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new CloseListener());
		content.add(tabbed);
		tabbed.addChangeListener(this);

		propertiesPanel = propertiesPanel();
		open(location);
		setVisible(visible);
		if (corpus == null)
			chooseFile();
		SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					changed = false;
				}
				});
	}
	// Make window, by default visible.
	public CorpusViewer(String location) {
		this(location, true);
	}

	// Make window without given corpus.
	public CorpusViewer() {
		this(null);
	}

	//////////////////////////////////////////////////////////////////////////////
	// Menu at top of window.

	// Items in menu that may be disabled/enabled.
	private final JMenu fileMenu = new EnabledMenu(
			"<u>F</u>ile", KeyEvent.VK_F);
	private final JMenuItem openItem = new EnabledMenuItem(this,
			"<u>O</u>pen", "open", KeyEvent.VK_O);
	private final JMenuItem closeItem = new EnabledMenuItem(this,
			"clo<u>S</u>e", "close", KeyEvent.VK_S);
	private final JMenuItem moveItem = new EnabledMenuItem(this,
			"<u>M</u>ove", "move", KeyEvent.VK_M);
	private final JMenuItem textItem = new EnabledMenuItem(this,
			"<u>T</u>ext", "text", KeyEvent.VK_T);
	private final JMenuItem refreshItem = new EnabledMenuItem(this,
			"<u>R</u>efresh", "refresh", KeyEvent.VK_R);
	private final JMenuItem pdfItem = new EnabledMenuItem(this,
			"<u>P</u>df", "pdf", KeyEvent.VK_P);

	// Button showing status.
	private JButton statusButton = new JButton("");

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
			// open, close, move
			fileMenu.add(openItem);
			fileMenu.add(closeItem);
			fileMenu.add(moveItem);
			fileMenu.addSeparator();
			// edit, text, refresh
			fileMenu.add(textItem);
			fileMenu.add(refreshItem);
			fileMenu.addSeparator();
			// pdf
			fileMenu.add(pdfItem);

			add(new ClickButton(CorpusViewer.this, "<u>H</u>elp", "help", KeyEvent.VK_H));
			add(Box.createHorizontalStrut(STRUT_SIZE));

			// status
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

	// Actions belonging to buttons.
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("open")) {
			if (trySave()) {
				corpus = null;
				processCorpus();
				chooseFile();
			}
		} else if (e.getActionCommand().equals("close")) {
			if (trySave())
				dispose();
		} else if (e.getActionCommand().equals("move")) {
			if (trySave()) 
				chooseFile();
		} else if (e.getActionCommand().equals("text")) {
			editText(null);
		} else if (e.getActionCommand().equals("refresh")) {
			if (corpus != null)
				corpus.refreshTrees();
			makeIndices();
			changed = true;
		} else if (e.getActionCommand().equals("pdf")) {
			pdfExport();
		} else if (e.getActionCommand().equals("help")) {
			if (helpWindow == null) {
				URL url = FileAux.fromBase("data/help/corpus/viewer.html");
				helpWindow = new HTMLWindow("Corpus viewer manual", url);
			}
			helpWindow.setVisible(true);
		}
	}

	//////////////////////////////////////////////////////
	// Building the tabs.

	// Process corpus. Make indices.
	private void processCorpus() {
		if (corpus != null) {
			setTitle(corpus.getName());
			fileLabel.setText(corpus.getLocation());
			typeLabel.setText(corpus.getType());
			nameField.setText(corpus.getName());
		} else {
			setTitle("Corpus viewer");
			fileLabel.setText("");
			typeLabel.setText("");
			nameField.setText("");
		}
		makeIndices();
		enableDisable();
	}

	// Put trees with indices in tabs.
	protected void makeIndices() {
		int sel = tabbed.getSelectedIndex();
		tabbed.removeAll();
		if (corpus != null) {
			Vector kinds = corpus.getKinds();
			for (int i = 0; i < kinds.size(); i++) {
				String kind = (String) kinds.get(i);
				TreeSet tree = (TreeSet) corpus.getTree(kind);
				if (tree != null) {
					JTree index = new IndexTree(tree) {
						public void viewSelect(Text text) {
							selectTextForView(text);
						}
						public void editSelect(Text text) {
							selectTextForEdit(text);
						}
					};
					JScrollPane scroll = new JScrollPane(index);
					scroll.setFocusable(false);
					tabbed.addTab(kind, scroll);
				}
			}
		}
		tabbed.addTab("corpus", propertiesPanel);
		if (sel >= 0 && sel < tabbed.getTabCount())
			tabbed.setSelectedIndex(sel);
		else
			tabbed.setSelectedIndex(0);
	}

	// Attempt to circumvent bug in handling clicks on Windows,
	// by ensuring that the focus always goes directly to the tree.
	public void stateChanged(ChangeEvent evt) {
		if (evt.getSource() == tabbed) {
			final Component sel = tabbed.getSelectedComponent();
			SwingUtilities.invokeLater(new Runnable() {
					public void run() {
					if (sel instanceof JScrollPane) {
						JScrollPane cont = (JScrollPane) sel;
						Component comp = cont.getViewport().getView();
						if (comp != null && comp instanceof JTree) {
							final JTree tree = (JTree) comp;
							tree.requestFocus();
						}
						}
						} 
					});
		}
	}

	// View text.
	private void selectTextForView(Text text) {
		tryViewText(text);
	}

	// Edit text.
	private void selectTextForEdit(Text text) {
		if (editableCorpus())
			editText(text);
	}

	/////////////////////////////////////////////////////
	// Manipulation of corpus.

	// Make file selector to open existing corpus or create new corpus
	private void chooseFile() {
		fileSelection = true;
		enableDisable();
		if (fileWindow == null) {
			fileWindow = 
				new FileChoosingWindow("XML files", new String[] {"xml"}) {
					protected void choose(File file) {
						fileSelection = false;
						CorpusViewer.this.open(file);
					}
					public void exit() {
						fileSelection = false;
						enableDisable();
					}
				};
			fileWindow.setCurrentDirectory(new File("."));
		}
		if (corpus != null) 
			fileWindow.setSelectedFile(new File(corpus.getLocation()));
		fileWindow.setVisible(true);
	}

	// Open existing corpus, when viewer is constructed.
	private void open(String uri) {
		try {
			if (uri != null) {
				corpus = new Corpus(uri);
				changed = false;
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this,
					"Could not read corpus: " + e.getMessage(),
					"Reading error", JOptionPane.ERROR_MESSAGE);
			corpus = null;
		}
		processCorpus();
	}

	// Open existing corpus or create new corpus.
	private void open(File file) {
		if (corpus != null) 
			try {
				corpus.moveTo(file);
				changed = true;
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this,
						"Could not move corpus: " + e.getMessage(),
						"File error", JOptionPane.ERROR_MESSAGE);
			}
		else if (file.exists())
			try {
				corpus = new Corpus(file.getPath());
				changed = false;
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, 
						"Could not read corpus: " + e.getMessage(),
						"Reading error", JOptionPane.ERROR_MESSAGE);
				corpus = null;
			}
		else
			try {
				corpus = Corpus.makeCorpus(type(), "", file);
				changed = false;
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, 
						"Could not create corpus: " + e.getMessage(),
						"Writing error", JOptionPane.ERROR_MESSAGE);
				corpus = null;
			}
		processCorpus();
	}

	///////////////////////////////////////////////////
	// Editing corpus properties.

	// The title.
	private TitledBorder fileTitle = BorderFactory.createTitledBorder(
			new LineBorder(Color.GRAY, 2), "file");
	private TitledBorder typeTitle = BorderFactory.createTitledBorder(
			new LineBorder(Color.GRAY, 2), "type");
	private TitledBorder nameTitle = BorderFactory.createTitledBorder(
			new LineBorder(Color.GRAY, 2), "name");

	// Label containing file.
	private JLabel fileLabel = new PlainLabel("");

	// Label containing type.
	private JLabel typeLabel = new PlainLabel("");

	// Label containing type.
	private SmallField nameField = new SmallField(30);

	// Panel for editing properties about corpus.
	private JPanel propertiesPanel() {
		JPanel panel = new JPanel();
		panel.setBackground(backColor());
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(filePanel());
		panel.add(typePanel());
		panel.add(namePanel());
		return panel;
	}

	// Add button for choosing file.
	private JPanel filePanel() {
		JPanel filePanel = new JPanel();
		filePanel.setBackground(backColor());
		filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.X_AXIS));
		filePanel.setBorder(
				BorderFactory.createCompoundBorder(fileTitle,
					BorderFactory.createEmptyBorder(0,5,5,5)));
		filePanel.add(sep());
		filePanel.add(fileLabel);
		filePanel.add(horGlue());
		return filePanel;
	}

	// Type.
	private JPanel typePanel() {
		JPanel typePanel = new JPanel();
		typePanel.setBackground(backColor());
		typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.X_AXIS));
		typePanel.setBorder(
				BorderFactory.createCompoundBorder(typeTitle,
					BorderFactory.createEmptyBorder(0,5,5,5)));
		typePanel.add(sep());
		typePanel.add(typeLabel);
		typePanel.add(horGlue());
		return typePanel;
	}

	// Names
	private JPanel namePanel() {
		JPanel namePanel = new JPanel();
		namePanel.setBackground(backColor());
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
		namePanel.setBorder(
				BorderFactory.createCompoundBorder(nameTitle,
					BorderFactory.createEmptyBorder(0,5,5,5)));
		namePanel.add(sep());
		namePanel.add(nameField);
		namePanel.add(horGlue());
		return namePanel;
	}

	// Any change is recorded.
	public void changedUpdate(DocumentEvent e) {
		changed = true;
	}
	public void insertUpdate(DocumentEvent e) {
		changed = true;
	}
	public void removeUpdate(DocumentEvent e) {
		changed = true;
	}

	// Take properties to corpus.
	private void storeProperties() {
		if (corpus != null && changed)
			corpus.setName(nameField.getText());
	}

	//////////////////////////////////////////////////////////
	// Edit a text.

	// Last directory where a text was stored, if any.
	private String textDir = null;

	// See whether it is not already being edited.
	// If yes, raise the corresponding window.
	// If not, open new window for editing.
	private void editText(Text text) {
		for (int i = 0; i < textEditors.size(); i++) {
			TextEditor old = (TextEditor) textEditors.get(i);
			if (old.getText() == text) {
				old.setVisible(true);
				return;
			}
		}
		TextEditor editor = new TextEditor(text) {
			protected File textDirectory() {
				if (textDir != null)
					return new File(textDir);
				else if (corpus != null) {
					File corpusDir = new File(corpus.getLocation());
					return new File(corpusDir.getParent(), Settings.defaultTextDir);
				} else
					return new File(Settings.defaultTextDir);
			}
			protected Text addText(File file) {
				return addTextToCorpus(file);
			}
			protected void removeText(Text text) {
				removeTextFromCorpus(text);
			}
			protected void redoIndex() {
				if (corpus != null) {
					corpus.refreshTrees();
					makeIndices();
					changed = true;
				}
			}
			protected void view(Text text) {
				selectTextForView(text);
			}
			protected void exit() {
				textEditors.remove(this);
			}
		};
		textEditors.add(editor);
	}

	// Remove text from corpus.
	private void removeTextFromCorpus(Text text) {
		if (corpus != null) {
			corpus.removeText(text);
			corpus.refreshTrees();
			makeIndices();
			changed = true;
		}
	}

	// Add text for file to corpus. Make note of directory in
	// which it occurs. Report if fails.
	// If text is already being edited, then ignore.
	private Text addTextToCorpus(File file) {
		Text text = null;
		if (corpus != null)
			try {
				text = corpus.addText(file);
				corpus.refreshTrees();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this,
						"Could not add text: " + e.getMessage(),
						"File error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
		if (text != null) {
			textDir = file.getParent();
			makeIndices();
			changed = true;
		} 
		return text;
	}

	/////////////////////////////////////////////////////
	// View a text, manipulating resources.

	// Open viewer of text, with its various resources.
	// Only if viewer does not already exist.
	protected void tryViewText(Text text) {
		for (int i = 0; i < textViewers.size(); i++) {
			TextViewer old = (TextViewer) textViewers.get(i);
			if (old.getText() == text) {
				old.setVisible(true);
				return;
			}
		}
		TextViewer viewer = getTextViewer(text);
		if (viewer != null)
			textViewers.add(viewer);
	}

	protected abstract TextViewer getTextViewer(Text text);

	///////////////////////////////////////////////////
	// Export to PDF.

	private void pdfExport() {
		fileSelection = true;
		enableDisable();
		if (dirWindow == null) {
			dirWindow =
				new DirectoryChoosingWindow() {
					public void choose(File f) {
						setCursor(new Cursor(Cursor.WAIT_CURSOR));
						writePdfTo(f);
						setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						fileSelection = false;
						enableDisable();
					}
					public void exit() {
						fileSelection = false;
						enableDisable();
					}
				};
			File dir = new File(".");
			if (corpus != null && !corpus.getLocation().startsWith("jar:")) {
				File corpusLoc = new File(corpus.getLocation());
				dir = new File(corpusLoc.getParentFile(), Settings.defaultPdfDir);
			}
			dirWindow.setCurrentDirectory(dir);
		}
		dirWindow.setVisible(true);
	}

	protected void writePdfTo(File dir) {
		if (corpus != null) 
			try {
				corpus.writePdfTo(dir, resourceGenerators(), 
						autoaligner(),
						pdfRenderParameters());
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this,
						"Could not create PDF corpus: " + e.getMessage(),
						"Write error", JOptionPane.ERROR_MESSAGE);
			}
	}

	// Subclass to provide generators of resources..
	protected abstract Vector<ResourceGenerator> resourceGenerators();

	// Device for automatically aligning tiers.
	protected abstract Autoaligner autoaligner();

	// Subclass to provide parameters.
	protected abstract PdfRenderParameters pdfRenderParameters();

	///////////////////////////////////////////////////
	// Enabling/disabling of buttons.

	private void enableDisable() {
		fileMenu.setEnabled(!fileSelection);
		openItem.setEnabled(!fileSelection && hasFileAccess());
		closeItem.setEnabled(!fileSelection);
		moveItem.setEnabled(!fileSelection && editableCorpus());
		textItem.setEnabled(!fileSelection && editableCorpus());
		refreshItem.setEnabled(!fileSelection && editableCorpus());
		pdfItem.setEnabled(!fileSelection && hasFileAccess());

		fileTitle.setTitleColor(!fileSelection ?
				Color.BLACK : Color.GRAY);
		typeTitle.setTitleColor(!fileSelection && corpus != null ?
				Color.BLACK : Color.GRAY);
		nameTitle.setTitleColor(!fileSelection && corpus != null ?
				Color.BLACK : Color.GRAY);

		fileLabel.setEnabled(!fileSelection);
		typeLabel.setEnabled(!fileSelection);
		nameField.setEnabled(!fileSelection && corpus != null);
		nameField.setEditable(!fileSelection && editableCorpus());

		tabbed.setEnabled(!fileSelection);

		repaint();
	}

	// Can corpus be edited?
	private boolean editableCorpus() {
		return corpus != null && corpus.isEditable();
	}

	// Can look in files?
	private boolean hasFileAccess() {
		try {
			return (new File(".")).canWrite();
		} catch (SecurityException e) {
			// ignore
		}
		return false;
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

	// Get user to close text windows.
	private void closeTextEditors() {
		for (int i = 0; i < textEditors.size(); i++) {
			TextEditor editor = (TextEditor) textEditors.get(i);
			editor.setVisible(true);
			editor.trySaveQuit();
		}
	}

	// Get user to close viewing windows. Return whether successful.
	private boolean closeTextViewers() {
		for (int i = 0; i < textViewers.size(); i++) {
			TextViewer viewer = (TextViewer) textViewers.get(i);
			viewer.setVisible(true);
			boolean success = viewer.trySaveQuit();
			if (!success)
				return false;
		}
		return true;
	}

	// Kill all windows, and exit.
	// Do not proceed if there are open text windows.
	public void dispose() {
		for (int i = 0; i < textEditors.size(); i++) {
			TextEditor editor = (TextEditor) textEditors.get(i);
			editor.dispose();
		}
		for (int i = 0; i < textViewers.size(); i++) {
			TextViewer viewer = (TextViewer) textViewers.get(i);
			viewer.dispose();
		}
		if (fileWindow != null)
			fileWindow.dispose();
		if (dirWindow != null)
			dirWindow.dispose();
		if (helpWindow != null)
			helpWindow.dispose();
		if (renderParameters != null)
			renderParameters.dispose();
		super.dispose();
		if (standAlone)
			System.exit(0);
	}

	// Try saving. Report if not successful. Return whether continue.
	private boolean trySave() {
		closeTextEditors();
		if (!closeTextViewers())
			return false;
		storeProperties();
		if (corpus != null && changed) {
			try {
				corpus.save();
				changed = false;
			} catch (IOException e) {
				if (userConfirmsLoss("Could not save corpus. Want to close anyway?")) {
					changed = false;
					return true;
				} else
					return false;
			}
		}
		return true;
	}

	// Listen if window to be closed or iconified.
	// Open quit, save.
	private class CloseListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			if (trySave())
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

	/////////////////////////////////////////////////////
	// Auxiliary GUI.

	// Field this is not bigger than necessary.
	private class SmallField extends SpecialTextField {
		public SmallField(int size) {
			super(size);
			setMaximumSize(getPreferredSize());
			setFont(Settings.inputTextFont());
			getDocument().addDocumentListener(CorpusViewer.this);
		}
	}

	// Label with custom font.
	private class PlainLabel extends JLabel {
		public PlainLabel(String s) {
			super(s);
			setFont(Settings.labelFont(Font.PLAIN));
		}
	}

	// Horizontal glue.
	private Component horGlue() {
		return Box.createHorizontalGlue();
	}
	// Vertical glue.
	private Component vertGlue() {
		return Box.createVerticalGlue();
	}
	// Some separation between panels.
	private Component sep() {
		return Box.createRigidArea(new Dimension(10, 10));
	}

	// Backgroup color.
	private Color backColor() {
		return Color.WHITE;
	}

}
