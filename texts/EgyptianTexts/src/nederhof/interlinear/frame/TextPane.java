/***************************************************************************/
/*                                                                         */
/*  TextPane.java                                                          */
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

// Part containing interlinear text.
// The main part is the interlinear representation.
// An optional part is for editing a phrase.

package nederhof.interlinear.frame;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import nederhof.alignment.*;
import nederhof.corpus.*;
import nederhof.interlinear.*;
import nederhof.interlinear.labels.*;
import nederhof.util.*;

abstract class TextPane extends JPanel 
	implements EditChainElement {

    // Directory of corpus.
    private String corpusDirectory;

    // Render parameters.
    private RenderParameters renderParameters;

    // In edit mode?
    private boolean edit = false;

    // The formatted text.
    private FormattedPane formattedPane;

    // The scroll pane containing formatted text.
    private JScrollPane scroll;

    // Pane in which to edit phrase.
    private PhraseEditor phraseEditor;

    // Frame in which to edit resource.
    private ResourceEditor resourceEditor;

    // Create pane.
    public TextPane(String corpusDirectory,
	    Text text, Autoaligner aligner, RenderParameters renderParameters) {
	this.corpusDirectory = corpusDirectory;
	this.renderParameters = renderParameters;
	formattedPane = new FormattedPane(corpusDirectory,
		    text, aligner, renderParameters, this) {
	    public void editPhrase(PhraseEditor phraseEditor) {
		makeTextAndEditor(phraseEditor);
	    }
	    public void editResource(ResourceEditor resourceEditor) {
		makeResourceEditor(resourceEditor);
	    }
	    public void focusExternalViewer(TextResource resource, int pos) {
		TextPane.this.focusExternalViewer(resource, pos);
	    }
	};
	scroll = new JScrollPane(formattedPane,
		JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	scroll.getVerticalScrollBar().setUnitIncrement(10);

	renderParameters.setListener(formattedPane);
	makeText();
	formattedPane.setResources(new Vector(), new Vector(), new Vector(), false);
    }

    // Make text consisting of interlinear representation.
    public void makeText() {
	this.phraseEditor = null;
	this.resourceEditor = null;
	removeAll();
	add(scroll);
	validate();
    }

    // Make text and phrase editor.
    public void makeTextAndEditor(PhraseEditor phraseEditor) {
	this.phraseEditor = phraseEditor;
	this.resourceEditor = null;
	removeAll();
	JSplitPane split =
	    new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	split.setOneTouchExpandable(true);
	split.setDividerSize((int) (split.getDividerSize() * 1.3));
	split.setResizeWeight(0.5);
	split.setDividerLocation(0.2);
	JScrollPane scrollLeft = scroll;
	JScrollPane scrollRight = new ScrollConservative(phraseEditor);
	split.setLeftComponent(scrollLeft);
	split.setRightComponent(scrollRight);
	add(split);
	validate();
	setResourceOpen(true);
	setExternEdit(false);
	phraseEditor.refreshLayout();
	int height = split.getHeight() 
	    - split.getDividerSize()
	    - phraseEditor.getPreferredSize().height;
	split.setDividerLocation(height);
    }

    // Make external editor.
    public void makeResourceEditor(ResourceEditor resourceEditor) {
	this.phraseEditor = null;
	this.resourceEditor = resourceEditor;
	setResourceOpen(true);
	setExternEdit(false);
	resourceEditor.setParent(this);
	resourceEditor.setVisible(true);
    }

    // Called by editor of phrase.
    public void returnFromEdit() {
        makeText();
        setResourceOpen(false);
        setExternEdit(false);
	formattedPane.restoreAfterAppend();
    }

    ///////////////////////////////////////
    // Interaction between panels.

    // If editing not allowed, because of external edit,
    // notify parent.
    public void allowEditing(boolean b) {
	setExternEdit(!b);
    }

    // Enable or disable buttons.
    public void enableDisable(boolean changed, boolean externEdit) {
	formattedPane.setEnabled(!externEdit);
	if (phraseEditor != null)
	    phraseEditor.setEnabled(!externEdit);
    }

    // Action from menu.
    // Return whether resource has changed and the text in the window
    // needs to be reset.
    public boolean actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("resource close")) {
	    if (phraseEditor != null) {
		boolean changed = phraseEditor.save();
		if (changed)
		    makeResourceChanged();
		returnFromEdit();
	    } else
		closeResourceEditor();
	    return false;
	} else 
	    return formattedPane.actionPerformed(e);
    }

    // Can be called from resource editor itself.
    public void closeResourceEditor() {
	if (resourceEditor != null) {
	    boolean closed = resourceEditor.trySaveQuit();
	    boolean changed = resourceEditor.anyChange();
	    if (!closed)
		return;
	    resourceEditor.dispose();
	    if (changed)
		makeResourceChanged();
	    returnFromEdit();
	}
    }

    // Make precedence.
    public Vector makePrecedence(int type) {
	return formattedPane.makePrecedence(type);
    }

    // Insert auto align, or remove auto align. 
    public Vector autoalign(boolean b) {
	return formattedPane.autoalign(b);
    }

    // Open window of editing settings.
    public void editSettings() {
	renderParameters.setListener(formattedPane);
	renderParameters.edit();
    }

    // Reset resources.
    public void reset(Vector<TextResource> resources, 
	    Vector<ResourcePrecedence> precedences, Vector<Object[]> autoaligns, 
	    boolean edit) {
	formattedPane.setResources(resources, precedences, autoaligns, edit);
	refreshExternalViewers(resources);
	requestFocus();
    }

    //////////////////////////////////////////
    // External viewers.

    // List of all open viewers.
    private Vector<ResourceViewer> externalViewers = new Vector<ResourceViewer>();

    // Refresh viewers after edits.
    // Dispose of those that are no longer needed.
    private void refreshExternalViewers(Vector<TextResource> resources) {
	Vector<ResourceViewer> currentViewers = new Vector<ResourceViewer>();
	for (TextResource resource : resources) 
	    if (resource.hasViewer()) {
		ResourceViewer viewer = viewerIn(resource, externalViewers);
		if (viewer == null) {
		    viewer = resource.getViewer();
		    viewer.setListener(this);
		} else
		    viewer.refresh();
		currentViewers.add(viewer);
	    } 
	for (ResourceViewer viewer : externalViewers) 
	    if (viewerIn(viewer.getResource(), currentViewers) == null)
		viewer.dispose();
	externalViewers = currentViewers;
    }

    // Is there viewer in list for resource? If so, then return it.
    private ResourceViewer viewerIn(TextResource resource, Vector<ResourceViewer> viewers) {
	for (ResourceViewer viewer : viewers) 
	    if (viewer.getResource() == resource)
		return viewer;
	return null;
    }

    public void focusExternalViewer(TextResource resource, int pos) {
	for (ResourceViewer viewer : externalViewers) {
	    if (viewer.getResource() == resource) {
		viewer.setFocus(pos);
		return;
	    }
	}
    }

    // Scroll to position of resource.
    public void scrollTo(TextResource resource, int pos) {
	formattedPane.scrollTo(resource, pos);
    }

    // Dispose of external viewers.
    public void dispose() {
	for (ResourceViewer viewer : externalViewers)
	    viewer.dispose();
    }

    //////////////////////////////////////
    // Information back to caller.

    // Inform of change.
    protected abstract void makeResourceChanged();

    // Inform of external edit.
    protected abstract void setExternEdit(boolean b);

    // Inform of open resource editing.
    protected abstract void setResourceOpen(boolean b);

}
