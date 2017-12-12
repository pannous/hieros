/***************************************************************************/
/*                                                                         */
/*  ContentEditorPane.java                                                 */
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

// Customized editor pane, with undo/redo, backup, and parsing of RES.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;

abstract class ContentEditorPane extends JEditorPane 
	implements DocumentListener, UndoableEditListener {

    // File, if any, that corresponds to the contents.
    // If this is non-null, then the save button should be enabled.
    private File file = null;

    // Has file been changed after last load or save.
    private boolean beenChanged = false;

    // Number of changes since last auto-save.
    private int nrChanged = 0;

    // In case of problems with saving, we should not 
    // remove the backup file.
    private boolean writingProblems = false;

    // Undo manager.
    private UndoManager undoManager = new UndoManager();

    // Scanner for pane.
    private ResScanner scanner = new ResScanner(this);

    public ContentEditorPane() {
	super();
	setContentType("text/plain");
	getDocument().addDocumentListener(this);
	getDocument().addUndoableEditListener(this);
    }

    // Make smoother fonts than supertype.
    public void paintComponent(Graphics g) {
	Graphics2D g2 = (Graphics2D) g;
	g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	g2.setRenderingHint(RenderingHints.KEY_RENDERING,
		RenderingHints.VALUE_RENDER_QUALITY);
	super.paintComponent(g2);
    }

    // Workaround for bug/feature in JEditorPane,
    // which doesn't allow loading same URL twice in a row.
    public URL getPage() {
	if (file == null) 
	    return null;
	else
	    return super.getPage();
    }

    // Get file.
    public File getFile() {
	return file;
    }

    // Has text been changed?
    public boolean beenChanged() {
	return beenChanged;
    }

    // Clear contents of pane.
    public void clear() {
	setText("");
	cleanBackup();
	file = null;
	enableSaving(false);
	beenChanged = false;
	nrChanged = 0;
	undoManager.discardAllEdits();
	enableUndoRedo();
    }

    // Load URL into pane. Make backup right away.
    public void setPage(File file) {
	try {
	    setPage(file.toURI().toURL());
	    this.file = file;
	    enableSaving(true);
	    getDocument().addDocumentListener(this);
	    getDocument().addUndoableEditListener(this);
	    makeBackup(false);
	} catch (IOException e) {
	    System.err.println("Could not read from: " + file);
	    System.err.println(e.getMessage());
	}
    }

    // Save contents back to file.
    public void save() {
	if (file != null) // will always be true upon call
	    saveOverwrite(file);
    }

    // Save contents to file, but do not overwrite if file already
    // exists.
    public void save(File file) {
	if (this.file != null && 
		!this.file.getAbsolutePath().equals(file.getAbsolutePath()) &&
		file.exists()) {
	    Object[] options = {"proceed", "cancel"};
	    int answer = JOptionPane.showOptionDialog(this,
		    "Overwrite content of file?",
		    "warning: file overwrite",
		    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
		    null, options, options[1]);
	    if (answer != 0)
		return;
	} 
	saveOverwrite(file);
    }

    // Save contents to given file.
    // The backup can be removed.
    // If there are problems with writing, the problems are
    // permanently recorded, so that subsequent forced
    // exit does not destroy backup file.
    public void saveOverwrite(File file) {
	writingProblems = false;
	try {
	    showEmphasizedStatus("saving to: " + file.getName());
	    Writer writer = new FileWriter(file);
	    write(writer);
	    writer.close();
	    if (this.file != null && !this.file.equals(file)) 
		cleanBackup();
	    this.file = file;
	    enableSaving(true);
	    beenChanged = false;
	    nrChanged = 0;
	    makeBackup(false);
	    StatusClearer clearer = new StatusClearer();
	    clearer.start();
	} catch (IOException e) {
	    System.err.println("Could not save to: " + file);
	    System.err.println(e.getMessage());
	    writingProblems = true;
	    showErrorStatus("could not save to: " + file.getName());
	}
    }

    // Clean up backup.
    private void cleanBackup() {
	if (writingProblems)
	    return;
	File backup = backupFile();
	if (backup != null) 
	    try {
		backup.delete();
	    } catch (Exception e) {
		// not important, only leaves junk
	    }
    }

    // Do save of backup, possibly auto-save, in thread.
    private void makeBackup(boolean autoSave) {
	SwingUtilities.invokeLater(new BackupSaver(autoSave));
    }

    // Thread doing backup, possibly as auto-save.
    private class BackupSaver extends Thread {
	// Is auto save, or save of backup from normal save.
	private boolean autoSave = false;

	// Create saver.
	public BackupSaver(boolean autoSave) {
	    this.autoSave = autoSave;
	}

	// Do auto-save to auxiliary file, whose name ends on ~.
	public void run() {
	    File f = backupFile();
	    if (f != null) {
		if (autoSave)
		    showStatus("auto-saving to: " + f.getName());
		try {
		    Writer writer = new FileWriter(f);
		    write(writer);
		    writer.close();
		} catch (IOException e) {
		    JOptionPane.showMessageDialog(ContentEditorPane.this,
			    "Save of backup failed", "IO warning", 
			    JOptionPane.WARNING_MESSAGE);
		}
		StatusClearer clearer = new StatusClearer();
		if (autoSave)
		    clearer.start();
	    }
	}
    }

    // Thread that clears status, after a certain amount of time.
    private class StatusClearer extends Thread {
	// Time until clear.
	private int msecs = 2000;

	// Clear status in main window.
	public void run() {
	    try {
		Thread.sleep(msecs);
		showStatus("");
	    } catch (InterruptedException e) {
		// ignore
	    }
	}
    }

    // Get file for auto-save.
    private File backupFile() {
	if (file != null) 
	    return new File(file.toString() + "~");
	else
	    return null;
    }

    // Process undoable change to document.
    public void undoableEditHappened(UndoableEditEvent e) {
	undoManager.addEdit(e.getEdit());
	enableUndoRedo();
    }

    // Signal any change to document.
    public void changedUpdate(DocumentEvent e) {
	nrChanged += e.getLength();
	detectChange();
    }
    public void insertUpdate(DocumentEvent e) {
	nrChanged += e.getLength();
	detectChange();
    }
    public void removeUpdate(DocumentEvent e) {
	nrChanged++;
	detectChange();
    }
    // After a number of changes, do auto-save in separate thread.
    private void detectChange() {
	beenChanged = true;
	if (nrChanged > Settings.autoSaveInterval) {
	    makeBackup(true);
	    nrChanged = 0;
	}
    }

    // Undo last change.
    public void undo() {
	try {
	    undoManager.undo();
	} catch (CannotUndoException e) {
	    // ignore
	}
	enableUndoRedo();
    }

    // Redo last.
    public void redo() {
	try {
	    undoManager.redo();
	} catch (CannotRedoException e) {
	    // ignore
	}
	enableUndoRedo();
    }

    // See if buttons for undo/redo should be enabled.
    public void enableUndoRedo() {
	enableUndo(undoManager.canUndo());
	enableRedo(undoManager.canRedo());
    }

    // Starting from current position, select a piece of RES.
    public void grab(boolean xmlEscape) {
	int i = getCaretPosition();
	scanner.setXmlSensitive(xmlEscape);
	Substring substring = scanner.findResAt(i);
	if (substring != null) {
	    setCaretPosition(substring.start);
	    moveCaretPosition(substring.end);
	} 
    }

    // Following current position, select a piece of RES.
    public void jump(boolean xmlEscape) {
	int i = getCaretPosition();
	scanner.setXmlSensitive(xmlEscape);
	Substring substring = scanner.findResAfter(i);
	if (substring != null) {
	    setCaretPosition(substring.start);
	    moveCaretPosition(substring.end);
	} 
    }

    ///////////////////////////////////////////////
    // Information flow back to application.

    // Communicate that file can be saved.
    protected abstract void enableSaving(boolean b);

    // Communicate that there can be undo.
    protected abstract void enableUndo(boolean b);

    // Communicate that there can be redo.
    protected abstract void enableRedo(boolean b);

    // Show normal status.
    protected abstract void showStatus(String status);

    // Show status in emphatic way.
    protected abstract void showEmphasizedStatus(String status);

    // Show error status.
    protected abstract void showErrorStatus(String status);

}
