// Separate frame for editing single resource.

package nederhof.interlinear.frame;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.interlinear.*;
import nederhof.util.*;

public abstract class ResourceEditor extends JFrame 
	implements ActionListener, DocumentListener,
	ChangeListener, EditChainElement {

    // There may be auxiliary help window.
    protected JFrame helpWindow = null;

    // The resource being edited.
    protected TextResource resource;

    // Current segment number.
    protected int currentSegment;

    // Has there been any change to the resource since
    // beginning?
    // Or since last save?
    // Or since last time it was reset by user?
    protected boolean anyChange = false;
    protected boolean unsavedChange = false;
    protected boolean recentChange = false;

    // Is external edit window open (e.g. file selection)?
    protected boolean externEdit = false;

    // Elements to change appearance upon external edit.
    protected Vector variableJComponents = new Vector();
    protected Vector variableStyledPhraseEditors = new Vector();

    // The menu.
    protected JMenuBar menu;

    // Constructor.
    public ResourceEditor(TextResource resource,
	    int currentSegment) {
	this.resource = resource;
	this.currentSegment = currentSegment;

	setTitle(getName());
	menu = new Menu();
	setJMenuBar(menu);
	variableJComponents.add(fileMenu);
	variableJComponents.add(fileCloseItem);

	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	addWindowListener(new CloseListener());
    }

    // Name of editor.
    public abstract String getName();

    // Position of segment in resource being edited.
    public int getCurrentSegment() {
	return currentSegment;
    }

    /////////////////////////////////////////////
    // Menu.

    // Distance between buttons.
    protected static final int STRUT_SIZE = 10;

    // Items in menu that may be disabled/enabled.
    protected final JMenu fileMenu = new EnabledMenu(
            "<u>F</u>ile", KeyEvent.VK_F);
    protected final JMenuItem fileCloseItem = new EnabledMenuItem(this,
            "clo<u>S</u>e", "close", KeyEvent.VK_S);

    // Menu containing quit and edit buttons.
    protected class Menu extends JMenuBar {
        public Menu() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setBackground(Color.LIGHT_GRAY);
            add(Box.createHorizontalStrut(STRUT_SIZE));

            // file
            add(fileMenu);
            fileMenu.add(fileCloseItem);
	}
    }

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("close")) {
	    parentQuit();
	}
    }

    // Listen if window to be closed or iconified.
    // Let legend disappear with iconification of main window.
    private class CloseListener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
	    parentQuit();
        }
        public void windowIconified(WindowEvent e) {
            setState(Frame.ICONIFIED);
        }
        public void windowDeiconified(WindowEvent e) {
            setState(Frame.NORMAL);
        }
    }

    // Ask user whether loss of data is intended.
    protected boolean userConfirmsLoss(String message) {
        Object[] options = {"proceed", "cancel"};
        int answer = JOptionPane.showOptionDialog(this, message,
                "warning: impending loss of data",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[1]);
        return answer == 0;
    }

    ////////////////////////////////////////////
    // Value manipulation and quiting.

    // Any change is recorded.
    public void changedUpdate(DocumentEvent e) {
	recordChange();
    }
    public void insertUpdate(DocumentEvent e) {
	recordChange();
    }
    public void removeUpdate(DocumentEvent e) {
	recordChange();
    }
    public void stateChanged(ChangeEvent e) {
	recordChange();
    }
    public void recordChange() {
	anyChange = true;
        unsavedChange = true;
	recentChange = true;
    }

    // Has there been any change since beginning?
    protected boolean anyChange() {
	return anyChange;
    }

    // Parent to notify upon quit.
    private TextPane parent;

    // Set parent.
    public void setParent(TextPane parent) {
	this.parent = parent;
    }

    // Ask parent to quit. If there is no parent, quit anyway.
    private void parentQuit() {
	if (parent != null) 
		parent.closeResourceEditor();
	else if (trySaveQuit())
	    dispose();
    }

    // Try saving and quit. Report if not successful.
    public boolean trySaveQuit() {
	if (externEdit &&
		    !userConfirmsLoss("Do you want to proceed and discard any edits?")) 
	    return false;
	try {
	    save();
        } catch (IOException e) {
            if (!userConfirmsLoss("Could not save text. Want to close anyway?"))
                return false;
        }
        return true;
    }

    // Try saving. Warn if there is problem.
    // Return whether there was any change since the beginning.
    public void trySave() {
	try {
	    save();
	} catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not save resource:\n" + e.getMessage(),
                    "File error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Save changes.
    public void save() throws IOException {
	saveToResource();
        if (unsavedChange) {
	    resource.makeModified();
            resource.save();
	    unsavedChange = false;
        }
    }

    // Write data in window to resource.
    public abstract void saveToResource();

    // Kill all auxiliary windows, and exit.
    public void dispose() {
        super.dispose();
        if (helpWindow != null)
            helpWindow.dispose();
    }

    ////////////////////////////////////////////
    // Appearance.

    // Allow editing. (I.e. not blocked due to other edit.)
    public void allowEditing(boolean allow) {
	externEdit = !allow;
        fileMenu.setEnabled(allow);
        fileCloseItem.setEnabled(allow);
        for (int i = 0; i < variableJComponents.size(); i++) {
            JComponent comp = (JComponent) variableJComponents.get(i);
	    if (!(comp instanceof EnabledMenuItem) && !(comp instanceof EnabledMenu))
		comp.setBackground(backColor(allow));
            comp.setEnabled(allow);
        }
        for (int i = 0; i < variableStyledPhraseEditors.size(); i++) {
            StyledPhraseEditor comp = (StyledPhraseEditor) variableStyledPhraseEditors.get(i);
            comp.setEnabled(allow);
        }
        repaint();
    }

    // Color may depend on allowed editing.
    protected Color backColor(boolean editable) {
	return Color.WHITE;
    }

    //////////////////////////////
    // Auxiliaries.

    // Horizontal glue.
    protected static Component horGlue() {
        return Box.createHorizontalGlue();
    }
    // Some separation between panels.
    protected static Component sep() {
        return Box.createRigidArea(new Dimension(10, 10));
    }

}
