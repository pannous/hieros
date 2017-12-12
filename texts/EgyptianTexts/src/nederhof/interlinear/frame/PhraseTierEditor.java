/***************************************************************************/
/*                                                                         */
/*  PhraseTierEditor.java                                                  */
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

// Editor for one tier of phrase.

package nederhof.interlinear.frame;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public abstract class PhraseTierEditor extends JPanel {

    // Parent.
    protected EditChainElement parent;

    // Number of tier in resource.
    private int tierNum;

    // Has changed since last initialization?
    protected boolean changed = false;

    // Panel elements.
    protected Vector panelElements = new Vector();

    // Text elements.
    protected Vector textElements = new Vector();

    // Construct editor of property.
    public PhraseTierEditor(int tierNum) {
	this.tierNum = tierNum;
        panelElements.add(this);
        layoutEditor();
    }

    // One element to the right of the other.
    protected void layoutEditor() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    }

    // Set parent. To be used to disable all siblings.
    public void setParent(EditChainElement parent) {
        this.parent = parent;
    }

    public int getTierNum() {
	return tierNum;
    }

    // Get layout right, by going to text level, where necessary.
    public void refreshLayout() {
    }

    //////////////////////////////
    // Value manipulation.

    // Has been changed?
    public boolean isChanged() {
        return changed;
    }

    // Some subclasses may want to be notified of change.
    // Then this should be overridden.
    protected void makeChanged() {
    }

    // Get current value.
    public abstract Vector getValue();

    //////////////////////////////
    // Appearance.

    // Allow editing. (I.e. not blocked due to other edit.)
    // Subclasses may override this method.
    public void setEnabled(boolean allow) {
        for (int i = 0; i < panelElements.size(); i++) {
            JComponent comp = (JComponent) panelElements.get(i);
            comp.setBackground(backColor(allow));
        }
        for (int i = 0; i < textElements.size(); i++) {
            JComponent comp = (JComponent) textElements.get(i);
            comp.setEnabled(allow);
        }
    }

    // Color may depend on allowed editing.
    protected Color backColor(boolean editable) {
        return Color.WHITE;
    }

    //////////////////////////////
    // Auxiliaries for subclasses.

    // Some separation between panels.
    protected Component panelSep() {
       return Box.createRigidArea(new Dimension(10, 10));
    }

    // Trailing white space.
    protected Component panelGlue() {
        return Box.createHorizontalGlue();
    }

    // Fonts.
    protected static Font inputTextFont() {
        return new Font(
                Settings.inputTextFontName,
                Font.PLAIN,
                Settings.inputTextFontSize);
    }
    protected static Font labelFont(int style) {
        return new Font(
                Settings.labelFontName,
                style,
                Settings.labelFontSize);
    }

    // Label in style.
    public static class StyleLabel extends JLabel {
        public StyleLabel(String text, int style) {
            super(text);
            setFont(labelFont(style));
        }
    }
    public static class PlainLabel extends StyleLabel {
        public PlainLabel(String text) {
            super(text, Font.PLAIN);
        }
    }
    public static class ItalicLabel extends StyleLabel {
        public ItalicLabel(String text) {
            super(text, Font.ITALIC);
        }
    }
    public static class BoldLabel extends StyleLabel {
        public BoldLabel(String text) {
            super(text, Font.BOLD);
        }
    }

}
