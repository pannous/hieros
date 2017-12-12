/***************************************************************************/
/*                                                                         */
/*  SchemeMapEditor.java                                                   */
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

// Some element that is used to modify a mapping between
// two numbering schemes.

package nederhof.interlinear.frame;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.interlinear.*;

public class SchemeMapEditor extends NamedPropertyEditor
	implements ActionListener, DocumentListener {

    // Pane holding table.
    private JPanel table;

    public SchemeMapEditor(TextResource resource, String name) {
	super(resource, name);
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	setBackground(backColor(true));

	table = new JPanel();
	table.setLayout(new GridLayout(0, 3));
	table.setBackground(backColor(true));
	add(panelGlue(), BorderLayout.WEST);
	add(table, BorderLayout.CENTER);
	add(panelGlue(), BorderLayout.EAST);
    }

    ////////////////////////////////////////
    // Values.

    // Added elements at end.
    private static final int incrRows = 10;

    // Header labels.
    private JLabel schemeLabel1 = new JLabel("scheme 1");
    private JLabel schemeLabel2 = new JLabel("scheme 2");

    // Fill table. Some extra empty elements at end.
    public void putValue(Object val) {
	table.removeAll();
	Vector maps = val == null ? new Vector() : (Vector) val;
	table.add(schemeLabel1);
	table.add(schemeLabel2);
	table.add(panelGlue());
	for (int i = 0; i < maps.size(); i++) {
	    String[] map = (String[]) maps.get(i);
	    JTextField f1 = new ListeningField(map[0]);
	    JTextField f2 = new ListeningField(map[1]);
	    JButton plus = new PlusButton("+1", "plus");
	    table.add(f1);
	    table.add(f2);
	    table.add(plus);
	}
	for (int i = 0; i < incrRows; i++) {
	    JTextField f1 = new ListeningField("");
	    JTextField f2 = new ListeningField("");
	    JButton plus = i < incrRows - 1 ?
		new PlusButton("+1", "plus") :
		    new PlusButton("+" + incrRows, "plusmany");
	    table.add(f1);
	    table.add(f2);
	    table.add(plus);
	}
	changed = false;
    }

    // Retrieve table.
    public Object retrieveValue() {
	Vector maps = new Vector();
	Component[] children = table.getComponents();
	for (int i = 0; i < children.length - 2; i += 3) {
	    Component c1 = children[i];
	    Component c2 = children[i+1];
	    if (c1 instanceof JTextField && c2 instanceof JTextField) {
		JTextField f1 = (JTextField) c1;
		JTextField f2 = (JTextField) c2;
		String t1 = f1.getText();
		String t2 = f2.getText();
		if (!t1.matches("\\s*") || !t2.matches("\\s*")) 
		    maps.add(new String[] {t1, t2});
	    }
	}
	return maps;
    }

    /////////////////////////////////////////////////////
    // Extending table.

    private class PlusButton extends JButton {
	public PlusButton(String text, String command) {
	    setText(text);
	    setActionCommand(command);
	    setFocusable(false);
	    setMaximumSize(getPreferredSize());
	    addActionListener(SchemeMapEditor.this);
	}
    }

    // Listen to buttons.
    // Add extra rows where needed.
    public void actionPerformed(ActionEvent e) {
	Object source = e.getSource();
	Component[] children = table.getComponents();
	for (int i = 0; i < children.length; i++) 
	    if (children[i] == source) {
		if (e.getActionCommand().equals("plus")) {
		    JTextField f1 = new ListeningField("");
		    JTextField f2 = new ListeningField("");
		    JButton plus = new PlusButton("+1", "plus");
		    table.add(f1, i+1);
		    table.add(f2, i+2);
		    table.add(plus, i+3);
		} else {
		    Component plusTen = (Component) source;
		    table.remove(plusTen);
		    for (int j = 0; j < incrRows; j++) {
			JButton plus = new PlusButton("+1", "plus");
			JTextField f1 = new ListeningField("");
			JTextField f2 = new ListeningField("");
			table.add(plus);
			table.add(f1);
			table.add(f2);
		    }
		    table.add(plusTen);
		}
		revalidate();
		break;
	    }
    }

    /////////////////////////////////////////////////////
    // Changes.

    private class ListeningField extends JTextField {
	public ListeningField(String text) {
	    setText(text);
	    setFont(inputTextFont());
	    getDocument().addDocumentListener(SchemeMapEditor.this);
	}
    }

    // Any change is recorded.
    public void changedUpdate(DocumentEvent e) {
        changed = true;
    }
    public void insertUpdate(DocumentEvent e) {
        changed = true;
    }
    public void removeUpdate(DocumentEvent e) {
        changed = true;
    }

    /////////////////////////////////////////////
    // Appearance.

    // Change appearance if not editing.
    public void setEnabled(boolean allow) {
	super.setEnabled(allow);
	schemeLabel1.setEnabled(allow);
	schemeLabel2.setEnabled(allow);
	Component[] children = table.getComponents();
	for (int i = 0; i < children.length - 2; i += 3) {
	    Component c1 = children[i];
	    Component c2 = children[i+1];
	    Component c3 = children[i+2];
	    if (c1 instanceof JTextField && 
		    c2 instanceof JTextField && 
		    c3 instanceof JButton) {
		JTextField f1 = (JTextField) c1;
		JTextField f2 = (JTextField) c2;
		JButton b = (JButton) c3;
		f1.setEnabled(allow);
		f2.setEnabled(allow);
		b.setEnabled(allow);
	    }
	}
    }

}
