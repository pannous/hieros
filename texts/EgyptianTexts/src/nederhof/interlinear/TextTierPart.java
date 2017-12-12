/***************************************************************************/
/*                                                                         */
/*  TextTierPart.java                                                      */
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

// Part of a tier that is plain text.

package nederhof.interlinear;

abstract class TextTierPart extends TierPart {

    // String representation including whitespace.
    protected String text;

    // Cached length, excluding whitespace.
    private int nSymbols;

    // Mapping from whitespace-free position to original
    // position.
    private int[] originalPos;

    // Whether symbol follows space.
    protected boolean[] followsSpace;

    // Penalty at end, and at whitespace.
    protected double endPenalty = 0.0;
    protected double spacePenalty = 0.0;

    // The text is assumed not to start with whitespace, but
    // it may end with whitespace.
    protected TextTierPart(String text) {
	this.text = text;
	nSymbols = 0;
	for (int i = 0; i < text.length(); i++)
	    if (!isSpace(i))
		nSymbols++;
	originalPos = new int[nSymbols+1];
	followsSpace = new boolean[nSymbols+1];
	for (int i = 0, j = 0; i < text.length(); i++)
	    if (!isSpace(i)) {
		originalPos[j] = i;
		followsSpace[j] = i > 0 && isSpace(i-1);
		j++;
	    }
	originalPos[nSymbols] = text.length();
	followsSpace[nSymbols] = isSpace(text.length()-1);
    }

    // Symbol in original text is space.
    protected boolean isSpace(int i) {
	return Character.isWhitespace(text.charAt(i));
    }

    // Get text between positions.
    public String getText(int i, int j) {
	return text.substring(originalPos[i], originalPos[j]);
    }

    // Set penalties.
    public void setEndPenalty(double penalty) {
	endPenalty = penalty;
    }
    public void setSpacePenalty(double penalty) {
	spacePenalty = penalty;
    }

    // Translate whitespace-free position to original position.
    protected int originalPos(int pos) {
	return originalPos[pos];
    }

    public int nSymbols() {
	return nSymbols;
    }

    // Breakable if at beginning or previous symbol is whitespace.
    public boolean breakable(int i) {
	return followsSpace[i];
    }

}
