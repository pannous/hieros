package nederhof.ocr;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.plaf.metal.*;

// For choosing signs.
public abstract class ChoiceBox extends JComboBox implements ItemListener, PopupMenuListener {

	// The glyph for which this is to choose name.
	protected Blob glyph;

	// To be called from constructor of subclass.
	protected ChoiceBox(Blob glyph, Vector<String> names) {
		super(names);
		// needed in MacOS or height is wrong
		setUI(new MetalComboBoxUI());
		this.glyph = glyph;
		setRenderer(getCellRenderer());
		addItemListener(this);
		addPopupMenuListener(this);
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3 || e.isControlDown())
					rightClicked();
			}
		});
	}

	protected abstract ListCellRenderer getCellRenderer();

	// For first few fields, take names from glyph.
	protected static Vector<String> fields(Blob glyph) {
		Vector<String> allNames = new Vector<String>();
		if (glyph.getName().equals("") && glyph.getGuessed() == null) {
			allNames.add("?");
		} else {
			if (!glyph.getName().equals("")) 
				allNames.add(glyph.getName());
			if (glyph.getGuessed() != null) 
				for (String guess : glyph.getGuessed())
					if (!guess.equals(glyph.getName()))
						allNames.add(guess);
		}
		return allNames;
	}

	// If string present, select it. 
	// Otherwise, add at top.
	public void receive(String name) {
		if (name != null) {
			for (int i = 0; i < getItemCount(); i++) {
				if (name.equals(getItemAt(i))) {
					// simply doing setSelectedIndex(i) doesn't work
					removeItemAt(i);
					insertItemAt(name, 0);
					setSelectedItem(name);
					return;
				}
			}
			insertItemAt(name, 0);
			setSelectedItem(name);
		} 
	}

	public Dimension getMaximumSize() {
		return super.getPreferredSize();
	}

	public void itemStateChanged(ItemEvent event) {
		if (event.getStateChange() == ItemEvent.SELECTED) {
			String val = (String) getSelectedItem();
			if (!val.equals("?"))
				glyph.setName(val);
		}
	}

	public void popupMenuCanceled(PopupMenuEvent e) {
		// ignore
	}
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		// ignore
	}
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		setFocus(glyph);
	}

	private void rightClicked() {
		setFocus(glyph);
		openNoteEditor();
	}

	// Communication to caller.
	public abstract void setFocus(Blob blob);
	public abstract void openNoteEditor();

}
