/***************************************************************************/
/*                                                                         */
/*  Testing.java                                                           */
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

// Only to be used for testing.

package nederhof.interlinear;

import java.util.*;

class Testing {

    public static void main(String[] args) {
	SimpleTextTierPart p1 = new SimpleTextTierPart("this is some text ");
	SimpleTextTierPart p1b = new SimpleTextTierPart("followed by more text");
	SimpleTextTierPart p2 = new SimpleTextTierPart("and here some other word");
	SimpleTextTierPart p2b = new SimpleTextTierPart("s and more words");
	Vector v1 = new Vector();
	Vector v2 = new Vector();
	v1.add(p1);
	v1.add(p1b);
	v2.add(p2);
	v2.add(p2b);
	Tier t1 = new Tier(0, TextResource.SHOWN, v1);
	Tier t2 = new Tier(1, TextResource.SHOWN, v2);

	TierPos pos1 = new TierPos("start", t1, 6);
	TreeSet pos1set = new TreeSet();
	pos1set.add(pos1);
	t2.setPrecedings(5, pos1set);

	TierPos pos2 = new TierPos("start", t2, 7);
	TreeSet pos2set = new TreeSet();
	pos2set.add(pos2);
	t1.setPrecedings(6, pos2set);

	TierPos endPos1 = new TierPos("start", t1, t1.nSymbols());
	TierPos endPos2 = new TierPos("start", t2, t2.nSymbols());
	TreeSet ends = new TreeSet();
	ends.add(endPos1);
	ends.add(endPos2);
	// t1.setBreaks(t1.nSymbols(), ends);
	// t2.setBreaks(t2.nSymbols(), ends);

	TierPos breakPos1 = new TierPos("start", t1, 24);
	TierPos breakPos2 = new TierPos("start", t2, 11);
	TierPos breakPos3 = new TierPos("start", t2, 16);
	TreeSet breaks = new TreeSet();
	breaks.add(breakPos1);
	breaks.add(breakPos2);
	breaks.add(breakPos3);
	// t1.setBreaks(24, breaks);
	// t2.setBreaks(11, breaks);
	// t2.setBreaks(16, breaks);

	Vector tiers = new Vector();
	tiers.add(t1);
	tiers.add(t2);
	new InterlinearFormatting(tiers) {
	    protected float width() {
		return 45;
	    }

	    protected boolean processSection(String[] modes,
		    Vector[] sectionSpans, TreeMap[] sectionSpanLocations) {
		for (int i = 0; i < sectionSpans.length; i++) {
		    for (int j = 0; j < sectionSpans[i].size(); j++) {
			TierSpan span = (TierSpan) sectionSpans[i].get(j);
			Float location = 
			    (Float) sectionSpanLocations[i].get(new Integer(span.fromPos));
			Vector parts = span.parts(location.floatValue());
			for (int k = 0; k < parts.size(); k++) {
			    LocatedTierPartSpan subpart =
				(LocatedTierPartSpan) parts.get(k);
			    SimpleTextTierPart simplePart = (SimpleTextTierPart) subpart.part;
			    String text = 
				simplePart.getText(subpart.fromPos, subpart.toPos);
			    for (int s = 0; s < subpart.location; s++)
				System.out.print(" ");
			    System.out.println(text);
			}
		    }
		}
		System.out.println("--");
		return true;
	    }

	    protected void finish() {
		System.out.println("done");
	    }
	};
    }

}
