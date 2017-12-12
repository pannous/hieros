/***************************************************************************/
/*                                                                         */
/*  ResourcePanel.java                                                     */
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

// Part of index, showing one resource.

package nederhof.interlinear.frame;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import nederhof.interlinear.*;
import nederhof.util.*;

abstract class ResourcePanel extends JPanel implements ActionListener {

    // The resource.
    private TextResource resource;

    // Is to be highlighted.
    private boolean highlighted = false;

    // Can resource be edited?
    private boolean editable = false;

    // Should buttons be enabled?
    private boolean enabled = true;

    // Can be moved up/down relative to other resources.
    private boolean moveableUp = false;
    private boolean moveableDown = false;

    // Text elements. To be enabled/disabled.
    protected Vector textElements = new Vector();

    // Construct. Make tiers visible according to resource.
    public ResourcePanel(TextResource resource) {
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

	this.resource = resource;
    }

    // Get corresponding resource.
    public TextResource getResource() {
	return resource;
    }

    // Set highlighted.
    public void setHighlight(boolean highlighted) {
	this.highlighted = highlighted;
    }

    // Set editable.
    public void setEditable(boolean editable) {
	this.editable = editable;
    }

    // Enable/disable buttons.
    public void setEnabled(boolean enabled) {
	this.enabled = enabled;
	refreshLayout();
    }

    // Allow moving up.
    public void setMoveableUp(boolean allow) {
	moveableUp = allow;
    }

    // Allow moving down.
    public void setMoveableDown(boolean allow) {
	moveableDown = allow;
    }

    // Make buttons where needed.
    public void refreshLayout() {
	removeAll();
	textElements.clear();
	setBackground(backColor());

	addBorder();
	addTiers();
	addFunctionButtons();
	if (!editable && !tierShowing()) // if no buttons or tiers
	    add(Box.createHorizontalGlue()); // then at least empty space
	for (int i = 0; i < textElements.size(); i++) {
	    JComponent comp = (JComponent) textElements.get(i);
	    comp.setEnabled(enabled);
	}
	validate();
    }

    // Add border.
    private void addBorder() {
	String name = "resource";
	if (resource.getName() != null)
	    name = "" + resource.getName();
	Color lineColor = highlighted ? Color.BLUE : 
	    (enabled ? Color.BLACK : Color.GRAY);
	int lineThick = highlighted ? 3 : 1;
	TitledBorder title = BorderFactory.createTitledBorder(
		new LineBorder(lineColor, lineThick), name);
	title.setTitleColor(highlighted ? Color.BLUE : 
		(enabled ? Color.BLACK : Color.GRAY));
	setBorder(BorderFactory.createCompoundBorder(title,
		    BorderFactory.createEmptyBorder(0,5,5,5)));
    }

    // Add tiers, where needed.
    private void addTiers() {
	if (!tierShowing())
	    return;
	JPanel tierPanel = new JPanel(new SpringLayout());
	tierPanel.setBackground(backColor());
	int nCols = 0;
	int nRows = 0;
	if (resource.isEditable() && resource.nTiers() > 0) {
	    nCols = 4;
	    for (int i = 0; i < resource.nTiers(); i++) {
		String name = resource.tierName(i);
		EnableLabel nameLabel = 
		    new EnableLabel(name + "   ");
		if (resource.isEmptyTier(i))
		    nameLabel.setForeground(Color.GRAY);
		else
		    nameLabel.setForeground(Color.BLACK);
		TierMode tierMode = new TierMode(new Integer(i));
		tierPanel.add(nameLabel);
		tierPanel.add(tierMode.check);
		tierPanel.add(tierMode.combo);
		tierPanel.add(Box.createHorizontalGlue()); 
		nRows++;
	    }
	} else {
	    nCols = 4;
	    for (int i = 0; i < resource.nTiers(); i++) 
		if (!resource.isEmptyTier(i)) {
		    String name = resource.tierName(i);
		    TierMode tierMode = new TierMode(new Integer(i));
		    tierPanel.add(new EnableLabel(name + "   "));
		    tierPanel.add(tierMode.check);
		    tierPanel.add(tierMode.combo);
		    tierPanel.add(Box.createHorizontalGlue()); 
		    nRows++;
		}
	}
	SpringUtilities.makeCompactGrid(tierPanel, nRows, nCols, 5, 5, 5, 5);
	add(tierPanel);
    }

    // A tier is showing when resource is editable and there is at least one
    // tier, or if at least one tier is selected to be used.
    private boolean tierShowing() {
	if (resource.isEditable() && resource.nTiers() > 0)
	    return true;
	for (int i = 0; i < resource.nTiers(); i++)
	    if (!resource.isEmptyTier(i))
		return true;
	return false;
    }

    // Add buttons, if needed.
    private void addFunctionButtons() {
	JPanel functionsPanel = new JPanel();
	functionsPanel.setLayout(new BoxLayout(functionsPanel, BoxLayout.X_AXIS));
	functionsPanel.setBackground(backColor());
	JButton viewButton = new FunctionButton("view");
	functionsPanel.add(viewButton);
	functionsPanel.add(panelSep());
	if (editable) {
	    if (resource.getLocation() == null || resource.isEditable()) {
		JButton editButton = new FunctionButton("edit");
		functionsPanel.add(editButton);
		functionsPanel.add(panelSep());
	    }
	    if (moveableUp) {
		JButton upButton = new FunctionButton("up");
		functionsPanel.add(upButton);
		functionsPanel.add(panelSep());
	    }
	    if (moveableDown) {
		JButton downButton = new FunctionButton("down");
		functionsPanel.add(downButton);
		functionsPanel.add(panelSep());
	    }
	    JButton delButton = new FunctionButton("delete");
	    delButton.setForeground(Color.RED);
	    functionsPanel.add(delButton);
	}
	functionsPanel.add(Box.createHorizontalGlue());
	add(functionsPanel);
    }

    // Background color may depend on highlight.
    private Color backColor() {
	return Color.WHITE;
    }

    // Some separation between panels.
    protected Component panelSep() {
	return Box.createRigidArea(new Dimension(10, 10));
    }

    //////////////////////////////////////////
    // Components.

    // Label that is registered as text element.
    private class EnableLabel extends JLabel {
	public EnableLabel(String text) {
	    super(text);
	    textElements.add(this);
	}
    }

    // Button for operation on resource.
    private class FunctionButton extends JButton {
	public FunctionButton(String action) {
	    setFocusPainted(false);
	    setRolloverEnabled(true);
	    setText(action);
	    setActionCommand(action);
	    setMaximumSize(getPreferredSize());
	    addActionListener(ResourcePanel.this);
	    textElements.add(this);
	}
    }

    // For selecting mode of tier. Consists of checkbox and combobox.
    private class TierMode {
	public ModeCheck check;
	public ModeCombo combo;

	public TierMode(Integer i) {
	    check = new ModeCheck(i);
	    combo = new ModeCombo(i);
	    check.combo = combo;
	    combo.check = check;
	}
    }

    // Check button for viewing mode.
    // Is tied to combobox.
    private class ModeCheck extends JCheckBox implements ItemListener {
	int i;
	ModeCombo combo;

	public ModeCheck(Integer i) {
	    setBackground(backColor());
	    this.i = i.intValue();
	    setSelected(!resource.getMode(this.i).equals(TextResource.IGNORED));
	    addItemListener(this);
	    textElements.add(this);
	}

	public void itemStateChanged(ItemEvent e) {
	    if (combo == null)
		return;
	    boolean view = (e.getStateChange() == ItemEvent.SELECTED);
	    String mode = view ? (String) combo.getSelectedItem() 
				: TextResource.IGNORED;
	    resource.setMode(i, mode);
	    if (editable && resource.isEditable()) 
		save();
	    makeResourceChanged();
	    combo.setEnabled(!mode.equals(TextResource.IGNORED));
	}
    }

    // Combo for viewing mode.
    // Is tied to check box.
    // This excludes the ignored mode.
    private class ModeCombo extends JComboBox implements ActionListener {
	int i;
	ModeCheck check;

	public ModeCombo(Integer i) {
	    super(resource.nonIgnoreModes());
	    setBackground(backColor());
	    setMaximumSize(getPreferredSize());
	    this.i = i.intValue();
	    String mode = resource.getMode(this.i);
	    if (mode.equals(TextResource.IGNORED))
		setSelectedItem(TextResource.SHOWN);
	    else
		setSelectedItem(mode);
	    addActionListener(this);
	    textElements.add(this);
	    setEnabled(!mode.equals(TextResource.IGNORED));
	}

	public void actionPerformed(ActionEvent e) {
	    if (check == null || !check.isSelected())
		return;
	    String mode = (String) getSelectedItem();
	    resource.setMode(i, mode);
	    if (editable && resource.isEditable())
		save();
	    makeResourceChanged();
	}

	// Do not enable if mode is IGNORED.
	public void setEnabled(boolean enable) {
	    super.setEnabled(enable && check != null && check.isSelected());
	}
    }

    // Try save. Warn if unsuccessful.
    private void save() {
	try {
	    resource.save();
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(this,
		    "Could not save resource:\n" + e.getMessage(),
		    "File error", JOptionPane.ERROR_MESSAGE);
	}
    }

    ///////////////////////////////////////////////////
    // Interface to caller.

    // Listen to buttons.
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("view"))
	    viewResource();
	else if (e.getActionCommand().equals("edit"))
	    editResource();
	else if (e.getActionCommand().equals("up"))
	    moveUp();
	else if (e.getActionCommand().equals("down"))
	    moveDown();
	else if (e.getActionCommand().equals("delete"))
	    deleteResource();
    }

    // Caller defines:
    public abstract void viewResource();
    public abstract void editResource();
    public abstract void moveUp();
    public abstract void moveDown();
    public abstract void deleteResource();

    public abstract void makeTextChanged();
    public abstract void makeResourceChanged();

}
