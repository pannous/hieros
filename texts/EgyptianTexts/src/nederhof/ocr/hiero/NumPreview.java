package nederhof.ocr.hiero;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.ocr.*;

public abstract class NumPreview extends JButton implements ActionListener, PreviewElem {

	// Text.
	private String text;

	// The preview object.
	private LineFormat preview;

	public NumPreview(LineFormat preview) {
		super(preview.getVal());
		this.preview = preview;
		this.text = preview.getVal();
		setActionCommand("edit");
		addActionListener(this);
	}

	// Name(s) represented by this.
	public Vector<String> names() {
		Vector<String> names = new Vector<String>();
		names.add(text);
		return names;
	}

	//////////////////////////////////////////////////
	// Communication to caller.

	public void actionPerformed(ActionEvent e) {
		selected(preview);
	}

	// Notify user that this is clicked.
	public abstract void selected(LineFormat preview);

}
