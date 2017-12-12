/***************************************************************************/
/*                                                                         */
/*  FragmentPanel.java                                                     */
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

// Panel within editor for editing hieroglyphic text.

package nederhof.res.editor;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import nederhof.res.*;

abstract class FragmentPanel extends JPanel {

    // Key listener.
    private KeyListener listener;

    // Listener to closing.
    private WindowAdapter closeListener;

    // Context for parsing.
    private IParsingContext parsingContext = new ParsingContext(true);
    // Contexts for rendering.
    private HieroRenderContext previewContext;
    private HieroRenderContext treeContext;

    // Panel for changing direction and size.
    private DirectionSizePanel directionAndSize;

    // Panel for preview of hieroglyphic.
    private PreviewPanel preview;

    // Tree form of RES.
    private TreeFragment tree;

    public FragmentPanel(ResFragment res, 
	    KeyListener listener, WindowAdapter closeListener,
	    int previewHieroFontSize, int treeHieroFontSize) {
        previewContext = new HieroRenderContext(previewHieroFontSize);
        previewContext.setSuppressErrors(true);
        treeContext = new HieroRenderContext(treeHieroFontSize);
        treeContext.setSuppressErrors(true);
	this.listener = listener;
	this.closeListener = closeListener;
	makePanels(res);
	if (ResValues.isRL(tree.direction))
	    tree.moveFocusEnd();
	else
	    tree.moveFocusStart();
	enableUndoRedo();
	addKeyListener(listener);
    }

    // It is difficult to initialise scrolling to be right-most for 
    // right-to-left text direction. This is to be called after
    // parent frame has been made visible.
    // Similarly, placement of legend is best done after.
    public void initialize(Point legendLocation) {
	preview.scrollToFocus();
	tree.scrollToFocus();
	tree.moveLegend(legendLocation);
    }

    // Move legend by indicated number of pixels.
    public void moveLegend(int xIncr, int yIncr, int xMin, int yMin) {
	tree.moveLegend(xIncr, yIncr, xMin, yMin);
    }

    // Show legend.
    public void showLegend() {
	tree.showLegend();
    }

    // Set new hieroglyphic.
    public void setContents(String text, int focus) {
	ResFragment res = ResFragment.parse(text, parsingContext);
	makePanels(res);
	tree.setFocus(focus);
    }

    public void setPreviewFontSize(int size) {
        previewContext = new HieroRenderContext(size);
        previewContext.setSuppressErrors(true);
	redisplay();
    }
    public void setTreeFontSize(int size) {
        treeContext = new HieroRenderContext(size);
        treeContext.setSuppressErrors(true);
	redisplay();
    }

    // Make panels containing hieroglyphic.
    private void makePanels(ResFragment res) {
        preview =
            new PreviewPanel(res, previewContext) {
                public void transferFocus() {
                    transferPreviewFocus();
                }
            };
        tree =
            new TreeFragment(res, treeContext, listener, closeListener) {
                public void prepareChange() {
                    prepareTreeChange();
                }
                public void finishChange() {
                    finishTreeChange();
                }
                public void transferChange() {
                    transferTreeChange();
                }
                public void transferFocus() {
                    transferTreeFocus();
                }
		public GlyphChooser getGlyphChooserWindow() {
		    FragmentPanel.this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		    GlyphChooser chooser = getChooserWindow();
		    FragmentPanel.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		    return chooser;
		}
            };
        directionAndSize =
            new DirectionSizePanel(tree.direction, tree.size) {
                protected void returnValues(int direction, float size) {
                    changeDirectionSize(direction, size);
                }
            };
        makeLayout();
    }

    // Dispose of auxiliary panels.
    public void dispose() {
	tree.dispose();
    }

    public void setVisible(boolean b) {
	tree.setVisible(b);
    }

    // Receive glyph from glyph chooser.
    public void receiveGlyph(String name) {
	tree.receiveGlyph(name);
    }

    // Application to provide implementation.
    protected abstract GlyphChooser getChooserWindow();
    // Application to provide buttons for undo/redo.
    protected abstract void enableUndo(boolean b);
    protected abstract void enableRedo(boolean b);

    // RES as edited.
    public String contents() {
	return tree.toString();
    }

    ///////////////////////////////////////////////////////////////
    // Generic edits.

    // State, for undoing.
    private static class UndoState {
        // Hieroglyphic.
        public String text;

        // Position before and after edit.
        public int posBefore;
        public int posAfter;

        public UndoState(String text, int pos) {
            this.text = text;
            this.posBefore = pos;
            this.posAfter = pos;
        }

	// For debugging.
	public String toString() {
	    return "" + text + "\n" + posBefore + " " + posAfter;
	}
    }

    // The states until now, used for undoing.
    private Vector states = new Vector();
    // Number of states in present history. May be less than
    // total number of states, in order to allow redo.
    private int historySize = 0;

    // Save present state, provided it is different from present.
    // Return whether state was saved.
    // The argument is the position to be saved.
    private void saveState() {
	states.setSize(historySize);
        String text = tree.toString();
	int pos = tree.getFocus();
	states.add(new UndoState(text, pos));
	historySize++;
	enableUndoRedo();
    }

    // Save position after edit.
    private void savePos() {
	UndoState state = (UndoState) states.get(historySize - 1);
	state.posAfter = tree.getFocus();
    }

    // Undo last change.
    public void undo() {
	if (historySize > 0) {
	    if (states.size() == historySize) {
		String text = tree.toString();
		int pos = tree.getFocus();
		states.add(new UndoState(text, pos));
	    }
	    UndoState past = (UndoState) states.get(historySize-1);
	    Point savedLegendLocation = tree.getLegendLocation();
	    dispose();
	    setContents(past.text, past.posBefore);
	    tree.moveLegend(savedLegendLocation);
	    historySize--;
	    enableUndoRedo();
	}
    }

    // Redo last.
    public void redo() {
	if (historySize < states.size() - 1) {
	    UndoState future = (UndoState) states.get(historySize+1);
	    UndoState beforeFuture = (UndoState) states.get(historySize);
	    Point savedLegendLocation = tree.getLegendLocation();
	    dispose();
	    setContents(future.text, beforeFuture.posAfter);
	    tree.moveLegend(savedLegendLocation);
	    historySize++;
	    enableUndoRedo();
	}
    }

    public void enableUndoRedo() {
        enableUndo(historySize > 0);
        enableRedo(historySize < states.size() - 1);
    }

    // Set blank hieroglyphic, but maintain direction/size.
    public void clear() {
	saveState();
	Point savedLegendLocation = tree.getLegendLocation();
	dispose();
	setContents(tree.argsToString(), 0);
	tree.moveLegend(savedLegendLocation);
	savePos();
    }

    // Set to normalized string.
    public void normalize() {
	saveState();
	Point savedLegendLocation = tree.getLegendLocation();
	dispose();
	ResComposer normalizer = new ResComposer(parsingContext,
		true, true, false, false, false, false);
	ResFragment normalized = normalizer.normalize(tree);
	makePanels(normalized);
	if (ResValues.isRL(tree.direction))
	    tree.moveFocusEnd();
	else
	    tree.moveFocusStart();
	tree.moveLegend(savedLegendLocation);
	savePos();
    }

    // Flatten. Each glyph becomes individual group.
    public void flatten() {
	saveState();
	Point savedLegendLocation = tree.getLegendLocation();
	dispose();
	ResComposer composer = new ResComposer(parsingContext,
		false, false, false, false, false, false);
	Vector<String> glyphNames = tree.glyphNames();
	ResFragment flattened = composer.composeNames(glyphNames);
	makePanels(flattened);
	if (ResValues.isRL(tree.direction))
	    tree.moveFocusEnd();
	else
	    tree.moveFocusStart();
	tree.moveLegend(savedLegendLocation);
	savePos();
    }

    // Swap current named glyph and next.
    public void swap() {
	saveState();
	int pos = tree.getFocus();
	Point savedLegendLocation = tree.getLegendLocation();
	dispose();
	tree.swap();
	makePanels(tree);
	tree.setFocus(pos);
	tree.moveLegend(savedLegendLocation);
	savePos();
    }

    // Have there been changes?
    public boolean modified() {
	return historySize > 0;
    }

    // Display again, after change of font size.
    public void redisplay() {
	String text = tree.toString();
	int pos = tree.getFocus();
	Point savedLegendLocation = tree.getLegendLocation();
	dispose();
	setContents(text, pos);
	tree.moveLegend(savedLegendLocation);
    }

    private void changeDirectionSize(int direction, float size) {
	boolean dirChanged = direction != tree.direction;
	if (dirChanged || !equal(size, tree.size)) 
	    tree.changeDirectionSize(direction, size);
	if (dirChanged) {
	    makeLayout();
	    preview.scrollToFocus();
	    tree.scrollToFocus();
	}
    }
    // Extended notion of equality of floats.
    private boolean equal(float size1, float size2) {
	return (Float.isNaN(size1) && Float.isNaN(size2)) || size1 == size2;
    }

    // Has hieroglyphic been changed?
    public boolean beenChanged() {
	return historySize > 0;
    }

    // In advance of edit, save state.
    public void prepareTreeChange() {
	saveState();
    }
    // After edit, save position of focus.
    public void finishTreeChange() {
	savePos();
    }


    // For debugging purposes, print history.
    public void printHistory() {
	System.out.println("historySize " + historySize);
	for (int i = 0; i < states.size(); i++)
	    System.out.println(states.get(i));
    }

    // Changes in tree to be processed in preview.
    public void transferTreeChange() {
	preview.setHiero(tree);
    }

    // Changes in focus to be processed.
    public void transferTreeFocus() {
	int groupIndex = tree.getFocusGroup();
	preview.setFocus(groupIndex);
    }

    // Changes in focus to be processed.
    public void transferPreviewFocus() {
	tree.setFocusGroup(preview.getFocus());
    }

    ///////////////////////////////////////////////////////////////
    // Layout.

    // Depending on direction and size, make layout of hiero panel.
    public void makeLayout() {
	setCursor(new Cursor(Cursor.WAIT_CURSOR));
	removeAll();
	int dir = tree.direction;
	if (!ResValues.isV(dir)) {
	    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	    JPanel topPanel = new ShortPanel();
	    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
	    add(topPanel);

	    JScrollPane previewScroll = new JScrollPane(preview,
		    JScrollPane.VERTICAL_SCROLLBAR_NEVER,
		    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	    previewScroll.getVerticalScrollBar().setUnitIncrement(10);
	    if (!ResValues.isRL(dir)) {
		topPanel.add(directionAndSize);
		topPanel.add(previewScroll);
	    } else {
		topPanel.add(previewScroll);
		topPanel.add(directionAndSize);
	    }
	    JScrollPane treeScroll = new JScrollPane(tree.rootPanel());
	    treeScroll.getVerticalScrollBar().setUnitIncrement(10);
	    add(treeScroll);
	} else {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            JPanel leftPanel = new NarrowPanel();
            leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

            JScrollPane previewScroll = new JScrollPane(preview,
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            previewScroll.getVerticalScrollBar().setUnitIncrement(10);

            leftPanel.add(directionAndSize);
            leftPanel.add(previewScroll);

            JScrollPane treeScroll = new JScrollPane(tree.rootPanel());
            treeScroll.getVerticalScrollBar().setUnitIncrement(10);
            if (dir == ResValues.DIR_VLR) {
                add(leftPanel);
                add(treeScroll);
            } else {
                add(treeScroll);
                add(leftPanel);
            }
	}

        validate();
        repaint();
        grabFocus();
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    // Panel that is not higher than preferable.
    private class ShortPanel extends JPanel {
        public Dimension getMaximumSize() {
            return new Dimension(super.getMaximumSize().width,
                    getPreferredSize().height);
        }
    }

    // Panel that is not wider than preferable.
    private class NarrowPanel extends JPanel {
        public Dimension getMaximumSize() {
            return new Dimension(getPreferredSize().width,
                    super.getMaximumSize().height);
        }
    }

    ///////////////////////////////////////////////////////////////
    // Processing keyboard input.

    // The keys are passed on to the fragment panel.
    // (No arrow keys, and no alt or cntr.)
    public void keyTyped(KeyEvent e) {
	char c = e.getKeyChar();
	final int shiftMask = InputEvent.SHIFT_DOWN_MASK;
	if (e.getModifiersEx() == 0 ||
		(e.getModifiersEx() & shiftMask) == shiftMask)
	    tree.processCommand(c);
    }

    // The arrows for moving focus.
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        switch (code) {
            case KeyEvent.VK_DOWN:
                tree.moveFocusDown();
                break;
            case KeyEvent.VK_KP_DOWN:
                tree.moveFocusDown();
                break;
            case KeyEvent.VK_LEFT:
                tree.moveFocusLeft();
                break;
            case KeyEvent.VK_KP_LEFT:
                tree.moveFocusLeft();
                break;
            case KeyEvent.VK_RIGHT:
                tree.moveFocusRight();
                break;
            case KeyEvent.VK_KP_RIGHT:
                tree.moveFocusRight();
                break;
            case KeyEvent.VK_UP:
                tree.moveFocusUp();
                break;
            case KeyEvent.VK_KP_UP:
                tree.moveFocusUp();
                break;
            case KeyEvent.VK_HOME:
                tree.moveFocusStart();
                break;
            case KeyEvent.VK_END:
                tree.moveFocusEnd();
                break;
        }
    }

    // Ignored.
    public void keyReleased(KeyEvent e) {
        // ignored
    }

}
