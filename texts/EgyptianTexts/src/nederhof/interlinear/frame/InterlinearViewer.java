/***************************************************************************/
/*                                                                         */
/*  InterlinearViewer.java                                                 */
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

// Frame in which to view/edit interlinear text.

package nederhof.interlinear.frame;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import nederhof.alignment.egyptian.*;
import nederhof.alignment.*;
import nederhof.corpus.*;
import nederhof.corpus.frame.*;
import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.labels.*;
import nederhof.interlinear.frame.pdf.*;
import nederhof.util.*;

public class InterlinearViewer extends JFrame 
		implements TextViewer, ActionListener, ChangeListener {

    // Directory of corpus.
    private String corpusDirectory;

    // The text being viewed.
    private Text text;

    // Automatic alignment.
    private Autoaligner autoaligner;

    // Parameters for rendering in PDF.
    private PdfRenderParameters pdfRenderParameters;

    // Have any properties of text been edited, but not saved.
    private boolean changedText = false;
    // Have any properties of resources been edited since text window last reset.
    private boolean changedResource = true;

    // Is resource open?
    private boolean resourceOpen = false;

    // Is external edit window open (e.g. file selection)?
    private boolean externEdit = false;

    // Is text window in edit mode?
    private boolean edit = false;

    // The index pane for resources.
    private IndexPane indexPane;

    // The interlinear text.
    private TextPane textPane;

    // Auxiliary window.
    private JFrame helpWindow = null;

    // Pane containing index and text.
    private JTabbedPane tabbed = new JTabbedPane(JTabbedPane.TOP);

    // Make window.
    public InterlinearViewer(String corpusDirectory, Text text, Vector resourceGenerators, 
	    Autoaligner autoaligner,
	    RenderParameters renderParameters,
	    PdfRenderParameters pdfRenderParameters) {
	this.corpusDirectory = corpusDirectory;
	this.text = text;
	this.autoaligner = autoaligner;
	this.pdfRenderParameters = pdfRenderParameters;
	renderParameters.setTargetFrame(this);

	setTitle(text.getName());
	setJMenuBar(new Menu(this));
	setSize(Settings.displayWidthInit, Settings.displayHeightInit);
	Container content = getContentPane();
	content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	addWindowListener(new CloseListener());
	tabbed.addChangeListener(this);

	indexPane = new IndexPane(corpusDirectory, text, resourceGenerators) {
	    protected void makeTextChanged() {
		changedText = true;
	    }
	    protected void makeResourceChanged() {
		changedResource = true;
	    }
	    protected void setResourceOpen(boolean b) {
		resourceOpen = b;
		InterlinearViewer.this.enableDisable();
	    }
	    protected void setExternEdit(boolean b) {
		externEdit = b;
		InterlinearViewer.this.enableDisable();
	    }
	};
	tabbed.addTab("index", indexPane);
	textPane = new TextPane(corpusDirectory, text, autoaligner, renderParameters) {
	    protected void makeResourceChanged() {
		changedText = true;
		resetText();
	    }
	    protected void setResourceOpen(boolean b) {
		resourceOpen = b;
		InterlinearViewer.this.enableDisable();
	    }
	    protected void setExternEdit(boolean b) {
		externEdit = b;
		InterlinearViewer.this.enableDisable();
	    }
	};
	tabbed.addTab("text", textPane);
	content.add(tabbed);
	if (text.getResources().size() > 0)
	    tabbed.setSelectedIndex(1);
	else
	    tabbed.setSelectedIndex(0);

	setFocusable(true);

	setVisible(true);
    }

    // Get text that this viewer is viewing.
    public Text getText() {
	return text;
    }

    //////////////////////////////////////////////////////////////////////////////
    // Menu at top of window.

    // Items in menu that may be disabled/enabled.
    private final JMenu fileMenu = new EnabledMenu(
	    "<u>F</u>ile", KeyEvent.VK_F);
    private final JMenuItem fileCloseItem = new EnabledMenuItem(this,
	    "clo<u>S</u>e", "close", KeyEvent.VK_S);
    private final JMenuItem exportItem = new EnabledMenuItem(this,
	    "p<u>D</u>f", "export", KeyEvent.VK_D);
    private final JMenuItem uploadItem = new EnabledMenuItem(this,
	    "upload", "upload");
    private final JMenu resourceMenu = new EnabledMenu(
	    "<u>R</u>esource", KeyEvent.VK_R);
    private final JMenuItem resourceCloseItem = new EnabledMenuItem(this,
	    "clo<u>S</u>e", "resource close", KeyEvent.VK_S);
    private final JMenuItem resourceEditItem = new EnabledMenuItem(this,
	    "<u>M</u>ode", "resource edit", KeyEvent.VK_M);
    private final JMenuItem leftItem = new EnabledMenuItem(this,
	    "left", "left", KeyEvent.VK_COMMA);
    private final JMenuItem rightItem = new EnabledMenuItem(this,
	    "right", "right", KeyEvent.VK_PERIOD);
    private final JMenuItem prependItem = new EnabledMenuItem(this,
	    "<u>P</u>repend", "prepend", KeyEvent.VK_P);
    private final JMenuItem editItem = new EnabledMenuItem(this,
	    "<u>E</u>dit", "edit", KeyEvent.VK_E);
    private final JMenuItem appendItem = new EnabledMenuItem(this,
	    "<u>A</u>ppend", "append", KeyEvent.VK_A);
    private final JMenuItem joinItem = new EnabledMenuItem(this,
	    "<u>J</u>oin", "join", KeyEvent.VK_J);
    private final JMenuItem cutItem = new EnabledMenuItem(this,
	    "c<u>U</u>t", "cut", KeyEvent.VK_U);
    private final JMenuItem onePrecItem = new EnabledMenuItem(this,
	    "<u>1</u>-way precedence", "1 prec", KeyEvent.VK_1);
    private final JMenuItem twoPrecItem = new EnabledMenuItem(this,
	    "<u>2</u>-way precedence", "2 prec", KeyEvent.VK_2);
    private final JMenuItem unPrecItem = new EnabledMenuItem(this,
	    "<u>0</u> precedences", "no prec", KeyEvent.VK_0);
    private final JMenuItem autoItem = new EnabledMenuItem(this,
	    "auto align", "autoalign", KeyEvent.VK_EQUALS);
    private final JMenuItem noAutoItem = new EnabledMenuItem(this,
	    "no auto align", "no autoalign", KeyEvent.VK_MINUS);
    private final JMenuItem settingsItem = new EnabledMenuItem(this,
	    "settin<u>G</u>s", "settings", KeyEvent.VK_G);

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
	    fileMenu.add(fileCloseItem);
	    fileMenu.add(exportItem);
	    fileMenu.add(uploadItem);
	    add(Box.createHorizontalStrut(STRUT_SIZE));

	    // resource
	    add(resourceMenu);
	    resourceMenu.add(resourceCloseItem);
	    resourceMenu.addSeparator();
	    resourceMenu.add(resourceEditItem);
	    resourceMenu.add(leftItem);
	    resourceMenu.add(rightItem);
	    resourceMenu.add(prependItem);
	    resourceMenu.add(editItem);
	    resourceMenu.add(appendItem);
	    resourceMenu.add(joinItem);
	    resourceMenu.add(cutItem);
	    resourceMenu.add(onePrecItem);
	    resourceMenu.add(twoPrecItem);
	    resourceMenu.add(unPrecItem);
	    resourceMenu.add(autoItem);
	    resourceMenu.add(noAutoItem);
	    resourceMenu.addSeparator();
	    resourceMenu.add(settingsItem);
	    add(Box.createHorizontalStrut(STRUT_SIZE));

	    add(new ClickButton(InterlinearViewer.this, "<u>H</u>elp", "help", KeyEvent.VK_H));
	    add(Box.createHorizontalStrut(STRUT_SIZE));

	    leftItem.setEnabled(edit);
	    rightItem.setEnabled(edit);
	    prependItem.setEnabled(edit);
	    editItem.setEnabled(edit);
	    appendItem.setEnabled(edit);
	    joinItem.setEnabled(edit);
	    cutItem.setEnabled(edit);
	    onePrecItem.setEnabled(edit);
	    twoPrecItem.setEnabled(edit);
	    unPrecItem.setEnabled(edit);
	    autoItem.setEnabled(edit);
	    noAutoItem.setEnabled(edit);

            add(Box.createHorizontalGlue());
        }
    }

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
	int sel = tabbed.getSelectedIndex();
        if (e.getActionCommand().equals("close")) {
	    trySaveQuit();
	} else if (e.getActionCommand().equals("export")) {
	    new Exporter(text.getName(), indexPane.getResourceList(), 
		indexPane.getPrecedenceList(), indexPane.getAutoalignList(),
		autoaligner,
		pdfRenderParameters);
	    pdfRenderParameters.edit();
	} else if (e.getActionCommand().equals("upload")) {
	    upload();
	} else if (e.getActionCommand().equals("resource close")) {
	    if (sel == 0)
		indexPane.actionPerformed(e);
	    else if (sel == 1) 
		textPane.actionPerformed(e);
	} else if (e.getActionCommand().equals("resource edit")) {
	    if (sel == 1 && text.isEditable()) {
		edit = !edit;
		leftItem.setEnabled(edit);
		rightItem.setEnabled(edit);
		prependItem.setEnabled(edit);
		editItem.setEnabled(edit);
		appendItem.setEnabled(edit);
		joinItem.setEnabled(edit);
		cutItem.setEnabled(edit);
		onePrecItem.setEnabled(edit);
		twoPrecItem.setEnabled(edit);
		unPrecItem.setEnabled(edit);
		autoItem.setEnabled(edit);
		noAutoItem.setEnabled(edit);
		resetText();
	    }
        } else if (e.getActionCommand().equals("1 prec")) {
	    if (sel == 1)
		makePrecedence(1);
        } else if (e.getActionCommand().equals("2 prec")) {
	    if (sel == 1)
		makePrecedence(2);
        } else if (e.getActionCommand().equals("no prec")) {
	    if (sel == 1)
		makePrecedence(0);
	} else if (e.getActionCommand().equals("autoalign")) {
	    if (sel == 1) 
		setAutoaligns(true);
	} else if (e.getActionCommand().equals("no autoalign")) {
	    if (sel == 1) 
		setAutoaligns(false);
	} else if (e.getActionCommand().equals("settings")) {
	    setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    textPane.editSettings();
	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	} else if (e.getActionCommand().equals("help")) {
	    if (helpWindow == null) {
		URL url = FileAux.fromBase("data/help/text/viewer.html");
		helpWindow = new HTMLWindow("Text viewer manual", url);
	    }
	    helpWindow.setVisible(true);
	} else if (sel == 1) {
	    boolean resourceChanged = textPane.actionPerformed(e);
	    if (resourceChanged) 
		resetText();
	}
    }

    // Action of tabbed pane.
    public void stateChanged(ChangeEvent evt) {
	if (evt.getSource() == tabbed) {
	    enableDisable();
	    int sel = tabbed.getSelectedIndex();
	    switch (sel) {
		case 0:
		    resourceEditItem.setEnabled(false);
		    indexPane.makeIndex();
		    break;
		case 1:
		    if (changedText) {
			trySave();
			resetText();
		    } else if (changedResource) {
			resetText();
			changedResource = false;
		    }
		    resourceEditItem.setEnabled(text.isEditable());
		    break;
	    }
	}
    }

    // Reset contents of text.
    private void resetText() {
	setCursor(new Cursor(Cursor.WAIT_CURSOR));
	textPane.reset(
		indexPane.getResourceList(),
		indexPane.getPrecedenceList(),
		indexPane.getAutoalignList(),
		edit);
	setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    ///////////////////////////////////////////////////
    // Interaction between panels.

    // Allow editing.
    private void enableDisable() {
	int sel = tabbed.getSelectedIndex();

	fileMenu.setEnabled(!resourceOpen);
	fileCloseItem.setEnabled(!resourceOpen);
	exportItem.setEnabled(!resourceOpen && hasFileAccess());
	uploadItem.setEnabled(!resourceOpen && canUpload());
	resourceMenu.setEnabled(!externEdit);
	resourceCloseItem.setEnabled(resourceOpen && !externEdit);

	resourceEditItem.setEnabled(sel == 1 && text.isEditable() && !resourceOpen);
	leftItem.setEnabled(sel == 1 && edit && !resourceOpen);
	rightItem.setEnabled(sel == 1 && edit && !resourceOpen);
	prependItem.setEnabled(sel == 1 && edit && !resourceOpen);
	editItem.setEnabled(sel == 1 && edit && !resourceOpen);
	appendItem.setEnabled(sel == 1 && edit && !resourceOpen);
	joinItem.setEnabled(sel == 1 && edit && !resourceOpen);
	cutItem.setEnabled(sel == 1 && edit && !resourceOpen);
	onePrecItem.setEnabled(sel == 1 && edit && !resourceOpen);
	twoPrecItem.setEnabled(sel == 1 && edit && !resourceOpen);
	unPrecItem.setEnabled(sel == 1 && edit && !resourceOpen);
	autoItem.setEnabled(sel == 1 && edit && !resourceOpen);
	noAutoItem.setEnabled(sel == 1 && edit && !resourceOpen);
	settingsItem.setEnabled(sel == 1 && !externEdit);

	tabbed.setEnabled(!resourceOpen);

	if (sel == 0)
	    indexPane.enableDisable(changedText, resourceOpen, externEdit);
	else if (sel == 1)
	    textPane.enableDisable(changedText, externEdit);
    }

    // Make precedence, and process changes.
    private void makePrecedence(int type) {
	Vector changed = textPane.makePrecedence(type);
	if (changed != null) {
	    indexPane.setPrecedences(changed);
	    changedText = true;
	    trySave();
	    resetText();
	}
    }

    // Do autoalign (or undo), and process changes.
    private void setAutoaligns(boolean b) {
	Vector changed = textPane.autoalign(b);
	if (changed != null) {
	    indexPane.setAutoaligns(changed);
	    changedText = true;
	    trySave();
	    resetText();
	}
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

    // Try saving and quit. Report if not successful.
    public boolean trySaveQuit() {
	if (resourceOpen &&
	    !userConfirmsLoss("Resource still open. Want to close anyway?"))
	    return false;
	try {
	    save();
	} catch (IOException e) {
	    if (!userConfirmsLoss("Could not save text. Want to close anyway?"))
		return false;
	}
	exit();
	dispose();
	return true;
    }

    // Try saving. Warn if there is problem.
    public void trySave() {
	try {
	    save();
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(this,
		    "Could not save text:\n" + e.getMessage(),
		    "File error", JOptionPane.ERROR_MESSAGE);
	}
    }

    // Save changes.
    public void save() throws IOException {
	if (changedText) {
	    text.setResources(indexPane.getResources());
	    text.setPrecedences(indexPane.getPrecedences());
	    text.setAutoaligns(indexPane.getAutoaligns());
	    text.setDescription(makeDescription(indexPane.getResourceList()));
	    text.save();
	    refreshIndices();
	}
	changedText = false;
    }

    // Kill all windows, and exit.
    public void dispose() {
	super.dispose();
	if (helpWindow != null)
	    helpWindow.dispose();
	if (standAlone)
	    System.exit(0);
	textPane.dispose();
    }

    // Listen if window to be closed or iconified.
    private class CloseListener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
	    if (!externEdit || 
		    userConfirmsLoss("Do you want to proceed and discard the edits?"))
		trySaveQuit();
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
    // Uploading.

    // Is there resource which can be uploaded?
    private boolean canUpload() {
	if (indexPane == null)
	    return false;
	Vector resources = indexPane.getResourceList();
	for (int i = 0; i < resources.size(); i++) {
	    TextResource resource = (TextResource) resources.get(i);
	    if (resource.uploadable())
		return true;
	}
	resources = indexPane.getPrecedenceList();
	for (int i = 0; i < resources.size(); i++) {
	    TextResource resource = (TextResource) resources.get(i);
	    if (resource.uploadable())
		return true;
	}
	return false;
    }

    // As far as resources are uploadable, upload them, and
    // gather messages from their success.
    private void upload() {
	String message = "Success";
	try {
	    Vector resources = indexPane.getResourceList();
	    for (int i = 0; i < resources.size(); i++) {
		TextResource resource = (TextResource) resources.get(i);
		if (resource.uploadable()) 
		    message += "\n" + resource.getName() + ": " + resource.upload();
	    }
	    resources = indexPane.getPrecedenceList();
	    for (int i = 0; i < resources.size(); i++) {
		TextResource resource = (TextResource) resources.get(i);
		if (resource.uploadable()) 
		    message += "\n" + resource.getName() + ": " + resource.upload();
	    }
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(this,
		    "Could not upload:\n" + e.getMessage(),
		    "Upload error", JOptionPane.ERROR_MESSAGE);
	    return;
	}
	JOptionPane.showMessageDialog(this, message,
		"Confirmed", JOptionPane.INFORMATION_MESSAGE);
    }

    /////////////////////////////////////////////////////
    // Return to caller.

    // Caller should override this.
    protected void exit() {
	// ignore
    }

    // Caller should override this.
    protected String makeDescription(Vector resources) {
	return "";
    }

    // Caller should override this
    protected void refreshIndices() {
	// ignore
    }

    /////////////////////////////////////////////////////
    // Testing.

    // For testing.
    public static void main(String[] args) {
	ResourceGenerator gen = new EgyptianResourceGenerator();
	SchemeMapGenerator mapGen = new SchemeMapGenerator();
	Vector generators = new Vector();
	generators.add(gen);
	generators.add(mapGen);
	JFrame dummy = new JFrame();
	InterlinearViewer frame =
	    new InterlinearViewer(null, new Text("corpus/texts/Peasant.xml"),
		    generators, 
		    new EgyptianAutoaligner(), new EgyptianRenderParameters(dummy), null);
	frame.setStandAlone(true);
    }

}
