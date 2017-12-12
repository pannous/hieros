/***************************************************************************/
/*                                                                         */
/*  FootnoteHelper.java                                                    */
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

package nederhof.interlinear.frame;

import java.util.*;

import nederhof.interlinear.*;

public class FootnoteHelper {

    // Filter out the footnotes.
    public static Vector<Footnote> footnotes(Vector<TierSpan> spans) {
	Vector<Footnote> notes = new Vector<Footnote>();
	for (int i = 0; i < spans.size(); i++) {
	    TierSpan span = spans.get(i);
	    notes.addAll(span.getFootnotes());
	}
	return notes;
    }

}
