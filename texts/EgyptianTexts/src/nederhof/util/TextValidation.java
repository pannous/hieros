/***************************************************************************/
/*                                                                         */
/*  TextValidation.java                                                    */
/*                                                                         */
/*  Copyright (c) 2008 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

package nederhof.util;

// Abstract class for editing a text, until it passes some
// type of validation.
public abstract class TextValidation {

    // The text in present state.
    private String text = "";

    // Present text tested yet?
    private boolean tested = false;
    // If so, results of validation.
    private boolean valid = false;
    private String error = "";
    private int pos = 0;

    // Set results of validation.
    // To be called by extending classes.
    protected void setResults(boolean valid, String error, int pos) {
	this.valid = valid;
	this.error = error;
	this.pos = pos;
	tested = true;
    }

    // Construct validator.
    public TextValidation(String text) {
        this.text = text;
    }

    // Is text valid?
    public boolean isValid() {
        if (!tested)
            validate();
        return valid;
    }

    // Get error message if text is not valid.
    public String getError() {
        if (!tested)
            validate();
        return error;
    }

    // Get position of error if text is not valid.
    public int getErrorPos() {
        if (!tested)
            validate();
        return pos;
    }

    // Change to new text.
    public void setText(String text) {
        this.text = text;
        tested = false;
    }

    // Get text.
    public String getText() {
	return text;
    }

    // Abort validation process.
    public abstract void abort();

    // Store or otherwise process validated text.
    public abstract void finish();

    // Do the validation, documenting result with setResults.
    protected abstract void validate();
}
