package nederhof.interlinear.egyptian.ortho;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import nederhof.alignment.generic.*;
import nederhof.interlinear.egyptian.ortho.OrthoEditor.Mode;
import nederhof.interlinear.frame.*;
import nederhof.interlinear.egyptian.*;
import nederhof.res.*;
import nederhof.res.editor.FragmentEditor;
import nederhof.res.format.FormatFragment;

public class OrthoSegmentsPane extends JPanel {

    // Parent GUI element containing this.
    private EditChainElement parent;

    // For hieroglyphic.
    private HieroRenderContext hieroContext;
    private ParsingContext parsingContext;

    // For manipulating resource.
    private OrthoManipulator manipulator;

    // The current segment.
    private CurrentSegment currentSegment = null;

    public OrthoSegmentsPane(OrthoManipulator manipulator, 
	    EditChainElement parent,
            HieroRenderContext hieroContext, ParsingContext parsingContext) {
	this.manipulator = manipulator;
        this.parent = parent;
        this.hieroContext = hieroContext;
        this.parsingContext = parsingContext;
	setLayout(new FlowLayout(FlowLayout.LEFT));
    }

    // Make components for segments.
    public void update() {
	removeAll();
	final int windowSize = 2;
	int current = manipulator.currentSegment();
	for (int i = current - windowSize; i <= current + windowSize; i++) {
	    if (i >= 0 && i < manipulator.nSegments()) {
		String hi = manipulator.hiero(i);
		String al = manipulator.trans(i);
		Vector<OrthoElem> orthos = manipulator.orthos(i);
		if (i == current) {
		    currentSegment = new CurrentSegment(i, hi, al, orthos);
		    add(currentSegment);
		} else {
		    OrthoSegmentPane otherSegment = new NonCurrentSegment(i, hi, al, orthos);
		    add(otherSegment);
		}
	    }
	}
	revalidate();
	repaint();
	showFocusFun();
    }

    // Show function with focus (if any) in panel.
    private void showFocusFun() {
	if (currentSegment != null) {
	    OrthoElem ortho = manipulator.currentFun();
	    if (ortho != null) {
		int[] signs = ortho.signs();
		int[] letters = ortho.letters();
		currentSegment.setHiFocus(signs);
		currentSegment.setFunFocus(ortho);
		currentSegment.setAlFocus(letters);
		if (!currentSegment.getHiFocus().isEmpty()) {
		    String sign = currentSegment.getFirstHiName();
		    if (sign != null)
			querySign(sign);
		}
	    } else 
		currentSegment.removeFocus();
	}
	showOrtho();
    }

    // Current segment.
    private class CurrentSegment extends OrthoSegmentPane {
	public CurrentSegment(int id, String hi, String al, Vector<OrthoElem> orthos) {
	    super(id, hi, al, orthos, true, !splitMode(), hieroContext, parsingContext);
	}
	public void hiClicked(int i) {
	    if (splitMode()) { 
		splitAt(i, getAlFocus());
	    } else if (manipulator.currentFun() != null) {
		saveCurrentFun();
		addHiFocus(i);
		setFunSelection();
	    } else {
		removeFunFocus();
		removeAlFocus();
		if (getHiFocus().contains(i)) 
		    removeHiFocus(i);
		else 
		    addHiFocus(i);
		setFunSelection();
		if (!getHiFocus().isEmpty()) {
		    String sign = getFirstHiName();
		    if (sign != null)
			querySign(sign);
		}
	    }
	}
	public void funClicked(int i) {
	    if (splitMode()) 
		; // ignored
	    else {
		OrthoElem ortho = manipulator.currentFun();
		if (ortho != null) 
		    saveCurrentFun();
		manipulator.setCurrentFun(getFun(i));
		showFocusFun();
	    }
	}
	public void alClicked(int i) {
	    if (splitMode()) {
		removeHiFocus();
		setAlFocus(i);
	    } else {
		OrthoElem ortho = manipulator.currentFun();
		if (ortho != null) {
		    saveFunValue();
		    manipulator.toggleLetterOfFun(i);
		    showFocusFun();
		}
	    }
	}
	public void hiRightClicked(int i) {
	    if (splitMode()) {
		if (getHiFocus().isEmpty())
		    setHiFocus(i);
		else 
		    splitAt(getHiFocus().first(), getAlFocus());
	    } else {
		if (manipulator.currentFun() != null) 
		    saveCurrentFun();
		createResEditor();
	    }
	}
	public void hiMiddleClicked(int i) {
	    // not used for now
	}
	public void alRightClicked(int i) {
	    if (splitMode()) 
		; // ignored
	    else {
		if (manipulator.currentFun() != null)
		    saveCurrentFun();
		createTransEditor();
	    }
	}
	public boolean isConsistent(OrthoElem ortho, Vector<String> hiNames, String al) {
	    return OrthoSegmentsPane.this.isConsistent(ortho, hiNames, al);
	}
    }

    // Other segment.
    private class NonCurrentSegment extends OrthoSegmentPane {
        public NonCurrentSegment(int id, String hi, String al, Vector<OrthoElem> orthos) {
            super(id, hi, al, orthos, false, !splitMode(), hieroContext, parsingContext);
        }   
	public void clicked(int i) {
	    if (splitMode())
		; // ignored
	    else 
		saveCurrentFun();
	    manipulator.setCurrent(i);
	    update();
	}
	public boolean isConsistent(OrthoElem ortho, Vector<String> hiNames, String al) {
	    return OrthoSegmentsPane.this.isConsistent(ortho, hiNames, al);
	}
    }

    //////////////////////////////////
    // Communication to caller.

    // Is editor in split mode?
    public boolean splitMode() {
	// caller overrides
	return false;
    }

    // Show information on sign.
    public void querySign(String sign) {
	// caller overrides
    }

    // Show ortho in tabbed pane.
    public void showOrtho() {
	// caller overrides
    }

    // Enable or not selection of functions.
    public void setFunSelection() {
	// caller overrides
    }

    // Save text element to function.
    // And clear function focus.
    public void saveCurrentFun() {
	// caller extends
	manipulator.setCurrentFun(null);
	showFocusFun();
    }

    // Save value under tab.
    public void saveFunValue() {
	// caller overrides
    }

    // Find out whether annotation is consistent with list list.
    public boolean isConsistent(OrthoElem ortho, Vector<String> hiNames, String al) {
	// caller overrides
	return true;
    }

    // Get confirmation from user.
    public boolean userConfirmation(String message) {
	// called overrides
	return true;
    }

    //////////////////////////////////
    // Communication from caller.

    // Move glyph left.
    public void glyphLeft() {
	if (currentSegment != null &&
		!currentSegment.getHiFocus().isEmpty()) {
	    int leftMost = currentSegment.getHiFocus().first();
	    if (manipulator.currentFun() == null) {
		if (leftMost > 0) {
		    currentSegment.removeHiFocus(leftMost);
		    if (currentSegment.getHiFocus().contains(leftMost-1)) 
			currentSegment.removeHiFocus(leftMost-1);
		    else 
			currentSegment.addHiFocus(leftMost-1);
		    setFunSelection();
		}
	    } else if (leftMost <= 0) {
		if (manipulator.currentSegment() > 0) {
		    saveCurrentFun();
		    manipulator.left();
		    update();
		    if (currentSegment.nHiFocusables() > 0) {
			currentSegment.addHiFocus(currentSegment.nHiFocusables()-1);
			setFunSelection();
		    }
		}
	    } else {
		saveCurrentFun();
		currentSegment.addHiFocus(leftMost - 1);
		setFunSelection();
	    }
	}
    }

    // Move glyph right.
    public void glyphRight() {
	if (currentSegment != null &&
		!currentSegment.getHiFocus().isEmpty()) {
	    int rightMost = currentSegment.getHiFocus().last();
	    if (manipulator.currentFun() == null) {
		if (rightMost < currentSegment.nHiFocusables()-1) {
		    currentSegment.removeHiFocus(rightMost);
		    if (currentSegment.getHiFocus().contains(rightMost+1)) 
			currentSegment.removeHiFocus(rightMost+1);
		    else 
			currentSegment.addHiFocus(rightMost+1);
		    setFunSelection();
		}
	    } else if (rightMost >= currentSegment.nHiFocusables() - 1) {
		if (manipulator.currentSegment() < manipulator.nSegments() - 1) {
		    saveCurrentFun();
		    manipulator.right();
		    update();
		    if (currentSegment.nHiFocusables() > 0) {
			currentSegment.addHiFocus(0);
			setFunSelection();
			querySign();
		    }
		}
	    } else {
		saveCurrentFun();
		currentSegment.addHiFocus(rightMost + 1);
		setFunSelection();
		querySign();
	    }
	} else if (currentSegment != null && 
		    manipulator.currentFun() == null &&
		    currentSegment.nHiFocusables() > 0) {
		currentSegment.addHiFocus(0);
		setFunSelection();
	}
    }

    // Include next glyph in focus.
    public void includeNextGlyph() {
	if (currentSegment != null &&
		    !currentSegment.getHiFocus().isEmpty()) {
	    int rightMost = currentSegment.getHiFocus().last();
	    if (manipulator.currentFun() == null &&
		    rightMost+1 < currentSegment.nHiFocusables()) 
		currentSegment.addHiFocus(rightMost+1);
	} else if (currentSegment != null && currentSegment.nHiFocusables() > 0)
	    currentSegment.addHiFocus(0);
    }

    // RES string of hieroglyphs under focus.
    public String focussedRes() {
	if (currentSegment != null)
	    return currentSegment.getHiRes();
	else
	    return "";
    }
    // RES string of hieroglyphs under focus, plus next i.
    public String focussedResPlusNext(int i) {
	if (currentSegment != null)
	    return currentSegment.getHiResPlusNext(i);
	else
	    return "";
    }

    // Signs under focus.
    public TreeSet<Integer> focussedSigns() {
	if (currentSegment != null)
	    return currentSegment.getHiFocus();
	else
	    return new TreeSet<Integer>();
    }

    // Query glyph (if not in split mode) or null.
    public void querySign() {
	if (!splitMode() && currentSegment != null &&
		!currentSegment.getHiFocus().isEmpty()) {
	    String sign = currentSegment.getFirstHiName();
	    if (sign != null)
		querySign(sign);
	}
    }

    // Add function.
    public void addFun(OrthoElem ortho) {
	manipulator.addFun(ortho);
	update();
    }

    // Remove focussed function.
    public void removeFun() {
	manipulator.removeFun();
	update();
    }

    // Put function in place of focussed one.
    public void replaceFun(OrthoElem ortho) {
	manipulator.replaceFunction(ortho);
	update();
    }

    //////////////////////////////////
    // Appearance.

    public void allowEditing(boolean allow) {
	parent.allowEditing(allow);
	setEnabled(allow);
	Component[] comps = getComponents();
	if (comps != null)
	    for (int i = 0; i < comps.length; i++) {
		if (comps[i] instanceof OrthoSegmentPane) {
		    OrthoSegmentPane pane = (OrthoSegmentPane) comps[i];
		    pane.allowEditing(allow);
		}
	    }
    }

    ////////////////////////////////////////////////
    // Editors.

    /**
     * Open a RES editor for editing hieroglyphic.
     */
    private void createResEditor() {
	allowEditing(false);
        new FragmentEditor(manipulator.hiero(), true, false,
                Settings.embeddedPreviewHieroFontSize,
                Settings.embeddedTreeHieroFontSize) {

            protected void receive(String hi) {
		manipulator.replaceHiero(hi);
                allowEditing(true);
		OrthoSegmentsPane.this.update();
            }

            protected void cancel() {
                allowEditing(true);
            }

            protected void error(int pos) {
            }
        };
    }

    // Open a editor for editing transliteration.
    private void createTransEditor() {
	allowEditing(false);
	AlEditorFrame f = new AlEditorFrame(manipulator.trans()) {
            protected void receive(String al) {
		manipulator.replaceTrans(al);
                allowEditing(true);
		update();
            }

	    protected void cancel() {
                allowEditing(true);
	    }
	};
	f.setParent(parent);
    }

    //////////////////////////////////////////////////////////////////
    // Edits.

    // Split segment in two. 
    // If no focus on transliteration, then split at next word.
    private void splitAt(int hiPos, TreeSet<Integer> alPoss) {
	int alPos = 0;
	if (alPoss.isEmpty())
	    alPos = manipulator.secondWordPos();
	else 
	    alPos = alPoss.first();
	if (manipulator.splitWouldBreak(hiPos, alPos) &&
	       !userConfirmation("Do you want to split across links?"))	
	    return;
	manipulator.splitSegment(hiPos, alPos);
	update();
    }

}

