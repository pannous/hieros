/***************************************************************************/
/*                                                                         */
/*  PrecedencePanel.java                                                   */
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

// Part of index, showing one precedence resource.

package nederhof.interlinear.frame;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import nederhof.interlinear.*;
import nederhof.interlinear.labels.*;
import nederhof.util.*;

abstract class PrecedencePanel extends JPanel implements ActionListener {

    // The resource.
    private ResourcePrecedence precedence;

    // Is to be highlighted.
    private boolean highlighted = false;

    // Should buttons be enabled?
    private boolean enabled = true;

    // Text elements. To be enabled/disabled.
    protected Vector textElements = new Vector();

    // Construct. 
    public PrecedencePanel(ResourcePrecedence precedence) {
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	this.precedence = precedence;
    }

    // Get corresponding resource.
    public ResourcePrecedence getResource() {
        return precedence;
    }

    // Set highlighted.
    public void setHighlight(boolean highlighted) {
        this.highlighted = highlighted;
    }

    // Enable/disable buttons.
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        refreshLayout();
    }

    // Make buttons where needed.
    public void refreshLayout() {
        removeAll();
        textElements.clear();
        setBackground(backColor());

        addBorder();
        addFunctionButtons();
        for (int i = 0; i < textElements.size(); i++) {
            JComponent comp = (JComponent) textElements.get(i);
            comp.setEnabled(enabled);
        }
        validate();
    }

    // Add border.
    private void addBorder() {
        String name = "alignment";
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

    // Add buttons, if needed.
    private void addFunctionButtons() {
        JPanel functionsPanel = new JPanel();
        functionsPanel.setLayout(new BoxLayout(functionsPanel, BoxLayout.X_AXIS));
        functionsPanel.setBackground(backColor());
	if (precedence.getLocation() == null || precedence.isEditable()) {
	    JButton editButton = new FunctionButton("edit");
	    functionsPanel.add(editButton);
	    functionsPanel.add(panelSep());
	    String name1 = precedence.getResource1().getName();
	    String name2 = precedence.getResource2().getName();
	    JLabel lab = new JLabel(name1 + " / " + name2);
	    functionsPanel.add(lab);
	    textElements.add(lab);
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

    // Button for operation on resource.
    private class FunctionButton extends JButton {
        public FunctionButton(String action) {
            setFocusPainted(false);
            setRolloverEnabled(true);
            setText(action);
            setActionCommand(action);
            setMaximumSize(getPreferredSize());
            addActionListener(PrecedencePanel.this);
            textElements.add(this);
        }
    }

    ///////////////////////////////////////////////////
    // Interface to caller.

    // Listen to buttons.
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("edit"))
            editPrecedence();
    }

    // Caller defines:
    public abstract void editPrecedence();

}
