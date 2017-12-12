/***************************************************************************/
/*                                                                         */
/*  ParsingContext.java                                                    */
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

// The context for parsing, allows gathering
// of errors and checking of availability of
// glyphs in font.

package nederhof.res;

import java.util.*;

public class ParsingContext implements IParsingContext {

    // Needed for checking availability of glyphs.
    private HieroRenderContext hieroContext;

    // Error messages are collected as strings.
    private Vector errors = new Vector();

    // Positions of error messages. Negative if no position.
    private Vector errorsPos = new Vector();

    // Is error reporting to be suppressed?
    private boolean suppressReporting = false;

    // Are warnings to be ignored?
    private boolean warningsIgnored = false;

    // String that is parsed. Kept for error reporting.
    private String string;

    // Default, mainly for testing.
    public ParsingContext() {
	this(new HieroRenderContext(14), false);
    }

    // Generic creation before applying to any string.
    public ParsingContext(HieroRenderContext hieroContext, 
	    boolean suppressReporting) {
	this.hieroContext = hieroContext;
	this.suppressReporting = suppressReporting;
    }

    // Assuming some arbitrary fontsize.
    public ParsingContext(boolean suppressReporting) {
	this(new HieroRenderContext(14), suppressReporting);
    }

    // Prepare for next parsing.
    public void setInput(String string) {
	this.errors = new Vector();
	this.errorsPos = new Vector();
	this.string = string;
    }

    // How many errors?
    public int nErrors() {
	return errors.size();
    }

    // Get i-th error.
    public String error(int i) {
	return (String) errors.get(i);
    }

    // Get position of i-th error. Negative if none.
    public int errorPos(int i) {
	Integer pos = (Integer) errorsPos.get(i);
	return pos.intValue();
    }

    // Add error already in string format.
    public void addError(String message) {
	errors.add(message);
	errorsPos.add(new Integer(-1));
    }

    // Create error message with character position and line number.
    // Print previous and next line(s).
    public void reportError(String message, int charPos, int lineNo) {
        if (lineNo >= 0 && charPos >= 0) {
            int fromPos = string.lastIndexOf('\n', charPos);
            if (fromPos < 0)
                fromPos = 0;
            int toPos = string.indexOf('\n', charPos);
            if (toPos < 0)
                toPos = string.length();
            String before = (fromPos < charPos ? string.substring(fromPos, charPos) : "");
            String after = (charPos < toPos ? string.substring(charPos, toPos) : "");
            String report = message + " at line " + lineNo + ":\n";
            if (!before.matches("\\s*")) {
                report += before;
                if (!before.matches("(?s).*\n\\s*"))
                    report += "\n";
            }
            report += "***HERE***\n";
            if (!after.matches("\\s*")) {
                report += after;
                if (!after.matches("(?s).*\n\\s*"))
                    report += "\n";
            }
            errors.add(report);
	    errorsPos.add(new Integer(charPos));
        } else {
            errors.add(message + "\n");
	    errorsPos.add(new Integer(-1));
	}
    }

    public void reportWarning(String message, int charPos, int lineNo) {
	if (!warningsIgnored)
	    reportError(message, charPos, lineNo);
    }

    public boolean suppressReporting() {
	return suppressReporting;
    }

    public void setSuppressReporting(boolean suppress) {
	suppressReporting = suppress;
    }

    public void setIgnoreWarnings(boolean suppress) {
	warningsIgnored = suppress;
    }

    public BoxPlaces getBox(String type) {
	return hieroContext.getBox(type);
    }

    public GlyphPlace getGlyph(String name) {
	return hieroContext.getGlyph(name);
    }

    public String nameToGardiner(String name) {
	return hieroContext.nameToGardiner(name);
    }

}

