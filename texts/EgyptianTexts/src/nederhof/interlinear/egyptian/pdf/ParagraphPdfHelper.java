/***************************************************************************/
/*                                                                         */
/*  ParagraphPdfHelper.java                                                */
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

// For paragraph, produce vector of parts.

package nederhof.interlinear.egyptian.pdf;

import java.util.*;

import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.frame.pdf.*;

public class ParagraphPdfHelper {

    // Carry over space to next part.
    public static Vector paragraphToParts(Vector par, PdfRenderParameters params) {
	boolean transferSpace = false;
	Vector parts = new Vector();
	for (int j = 0; j < par.size(); j++) {
	    Object[] s = (Object[]) par.get(j);
	    String kind = (String) s[0];
	    if (kind.equals("link")) {
		String[] linkText = (String[]) s[1];
		String info = (String) linkText[1];
		if (transferSpace) {
		    info = info + " ";
		    transferSpace = false;
		}
		parts.add(new LinkPdfPart(info, linkText[0]));
	    } else {
		String info = (String) s[1];
		if (transferSpace) {
		    info = " " + info;
		    transferSpace = false;
		}
		if (info.matches(""))
		    ; // ignore
		else if (info.matches("\\s*"))
		    transferSpace = true;
		else if (kind.equals("plain"))
		    parts.add(new PlainPdfPart(info));
		else if (kind.equals("italic"))
		    parts.add(new IPdfPart(info));
		else if (kind.equals("hiero"))
		    parts.add(new HiPdfPart(info, false));
		else if (kind.equals("translower")) 
		    parts.add(new AlPdfPart(info, false, false));
		else if (kind.equals("transupper"))
		    parts.add(new AlPdfPart(info, true, false));
	    }
	}
	EgyptianTierPdfPart previous = null;
	for (int i = 0; i < parts.size(); i++) {
	    EgyptianTierPdfPart p = (EgyptianTierPdfPart) parts.get(i);
	    p.setParams(params);
	    if (previous != null)
		previous.setNext(p);
	    previous = p;
	}
	return parts;
    }

}
