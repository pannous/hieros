/***************************************************************************/
/*                                                                         */
/*  EgyptianResourceGenerator.java                                         */
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

package nederhof.interlinear.egyptian;

import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.interlinear.*;

public class EgyptianResourceGenerator extends ResourceGenerator {

    public TextResource generate(File file) throws IOException {
	if (!file.exists()) 
	    return EgyptianResource.make(file);
	else
	    return new EgyptianResource(file.getPath());
    }

    public TextResource interpret(String fileName, Object in) {
	if (in instanceof Document) {
	    Document doc = (Document) in;
	    try {
		return new EgyptianResource(fileName, doc);
	    } catch (IOException e) {
		// file is not this type of resource
	    }
	} else if (in instanceof LineNumberReader) {
	    LineNumberReader reader = (LineNumberReader) in;
	    try {
		return new EgyptianResource(fileName, reader);
	    } catch (IOException e) {
		// file is not this type of resource
	    }
	} 
	return null;
    }

    public String getName() {
	return "basic resource";
    }

    public String getDescription() {
	return "tiers for hieroglyphic, transliteration, translation, lexicon";
    }

}
