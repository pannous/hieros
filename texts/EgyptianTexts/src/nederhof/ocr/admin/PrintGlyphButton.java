package nederhof.ocr.admin;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.xml.parsers.*;

import nederhof.res.*;
import nederhof.res.format.*;
import nederhof.util.*;

// Glyph button containing name and normalized representation.
public abstract class PrintGlyphButton extends JButton {

	// Name of glyph.
	protected String text;

	// Constructor.
	public PrintGlyphButton(String text) {
		this.text = text;
		setBackground(Color.WHITE);
		setFocusable(false);
	}

	// Get dimensions of glyph.
	public abstract int getGlyphWidth();
	public abstract int getGlyphHeight();

}
