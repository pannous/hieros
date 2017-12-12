/***************************************************************************/
/*                                                                         */
/*  EditActionHelper.java                                                  */
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

// Helper for clicks and keyboard events.

package nederhof.interlinear.frame;

import java.io.*;
import java.util.*;

import nederhof.corpus.*;
import nederhof.interlinear.*;
import nederhof.interlinear.labels.*;
import nederhof.util.*;

abstract class EditActionHelper {

    // Directory of corpus.
    private String corpusDirectory;

    // Parent of it all.
    private EditChainElement parent;

    // Construct.
    public EditActionHelper(String corpusDirectory, EditChainElement parent) {
	this.corpusDirectory = corpusDirectory;
	this.parent = parent;
    }

    // Tiers.
    private Vector<Tier> tiers;
    // Numbers of tiers within resources.
    private Vector<Integer> tierNums;
    // Vector of vectors of positions where phrases start.
    private Vector<Vector<Integer>> phraseStarts;
    // Resource for each tier.
    public Vector<TextResource> tierResources;

    // Is more than one resource shown?
    private boolean severalResourcesShown;

    // Combinations of resource, tier, tier number and position in tier.
    private Vector<Click> clicked = new Vector<Click>();

    // After append, the first clear goes back to the end of the affected
    // resource.
    private TextResource afterAppend = null;

    // Position at middle of screen. Null of none.
    private TierPos middlePos = null;

    // Clear all.
    public void clear(Vector<Tier> tiers,
	    Vector<Integer> tierNums, Vector<Vector<Integer>> phraseStarts, 
	    Vector<TextResource> tierResources,
	    boolean severalResourcesShown) {
	this.tiers = tiers;
	this.tierNums = tierNums;
	this.phraseStarts = phraseStarts;
	this.tierResources = tierResources;
	this.severalResourcesShown = severalResourcesShown;
	clear();
    }
    // Clear all, keeping phrase starts.
    public void clear() {
	for (int i = clicked.size() - 1; i >= 0; i--) {
	    Click click = clicked.get(i);
	    TextResource oldResource = click.resource;
	    Tier oldTier = click.tier;
	    int oldNum = click.num;
	    int oldPos = click.pos;
	    undoClick(oldResource, oldTier, oldNum, oldPos);
	}
	clicked.clear();
	middlePos = null;
    }

    // Restore clicks after append.
    public void restoreAfterAppend() {
	if (afterAppend != null) {
	    for (int i = 0; i < tiers.size(); i++) {
		TextResource resource = tierResources.get(i);
		if (resource == afterAppend) { 
		    Tier tier = tiers.get(i);
		    Integer tierNum = tierNums.get(i);
		    if (tier.nSymbols() > 0)
			recordClick(resource, tier, tierNum.intValue(), tier.nSymbols() - 1);
		}
	    }
	    afterAppend = null;
	}
    }

    // Record mouse click on position.
    public void recordClick(TierPos pos, Vector<Integer> tierNums, 
	    Vector<TextResource> tierResources) {
	Tier tier = pos.tier;
	if (tier.id() < tierNums.size()) {
	    Integer tierNum = tierNums.get(tier.id());
	    TextResource resource = tierResources.get(tier.id());
	    recordClick(resource, tier, tierNum.intValue(), pos.pos);
	}
    }

    // Record mouse click. Undo clicks on same tier.
    // Toggle the type, in case the same position was clicked
    // before.
    private void recordClick(TextResource resource, Tier tier, int num, int pos) {
	String type = START;
	for (int i = clicked.size() - 1; i >= 0; i--) {
	    Click click = clicked.get(i);
	    TextResource oldResource = click.resource;
	    Tier oldTier = click.tier;
	    int oldNum = click.num;
	    int oldPos = click.pos;
	    String oldType = click.type;
	    if (oldTier == tier) {
		clicked.removeElementAt(i);
		undoClick(oldResource, oldTier, oldNum, oldPos);
		if (oldNum == num && oldPos == pos) 
		    type = (oldType.equals(AFTER) ? START : AFTER);
	    } 
	}
	clicked.add(new Click(resource, tier, num, pos, type));
	doClick(resource, tier, num, pos, type);
    }

    // Put highlight in window.
    private void doClick(TextResource resource, Tier tier, int num, 
	    int pos, String type) {
	if (type.equals(START))
	    tier.highlight(pos);
	else
	    tier.highlightAfter(pos);
	refresh();
    }

    // Remove highlight from window.
    private void undoClick(TextResource resource, Tier tier, int num, int pos) {
	tier.unhighlight(pos);
	tier.unhighlightAfter(pos);
	refresh();
    }

    // Some clicked positions can be given special meaning,
    // signifying that 'after' position is meant instead of
    // default start-of-position.
    private static final String START = "start";
    private static final String AFTER = "after";

    // For clicked position: resource, tier, tier number, position within
    // tier.
    private class Click {
	public TextResource resource;
	public Tier tier;
	public int num;
	public int pos;
	public String type;

	public Click(TextResource resource, Tier tier, int num, int pos,
		String type) {
	    this.resource = resource;
	    this.tier = tier;
	    this.num = num;
	    this.pos = pos; 
	    this.type = type;
	}
    }

    // Store middle of screen position.
    public void storeMiddlePos(TierPos middlePos) {
	this.middlePos = middlePos;
    }
    // Retrieve middle of screen position.
    public TierPos retrieveMiddlePos() {
	return middlePos;
    }

    ///////////////////////////
    // Analysis.

    // Get phrase number of position.
    private int phraseNum(int tierNum, int pos) {
	Vector<Integer> phraseStart = phraseStarts.get(tierNum);
	return binarySearch(phraseStart, pos);
    }

    // Get position within phrase.
    // Subtract one, because of the separator at beginning of phrases.
    private int phrasePos(int tierNum, int phraseNum, int pos) {
	Vector<Integer> phraseStart = phraseStarts.get(tierNum);
	int start = (phraseStart.get(phraseNum)).intValue();
	return pos - start - 1;
    }

    // Does resource for precedences exist?
    // Return 0 if no, 1 if yes, and -1 if yes but in reverse
    // order.
    private int includesPrecedence(Vector precedences,
	    TextResource resource1, TextResource resource2) {
	for (int i = 0; i < precedences.size(); i++) {
	    ResourcePrecedence prec = (ResourcePrecedence) precedences.get(i);
	    TextResource res1 = prec.getResource1();
	    TextResource res2 = prec.getResource2();
	    if (res1 == resource1 && res2 == resource2)
		return 1;
	    else if (res1 == resource2 && res2 == resource1)
		return -1;
	}
	return 0;
    }

    // Get precedence resource. Return null if none.
    private ResourcePrecedence getPrecedence(Vector precedences,
	    TextResource resource1, TextResource resource2) {
	for (int i = 0; i < precedences.size(); i++) {
	    ResourcePrecedence prec = (ResourcePrecedence) precedences.get(i);
	    TextResource res1 = prec.getResource1();
	    TextResource res2 = prec.getResource2();
	    if (res1 == resource1 && res2 == resource2 ||
		    res1 == resource2 && res2 == resource1)
		return prec;
	}
	return null;
    }

    // Create precedence resource. Name is derived from original file names.
    // Look for number that make it non-existing file.
    private ResourcePrecedence createPrecedence(
	    TextResource resource1, TextResource resource2) throws IOException {
	File dir = alignmentDir();
	File path1 = new File(resource1.getLocation());
	String fileName1 = FileAux.removeExtension(path1.getName());
	File path2 = new File(resource2.getLocation());
	String fileName2 = FileAux.removeExtension(path2.getName());
	String prefix = StringAux.longestCommonPrefix(fileName1, fileName2);
	if (prefix.equals(""))
	    prefix = "prec";
	int suffix = 0;
	File precFile = new File(dir, prefix + ".xml");
	try {
	    while (precFile.exists()) {
		precFile = new File(dir, prefix + suffix + ".xml");
		suffix++;
	    }
	    ResourcePrecedence prec = 
		ResourcePrecedence.make(precFile);
	    prec.setResources(resource1, resource2);
	    return prec;
	} catch (SecurityException e) {
	    throw new IOException(e.getMessage());
	}
    }

    // Ensure directory of alignments exists. If not, then create.
    private File alignmentDir() throws IOException {
	File dir;
	try {
	    dir = corpusDirectory == null ? 
		new File(Settings.defaultAlignDir) :
		    new File(corpusDirectory, Settings.defaultAlignDir);
	    if (!dir.exists())
		dir.mkdir();
	} catch (SecurityException e) {
	    throw new IOException(e.getMessage());
	}
	return dir;
    }

    // Do existing auto alignment include this one?
    private boolean includesAuto(Vector autoaligns, 
		    TextResource resource1, String name1,
		    TextResource resource2, String name2) {
	for (int i = 0; i < autoaligns.size(); i++) {
	    Object[] align = (Object[]) autoaligns.get(i);
	    TextResource res1 = (TextResource) align[0];
	    String n1 = (String) align[1];
	    TextResource res2 = (TextResource) align[2];
	    String n2 = (String) align[3];
	    if (res1 == resource1 && n1.equals(name1) &&
		    res2 == resource2 && n2.equals(name2) ||
		    res1 == resource2 && n1.equals(name2) &&
		    res2 == resource1 && n2.equals(name1))
		return true;
	}
	return false;
    }

    // Add auto alignment to text.
    private Vector addAuto(Text text, 
	    String res1, String name1, String res2, String name2) {
	Vector autos = (Vector) text.getAutoaligns().clone();
	autos.add(new String[] {res1, name1, res2, name2});
	return autos;
    }

    // Remove auto alignment from text.
    private Vector removeAuto(Text text,
	    String res1, String name1, String res2, String name2) {
	Vector autos = (Vector) text.getAutoaligns().clone();
	for (int i = autos.size()-1; i >= 0; i--) {
	    String[] align = (String[]) autos.get(i);
	    String r1 = align[0];
	    String n1 = align[1];
	    String r2 = align[2];
	    String n2 = align[3];
	    if (r1.equals(res1) && n1.equals(name1) &&
		    r2.equals(res2) && n2.equals(name2) ||
		    r1.equals(res2) && n1.equals(name2) &&
		    r2.equals(res1) && n2.equals(name1))
		autos.remove(i);
	}
	return autos;
    }

    ///////////////////////////
    // Edits.

    // Edit operations leave error messages here, to be picked up by
    // application.
    public String errorMessage;

    // Move last click left.
    public void left() {
	errorMessage = null;
	if (clicked.size() == 0)
	    errorMessage = "First select symbols.";
	else 
	    moveClick(-1);
    }
    // Move last click right.
    public void right() {
	errorMessage = null;
	if (clicked.size() == 0)
	    errorMessage = "First select symbols.";
	else 
	    moveClick(1);
    }
    // If move to beginning of phrase, then move further.
    private void moveClick(int move) {
	Click last = clicked.get(clicked.size()-1);
	TextResource resource = last.resource;
	Tier tier = last.tier;
	int num = last.num;
	int pos = last.pos;
	int id = tier.id();
	if (0 <= pos + move && pos + move < tier.nSymbols()) {
	    recordClick(resource, tier, num, pos + move);
	}
    }

    // Take phrase of last click, and prepend.
    public void prependPhrase() {
	errorMessage = null;
	if (clicked.size() == 0)
	    errorMessage = "First select phrase following new phrase.";
	else {
	    Click last = clicked.get(clicked.size()-1);
	    TextResource resource = tierResources.get(last.tier.id());
	    TextPhrase empty = resource.emptyPhrase();
	    if (resource.nPhrases() == 0)
		makePhraseEditor(resource, empty, -1, 0);
	    else {
		int phraseNum = phraseNum(last.tier.id(), last.pos); 
		makePhraseEditor(resource, empty, -1, phraseNum);
	    }
	}
    }

    // Take phrase of last click, and edit.
    public void editPhrase() {
	errorMessage = null;
	if (clicked.size() == 0)
	    errorMessage = "First select a phrase to be edited.";
	else {
	    Click last = clicked.get(clicked.size()-1);
	    TextResource resource = tierResources.get(last.tier.id());
	    if (resource.nPhrases() == 0) {
		TextPhrase empty = resource.emptyPhrase();
		makePhraseEditor(resource, empty, -1, 0);
	    } else {
		int phraseNum = phraseNum(last.tier.id(), last.pos); 
		TextPhrase phrase = resource.getPhrase(phraseNum);
		makePhraseEditor(resource, phrase, phraseNum, phraseNum);
	    }
	}
    }

    // Take phrase of last click, and append. 
    // If there is only one resource, append at end.
    public void appendPhrase() {
	errorMessage = null;
	if (clicked.size() == 0) {
	    if (severalResourcesShown || tierResources.size() == 0)
		errorMessage = "First select phrase preceding new phrase.";
	    else {
		TextResource resource = tierResources.get(0);
		TextPhrase empty = resource.emptyPhrase();
		if (resource.nPhrases() == 0)
		    makePhraseEditor(resource, empty, -1, 0);
		else {
		    int phraseNum = resource.nPhrases() - 1;
		    makePhraseEditor(resource, empty, -1, phraseNum + 1);
		}
	    }
	} else {
	    Click last = clicked.get(clicked.size()-1);
	    TextResource resource = tierResources.get(last.tier.id());
	    TextPhrase empty = resource.emptyPhrase();
	    if (resource.nPhrases() == 0)
		makePhraseEditor(resource, empty, -1, 0);
	    else {
		int phraseNum = phraseNum(last.tier.id(), last.pos);
		makePhraseEditor(resource, empty, -1, phraseNum + 1);
	    }
	    afterAppend = resource;
	}
    }

    private void makePhraseEditor(TextResource resource, 
	    TextPhrase phrase,
	    int oldPhraseNum, int beforePhraseNum) {
	Vector editors = resource.getEditors(phrase);
	ResourceEditor resourceEditor = resource.getEditor(beforePhraseNum);
	if (editors != null) {
	    PhraseEditor editor = new PhraseEditor(resource,
		    oldPhraseNum, beforePhraseNum, editors, parent);
	    editPhrase(editor);
	} else if (resourceEditor != null) {
	    editResource(resourceEditor);
	}
    }

    // Take phrase of last click, and join with next phrase.
    public void joinPhrases() {
	errorMessage = null;
	if (clicked.size() == 0)
	    errorMessage = "Select first of pair of phrases to be joined.";
	else {
	    Click last = clicked.get(clicked.size()-1);
	    TextResource resource = tierResources.get(last.tier.id());
	    int phraseNum = phraseNum(last.tier.id(), last.pos); 
	    if (phraseNum < resource.nPhrases()-1) {
		TextPhrase phrase1 = resource.getPhrase(phraseNum);
		TextPhrase phrase2 = resource.getPhrase(phraseNum + 1);
		TextPhrase combined = resource.joinPhrases(phrase1, phrase2);
		if (combined != null) {
		    resource.removePhrase(phraseNum + 1);
		    resource.removePhrase(phraseNum);
		    resource.insertPhrase(combined, phraseNum);
		    try {
			resource.makeModified();
			resource.save();
		    } catch (IOException e) {
			errorMessage = e.getMessage();
			return;
		    }
		}
	    } else
		errorMessage = "Select first of pair of phrases to be joined.";
	}
    }

    // Cut phrase into two.
    // Choose phrase on basis of last click. Then take other positions
    // within phrase from other clicks. Ignore other clicks.
    public void cutPhrase() {
	errorMessage = null;
	if (clicked.size() == 0)
	    errorMessage = "Select symbol for each tier in phrase.";
	else {
	    Click last = clicked.get(clicked.size()-1);
	    TextResource resource = tierResources.get(last.tier.id());
	    if (resource.nPhrases() < 1) {
		errorMessage = "Select symbol for each tier in phrase.";
		return;
	    }
	    int phraseNum = phraseNum(last.tier.id(), last.pos);
	    TextPhrase phrase = resource.getPhrase(phraseNum);
	    int[] cutPositions = new int[phrase.nTiers()];
	    for (int i = 0; i < phrase.nTiers(); i++)
		cutPositions[i] = Integer.MAX_VALUE;
	    for (int i = 0; i < clicked.size(); i++) {
		Click click = clicked.get(i);
		int id = click.tier.id();
		TextResource res = tierResources.get(id);
		int num = phraseNum(id, click.pos);
		if (res == resource && num == phraseNum) {
		    int tierNum = tierNums.get(id).intValue();
		    int tierPos = phrasePos(id, num, click.pos); 
		    if (tierPos >= 0)
			cutPositions[tierNum] = tierPos;
		}
	    }
	    TextPhrase left = new TextPhrase(resource);
	    TextPhrase right = new TextPhrase(resource);
	    for (int i = 0; i < phrase.nTiers(); i++) 
		if (cutPositions[i] == 0)
		    right.getTier(i).addAll(phrase.getTier(i));
		else if (cutPositions[i] > 0)
		    resource.cutPhrase(i, phrase.getTier(i), cutPositions[i],
			    left.getTier(i), right.getTier(i));
	    if (left.tiersEmpty() || right.tiersEmpty())
		errorMessage = "Cannot cut phrase in this way.";
	    else {
		resource.removePhrase(phraseNum);
		resource.insertPhrase(right, phraseNum);
		resource.insertPhrase(left, phraseNum);
		try {
		    resource.makeModified();
		    resource.save();
		} catch (IOException e) {
		    errorMessage = e.getMessage();
		}
	    }
	}
    }

    // Make precedence between two positions, one-way or two-way, or remove
    // any.
    // Return null, or a new list of precedences if it has changed.
    public Vector makePrecedence(int type, Vector precedences) {
	errorMessage = null;
	if (clicked.size() < 2) {
	    errorMessage = "Select two positions in two distinct tiers";
	    return null;
	} else {
	    Click click1 = clicked.get(clicked.size()-2);
	    Click click2 = clicked.get(clicked.size()-1);
	    int id1 = click1.tier.id();
	    int id2 = click2.tier.id();
	    TextResource resource1 = tierResources.get(id1);
	    TextResource resource2 = tierResources.get(id2);
	    int tierNum1 = (tierNums.get(id1)).intValue();
	    int tierNum2 = (tierNums.get(id2)).intValue();
	    int phraseNum1 = phraseNum(id1, click1.pos);
	    int phraseNum2 = phraseNum(id2, click2.pos);
	    int tierPos1 = phrasePos(id1, phraseNum1, click1.pos); 
	    int tierPos2 = phrasePos(id2, phraseNum2, click2.pos); 
	    String posType1 = type == 1 ? click1.type : START;
	    String posType2 = START;
	    if (type != 0 && (tierPos1 < 0 || tierPos2 < 0)) {
		errorMessage = "Select two positions in two distinct tiers";
		return null;
	    }

	    if (resource1 == resource2) {
		if (!resource1.isEditable()) {
		    errorMessage = "Resource is not editable";
		    return null;
		}
		String posName1 = resource1.positionId(phraseNum1, tierNum1, tierPos1, type != 0);
		String posName2 = resource2.positionId(phraseNum2, tierNum2, tierPos2, type != 0);
		if (posName1 == null || posName2 == null) {
		    if (type != 0) {
			errorMessage = "Cannot place positions";
			return null;
		    }
		} else {
		    manipulatePrecedence(type, resource1, 
			    posName1, posType1, posName2, posType2);
		    try {
			resource1.save();
		    } catch (IOException e) {
			errorMessage = e.getMessage();
			return null;
		    }
		}
	    } else {
		WrappedBool changed1 = new WrappedBool(false);
		WrappedBool changed2 = new WrappedBool(false);
		TreeSet<LabelOffset> positions1 = resource1.positionIdOffset(
			phraseNum1, tierNum1, tierPos1, 
			type != 0 && resource1.isEditable(), type == 0, changed1);
		TreeSet<LabelOffset> positions2 = resource2.positionIdOffset(
			phraseNum2, tierNum2, tierPos2, 
			type != 0 && resource2.isEditable(), type == 0, changed2);
		ResourcePrecedence precedence;
		int present = includesPrecedence(precedences, resource1, resource2);
		if (present != 0)
		    precedence = getPrecedence(precedences, resource1, resource2);
		else 
		    try {
			precedence = createPrecedence(resource1, resource2);
			precedences.add(precedence);
		    } catch (IOException e) {
			errorMessage = e.getMessage();
			return null;
		    }
		if (type == 0) 
		    removeResourcePrecedence(present, precedence,
			    positions1, positions2);
		else {
		    LabelOffset labelOffset1 = positions1.first();
		    LabelOffset labelOffset2 = positions2.first();
		    manipulateResourcePrecedence(type, present, precedence, 
			    labelOffset1, posType1, labelOffset2, posType2);
		} 
		try {
		    if (changed1.get()) {
			resource1.makeModified();
			resource1.save();
		    }
		    if (changed2.get()) {
			resource2.makeModified();
			resource2.save();
		    }
		    precedence.save();
		} catch (IOException e) {
		    errorMessage = e.getMessage();
		    return null;
		}
	    }
	}
	return precedences;
    }

    // Manipulate precedences in one resource.
    private void manipulatePrecedence(int type, TextResource resource, 
	    String posName1, String posType1, 
	    String posName2, String posType2) {
	switch (type) {
	    case 0:
		resource.removePrecedence(posName1, posName2);
		resource.removePrecedence(posName2, posName1);
		break;
	    case 1:
		resource.addPrecedence(posName1, posType1, posName2, posType2);
		break;
	    case 2:
		resource.addPrecedence(posName1, posType1, posName2, posType2);
		resource.addPrecedence(posName2, posType2, posName1, posType1);
		break;
	}
    }

    // Manipulate precedences between two resources.
    // Present is 1 if order is as in resourceprecendence, or -1
    // if in reverse order.
    private void manipulateResourcePrecedence(int type, int present, 
	    ResourcePrecedence precedence,
	    LabelOffset labelOffset1, String posType1,
	    LabelOffset labelOffset2, String posType2) {
	switch (type) {
	    case 1:
		if (present == 1)
		    precedence.addForward(
			    labelOffset1.label, labelOffset1.offset, posType1,
			    labelOffset2.label, labelOffset2.offset, posType2);
		else
		    precedence.addBackward(
			    labelOffset1.label, labelOffset1.offset, posType1,
			    labelOffset2.label, labelOffset2.offset, posType2);
		break;
	    case 2:
		if (present == 1) {
		    precedence.addForward(
			    labelOffset1.label, labelOffset1.offset, posType1,
			    labelOffset2.label, labelOffset2.offset, posType2);
		    precedence.addBackward(
			    labelOffset2.label, labelOffset2.offset, posType2,
			    labelOffset1.label, labelOffset1.offset, posType1);
		} else {
		    precedence.addBackward(
			    labelOffset1.label, labelOffset1.offset, posType1,
			    labelOffset2.label, labelOffset2.offset, posType2);
		    precedence.addForward(
			    labelOffset2.label, labelOffset2.offset, posType2,
			    labelOffset1.label, labelOffset1.offset, posType1);
		}
		break;
	}
    }

    // Remove carthesian product of precedences.
    private void removeResourcePrecedence(int present, 
	    ResourcePrecedence precedence,
	    TreeSet positions1, TreeSet positions2) {
	if (present == 1) {
	    precedence.removeForward(positions1, positions2);
	    precedence.removeBackward(positions2, positions1);
	} else {
	    precedence.removeBackward(positions1, positions2);
	    precedence.removeForward(positions2, positions1);
	}
    }

    // Make auto align. Or remove.
    // Return new autoaligns, or null if nothing has changed.
    public Vector autoalign(Text text, Vector autoaligns, boolean b) {
	errorMessage = null;
	if (clicked.size() < 2) {
	    errorMessage = "Select two tiers";
	    return null;
	} else {
	    Click click1 = clicked.get(clicked.size()-2);
	    Click click2 = clicked.get(clicked.size()-1);
	    int id1 = click1.tier.id();
	    int id2 = click2.tier.id();
	    TextResource resource1 = tierResources.get(id1);
	    TextResource resource2 = tierResources.get(id2);
	    int tierNum1 = (tierNums.get(id1)).intValue();
	    int tierNum2 = (tierNums.get(id2)).intValue();
	    String name1 = resource1.tierName(tierNum1);
	    String name2 = resource2.tierName(tierNum2);
	    boolean present = includesAuto(autoaligns, 
		    resource1, name1,
		    resource2, name2);
	    if (b) {
	        if (present) {
		    errorMessage = "Auto alignment already present";
		    return null;
		} else {
		    String rel1 = resource1.getLocation();
		    String rel2 = resource2.getLocation();
		    return addAuto(text, rel1, name1, rel2, name2);
		}
	    } else {
		if (!present) {
		    errorMessage = "Auto alignment not present";
		    return null;
		} else {
		    String rel1 = resource1.getLocation();
		    String rel2 = resource2.getLocation();
		    return removeAuto(text, rel1, name1, rel2, name2);
		}
	    }
	}
    }

    ///////////////////////////
    // Communication back to user.

    public abstract void refresh();

    public abstract void editPhrase(PhraseEditor editor);

    public abstract void editResource(ResourceEditor editor);

    ////////////////////////////
    // Auxiliaries.

    // Find index in sorted vector that is just before key.
    public int binarySearch(Vector<Integer> vals, int key) {
	int i = 0;
	int j = vals.size();
	while (i < j - 1) {
	    int k = i + (j-i) / 2;
	    int val = ((Integer) vals.get(k)).intValue();
	    if (key < val) 
		j = k;
	    else 
		i = k;
	}
	return i;
    }

}
