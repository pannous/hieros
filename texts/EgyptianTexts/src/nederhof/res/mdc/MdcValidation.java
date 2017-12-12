/***************************************************************************/
/*                                                                         */
/*  MdcValidation.java                                                     */
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

package nederhof.res.mdc;

import java.io.*;

import nederhof.res.*;
import nederhof.util.*;

// Editing text, until it passes through MDC parser.
public class MdcValidation extends TextValidation {

    // Result of parsing.
    private Object result = null;

    // File in which result is to be stored, upon
    // successful validation.
    private File outFile;

    // Construct validator.
    public MdcValidation(String text, File outFile) {
	super(text);
	this.outFile = outFile;
    }

    // Abort validation process.
    public void abort() {
	System.err.println("Conversion aborted");
    }

    // Store result of parsing.
    public void finish() {
	try {
	    FileWriter fileWriter = new FileWriter(outFile);
	    fileWriter.write("" + result);
	    fileWriter.close();
	} catch (IOException e) {
	    System.err.println(e);
	}
    }

    // Validate by calling MdC parser.
    protected void validate() {
	parser p = new parser(getText(), new MdcResAux());
	try {
	    result = p.parse().value;
	} catch (Exception e) {
	    System.err.println(e);
	    e.printStackTrace();
	    result = null;
	}
	if (p.errors.size() > 0) {
	    String error = (String) p.errors.get(0);
	    int pos = p.errorPosition.y;
	    setResults(false, error, pos);
	} else
	    setResults(true, "", 0);
    }
}
