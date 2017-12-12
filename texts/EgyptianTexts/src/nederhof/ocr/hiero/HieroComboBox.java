package nederhof.ocr.hiero;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import nederhof.ocr.*;

// For choosing hieroglyphs.
public abstract class HieroComboBox extends ChoiceBox {

	public HieroComboBox(Blob glyph) {
		super(glyph, addFields(ChoiceBox.fields(glyph)));
	}

	protected ListCellRenderer getCellRenderer() {
		return new HieroCellRenderer();
	}

    // Add extra fields.
    private static Vector<String> addFields(Vector<String> allNames) {
        allNames.add("menu");
        allNames.add("extra");
        return allNames;
    }

    // Subclass to handle special selections.
	public void receive(String name) {
		if (name == null) {
			if (getSelectedItem().equals("menu") || getSelectedItem().equals("extra"))
				for (int i = 0; i < getItemCount(); i++) 
					if (!getItemAt(i).equals("?") &&
							!getItemAt(i).equals("menu") &&
							!getItemAt(i).equals("extra")) {
						setSelectedIndex(i);
						break;
					}   
		} else
			super.receive(name);
	}

	// Try to handle special cases first. Then delegate to superclass.
    public void itemStateChanged(ItemEvent event) {
        if (event.getStateChange() == ItemEvent.SELECTED) {
            String val = (String) getSelectedItem();
            if (val.equals("menu"))
                openMenu();
            else if (val.equals("extra"))
                openExtraMenu();
            else 
				super.itemStateChanged(event);
        }
    }

    // Communication to caller.
    public abstract void openMenu();
    public abstract void openExtraMenu();

}
