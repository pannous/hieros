/***************************************************************************/
/*                                                                         */
/*  ComboEditor.java                                                       */
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

// Element used to edit a property of a resource.

package nederhof.interlinear.frame;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.interlinear.*;

public class ComboEditor extends NamedPropertyEditor
                implements ActionListener {

     // The element holding the value.
     private JComboBox box;

    // Field embedded in name and comment.
    public ComboEditor(TextResource resource, String name, 
	    Vector values, ListCellRenderer cellRender,
	    int nameWidth, String comment) {
        super(resource, name);

        box = new JComboBox(values);
	box.setMaximumRowCount(5);
        box.setMaximumSize(box.getPreferredSize());
	box.setRenderer(cellRender);
        box.addActionListener(this);

        JLabel nameLabel = new BoldLabel(name + " ");
        nameLabel.setPreferredSize(new Dimension(nameWidth,
                    nameLabel.getPreferredSize().height));
        nameLabel.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel commentLabel = new PlainLabel(comment);

        add(panelSep());
        add(nameLabel);
        add(box);
        add(panelSep());
        add(commentLabel);
        add(panelGlue());

        textElements.add(nameLabel);
        textElements.add(box);
        textElements.add(commentLabel);
    }

    ////////////////////////////////////////
    // Values.

    // Put string in text.
    public void putValue(Object val) {
        String text = "";
        if (val != null)
            text = (String) val;
	box.setSelectedItem(text);
        changed = false;
    }

    // Current string.
    public Object retrieveValue() {
        return box.getSelectedItem();
    }

    // Any change is recorded.
    public void actionPerformed(ActionEvent e) {
        changed = true;
    }

}
