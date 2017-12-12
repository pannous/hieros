/***************************************************************************/
/*                                                                         */
/*  FileLocationEditor.java                                                */
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

// Element used to edit file location.

package nederhof.interlinear.frame;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;

import nederhof.interlinear.*;
import nederhof.util.*;

public class FileLocationEditor extends PropertyEditor 
		implements ActionListener {

    // The resource.
    private TextResource resource;

    // The element holding the value.
    private TruncateLabel fileLabel;

    // The extension.
    private JComboBox combo;

    // Allowable extensions.
    private Vector extensions;

    // File preceded by name of property.
    public FileLocationEditor(TextResource resource, 
	    int nameWidth, Vector extensions) {
	this.resource = resource;
	this.extensions = extensions;

        JLabel nameLabel = new BoldLabel("file ");
        nameLabel.setPreferredSize(new Dimension(nameWidth,
                    nameLabel.getPreferredSize().height));
        nameLabel.setHorizontalAlignment(SwingConstants.LEFT);

        fileLabel = new TruncateLabel();

	combo = new ExtensionCombo(extensions);

	JButton moveButton = new SelectButton();

        add(panelSep());
        add(nameLabel);
        add(fileLabel);
        add(panelSep());
	add(combo);
        add(panelSep());
	add(moveButton);
        add(panelGlue());

        textElements.add(nameLabel);
        textElements.add(fileLabel);
        textElements.add(combo);
        textElements.add(moveButton);
    }

    // Changes to file to be propagated to text.
    public boolean isGlobal() {
	return true;
    }

    // Text field, with truncated text.
    private class TruncateLabel extends JLabel {
	private final int maxLen = 40;
	private String full = ""; // untruncated
	public TruncateLabel() {
            setFont(inputTextFont());
        }
	public void setText(String s) {
	    full = s;
	    if (s.length() > maxLen) 
		s = "..." + s.substring(s.length() - maxLen);
	    super.setText(s);
	}
	public String getFullText() {
	    return full;
	}
    }

    // Button for move.
    private class SelectButton extends JButton {
	public SelectButton() {
	    super("move");
	    setFocusPainted(false);
	    setRolloverEnabled(true);
	    setMaximumSize(getPreferredSize());
	    addActionListener(FileLocationEditor.this);
	}
    }

    // Combo box for selecting extension.
    private class ExtensionCombo extends JComboBox {
	public ExtensionCombo(Vector values) {
	    super(values);
	    setMaximumRowCount(5);
	    setMaximumSize(getPreferredSize());
	    addActionListener(FileLocationEditor.this);
	}
    }

    // Listen to move.
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("move")) {
	    makeFileChooser();
	} else {
	    String ext = (String) combo.getSelectedItem();
	    changeExtension(ext);
	}
    }

    private void changeExtension(String ext) {
	String oldName = fileLabel.getFullText();
	if (!FileAux.hasExtension(oldName, ext)) {
	    String base = FileAux.removeExtension(oldName);
	    if (base != null) {
		String newName = base + "." + ext;
		moveTo(new File(newName));
	    }
	}
    }
    
    ////////////////////////////////////////
    // File chooser.

    // Make chooser of resource file.
    private void makeFileChooser() {
	parent.allowEditing(false);
	FileChoosingWindow chooser = 
	    new FileChoosingWindow("resource files", extensionArray()) {
		public void choose(File file) {
		    moveTo(file);
		    parent.allowEditing(true);
		    dispose();
		}
		public void exit() {
		    parent.allowEditing(true);
		    dispose();
		}
	    };
	chooser.setSelectedFile(new File(resource.getLocation()));
    }

    // Turn vector of allowed extensions into array.
    private String[] extensionArray() {
	if (extensions.size() == 0)
	    return null;
	else {
	    String[] ar = new String[extensions.size()];
	    for (int i = 0; i < extensions.size(); i++) 
		ar[i] = (String) extensions.get(i);
	    return ar;
	}
    }

    // Try moving to other file.
    private void moveTo(File file) {
	file = FileAux.getRelativePath(file);
	try {
	    resource.moveTo(file);
	    fileLabel.setText(resource.getLocation());
	    adjustCombo(resource.getLocation());
	    changed = true;
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(this,
		    "Could not move resource:\n" + e.getMessage(),
		     "File error", JOptionPane.ERROR_MESSAGE);
	    adjustCombo(resource.getLocation());
	}
    }

    ////////////////////////////////////////
    // Values.

    public void initValue() {
	fileLabel.setText(resource.getLocation());
	adjustCombo(resource.getLocation());
    }

    public void saveValue() {
	// will already have been done
    }

    // Whenever file is changed, see if extension matches
    // known extension and change combo accordingly.
    private void adjustCombo(String text) {
	fileLabel.getFullText();
	for (int i = 0; i < extensions.size(); i++) {
	    String ext = (String) extensions.get(i);
	    if (FileAux.hasExtension(text, ext)) {
		combo.setSelectedItem(ext);
		break;
	    }
	}
    }

}
