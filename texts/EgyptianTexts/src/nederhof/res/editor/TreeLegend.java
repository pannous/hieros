/***************************************************************************/
/*                                                                         */
/*  TreeLegend.java                                                        */
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

// Summarizes available options.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import nederhof.res.*;
import nederhof.util.*;

class TreeLegend extends JFrame implements ActionListener {

    // Context for rendering in legend.
    private HieroRenderContext context;

    // Panel that contains header and top label.
    private JPanel headerPanel = new JPanel();

    // Panel that contains hieroglyphic rendering.
    private LegendHieroPanel hieroPanel = new LegendHieroPanel();

    // Panel that contains parameters plus confirmation buttons.
    private JPanel centralPanel = new JPanel();

    // Panel that contains parameters.
    private JPanel paramPanel = new JPanel();

    // Panel that contains buttons for structural changes.
    private JPanel structurePanel = new JPanel();

    // Space between elements.
    private final int STRUT_SIZE = 6;

    // The legend is initially empty.
    public TreeLegend(HieroRenderContext context,
	    KeyListener listener, WindowAdapter closeListener) {
	this.context = context;
	setTitle("Legend");
	JPanel confirmationPanel = new JPanel(new SpringLayout());
	Container content = getContentPane();
	content.setLayout(new BorderLayout());
	headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
	// Panel that contains confirmation buttons.
	confirmationPanel.setLayout(
		new BoxLayout(confirmationPanel, BoxLayout.X_AXIS));
	confirmationPanel.add(Box.createHorizontalGlue());
	confirmationPanel.add(new KeyButton("apply", "<u>a</u>pply")); 
	confirmationPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
	confirmationPanel.add(new KeyButton("restore", "<u>r</u>estore")); 
	confirmationPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
	confirmationPanel.add(new KeyButton("default", "<u>d</u>efault")); 
	confirmationPanel.add(Box.createHorizontalGlue());

	centralPanel.setLayout(new BoxLayout(centralPanel, BoxLayout.Y_AXIS));
	centralPanel.add(paramPanel);
	centralPanel.add(Box.createVerticalStrut(5));
	centralPanel.add(confirmationPanel);
	content.add(headerPanel, BorderLayout.NORTH);
	content.add(structurePanel, BorderLayout.SOUTH);
	paramPanel.setLayout(new BoxLayout(paramPanel, BoxLayout.Y_AXIS));

	addKeyListener(listener);
	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	addWindowListener(closeListener);
	// Needed to avoid legend drawing focus upon making visible.
	setFocusableWindowState(false);
    }

    // Button that can also be activated by key.
    private class KeyButton extends JButton {
	public KeyButton(String action, String key) {
	    setActionCommand(action);
	    setText("<html>" + key + "</html>");
	    setMinimumSize(getPreferredSize());
	    setMaximumSize(getPreferredSize());
	    setFocusable(false);
	    addActionListener(TreeLegend.this);
	}
    }

    // Do apply.
    private void doApply() {
	setCursor(new Cursor(Cursor.WAIT_CURSOR));
	node.root().refresh();
	setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    // Do restore.
    private void doRestore() {
	if (params != null)
	    params.reset();
    }
    
    // Do default.
    private void doDefault() {
	params.clear();
    }

    // Actions belonging to buttons.
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("apply")) 
	    doApply();
	else if (e.getActionCommand().equals("restore")) 
	    doRestore();
	else if (e.getActionCommand().equals("default")) 
	    doDefault();
    }

    // Try running command through parameters. 
    // If no luck, then interpret as confirmation buttons.
    public void processCommand(char c) {
	boolean processed = false;
	if (!processed && params != null)
	    processed = params.processCommand(c);
	if (!processed)
	    processed = takeKeyCommand(c);
	if (!processed && buttons != null)
	    processed = buttons.push(c);
    }

    // Take command from keyboard.
    public boolean takeKeyCommand(char key) {
	switch(key) {
	    case 'a':
		doApply();
		return true;
	    case 'r':
		doRestore();
		return true;
	    case 'd':
		doDefault();
		return true;
	}
	return false;
    }

    // Receive glyph and try to find matching parameter.
    public void receiveGlyph(String name) {
	if (params != null)
	    params.receiveGlyph(name);
    }

    ///////////////////////////////////////////////////////
    // Filling legend.

    // Current node.
    private TreeNode node;

    // Current parameters.
    private LegendParams params;

    // Current buttons.
    private LegendStructure buttons;

    // Initiate creation of new content for group panel.
    public void newContent(TreeNode node) {
	this.node = node;
	headerPanel.removeAll();
	JLabel lab = new JLabel(node.label());
	lab.setAlignmentX(CENTER_ALIGNMENT);
	headerPanel.add(lab);
	if (node.legendPreview()) {
	    headerPanel.add(hieroPanel);
	    hieroPanel.refresh();
	}
	getContentPane().remove(centralPanel);
	params = node.makeParams();
	if (!params.isEmpty()) {
	    getContentPane().add(centralPanel, BorderLayout.CENTER);
	    paramPanel.removeAll();
	    paramPanel.add(params);
	}
	structurePanel.removeAll();
	buttons = node.makeStructureButtons();
	structurePanel.add(buttons);

	pack(); 
	repaint(); 
    }

    // A panel used in the legend to preview settings.
    public class LegendHieroPanel extends HieroglyphicPanel {

        // Make panel.
        public LegendHieroPanel() {
	    super();
            setAlignmentX(CENTER_ALIGNMENT);
        }

	// Get context.
	public HieroRenderContext context() {
	    return context;
	}

	// Get hieroglyphic.
	public String hiero() {
	    if (node == null)
		return ""; // should not happen
	    else
		return node.resString();
	}

        // Propagate changed size of board in legend.
        protected void refit() {
            invalidate();
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

    }

    // Refresh hieropanel if needed.
    public void refresh() {
	if (node != null && node.legendPreview()) 
	    hieroPanel.refresh();
    }

}
