// Editor for transliteration.

package nederhof.interlinear.egyptian.ortho;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.frame.*;

public class AlPlainEditor extends StyledPhraseEditor {

    // Negative is none specified.
    private int nChars = -1;

    public AlPlainEditor(String name, String value) {
	super(new AlPlainEditPopup(), -1, name, singleton(value));
    }

    public AlPlainEditor(String name, int nChars) {
	super(new AlPlainEditPopup(), -1, name, singleton(""));
	this.nChars = nChars;
    }

    protected Vector toEditParts(Vector parts) {
	return TransHelper.lowerUpperParts(getSingle(parts));
    }

    protected Vector fromEditParts(Vector parts) {
	return singleton(TransHelper.simpleMergeTransLowerUpper(parts));
    }

    // Additional public method, putting in string
    public void putString(String value) {
	putValue(singleton(value));
    }

    // Additional public method, expecting String value.
    public String getString() {
	return getSingle(getValue());
    }

    // This subclass of StyledPhraseEditor requires a hack.
    // This is because superclass expects a vector rather than a single
    // value.
    private static Vector singleton(String s) {
	Vector v = new Vector();
	v.add(s);
	return v;
    }
    private static String getSingle(Vector v) {
	return (String) v.get(0);
    }

    // As soon as parent is set, the styledpane is know.
    // Then set disabled text color of styledpane.
    public void setParent(EditChainElement parent) {
	super.setParent(parent);
	styledPane.setDisabledTextColor(Color.BLACK);

	if (nChars >= 0) {
	    Font fontAl = TransHelper.translitUpper(
		    Settings.translitFontStyle, Settings.translitFontSize);
	    FontMetrics fm = styledPane.getFontMetrics(fontAl);
	    int width = fm.charWidth('H') * nChars;
	    int height = fm.getHeight() + 4; // seems to be needed

	    // setPreferredSize(new Dimension(300, 40));
	    styledPane.setPreferredSize(new Dimension(width, height));
	    styledPane.setMinimumSize(new Dimension(width, height));
	}
    }

}
