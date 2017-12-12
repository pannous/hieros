/***************************************************************************/
/*                                                                         */
/*  FileChoosingWindow.java                                                */
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

// Window for selecting file.

package nederhof.util;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;

public abstract class FileChoosingWindow extends JFrame 
		implements ActionListener {

    // Actual chooser in window.
    private JFileChooser filePanel;

    // File chooser with filter name and filter extensions.
    public FileChoosingWindow(String filter, String[] exts) {
	setTitle("File selection");
	setJMenuBar(new QuitMenu());

	filePanel = new JFileChooser();
	filePanel.setDialogType(JFileChooser.OPEN_DIALOG);
	filePanel.setApproveButtonText("Select");
	filePanel.addActionListener(this);
	if (exts != null)
	    filePanel.setFileFilter(new CustomFilter(filter, exts));
	getContentPane().add(filePanel);

	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	addWindowListener(new ConservativeListener(this) {
		public void windowClosing(WindowEvent e) {
		    exit();
		}
	    });
	pack();
	setVisible(true);
    }
    // Without filter.
    public FileChoosingWindow() {
	this("none", null);
    }

    // Set current directory.
    public void setCurrentDirectory(File f) {
	filePanel.setCurrentDirectory(f);
    }

    // Set selected file.
    public void setSelectedFile(File f) {
	try {
	    filePanel.setSelectedFile(f.getCanonicalFile());
	} catch (IOException e) {
	    filePanel.setSelectedFile(f);
	}
    }

    private class QuitMenu extends JMenuBar {
	// Distance between left edge and button.
	private static final int STRUT_SIZE = 10;

	public QuitMenu() {
	    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	    setBackground(Color.LIGHT_GRAY);
	    add(Box.createHorizontalStrut(STRUT_SIZE));
	    add(new ClickButton(FileChoosingWindow.this,
			"<html><u>Q</u>uit</html>", 
			"quit", KeyEvent.VK_Q));
	}
    }

    // Actions can be: select, cancel. In both cases, make window
    // invisible.
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
	    File file = filePanel.getSelectedFile();
	    choose(file);
	} else 
	    exit();
	setVisible(false);
    }

    // At revisit of window, make sure it is up-to-date.
    public void setVisible(boolean b) {
	if (b)
	    filePanel.rescanCurrentDirectory();
	super.setVisible(b);
    }

    // Filter that only allows directories and extensions.
    private static class CustomFilter extends FileFilter {

	// Name of filter.
	private String name;

	// Extensions.
	private String[] exts;

	// Construct.
	public CustomFilter(String name, String[] exts) {
	    this.name = name;
	    this.exts = exts;
	}

        // Accept only extensions in array. Also directories.
        public boolean accept(File f) {
            if (f.isDirectory())
                return true;
            else {
		String name = f.getName().toLowerCase();
		for (int i = 0; i < exts.length; i++) 
		    if (name.endsWith("." + exts[i]))
			return true;
            }
            return false;
        }

        // Description of filter.
        public String getDescription() {
            return name;
        }
    }

    /////////////////////////////
    // To be defined/overridden by caller.

    protected abstract void choose(File f);

    // Exit without choosing file.
    protected void exit() {
	// by default do nothing
    }

}
