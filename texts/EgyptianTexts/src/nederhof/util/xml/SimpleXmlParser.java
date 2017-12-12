/***************************************************************************/
/*                                                                         */
/*  SimpleXmlParser.java                                                   */
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

// Simple XML parser.

package nederhof.util.xml;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class SimpleXmlParser {

    // Simple kind of XML parser.
    public static DocumentBuilder construct(boolean validating, boolean toErr) {
	DocumentBuilder parser = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setCoalescing(false);
            factory.setExpandEntityReferences(true);
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(true);
            factory.setNamespaceAware(false);
            factory.setValidating(validating);
            parser = factory.newDocumentBuilder();
            parser.setErrorHandler(new SimpleXmlErrorHandler(toErr));
	    return parser;
        } catch (ParserConfigurationException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
	return parser;
    }

}
