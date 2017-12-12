/***************************************************************************/
/*                                                                         */
/*  TextEditor.java                                                        */
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

// Window for editing text properties.

package nederhof.corpus.frame;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.corpus.*;
import nederhof.util.*;

public abstract class TextEditor extends JFrame 
		implements ActionListener, DocumentListener {

    // The text being edited. Null if none.
    private Text text = null;

    // Have any properties of text been edited, but not saved.
    private boolean changed = false;

    // Is file being selected.
    private boolean fileSelection = false;

    // The titles.
    private TitledBorder fileTitle = BorderFactory.createTitledBorder(
	    new LineBorder(Color.GRAY, 2), "file");
    private TitledBorder mainNameTitle = BorderFactory.createTitledBorder(
	    new LineBorder(Color.GRAY, 2), "main name");
    private TitledBorder nameTitle = BorderFactory.createTitledBorder(
	    new LineBorder(Color.GRAY, 2), "other name");
    private TitledBorder collectionTitle = BorderFactory.createTitledBorder(
	    new LineBorder(Color.GRAY, 2), "collections");

    // Label containing file.
    private JLabel fileLabel = new PlainLabel("");

    // The panel for editing main names.
    // And for editing other names.
    private JPanel mainNameRows = new JPanel();
    private JPanel nameRows = new JPanel();
    
    // Vector containing panels for editing main names.
    // And for editing other names.
    private Vector mainNames = new Vector();
    private Vector names = new Vector();

    private UniqueLanguageChoose mainNameLanguageChoose = new UniqueLanguageChoose();
    private LanguageChoose nameLanguageChoose = new LanguageChoose();

    // For collections.
    private JPanel collectionRows = new JPanel();
    private Vector collections = new Vector();
    private JButton collectionButton = new EnabledButton(this, "collection");

    // Label containing description.
    private JLabel descrLabel = new PlainLabel("");

    // Auxiliary window.
    private JFrame helpWindow = null;

    // Construct pane.
    public TextEditor(Text text) {
	this.text = text;
	setTitle("Text editor");
	setJMenuBar(new Menu(this));
	setSize(Settings.displayWidthInit, Settings.displayHeightInit);
	Container content = getContentPane();
	content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	addWindowListener(new CloseListener());

	fileLabel.setHorizontalAlignment(SwingConstants.RIGHT);

	JPanel innerPanel = new JPanel();
	innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
	innerPanel.setBackground(backColor());
	innerPanel.add(sep());
	innerPanel.add(fileButtons());
	innerPanel.add(sep());
	innerPanel.add(mainNamePanel());
	innerPanel.add(sep());
	innerPanel.add(namePanel());
	innerPanel.add(sep());
	innerPanel.add(collectionPanel());
	innerPanel.add(sep());
	innerPanel.add(descriptionPanel());
	innerPanel.add(vertGlue());
	content.add(new ScrollConservative(innerPanel));

	showProperties();

	setVisible(true);
	if (text == null)
	    makeFileChooser();
    }

    // Construct window for new text.
    public TextEditor() {
	this(null);
    }

    public Text getText() {
	return text;
    }

    /////////////////////////////////////////////
    // GUI elements.

    // Items in menu that may be disabled/enabled.
    private final JMenu fileMenu = new EnabledMenu(
            "<u>F</u>ile", KeyEvent.VK_F);
    private final JMenuItem openItem = new EnabledMenuItem(this,
            "<u>O</u>pen", "open", KeyEvent.VK_O);
    private final JMenuItem closeItem = new EnabledMenuItem(this,
            "clo<u>S</u>e", "close", KeyEvent.VK_S);
    private final JMenuItem restoreItem = new EnabledMenuItem(this,
            "<u>U</u>ndo all", "restore", KeyEvent.VK_U);
    private final JMenuItem removeItem = new EnabledMenuItem(this,
            "<u>D</u>elete", "remove", KeyEvent.VK_D);
    private final JMenuItem moveItem = new EnabledMenuItem(this,
            "<u>M</u>ove", "move", KeyEvent.VK_M);
    private final JMenuItem viewItem = new EnabledMenuItem(this,
            "vie<u>W</u>", "view", KeyEvent.VK_W);

    // Menu containing exit buttons.
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
            // restore
            fileMenu.add(restoreItem);
            // remove
            fileMenu.add(removeItem);
            // move
            fileMenu.add(moveItem);
	    // view
            fileMenu.add(viewItem);

	    add(new ClickButton(TextEditor.this, 
			"<u>H</u>elp", "help", KeyEvent.VK_H));
	    add(Box.createHorizontalStrut(STRUT_SIZE));

            add(Box.createHorizontalGlue());
        }
    }

    // Listen if window to be closed or iconified.
    // Open quit, save.
    private class CloseListener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
	    if (!fileSelection) 
		trySaveQuit();
        }
        public void windowIconified(WindowEvent e) {
            setState(Frame.ICONIFIED);
        }
        public void windowDeiconified(WindowEvent e) {
            setState(Frame.NORMAL);
        }
    }

    // Add button for choosing file.
    private JPanel fileButtons() {
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
    // Main names.
    private JPanel mainNamePanel() {
	JPanel mainNamePanel = new JPanel();
	mainNamePanel.setBackground(backColor());
	mainNamePanel.setLayout(new BoxLayout(mainNamePanel, BoxLayout.Y_AXIS));
	mainNamePanel.setBorder(
		BorderFactory.createCompoundBorder(mainNameTitle,
		    BorderFactory.createEmptyBorder(0,5,5,5)));
	mainNameRows.setLayout(new BoxLayout(mainNameRows, BoxLayout.Y_AXIS));
	mainNameRows.setBackground(backColor());
	mainNamePanel.add(mainNameRows);
	mainNamePanel.add(mainNameLanguageChoose);
	return mainNamePanel;
    }

    // Names.
    private JPanel namePanel() {
	JPanel namePanel = new JPanel();
	namePanel.setBackground(backColor());
	namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
	namePanel.setBorder(
		BorderFactory.createCompoundBorder(nameTitle,
		    BorderFactory.createEmptyBorder(0,5,5,5)));
	nameRows.setLayout(new BoxLayout(nameRows, BoxLayout.Y_AXIS));
	nameRows.setBackground(backColor());
	namePanel.add(nameRows);
	namePanel.add(nameLanguageChoose);
	return namePanel;
    }

    // Collections.
    private JPanel collectionPanel() {
	JPanel collectionPanel = new JPanel();
	collectionPanel.setBackground(backColor());
	collectionPanel.setLayout(new BoxLayout(collectionPanel, BoxLayout.Y_AXIS));
	collectionPanel.setBorder(
		BorderFactory.createCompoundBorder(collectionTitle,
		    BorderFactory.createEmptyBorder(0,5,5,5)));
	collectionRows.setLayout(new BoxLayout(collectionRows, BoxLayout.Y_AXIS));
	collectionRows.setBackground(backColor());
	collectionPanel.add(collectionRows);
	JPanel buttonPanel = new JPanel();
	buttonPanel.setBackground(backColor());
	buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
	buttonPanel.add(sep());
	buttonPanel.add(collectionButton);
	buttonPanel.add(horGlue());
	collectionPanel.add(buttonPanel);
	return collectionPanel;
    }

    // Add description of resources.
    private JPanel descriptionPanel() {
	JPanel descriptionPanel = new JPanel();
	descriptionPanel.setBackground(backColor());
	descriptionPanel.setLayout(new BoxLayout(descriptionPanel, BoxLayout.X_AXIS));
	descriptionPanel.add(sep());
	descriptionPanel.add(descrLabel);
	descriptionPanel.add(horGlue());
	return descriptionPanel;
    }

    // Enable or disable buttons.
    private void enableDisable() {
	fileMenu.setEnabled(!fileSelection);
	openItem.setEnabled(!fileSelection && text == null);
	closeItem.setEnabled(!fileSelection);
	restoreItem.setEnabled(!fileSelection && changed);
	removeItem.setEnabled(!fileSelection && text != null);
	moveItem.setEnabled(!fileSelection && text != null && text.isEditable());
	viewItem.setEnabled(!fileSelection && text != null);

	fileTitle.setTitleColor(!fileSelection ? 
		Color.BLACK : Color.GRAY);
	fileLabel.setEnabled(!fileSelection);

	mainNameTitle.setTitleColor(!fileSelection && text != null ? 
		Color.BLACK : Color.GRAY);
	for (int i = 0; i < mainNames.size(); i++) {
	    NamePanel name = (NamePanel) mainNames.get(i);
	    name.setEnabled(!fileSelection && text != null);
	    name.setEditable(!fileSelection && text != null && text.isEditable());
	}
	mainNameLanguageChoose.setEnabled(!fileSelection && 
		text != null && text.isEditable());

	nameTitle.setTitleColor(!fileSelection && text != null ? 
		Color.BLACK : Color.GRAY);
	for (int i = 0; i < names.size(); i++) {
	    NamePanel name = (NamePanel) names.get(i);
	    name.setEnabled(!fileSelection && text != null);
	    name.setEditable(!fileSelection && text != null && text.isEditable());
	}
	nameLanguageChoose.setEnabled(!fileSelection && 
		text != null && text.isEditable());

	collectionTitle.setTitleColor(!fileSelection && text != null ? 
		Color.BLACK : Color.GRAY);
	for (int i = 0; i < collections.size(); i++) {
	    CollectionPanel collection = 
		    (CollectionPanel) collections.get(i);
	    collection.setEnabled(!fileSelection && text != null);
	    collection.setEditable(!fileSelection && text != null && text.isEditable());
	}
	collectionButton.setEnabled(!fileSelection && text != null &&
		text.isEditable());

	repaint();
    }

    // Label with custom font.
    private class PlainLabel extends JLabel {
        public PlainLabel(String s) {
            super(s);
            setFont(Settings.labelFont(Font.PLAIN));
        }
    }
    private class BoldLabel extends JLabel {
        public BoldLabel(String s) {
            super(s);
            setFont(Settings.labelFont(Font.BOLD));
        }
        public BoldLabel(String s, int c) {
            super(s, c);
            setFont(Settings.labelFont(Font.BOLD));
        }
    }

    ////////////////////////////////////////
    // Properties of text.

    // For newly taken text, put properties in panel.
    private void showProperties() {
	mainNameRows.removeAll();
	nameRows.removeAll();
	mainNames.clear();
	names.clear();
	mainNameLanguageChoose.resetLanguages();
	collectionRows.removeAll();
	collections.clear();

	if (text == null) {
	    fileLabel.setText("");
	    descrLabel.setText("");
	} else {
	    fileLabel.setText(text.getLocation());
	    descrLabel.setText(text.getDescription());

	    TreeMap mainName = text.getMainName();
	    for (Iterator it = mainName.keySet().iterator(); it.hasNext(); ) {
		String lang = (String) it.next();
		String name = (String) mainName.get(lang);
		addMainName(lang, name);
	    }
	    TreeMap langNames = text.getNames();
	    for (Iterator it = langNames.keySet().iterator(); it.hasNext(); ) {
		String lang = (String) it.next();
		TreeSet names = (TreeSet) langNames.get(lang);
		for (Iterator it2 = names.iterator(); it2.hasNext(); ) {
		    String name = (String) it2.next();
		    addName(lang, name);
		}
	    }
	    Vector textCollections = text.getCollections();
	    for (int i = 0; i < textCollections.size(); i++) {
		CollectionItem collection = (CollectionItem) textCollections.get(i);
		addCollection(collection);
	    }
	}
	changed = false;
	enableDisable();
    }

    // Store properties from panel in text.
    private void storeProperties() {
	TreeMap textMainNames = new TreeMap();
	for (int i = 0; i < mainNames.size(); i++) {
	    NamePanel panel = (NamePanel) mainNames.get(i);
	    String lang = panel.getLanguage();
	    String name = panel.getName();
	    if (!name.matches("\\s*")) 
		textMainNames.put(lang, name);
	}
	text.setMainName(textMainNames);

	TreeMap textNames = new TreeMap();
	for (int i = 0; i < names.size(); i++) {
	    NamePanel panel = (NamePanel) names.get(i);
	    String lang = panel.getLanguage();
	    String name = panel.getName();
	    if (!name.matches("\\s*")) {
		if (textNames.get(lang) == null)
		    textNames.put(lang, new TreeSet());
		TreeSet langNames = (TreeSet) textNames.get(lang);
		langNames.add(name);
	    }
	}
	text.setNames(textNames);

	Vector textCollections = new Vector();
	for (int i = 0; i < collections.size(); i++) {
	    CollectionPanel panel = (CollectionPanel) collections.get(i);
	    CollectionItem collect = panel.getItem();
	    textCollections.add(collect);
	}
	text.setCollections(textCollections);
    }

    // Add main name to panel.
    private void addMainName(String language, String name) {
	NamePanel row = new NamePanel(language, name);
	mainNameRows.add(row);
	mainNames.add(row);
	mainNameRows.add(sep());
	mainNameRows.revalidate();
	mainNameLanguageChoose.removeLanguage(language);
	row.toField();
    }

    // Add name to panel.
    private void addName(String language, String name) {
	NamePanel row = new NamePanel(language, name);
	nameRows.add(row);
	names.add(row);
	nameRows.add(sep());
	nameRows.revalidate();
	row.toField();
    }

    // Add collection to panel.
    private void addCollection(CollectionItem item) {
	CollectionPanel row = new CollectionPanel(item);
	if (collectionRows.getComponentCount() > 0) {
	    collectionRows.add(sep());
	    collectionRows.add(sep());
	}
	collectionRows.add(row);
	collections.add(row);
	collectionRows.revalidate();
	row.toFirstField();
    }

    // Listen to buttons of editing properties.
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("open")) {
	    makeFileChooser();
	} else if (e.getActionCommand().equals("close")) {
	    trySaveQuit();
        } else if (e.getActionCommand().equals("restore")) {
	    if (userConfirmsLoss("Discard all unsaved edits to text?"))
		showProperties();
        } else if (e.getActionCommand().equals("remove")) {
	    if (text != null && userConfirmsLoss("Remove text from corpus?")) 
		removeText(text);
	    exit();
	    dispose();
        } else if (e.getActionCommand().equals("move")) {
	    makeMoveChooser();
        } else if (e.getActionCommand().equals("collection")) {
	    addCollection(new CollectionItem());
        } else if (e.getActionCommand().equals("view")) {
	    if (text != null) 
		view(text);
        } else if (e.getActionCommand().equals("help")) {
	    if (helpWindow == null) {
		URL url = FileAux.fromBase("data/help/text/editor.html");
		helpWindow = new HTMLWindow("Text editor manual", url);
	    }
	    helpWindow.setVisible(true);
	}
    }

    // Try saving and quit. Report if not successful.
    public void trySaveQuit() {
	if (text != null && changed) {
	    storeProperties();
	    try {
		text.save();
		redoIndex();
	    } catch (IOException e) {
		if (!userConfirmsLoss("Could not save text. Want to close anyway?"))
		    return;
	    }
	}
	exit();
	dispose();
    }

    // Kill all windows, and exit.
    public void dispose() {
	super.dispose();
	if (helpWindow != null)
	    helpWindow.dispose();
    }

    // Try moving to other file.
    private void moveTo(File file) {
	file = FileAux.getRelativePath(file);
	if (text != null) {
	    try {
		text.moveTo(file);
		fileLabel.setText(text.getLocation());
		changed = true;
		redoIndex();
	    } catch (IOException e) {
		JOptionPane.showMessageDialog(this,
			"Could not move text: " + e.getMessage(),
			"File error", JOptionPane.ERROR_MESSAGE);
	    }
	}
    }

    // Make chooser of new file.
    private void makeFileChooser() {
	fileSelection = true;
	enableDisable();
	FileChoosingWindow chooser = 
	    new FileChoosingWindow("XML files", new String[] {"xml"}) {
		public void choose(File file) {
		    text = addText(file);
		    fileSelection = false;
		    showProperties();
		    dispose();
		}
		public void exit() {
		    fileSelection = false;
		    enableDisable();
		    dispose();
		}
	    };
	chooser.setCurrentDirectory(textDirectory());
    }

    // Make chooser of file for moving.
    private void makeMoveChooser() {
	fileSelection = true;
	enableDisable();
	FileChoosingWindow chooser = 
	    new FileChoosingWindow("XML files", new String[] {"xml"}) {
		public void choose(File file) {
		    moveTo(file);
		    fileSelection = false;
		    enableDisable();
		    dispose();
		}
		public void exit() {
		    fileSelection = false;
		    enableDisable();
		    dispose();
		}
	    };
	if (text != null)
	    chooser.setSelectedFile(new File(text.getLocation()));
	else
	    chooser.setCurrentDirectory(textDirectory());
    }

    /////////////////////////////////////
    // Auxiliary GUI elements.

    // Row in editing names.
    private class NamePanel extends JPanel {
	// For making all labels same width;
	private JLabel dummyLabel = new BoldLabel("xxxx ");

	// The language.
	private String lang;
	// The language label.
	private JLabel langLabel;
	// The name.
	private JTextField field;

	public NamePanel(String lang, String name) {
	    this.lang = lang;
	    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	    setBackground(backColor());
	    add(sep());
	    langLabel = new BoldLabel(lang + " ");
	    langLabel.setPreferredSize(dummyLabel.getPreferredSize());
	    langLabel.setHorizontalAlignment(SwingConstants.LEFT);
	    field = new SmallField(50, name);
	    add(langLabel);
	    add(field);
	    add(horGlue());
	    field.requestFocus();
	}

	public String getLanguage() {
	    return lang;
	}
	public String getName() {
	    return field.getText();
	}

	public void setEnabled(boolean allow) {
	    langLabel.setEnabled(allow);
	    field.setEnabled(allow);
	}
	public void setEditable(boolean allow) {
	    field.setEditable(allow);
	}

	public void toField() {
	    field.requestFocus();
	}
    }

    // Row for choosing language.
    private class LanguageChoose extends JPanel 
    		implements ActionListener {
	// The box containing language.
	protected JComboBox box;

	public LanguageChoose() {
	    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	    setBackground(backColor());
	    add(sep());
	    box = new JComboBox(EuropeanLanguages.getLanguages());
	    box.setMaximumRowCount(10);
	    box.setMaximumSize(box.getPreferredSize());
	    box.setRenderer(new EuropeanLanguageRenderer());
	    box.addActionListener(this);
	    add(box);
	    add(horGlue());
	}

	public void actionPerformed(ActionEvent e) {
	    String lang = (String) box.getSelectedItem();
	    if (lang.matches("\\s*"))
		return;
	    addName(lang, "");
	}

	public void setEnabled(boolean allow) {
	    box.setEnabled(allow);
	}
    }

    // Row for choosing language with uniqueness.
    private class UniqueLanguageChoose extends LanguageChoose {
	public void actionPerformed(ActionEvent e) {
	    String lang = (String) box.getSelectedItem();
	    if (lang == null || lang.matches("\\s*"))
		return;
	    addMainName(lang, "");
	}
	// Take away language.
	public void removeLanguage(String lang) {
	    box.removeItem(lang);
	}
	// Allow all languages again.
	public void resetLanguages() {
	    box.removeAllItems();
	    Vector langs = EuropeanLanguages.getLanguages();
	    for (int i = 0; i < langs.size(); i++) 
		box.addItem(langs.get(i));
	}
    }

    // Row in editing collections.
    private class CollectionPanel extends JPanel {
	// The labels.
	private JLabel kindLabel = new BoldLabel("kind: ", SwingConstants.RIGHT);
	private JLabel collectLabel = new BoldLabel("collect: ", SwingConstants.RIGHT);
	private JLabel collectKeyLabel = new BoldLabel("key: ", SwingConstants.RIGHT);
	private JLabel sectionLabel = new BoldLabel("section: ", SwingConstants.RIGHT);
	private JLabel sectionKeyLabel = new BoldLabel("key: ", SwingConstants.RIGHT);
	private JLabel subsectionLabel = new BoldLabel("subsection: ", SwingConstants.RIGHT);
	private JLabel subsectionKeyLabel = new BoldLabel("key: ", SwingConstants.RIGHT);
	private JLabel subsubsectionLabel = new BoldLabel("subsubsection: ", SwingConstants.RIGHT);
	private JLabel subsubsectionKeyLabel = new BoldLabel("key: ", SwingConstants.RIGHT);
	// The fields.
	private JTextField kindField;
	private JTextField collectField;
	private JTextField collectKeyField;
	private JTextField sectionField;
	private JTextField sectionKeyField;
	private JTextField subsectionField;
	private JTextField subsectionKeyField;
	private JTextField subsubsectionField;
	private JTextField subsubsectionKeyField;

	public CollectionPanel(CollectionItem item) {
	    setLayout(new SpringLayout());
	    setBackground(backColor());
	    kindField = new SmallField(25, item.kind);
	    collectField = new SmallField(25, item.collect);
	    collectKeyField = new SmallField(15, item.collectKey);
	    sectionField = new SmallField(25, item.section);
	    sectionKeyField = new SmallField(15, item.sectionKey);
	    subsectionField = new SmallField(25, item.subsection);
	    subsectionKeyField = new SmallField(15, item.subsectionKey);
	    subsubsectionField = new SmallField(25, item.subsubsection);
	    subsubsectionKeyField = new SmallField(15, item.subsubsectionKey);

	    add(kindLabel);
	    add(kindField);
	    add(sep());
	    add(sep());

	    add(collectLabel);
	    add(collectField);
	    add(collectKeyLabel);
	    add(collectKeyField);

	    add(sectionLabel);
	    add(sectionField);
	    add(sectionKeyLabel);
	    add(sectionKeyField);

	    add(subsectionLabel);
	    add(subsectionField);
	    add(subsectionKeyLabel);
	    add(subsectionKeyField);

	    add(subsubsectionLabel);
	    add(subsubsectionField);
	    add(subsubsectionKeyLabel);
	    add(subsubsectionKeyField);

	    SpringUtilities.makeCompactGrid(this, 5, 4, 5, 5, 5, 5);
	}
	public CollectionPanel() {
	    this(new CollectionItem());
	}

	public CollectionItem getItem() {
	    return new CollectionItem(
		kindField.getText(),
		collectField.getText(),
		collectKeyField.getText(),
		sectionField.getText(),
		sectionKeyField.getText(),
		subsectionField.getText(),
		subsectionKeyField.getText(),
		subsubsectionField.getText(),
		subsubsectionKeyField.getText());
	}

	public void setEnabled(boolean allow) {
	    kindLabel.setEnabled(allow);
	    collectLabel.setEnabled(allow);
	    collectKeyLabel.setEnabled(allow);
	    sectionLabel.setEnabled(allow);
	    sectionKeyLabel.setEnabled(allow);
	    subsectionLabel.setEnabled(allow);
	    subsectionKeyLabel.setEnabled(allow);
	    subsubsectionLabel.setEnabled(allow);
	    subsubsectionKeyLabel.setEnabled(allow);

	    kindField.setEnabled(allow);
	    collectField.setEnabled(allow);
	    collectKeyField.setEnabled(allow);
	    sectionField.setEnabled(allow);
	    sectionKeyField.setEnabled(allow);
	    subsectionField.setEnabled(allow);
	    subsectionKeyField.setEnabled(allow);
	    subsubsectionField.setEnabled(allow);
	    subsubsectionKeyField.setEnabled(allow);
	}
	public void setEditable(boolean allow) {
	    kindField.setEditable(allow);
	    collectField.setEditable(allow);
	    collectKeyField.setEditable(allow);
	    sectionField.setEditable(allow);
	    sectionKeyField.setEditable(allow);
	    subsectionField.setEditable(allow);
	    subsectionKeyField.setEditable(allow);
	    subsubsectionField.setEditable(allow);
	    subsubsectionKeyField.setEditable(allow);
	}

	public void toFirstField() {
	    kindField.requestFocus();
	}
    }

    // Field this is not bigger than necessary.
    private class SmallField extends SpecialTextField {
	public SmallField(int size, String init) {
	    super(size);
	    setText(init);
	    // setMaximumSize(getPreferredSize());
	    setFont(Settings.inputTextFont());
	    getDocument().addDocumentListener(TextEditor.this);
	}
	public SmallField(int size) {
	    this(size, "");
	}
	public Dimension getMaximumSize() {
	    return getPreferredSize();
	}
    }

    // Record change to names.
    public void changedUpdate(DocumentEvent e) {
        changed = true;
	restoreItem.setEnabled(changed);
    }
    public void insertUpdate(DocumentEvent e) {
        changed = true;
	restoreItem.setEnabled(changed);
    }
    public void removeUpdate(DocumentEvent e) {
        changed = true;
	restoreItem.setEnabled(changed);
    }

    // Ask user whether loss of data is allowed.
    private boolean userConfirmsLoss(String message) {
        Object[] options = {"proceed", "cancel"};
        int answer = JOptionPane.showOptionDialog(this, message,
                "warning: impending loss of data",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[1]);
        return answer == 0;
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

    //////////////////////////////////////
    // To be defined by caller.

    // Ask preferred directory for texts.
    protected abstract File textDirectory();

    // Add new text to corpus for file.
    protected abstract Text addText(File file);

    // Remove text from corpus.
    protected abstract void removeText(Text text);

    // Propagate text changes to index in corpus.
    protected abstract void redoIndex();

    // View text.
    protected abstract void view(Text text);

    // Return to caller.
    protected abstract void exit();

}
