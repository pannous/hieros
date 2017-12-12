/***************************************************************************/
/*                                                                         */
/*  SchemeMapGenerator.java                                                */
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

package nederhof.interlinear.labels;

import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import nederhof.interlinear.*;
import nederhof.util.*;

public class SchemeMapGenerator extends ResourceGenerator {

    public TextResource generate(File file) throws IOException {
	if (!file.exists()) 
	    return SchemeMap.make(file);
	else
	    return new SchemeMap(file.getPath());
    }

    public TextResource interpret(String fileName, Object in) {
	if (in instanceof Document) {
	    Document doc = (Document) in;
	    try {
		return new SchemeMap(fileName, doc);
	    } catch (IOException e) {
		// file is not scheme map
	    }
	} 
	return null;
    }

    public String getName() {
	return "scheme map";
    }

    public String getDescription() {
	return "mapping between numbering schemes";
    }

}

