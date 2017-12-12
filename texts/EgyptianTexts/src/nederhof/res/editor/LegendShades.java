/***************************************************************************/
/*                                                                         */
/*  LegendShades.java                                                      */
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

// Part of legend dealing with shade.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

abstract class LegendShades extends LegendParam implements ItemListener {

    private static final int MAX_RESOLUTION = 16;

    // Old value.
    private Vector old;
    // Last returned value.
    private Vector lastReturned;

    // Resolution.
    private JComboBox resolutionBox;

    // Grid of black/white buttons.
    private Board fields;

    // Make entry.
    public LegendShades(Vector old) {
	this.old = old;
	this.lastReturned = old;
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

	Vector resolutions = new Vector();
	for (int i = 2; i <= MAX_RESOLUTION; i *= 2)
	    resolutions.add(new Integer(i));
	resolutionBox = new JComboBox(resolutions);
	resolutionBox.setMaximumRowCount(10);
	resolutionBox.setSelectedItem(new Integer(2));
	resolutionBox.addItemListener(this);
	add(resolutionBox);

	fields = new Board(2);
	add(fields);
	add(Box.createHorizontalGlue());

	setValue(old);
    }

    // Set to value.
    private void setValue(Vector shades) {
	int resolution = maxResolution(shades);
	if (fields.dimension() == resolution) 
	    fields.clear();
	else {
	    removeAll();
	    add(resolutionBox);
	    fields = new Board(resolution);
	    add(fields);
	    refitContext();
	}
	fields.fillFields(shades);
	resolutionBox.setSelectedItem(new Integer(resolution));
    }

    // Get value.
    public Vector getValue() {
	return fields.patterns();
    }

    // Restore to blank.
    public void clear() {
	setValue(new Vector());
	processMaybeChanged(getValue());
    }

    // Reset to old value.
    public void resetValue() {
        setValue(old);
        processMaybeChanged(getValue());
    }

    // Get maximum resolution based on patterns.
    private int maxResolution(Vector shades) {
	int max = 2;
	for (int i = 0; i < shades.size(); i++) {
	    String pattern = (String) shades.get(i);
	    max = Math.max(max, maxResolution(pattern));
	}
	return max;
    }

    // Get maximum resolution based on pattern.
    private int maxResolution(String pattern) {
	int xMax = 1;
	int yMax = 1;
	for (int i = 0; i < pattern.length(); i++) {
	    char c = pattern.charAt(i);
	    switch (c) {
		case 't':
		case 'b':
		    yMax *= 2;
		    break;
		case 's':
		case 'e':
		    xMax *= 2;
		    break;
	    }
	}
	return Math.max(xMax, yMax);
    }

    // Process changed resolution.
    public void itemStateChanged(ItemEvent e) {
	if (e.getStateChange() != ItemEvent.SELECTED)
	    return;
	Vector shades = getValue();
	Integer newResolution = (Integer) resolutionBox.getSelectedItem();
	int resolution = newResolution.intValue();
	if (fields.dimension() == resolution) 
	    fields.clear();
	else {
	    removeAll();
	    add(resolutionBox);
	    fields = new Board(resolution);
	    add(fields);
	    refitContext();
	}
	fields.fillFields(shades);
	processMaybeChanged(getValue());
    }

    // Propagate changed size of board in legend.
    private void refitContext() {
	Container top = this;
	while (top.getParent() != null) {
	    top = top.getParent();
	    top.validate(); 
	}
	if (top instanceof JFrame) {
	    JFrame frame = (JFrame) top;
	    frame.pack();
	}
    }

    // Square board of black/white fields.
    private class Board extends JPanel {
	// The black/white squares.
	private Square[][] fields;

	// Create board.
	public Board(int dimension) {
	    super(new GridLayout(dimension, dimension));
	    int squareSize = 20;
	    if (dimension >= 16)
		squareSize = 13;
	    fields = new Square[dimension][dimension];
	    for (int y = 0; y < dimension; y++) 
		for (int x = 0; x < dimension; x++) {
		    Square f = new Square(squareSize);
		    add(f);
		    fields[x][y] = f;
		}
	}

	// Width/height.
	public int dimension() {
	    return fields[0].length;
	}

	// Make all white.
	public void clear() {
	    for (int x = 0; x < dimension(); x++)
		for (int y = 0; y < dimension(); y++)
		    fields[x][y].setOff();
	}

	// Set fields according to patterns.
	public void fillFields(Vector patterns) {
	    for (int i = 0; i < patterns.size(); i++) {
		String pattern = (String) patterns.get(i);
		fillFields(pattern);
	    }
	}

	// Set fields according to patterns.
	public void fillFields(String pattern) {
	    int xLow = 0;
	    int xHigh = dimension();
	    int yLow = 0;
	    int yHigh = dimension();
	    for (int i = 0; i < pattern.length(); i++) {
		char c = pattern.charAt(i);
		switch (c) {
		    case 't':
			if (yLow < yHigh - 1) 
			    yHigh -= (yHigh-yLow) / 2;
			break;
		    case 'b':
			if (yLow < yHigh - 1) 
			    yLow += (yHigh-yLow) / 2;
			break;
		    case 's':
			if (xLow < xHigh - 1) 
			    xHigh -= (xHigh-xLow) / 2;
			break;
		    case 'e':
			if (xLow < xHigh - 1) 
			    xLow += (xHigh-xLow) / 2;
			break;
		}
	    }
	    for (int x = xLow; x < xHigh; x++)
		for (int y = yLow; y < yHigh; y++)
		    fields[x][y].setOn();
	}

	// Get patterns corresponding to black fields.
	public Vector patterns() {
	    Vector patterns = new Vector();
	    int xLow = 0;
	    int xHigh = dimension();
	    int yLow = 0;
	    int yHigh = dimension();
	    patterns(xLow, xHigh, yLow, yHigh, "", patterns);
	    return patterns;
	}

	// Get patterns in section of board.
	private void patterns(int xLow, int xHigh, int yLow, int yHigh,
		String pattern, Vector patterns) {
	    if (xLow >= xHigh - 1)
		return;
	    int xCenter = xLow + (xHigh-xLow) / 2;
	    int yCenter = yLow + (yHigh-yLow) / 2;
	    boolean upperLeft = false;
	    boolean upperRight = false;
	    boolean lowerLeft = false;
	    boolean lowerRight = false;
	    // upper
	    if (allBlack(xLow, xHigh, yLow, yCenter)) {
		patterns.add(pattern + "t");
		upperLeft = true;
		upperRight = true;
	    }
	    // lower
	    if (allBlack(xLow, xHigh, yCenter, yHigh)) {
		patterns.add(pattern + "b");
		lowerLeft = true;
		lowerRight = true;
	    }
	    // left
	    if (allBlack(xLow, xCenter, yLow, yHigh)) {
		patterns.add(pattern + "s");
		upperLeft = true;
		lowerLeft = true;
	    }
	    // right
	    if (allBlack(xCenter, xHigh, yLow, yHigh)) {
		patterns.add(pattern + "e");
		upperRight = true;
		lowerRight = true;
	    }
	    // upper left
	    if (!upperLeft) {
		if (allBlack(xLow, xCenter, yLow, yCenter))
		    patterns.add(pattern + "ts");
		else
		    patterns(xLow, xCenter, yLow, yCenter, pattern + "ts", patterns);
	    }
	    // upper right
	    if (!upperRight) {
		if (allBlack(xCenter, xHigh, yLow, yCenter))
		    patterns.add(pattern + "te");
		else
		    patterns(xCenter, xHigh, yLow, yCenter, pattern + "te", patterns);
	    }
	    // lower left
	    if (!lowerLeft) {
		if (allBlack(xLow, xCenter, yCenter, yHigh))
		    patterns.add(pattern + "bs");
		else
		    patterns(xLow, xCenter, yCenter, yHigh, pattern + "bs", patterns);
	    }
	    // lower right
	    if (!lowerRight) {
		if (allBlack(xCenter, xHigh, yCenter, yHigh))
		    patterns.add(pattern + "be");
		else
		    patterns(xCenter, xHigh, yCenter, yHigh, pattern + "be", patterns);
	    }
	}

	// Are all fields black.
	private boolean allBlack(int xLow, int xHigh, int yLow, int yHigh) {
	    boolean black = true;
	    for (int x = xLow; x < xHigh; x++)
		for (int y = yLow; y < yHigh; y++)
		    if (!fields[x][y].isOn())
			black = false;
	    return black;
	}
    }

    // White or black square.
    private class Square extends JButton implements ActionListener {
	// Is black or white?
	private boolean on;

	// Size of square.
	private int size;

	// Create square of exact size.
	public Square(int size) {
	    this.size = size;
	    setOff();
	    addActionListener(this);
	}

	// Make no bigger than necessary.
	public Dimension getMaximumSize() {
	    return new Dimension(size, size);
	}
	public Dimension getPreferredSize() {
	    return new Dimension(size, size);
	}

	// Set on or off.
	public void setOn() {
	    setBackground(Color.BLACK);
	    on = true;
	}
	public void setOff() {
	    setBackground(Color.WHITE);
	    on = false;
	}

	public boolean isOn() {
	    return on;
	}

	public void actionPerformed(ActionEvent e) {
	    if (on)
		setOff();
	    else
		setOn();
	    processMaybeChanged(getValue());
	}
    }

    private void processMaybeChanged(Vector other) {
        if (!other.equals(lastReturned)) {
            lastReturned = other;
            processChanged(other);
        }
    }

    // Process that value is changed.
    abstract protected void processChanged(Vector val);

}
