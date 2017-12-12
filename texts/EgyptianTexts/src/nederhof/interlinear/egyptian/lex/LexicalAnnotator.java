package nederhof.interlinear.egyptian.lex;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.frame.*;
import nederhof.lexicon.egyptian.*;
import nederhof.res.*;
import nederhof.util.*;
import nederhof.util.gui.*;

// Editing lexical annotation.
public class LexicalAnnotator extends ResourceEditor {

    // Auxiliary class for manipulating resource.
    private LexicoManipulator manipulator;

    // Containing hieroglyphic.
    private HierosPanel hiPane;

    // Containing annotations.
    private LexesPanel lexPane;

    // The lexicon.
    private LexiconEditor editor;

    // Constructor.
    public LexicalAnnotator(EgyptianLexico resource, int currentSegment) {
	super(resource, currentSegment);
	setSize(Settings.displayWidthInit, Settings.displayHeightInit);
	extendMenu();
        setWait(true, "initializing");
	Container content = getContentPane();
	content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
	setVisible(true);

	manipulator = new ConnectedManipulator(resource, currentSegment);
	addHierosPanel();
	addLexesPanel();
	getLexica();
	lexPane.showFocus();
	lexPane.scrollToFocusLater();
        setWait(false);
	allowEditing(true);
    }

    private class ConnectedManipulator extends LexicoManipulator {
	public ConnectedManipulator(EgyptianLexico resource, int currentSegment) {
	    super(resource, currentSegment);
	}
	public void recordChange() {
	    LexicalAnnotator.this.recordChange();
	}
        public void showFocus() {
            LexicalAnnotator.this.showFocus();
        }
        public void unshowFocus() {
            LexicalAnnotator.this.unshowFocus();
        }
        public void scrollToFocus() {
            LexicalAnnotator.this.scrollToFocus();
        }
        public void refreshFragments() {
            LexicalAnnotator.this.refreshFragments();
        }
        public void refreshSegments() {
            LexicalAnnotator.this.refreshSegments();
        }
        public void refreshSegment(int i, LxPart lx) {
            LexicalAnnotator.this.refreshSegment(i, lx);
        }
        public void removeButton(int i) {
            LexicalAnnotator.this.removeButton(i);
        }
        public void addButton(int i, LxPart lx) {
            LexicalAnnotator.this.addButton(i, lx);
        }
        public void searchLexicon() {
	    if (lexPane != null)
		lexPane.searchLexicon();
        }
    }

    // Get the name of the editor.
    public String getName() {
        return "Lexical Annotator";
    }

    // Delegate to auxiliary class to keep track of
    // current segment (overrides superclass).
    public int getCurrentSegment() {
	return manipulator.current();
    }

    //////////////////////////////////////////////////////////////////////////////
    // Lexica.

    // Try get lexica.
    private void getLexica() {
	try {
	    editor = new ConnectedLexiconEditor();
	    add(editor);
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(this,
		    "Could not read:\n" + e.getMessage(), "Reading error",
		    JOptionPane.ERROR_MESSAGE);
	    add(new JButton("Lexica not loaded"));
	}
    }

    private class ConnectedLexiconEditor extends LexiconEditor {
	public ConnectedLexiconEditor() throws IOException {
	}

	public void reportSelection(LexRecord rec) {
	    LxInfo info = manipulator.info();
	    if (info == null)
		return;
	    if (!editingIsAllowed)
		return;
	    if (!rec.cite.equals(""))
		info.cite = rec.cite;
	    if (!rec.keyhi.equals(""))
		info.keyhi = rec.keyhi;
	    if (!rec.keyal.equals(""))
		info.keyal = rec.keyal;
	    if (!rec.keytr.equals("")) {
		String tr = rec.keytr;
		if (!rec.keyco.equals(""))
		    tr += " (" + rec.keyco + ")";
		info.keytr = tr;
	    }
	    if (!rec.keyfo.equals(""))
		info.keyfo = rec.keyfo;
	    else if (rec.cite.equals("gods") ||
		    rec.cite.equals("kings") ||
		    rec.cite.equals("names") ||
		    rec.cite.equals("places"))
		info.keyfo = "proper noun";
	    if (!rec.hi.equals(""))
		info.dicthi = rec.hi;
	    if (!rec.al.equals(""))
		info.dictal = rec.al;
	    if (!rec.tr.equals("")) {
		String tr = rec.tr;
		if (!rec.co.equals(""))
		    tr += " (" + rec.co + ")";
		info.dicttr = tr;
	    } else if (!rec.co.equals(""))
		info.dicttr = "(" + rec.co + ")";
	    if (!rec.fo.equals(""))
		info.dictfo = rec.fo;
	    manipulator.updateSegment(info);
	    refreshSegment(manipulator.current(), manipulator.segment());
	}
    }

    //////////////////////////////////////////////////////////////////////////////
    // Menu.

    // Button showing status.
    private JButton statusButton = new JButton("");

    // Add more buttons to menu.
    private void extendMenu() {
        JMenuItem importHieroItem = new EnabledMenuItem(this,
            "import h<u>I</u>eroglyphic", "import hiero", KeyEvent.VK_I);
        JMenuItem importTransItem = new EnabledMenuItem(this,
            "import <u>T</u>ransliteration", "import trans", KeyEvent.VK_T);
        JMenu resourceMenu = new EnabledMenu(
            "<u>R</u>esource", KeyEvent.VK_R);
        JMenuItem leftItem = new EnabledMenuItem(this,
            "left word", "left", KeyEvent.VK_LEFT);
        JMenuItem rightItem = new EnabledMenuItem(this,
            "right word", "right", KeyEvent.VK_RIGHT);
        JMenuItem newItem = new EnabledMenuItem(this,
            "<u>N</u>ew word", "new", KeyEvent.VK_N);
        JMenuItem initialItem = new EnabledMenuItem(this,
            "new initial word", "initial", KeyEvent.VK_0);
        JMenuItem deleteItem = new EnabledMenuItem(this,
            "<u>D</u>elete word", "delete", KeyEvent.VK_D);
        JMenuItem swapItem = new EnabledMenuItem(this,
            "s<u>W</u>ap words", "swap", KeyEvent.VK_W);
        JMenuItem lemmaItem = new EnabledMenuItem(this,
            "new <u>L</u>emma", "lemma", KeyEvent.VK_L);

        fileMenu.add(importHieroItem);
        fileMenu.add(importTransItem);

        // resource
        menu.add(Box.createHorizontalStrut(STRUT_SIZE));
        menu.add(resourceMenu);
        resourceMenu.add(leftItem);
        resourceMenu.add(rightItem);
        resourceMenu.add(newItem);
        resourceMenu.add(initialItem);
        resourceMenu.add(deleteItem);
        resourceMenu.add(swapItem);
	resourceMenu.addSeparator();
        resourceMenu.add(lemmaItem);
	variableJComponents.add(importHieroItem);
	variableJComponents.add(importTransItem);
	variableJComponents.add(leftItem);
	variableJComponents.add(rightItem);
	variableJComponents.add(newItem);
	variableJComponents.add(initialItem);
	variableJComponents.add(deleteItem);
	variableJComponents.add(swapItem);
	variableJComponents.add(lemmaItem);

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
    // Hieroglyphic panel.

    // Add panel to contain hieroglyphic.
    private void addHierosPanel() {
	Container content = getContentPane();
	hiPane = new ConnectedHierosPanel();
	content.add(hiPane);
    }

    private class ConnectedHierosPanel extends HierosPanel {
	public ConnectedHierosPanel() {
	    super(manipulator);
	}
	public boolean splitOff(ResFragment prefFragment, 
		int prefFragments, int prefGlyphs) {
	    if (manipulator.current() >= 0) {
		manipulator.removeHi(prefFragments, prefGlyphs);
		manipulator.setTexthi(prefFragment);
		manipulator.right();
		return true;
	    } else
		return false;
	}
    }

    ///////////////////////////////////////////////////
    // Annotations panel.

    // Add panel with existing annotations.
    private void addLexesPanel() {
	Container content = getContentPane();
	lexPane = new ConnectedLexesPanel();
	content.add(lexPane);
    }

    private class ConnectedLexesPanel extends LexesPanel {
	public ConnectedLexesPanel() {
	    super(LexicalAnnotator.this, manipulator);
	}
	public void searchLexicon(String al) {
	    if (al.startsWith("^"))
		al = "\\" + al;
	    if (editor != null)
		editor.searchTransInLexicon(al);
	}
    }

    // Propagate change of focus to panel (panel may not exist yet).
    public void showFocus() {
        if (lexPane != null)
            lexPane.showFocus();
    }

    // Take away visible focus.
    public void unshowFocus() {
        if (lexPane != null)
            lexPane.unshowFocus();
    }

    // Scroll to focus.
    public void scrollToFocus() {
        if (lexPane != null)
            lexPane.scrollToFocus();
    }

    // Refresh (after edit to fragments).
    public void refreshFragments() {
        if (hiPane != null)
            hiPane.refresh();
    }
    // Refresh (after edit to resource).
    public void refreshSegments() {
        if (lexPane != null)
            lexPane.refresh();
    }
    // Refresh (after edit to segment).
    public void refreshSegment(int i, LxPart lx) {
        if (lexPane != null)
            lexPane.refresh(i, lx);
    }
    // Remove buttons for lex.
    public void removeButton(int i) {
        if (lexPane != null)
            lexPane.removeButton(i);
    }
    // Add buttons for lex.
    public void addButton(int i, LxPart lx) {
        if (lexPane != null)
            lexPane.addButton(i, lx);
    }

    /////////////////////////////////////////////////////
    // Listeners.

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("import hiero")) {
            importHiero();
	} else if (e.getActionCommand().equals("import trans")) {
            importTrans();
        } else if (e.getActionCommand().equals("left")) {
            manipulator.left();
        } else if (e.getActionCommand().equals("right")) {
            manipulator.right();
        } else if (e.getActionCommand().equals("new")) {
            setWait(true, "adding");
            manipulator.addSegment();
            setWait(false);
        } else if (e.getActionCommand().equals("initial")) {
            setWait(true, "adding");
            manipulator.addInitialSegment();
            setWait(false);
        } else if (e.getActionCommand().equals("delete")) {
            setWait(true, "deleting");
            manipulator.removeSegment();
            setWait(false);
        } else if (e.getActionCommand().equals("swap")) {
            setWait(true, "swapping");
            manipulator.swapSegments();
            setWait(false);
        } else if (e.getActionCommand().equals("lemma")) {
	    editor.createLemma();
        } else if (e.getActionCommand().equals("help")) {
            openHelp();
        } else
            super.actionPerformed(e);
    }

    ///////////////////////////////////////////////////
    // Help.

    /**
     * Open help window if not already open.
     */
    private void openHelp() {
        if (helpWindow == null) {
            URL url = FileAux.fromBase("data/help/text/lex_edit.html");
            helpWindow = new HTMLWindow("Lexical annotator manual", url);
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
	editor.dispose();
    }

    // Save the information displayed on GUI to the resource.
    public void saveToResource() {
        // unused, as all changes are saved immediately.
    }

    ///////////////////////////////////////////////////
    // Appearance.

    // Is editing currently allowed?
    private boolean editingIsAllowed = true;

    // Overrides superclass.
    public void allowEditing(boolean allow) {
        super.allowEditing(allow);
	editingIsAllowed = allow;
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
     * Get file name and import transliteration from it.
     */
    private void importTrans() {
	allowEditing(false);
        FileChoosingWindow chooser = new FileChoosingWindow(
                "transliteration resource", new String[] { "xml", "txt" }) {
            public void choose(final File file) {
                dispose();
                setWait(true, "importing");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        incorporateTrans(file);
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
     * Put hieroglyphic from file at end.
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

    /**
     * Put transliteration from file in current segment.
     * 
     * @param file
     */
    private void incorporateTrans(File file) {
        try {
            if (file.exists()) {
                EgyptianResource transResource = new EgyptianResource(
                        file.getPath());
                manipulator.incorporateTrans(transResource);
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
        LexicalAnnotator editor = new LexicalAnnotator(null, 0);
    }
}
