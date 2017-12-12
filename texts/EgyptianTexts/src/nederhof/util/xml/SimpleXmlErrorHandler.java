/***************************************************************************/
/*                                                                         */
/*  SimpleXmlErrorHandler.java                                             */
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

// A very simple error handler for XML.

package nederhof.util.xml;

import org.xml.sax.*;

public class SimpleXmlErrorHandler implements ErrorHandler {

    // Is error to be written to standard error?
    private boolean toErr = false;

    public SimpleXmlErrorHandler(boolean toErr) {
	this.toErr = toErr;
    }

    public void error(SAXParseException e) throws SAXException {
	if (e.getSystemId() != null)
	    System.err.println("In " + e.getSystemId());
	else if (e.getPublicId() != null)
	    System.err.println("In " + e.getPublicId());
	System.err.println("line " + e.getLineNumber() +
		": " + e.getMessage());
	throw e;
    }

    public void fatalError(SAXParseException e) throws SAXException {
	error(e);
    }

    public void warning(SAXParseException e) throws SAXException {
	error(e);
    }
}

