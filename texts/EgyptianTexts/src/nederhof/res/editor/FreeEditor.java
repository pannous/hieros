/***************************************************************************/
/*                                                                         */
/*  FreeEditor.java                                                        */
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

// Window in which text and embedded RES can be edited.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.*;
import javax.swing.undo.*;

import nederhof.res.*;
import nederhof.util.*;
import nederhof.util.xml.*;

public class FreeEditor extends JFrame implements KeyListener, ActionListener {

    // Context for parsing.
    private IParsingContext context = new ParsingContext(true);

    // Main panel of editing hieroglyphic.
    private FragmentPanel fragmentPanel;

    // The listener to closing. To be passed to auxiliary frames.
    private CloseListener closeListener = new CloseListener();

    // Auxiliary windows: settings window, help window, file window.
    private JFrame settingsWindow = null;
    private JFrame helpWindow = null;
    private FileChoosingWindow fileWindow = null;

    // Defaults for new RES fragments.
    private int direction = ResValues.DIR_NONE;
    private float size = Float.NaN;
    // Whether XML entities are to be used.
    private boolean resEscaped = Settings.ResEscaped;
    // Size of hieroglyphic font in preview.
    private int previewHieroFontSize = Settings.previewHieroFontSize;
    // Size of hieroglyphic font in tree.
    private int treeHieroFontSize = Settings.treeHieroFontSize;

    // The entire split panel, the text panel and the editing panel.
    private JSplitPane main;
    private ContentEditorPane textPanel = new ContentEditorPane() {
        protected void enableSaving(boolean b) {
	    enableSaveItem(b);
        }
        protected void enableUndo(boolean b) {
	    enableUndoItem(b);
        }
        protected void enableRedo(boolean b) {
	    enableRedoItem(b);
        }
        protected void showStatus(String status) {
	    FreeEditor.this.showStatus(status);
        }
        protected void showEmphasizedStatus(String status) {
	    FreeEditor.this.showEmphasizedStatus(status);
        }
        protected void showErrorStatus(String status) {
	    FreeEditor.this.showErrorStatus(status);
        }
    };

    // Create editor window, without file at beginning.
    public FreeEditor() {
	this(null);
    }

    // Create editor window, with file at beginning.
    public FreeEditor(File file) {
	setTitle("RES Editor");
	setJMenuBar(getMenu()); 
	setSize(Settings.displayWidthInit, Settings.displayHeightInit);

	JScrollPane textScroller = new JScrollPane(textPanel);
	textScroller.getVerticalScrollBar().setUnitIncrement(10);
	textScroller.setMinimumSize(new Dimension(50, 50));

	main = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	main.setLeftComponent(textScroller);
	main.setOneTouchExpandable(true);
	main.setDividerSize((int) (main.getDividerSize() * 1.3));
	main.setResizeWeight(1.0);
	main.setDividerLocation(1.0);
	getContentPane().add(main);

	addKeyListener(this);
	textPanel.addKeyListener(this);
	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	addWindowListener(new CloseListener());
	addComponentListener(new MoveListener());

	if (file != null) {
	    textPanel.setPage(file);
	    resEscaped = FileAux.hasExtension(file, "xml");
	}

	setVisible(true);
    }

    //////////////////////////////////////////////////////////////////////////////
    // Buttons at top.

    // Items in menu that may be disabled/enabled.
    private final JMenuItem openItem = new EnabledMenuItem(this,
	    "<u>O</u>pen", "open", KeyEvent.VK_O);
    private final JMenuItem newItem = new EnabledMenuItem(this,
	    "<u>N</u>ew", "new", KeyEvent.VK_N);
    private final JMenuItem saveItem = new EnabledMenuItem(this,
	    "<u>S</u>ave", "save", KeyEvent.VK_S);
    private final JMenuItem asItem = new EnabledMenuItem(this,
	    "save <u>A</u>s", "as", KeyEvent.VK_A);
    private final JMenuItem quitItem = new EnabledMenuItem(this,
	    "qu<u>I</u>t", "quit", KeyEvent.VK_I);
    private final JMenuItem undoItem = new EnabledMenuItem(this,
	    "<u>U</u>ndo", "undo", KeyEvent.VK_U);
    private final JMenuItem redoItem = new EnabledMenuItem(this,
	    "<u>R</u>edo", "redo", KeyEvent.VK_R);
    private final JMenuItem editItem = new EnabledMenuItem(this,
	    "<u>E</u>dit", "edit", KeyEvent.VK_E);
    private final JMenuItem grabItem = new EnabledMenuItem(this,
	    "gra<u>B</u>", "grab", KeyEvent.VK_B);
    private final JMenuItem accessItem = new EnabledMenuItem(this,
	    "a<u>C</u>cess", "access", KeyEvent.VK_C);
    private final JMenuItem jumpItem = new EnabledMenuItem(this,
	    "<u>J</u>ump", "jump", KeyEvent.VK_J);
    private final JMenuItem textItem = new EnabledMenuItem(this,
	    "<u>T</u>ext", "text", KeyEvent.VK_T);
    private final JMenuItem normalizeItem = new EnabledMenuItem(this,
	    "normali<u>Z</u>e", "normalize", KeyEvent.VK_Z);
    private final JMenuItem flattenItem = new EnabledMenuItem(this,
	    "f<u>L</u>atten", "flatten", KeyEvent.VK_L);
    private final JMenuItem swapItem = new EnabledMenuItem(this,
	    "s<u>W</u>ap", "swap", KeyEvent.VK_W);

    // Button showing status.
    private JButton statusButton = new JButton("");

    private JMenuBar getMenu() {
	final int STRUT_SIZE = 10;
	JMenuBar box = new JMenuBar();
	box.setLayout(new BoxLayout(box, BoxLayout.X_AXIS));
	box.setBackground(Color.LIGHT_GRAY);
	box.add(Box.createHorizontalStrut(STRUT_SIZE));

	// file
	JMenu fileMenu = new JMenu("<html><u>F</u>ile</html>");
	fileMenu.setMnemonic(KeyEvent.VK_F);
	fileMenu.setBackground(Color.LIGHT_GRAY);
	box.add(fileMenu);
	box.add(Box.createHorizontalStrut(STRUT_SIZE));
	// open, new
	fileMenu.add(openItem);
	fileMenu.add(newItem);
	fileMenu.addSeparator();
	// save, save as
	fileMenu.add(saveItem);
	fileMenu.add(asItem);
	fileMenu.addSeparator();
	saveItem.setEnabled(false);
	// quit
	fileMenu.add(quitItem);

	// mode
	JMenu modeMenu = new JMenu("<html><u>M</u>ode</html>");
	modeMenu.setMnemonic(KeyEvent.VK_M);
	modeMenu.setBackground(Color.LIGHT_GRAY);
	box.add(modeMenu);
	box.add(Box.createHorizontalStrut(STRUT_SIZE));
	// undo, redo
	modeMenu.add(undoItem);
	modeMenu.add(redoItem);
	modeMenu.addSeparator();
	textPanel.enableUndoRedo();
	// edit, grab, access, jump
	modeMenu.add(editItem);
	modeMenu.add(grabItem);
	modeMenu.add(accessItem);
	modeMenu.add(jumpItem);
	modeMenu.addSeparator();
	// text
	modeMenu.add(textItem);
	modeMenu.add(normalizeItem);
	modeMenu.add(flattenItem);
	modeMenu.add(swapItem);
	textItem.setEnabled(false);
	normalizeItem.setEnabled(false);
	flattenItem.setEnabled(false);
	swapItem.setEnabled(false);

	// settings
	box.add(new ClickButton(this, "settin<u>G</u>s", "settings", KeyEvent.VK_G));
	box.add(Box.createHorizontalStrut(STRUT_SIZE));

	// help
	box.add(new ClickButton(this, "<u>H</u>elp", "help", KeyEvent.VK_H));
	box.add(Box.createHorizontalStrut(STRUT_SIZE));

	// status
	final int BSIZE = 5;
	statusButton.setBorder(new EmptyBorder(BSIZE,BSIZE,BSIZE,BSIZE));
	statusButton.setBackground(Color.LIGHT_GRAY);
	statusButton.setFocusable(false);
	box.add(statusButton);

	box.add(Box.createHorizontalGlue());
	return box;
    }

    // Enabling items externally.
    void enableSaveItem(boolean b) {
	saveItem.setEnabled(b);
    }
    void enableUndoItem(boolean b) {
	undoItem.setEnabled(b);
    }
    void enableRedoItem(boolean b) {
	redoItem.setEnabled(b);
    }

    // Show message in menu.
    void showStatus(String message) {
        showStatus(message, "gray");
    }
    // Show emphasized message in menu.
    void showEmphasizedStatus(String message) {
        showStatus(message, "blue");
    }
    // Same but for error.
    void showErrorStatus(String message) {
        showStatus(message, "red");
    }
    // message in menu in color.
    private void showStatus(String message, String color) {
        statusButton.setText("<html><font color=\"" + color + "\">" +
                message + "</font></html>");
        statusButton.setMaximumSize(statusButton.getPreferredSize());
    }

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("open")) {
	    if (clear())
		openFileWindow("open");
	} else if (e.getActionCommand().equals("new")) {
	    clear();
	} else if (e.getActionCommand().equals("save")) {
	    textPanel.save();
	} else if (e.getActionCommand().equals("as")) {
	    openFileWindow("as");
	} else if (e.getActionCommand().equals("quit")) {
	    dispose();
	} else if (e.getActionCommand().equals("undo")) {
	    if (textPanel.isEditable())
		textPanel.undo();
	    else if (fragmentPanel != null)
		fragmentPanel.undo();
	} else if (e.getActionCommand().equals("redo")) {
	    if (textPanel.isEditable())
		textPanel.redo();
	    else if (fragmentPanel != null)
		fragmentPanel.redo();
	} else if (e.getActionCommand().equals("edit")) {
	    goToEdit();
	} else if (e.getActionCommand().equals("grab")) {
	    textPanel.grab(resEscaped);
	} else if (e.getActionCommand().equals("access")) {
	    textPanel.grab(resEscaped);
	    goToEdit();
	} else if (e.getActionCommand().equals("jump")) {
	    textPanel.jump(resEscaped);
	} else if (e.getActionCommand().equals("text")) {
	    goToText();
	} else if (e.getActionCommand().equals("normalize")) {
	    if (fragmentPanel != null)
		fragmentPanel.normalize();
	} else if (e.getActionCommand().equals("flatten")) {
	    if (fragmentPanel != null)
		fragmentPanel.flatten();
	} else if (e.getActionCommand().equals("swap")) {
	    if (fragmentPanel != null)
		fragmentPanel.swap();
	} else if (e.getActionCommand().equals("settings")) {
	    if (settingsWindow == null) 
		settingsWindow = new SettingsWindow();
	    settingsWindow.setVisible(true);
	} else if (e.getActionCommand().equals("help")) {
	    if (helpWindow == null) {
		URL url = FileAux.fromBase("data/help/res/free_editor.html");
		helpWindow = new HTMLWindow("RES editor manual", url);
	    }
	    helpWindow.setVisible(true);
	}
    }

    // For stand-alone, exit upon window close.
    // (Resource leak in JFileChooser forces us to make this distinction.)
    private boolean standAlone = false;

    // Set whether editor is stand alone.
    public void setStandAlone(boolean standAlone) {
	this.standAlone = standAlone;
    }

    // Listen if window to be closed or iconified.
    // Let legend disappear with iconification of main window.
    private class CloseListener extends WindowAdapter {
	public void windowClosing(WindowEvent e) {
	    dispose();
	}
	public void windowIconified(WindowEvent e) {
	    setState(Frame.ICONIFIED);
	    if (fragmentPanel != null)
		fragmentPanel.setVisible(false);
	}
	public void windowDeiconified(WindowEvent e) {
            setState(Frame.NORMAL);
	    if (fragmentPanel != null)
		fragmentPanel.setVisible(true);
	}
    }

    // Listen if window moved. Move legend with it.
    private class MoveListener extends ComponentAdapter {
        int NONE = Integer.MAX_VALUE;
        int xOld = NONE;
        int yOld = NONE;
        public void componentMoved(ComponentEvent e) {
            if (e.getComponent() != FreeEditor.this || fragmentPanel == null)
                return;
            int xNew = getLocation().x;
            int yNew = getLocation().y;
            int xMin = xNew + getWidth();
            int yMin = yNew + getHeight();
            if (xOld != NONE && yOld != NONE)
                fragmentPanel.moveLegend(xNew-xOld, yNew-yOld, xMin, yMin);
            xOld = xNew;
            yOld = yNew;
        }
    }

    // Kill all windows, and exit.
    public void dispose() {
	if (!clear())
	    return;
	if (fragmentPanel != null)
	    fragmentPanel.dispose();
	if (glyphChooser != null)
	    glyphChooser.dispose();
	if (settingsWindow != null)
	    settingsWindow.dispose();
	if (helpWindow != null)
	    helpWindow.dispose();
	if (fileWindow != null)
	    fileWindow.dispose();
	super.dispose();
	if (standAlone)
	    System.exit(0);
    }

    // Clear contents of text panel and hiero panel.
    // Return whether cleared.
    private boolean clear() {
	if (textPanel.beenChanged() || 
		(fragmentPanel != null && fragmentPanel.modified())) {
            Object[] options = {"proceed", "cancel"};
            int answer = JOptionPane.showOptionDialog(this,
                    "Do you want to proceed and discard the existing contents?",
                    "warning: impending loss of data",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[1]);
            if (answer != 0)
                return false;
	}
	if (!textPanel.isEditable())
	    goToText();
	textPanel.clear();
	return true;
    }

    ///////////////////////////////////////////////////////////////
    // Choosing files, reading, saving.

    // The action that is to be performed upon selecting a file.
    private String fileAction = "none";

    // Open selector of file.
    private void openFileWindow(String action) {
	fileAction = action;
	if (fileWindow == null) {
	    fileWindow = 
		new FileChoosingWindow("XML/lite files", new String[] {"xml", "lite"}) {
		    protected void choose(File file) {
			if (fileAction.equals("open")) {
			    if (clear())
				textPanel.setPage(file);
			} else if (fileAction.equals("as")) 
			    textPanel.save(file);
		    }
		};
	    fileWindow.setCurrentDirectory(currentDir());
	}
	fileWindow.setVisible(true);
    }

    // Let directory depend on whether there is existing file in
    // text panel.
    private File currentDir() {
	if (textPanel.getFile() != null)
	    return textPanel.getFile().getParentFile();
	else
	    return new File(Settings.defaultDir);
    }

    ///////////////////////////////////////////////////////////////
    // Window for settings.

    private class SettingsWindow extends JFrame implements ActionListener, ItemListener {
	private JRadioButton noDir = new JRadioButton("none");
	private JRadioButton hlrDir = new JRadioButton("hlr");
	private JRadioButton hrlDir = new JRadioButton("hrl");
	private JRadioButton vlrDir = new JRadioButton("vlr");
	private JRadioButton vrlDir = new JRadioButton("vrl");
	private JCheckBox sizeCheck = new JCheckBox();
	private SpinnerNumberModel sizeModel = 
	    new SpinnerNumberModel(1.0, 0.01, 9.99, 0.1);
	private JSpinner sizeSpinner = new JSpinner(sizeModel);
	private JComboBox previewCombo;
	private JComboBox treeCombo;
	private JCheckBox escapeCheck = new JCheckBox();

	public SettingsWindow() {
	    final int STRUT_SIZE = 6;
	    setTitle("Settings");
	    setJMenuBar(new QuitMenu(this));
	    Container content = getContentPane();
	    content.setLayout(new BorderLayout());
	    JPanel mainPanel = new JPanel();
	    JPanel buttonsPanel = new JPanel();
	    content.add(mainPanel, BorderLayout.WEST);
	    content.add(buttonsPanel, BorderLayout.SOUTH);
	    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
	    buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));

	    // Hieroglyphic fonts.
	    JPanel fontPanel = new JPanel(new SpringLayout());
	    fontPanel.setBorder(
		    BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("Hieroglyphic font"),
			BorderFactory.createEmptyBorder(5,5,5,5)));
	    previewCombo = new JComboBox(getFontSizes(30, 70, 2));
	    previewCombo.setRenderer(new FontSizeRenderer());
	    previewCombo.setMaximumRowCount(14); 
	    treeCombo = new JComboBox(getFontSizes(40, 80, 2));
	    treeCombo.setRenderer(new FontSizeRenderer());
	    treeCombo.setMaximumRowCount(14);
	    fontPanel.add(new JLabel("Preview:"));
	    fontPanel.add(previewCombo);
	    fontPanel.add(new JLabel("pt"));
	    fontPanel.add(new JLabel("Tree:"));
	    fontPanel.add(treeCombo);
	    fontPanel.add(new JLabel("pt"));
	    mainPanel.add(fontPanel);
	    SpringUtilities.makeCompactGrid(fontPanel, 2, 3, 5, 5, 5, 5);
	    mainPanel.add(Box.createVerticalStrut(STRUT_SIZE));

	    // New hieroglyphic.
	    JPanel newPanel = new JPanel(new SpringLayout());
	    newPanel.setBorder(
		    BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("New fragments"),
			BorderFactory.createEmptyBorder(5,5,5,5)));
	    newPanel.add(new JLabel("Direction:"));
	    JPanel dirPanel = new JPanel();
	    JPanel dirYesPanel = new JPanel();
	    dirPanel.setLayout(new BoxLayout(dirPanel, BoxLayout.X_AXIS));
	    dirYesPanel.setLayout(new GridLayout(2, 2));
	    dirPanel.add(noDir);
	    dirPanel.add(dirYesPanel);
	    dirYesPanel.add(hlrDir);
	    dirYesPanel.add(hrlDir);
	    dirYesPanel.add(vlrDir);
	    dirYesPanel.add(vrlDir);
	    ButtonGroup group = new ButtonGroup();
	    group.add(noDir);
	    group.add(hlrDir);
	    group.add(hrlDir);
	    group.add(vlrDir);
	    group.add(vrlDir);
	    newPanel.add(dirPanel);

	    newPanel.add(new JLabel("Size:"));
	    JPanel sizePanel = new JPanel();
	    sizePanel.setLayout(new BoxLayout(sizePanel, BoxLayout.X_AXIS));
	    sizePanel.add(sizeCheck);
	    sizePanel.add(sizeSpinner);
	    newPanel.add(sizePanel);
	    mainPanel.add(newPanel);
	    sizeCheck.addItemListener(this);
	    sizeSpinner.setEnabled(false);
	    SpringUtilities.makeCompactGrid(newPanel, 2, 2, 5, 5, 5, 5);
	    mainPanel.add(Box.createVerticalStrut(STRUT_SIZE));

	    // XML entities.
	    JPanel contextPanel = new JPanel(new SpringLayout());
	    contextPanel.setBorder(
		    BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("Embedding"),
			BorderFactory.createEmptyBorder(5,5,5,5)));
	    contextPanel.add(escapeCheck);
	    contextPanel.add(new JLabel("XML entities"));
	    contextPanel.add(Box.createHorizontalGlue());
	    mainPanel.add(contextPanel);
	    SpringUtilities.makeCompactGrid(contextPanel, 1, 3, 5, 5, 5, 5);

	    // Confirmation buttons.
	    buttonsPanel.add(Box.createHorizontalGlue());
	    buttonsPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
	    buttonsPanel.add(new SettingButton(this,
			"<html><u>O</u>kay</html>", "okay", KeyEvent.VK_O));
	    buttonsPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
	    buttonsPanel.add(new SettingButton(this,
			"<html><u>A</u>pply</html>", "apply", KeyEvent.VK_A));
	    buttonsPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
	    buttonsPanel.add(new SettingButton(this,
			"<html><u>D</u>efaults</html>", "defaults", KeyEvent.VK_D));
	    buttonsPanel.add(Box.createHorizontalGlue());

	    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	    addWindowListener(new ConservativeListener(this));
	    pack();
	    makeSettingsVisible();
	}

	// Selection has changed.
	public void itemStateChanged(ItemEvent e) {
	    if (e.getStateChange() == ItemEvent.SELECTED) {
		sizeSpinner.setEnabled(true);
	    } else if (e.getStateChange() == ItemEvent.DESELECTED) {
		sizeSpinner.setEnabled(false);
	    }
	}

	// Actions belonging to buttons.
	public void actionPerformed(ActionEvent e) {
	    if (e.getActionCommand().equals("okay")) {
		applySettings();
		setVisible(false);
	    } else if (e.getActionCommand().equals("apply")) {
		applySettings();
	    } else if (e.getActionCommand().equals("defaults")) {
		makeDefaultSettingsVisible();
	    } else if (e.getActionCommand().equals("quit")) {
		makeSettingsVisible();
		setVisible(false);
	    }
	}

	// Apply values from screen.
	private void applySettings() {
	    if (hlrDir.isSelected())
		direction = ResValues.DIR_HLR;
	    else if (hrlDir.isSelected())
		direction = ResValues.DIR_HRL;
	    else if (vlrDir.isSelected())
		direction = ResValues.DIR_VLR;
	    else if (vrlDir.isSelected())
		direction = ResValues.DIR_VRL;
	    else
		direction = ResValues.DIR_NONE;
	    if (sizeCheck.isSelected()) {
		Number num = (Number) sizeModel.getNumber();
		size = num.floatValue();
	    } else 
		size = Float.NaN;
	    Integer previewInteger = (Integer) previewCombo.getSelectedItem();
	    previewHieroFontSize = previewInteger.intValue();
	    Integer treeInteger = (Integer) treeCombo.getSelectedItem();
	    treeHieroFontSize = treeInteger.intValue();
	    if (fragmentPanel != null) {
		fragmentPanel.setPreviewFontSize(previewHieroFontSize);
		fragmentPanel.setTreeFontSize(treeHieroFontSize);
	    }
	    resEscaped = escapeCheck.isSelected();
	}

	// Set values in screen to current values.
	private void makeSettingsVisible() {
	    switch (direction) {
		case ResValues.DIR_HLR:
		    hlrDir.setSelected(true);
		    break;
		case ResValues.DIR_HRL:
		    hrlDir.setSelected(true);
		    break;
		case ResValues.DIR_VLR:
		    vlrDir.setSelected(true);
		    break;
		case ResValues.DIR_VRL:
		    vrlDir.setSelected(true);
		    break;
		default:
		    noDir.setSelected(true);
	    }
	    if (Float.isNaN(size)) {
		sizeCheck.setSelected(false);
		sizeModel.setValue(new Float(1.0f));
		sizeSpinner.setEnabled(false);
	    } else {
		sizeCheck.setSelected(true);
		sizeModel.setValue(new Float(size));
		sizeSpinner.setEnabled(true);
	    }
	    previewCombo.setSelectedItem(new Integer(previewHieroFontSize));
	    treeCombo.setSelectedItem(new Integer(treeHieroFontSize));
	    escapeCheck.setSelected(resEscaped);
	}

	// Set values in screen to default values.
	private void makeDefaultSettingsVisible() {
	    noDir.setSelected(true);
	    sizeCheck.setSelected(false);
	    sizeModel.setValue(new Float(1.0f));
	    sizeSpinner.setEnabled(false);
	    previewCombo.setSelectedItem(new 
		    Integer(Settings.previewHieroFontSize));
	    treeCombo.setSelectedItem(new 
		    Integer(Settings.treeHieroFontSize));
	    escapeCheck.setSelected(Settings.ResEscaped);
	}
    }

    // Get vector of allowable font sizes between min and max.
    private static Vector getFontSizes(int min, int max, int step) {
	Vector vec = new Vector();
	for (int i = min; i <= max; i += step)
	    vec.addElement(new Integer(i));
	return vec;
    }

    // Print font size as string.
    private class FontSizeRenderer implements ListCellRenderer {
	public Component getListCellRendererComponent(JList list, Object value,
		int index, boolean isSelected, boolean cellHasFocus) {
	    Integer i = (Integer) value;
	    return new JLabel(i.toString());
	}
    }

    ///////////////////////////////////////////////////////////////
    // Text mode versus edit mode.

    // Location of divider before going from edit mode to text mode,
    // or negative if not yet been in edit mode.
    private int savedDividerLocation = -1;
    
    // To prevent selection in text panel while editing,
    // thus interfering with saving of edited hieroglyphic,
    // the positions of the selected text are saved.
    private int savedSelectionStart;
    private int savedSelectionEnd;

    // Do editing of hieroglyphic.
    private void goToEdit() {
	String originalHiero = textPanel.getSelectedText();
	if (originalHiero == null)
	    originalHiero = "";
	if (originalHiero.matches("(?s)\\s*"))
	    originalHiero = hieroInitiation();
	if (resEscaped)
	    originalHiero = XmlAux.unescape(originalHiero);
	textPanel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        boolean done = initiateResEditing(originalHiero);
	textPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	if (!done) 
	    return;
	savedSelectionStart = textPanel.getSelectionStart();
	savedSelectionEnd = textPanel.getSelectionEnd();
	textPanel.setEditable(false);
	double totalHeight = main.getSize().height;
	if (savedDividerLocation >= 0 && totalHeight > 0) {
	    double topPart = savedDividerLocation / totalHeight;
	    topPart = Math.max(topPart, 0.0);
	    topPart = Math.min(topPart, 1.0);
	    main.setDividerLocation(topPart);
	} else
	    main.setDividerLocation(0.2);
	fragmentPanel.enableUndoRedo();
	editItem.setEnabled(false);
	grabItem.setEnabled(false);
	accessItem.setEnabled(false);
	jumpItem.setEnabled(false);
	textItem.setEnabled(true);
	normalizeItem.setEnabled(true);
	flattenItem.setEnabled(true);
	swapItem.setEnabled(true);
	fragmentPanel.grabFocus(); 
	// Hack to prevent newly created Legend from grabbing focus.
	setVisible(true);
    }

    // Return to editing of text. Remember position of divider 
    // so it can be restored later.
    private void goToText() {
	textPanel.setEditable(true);
	String editedHiero = "";
	if (fragmentPanel != null) { // is normally true
	    editedHiero = fragmentPanel.contents();
	    fragmentPanel.dispose();
	    main.remove(fragmentPanel);
	    fragmentPanel = null; 
	}
	if (resEscaped)
	    editedHiero = XmlAux.escape(editedHiero);
	textPanel.setSelectionStart(savedSelectionStart);
	textPanel.setSelectionEnd(savedSelectionEnd);
	textPanel.replaceSelection(editedHiero);
	int currentLocation = main.getDividerLocation();
	if (savedDividerLocation < 0 ||
		!similarDividerLocation(savedDividerLocation, currentLocation))
	    savedDividerLocation = currentLocation;
	main.setDividerLocation(1.0);
	textPanel.enableUndoRedo();
	editItem.setEnabled(true);
	grabItem.setEnabled(true);
	accessItem.setEnabled(true);
	jumpItem.setEnabled(true);
	textItem.setEnabled(false);
	normalizeItem.setEnabled(false);
	flattenItem.setEnabled(false);
	swapItem.setEnabled(false);
	textPanel.grabFocus();
	textPanel.getCaret().setVisible(true);
    }

    // We assume divider position is similar to previous position
    // if difference is within margin.
    private final int dividerErrorMargin = 10;

    // Is difference within error margin?
    private boolean similarDividerLocation(int loc1, int loc2) {
	if (loc1 <= loc2)
	    return loc2 - loc1 <= dividerErrorMargin;
	else
	    return loc1 - loc2 <= dividerErrorMargin;
    }

    ////////////////////////////////////////////////////////////////////
    // Auxiliary to text processing.

    // New RES fragments are given settings.
    private String hieroInitiation() {
        Vector args = new Vector();
        switch (direction) {
            case ResValues.DIR_HLR:
                args.add("hlr");
                break;
            case ResValues.DIR_HRL:
                args.add("hrl");
                break;
            case ResValues.DIR_VLR:
                args.add("vlr");
                break;
            case ResValues.DIR_VRL:
                args.add("vrl");
                break;
        }
        if (!Float.isNaN(size))
            args.add("size=" + ResArg.realString(size));
        return ResArg.toString(args);
    }

    ///////////////////////////////////////////////////////////////
    // Processing keyboard input.

    // The keys are passed on to the hieroglyphic if
    // the text panel is not active.
    public void keyTyped(KeyEvent e) {
	if (!textPanel.isEditable() && fragmentPanel != null)
	    fragmentPanel.keyTyped(e);
    }

    // The arrows are passed on to the hieroglyphic if
    // the text panel is not active.
    public void keyPressed(KeyEvent e) {
	if (!textPanel.isEditable() && fragmentPanel != null) 
	    fragmentPanel.keyPressed(e);
    }

    // Ignored.
    public void keyReleased(KeyEvent e) {
	// ignored
    }

    ///////////////////////////////////////////////////////////////
    // Creation of pane for RES editing.

    // Cached chooser of glyphs. To be created only once.
    private GlyphChooser glyphChooser;

    // Get chooser window. Only once.
    private GlyphChooser getGlyphChooserWindow() {
	if (glyphChooser == null)
	    glyphChooser = new GlyphChooser() {
		protected void receive(String name) {
		    if (fragmentPanel != null)
			fragmentPanel.receiveGlyph(name);
		}
	    };
	return glyphChooser;
    }

    // Parse and open panels for editing.
    // Return whether successful.
    public boolean initiateResEditing(String res) {
        ResFragment parsed = parseRes(res);
        if (parsed != null) {
	    fragmentPanel = new FragmentPanel(parsed, this, closeListener,
		    previewHieroFontSize, treeHieroFontSize) {
		protected GlyphChooser getChooserWindow() {
		    return getGlyphChooserWindow();
		}
		protected void enableUndo(boolean b) {
		    enableUndoItem(b);
		}
		protected void enableRedo(boolean b) {
		    enableRedoItem(b);
		}
	    };
	    main.setRightComponent(fragmentPanel);
	    Point thisLocation = getLocation();
	    Point legendLocation =
		new Point(thisLocation.x + getWidth(),  thisLocation.y);
	    fragmentPanel.initialize(legendLocation);
	    return true;
	} else
	    return false;
    }

    // Parse RES. If error, report and set caret to error position.
    private ResFragment parseRes(String text) {
        nederhof.res.parser p = new nederhof.res.parser(text, context);
        Object result = null;
        try {
            result = p.parse().value;
        } catch (Exception e) {
            result = null;
        }
        if (context.nErrors() > 0) {
            JOptionPane.showMessageDialog(this, context.error(0),
                    "Parsing error", JOptionPane.ERROR_MESSAGE);
            int pos = context.errorPos(0);
            if (pos >= 0)
		textPanel.setCaretPosition(
			textPanel.getSelectionStart() + pos);
            return null;
        }
        if (result == null || !(result instanceof ResFragment)) {
            return null;
        } else
            return (ResFragment) result;
    }

    ///////////////////////////////////////////////////////////////

    // Running editor stand-alone.
    public static void main(String[] args) {
	FreeEditor editor;
	if (args.length == 0)
	    editor = new FreeEditor();
	else {
	    String fileName = args[0];
	    File file = new File(fileName);
	    editor = new FreeEditor(file);
	}
	editor.setStandAlone(true);
    }

}
