/***************************************************************************/
/*                                                                         */
/*  PhraseEditHelper.java                                                  */
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

// Helper for editing phrases.

package nederhof.interlinear.egyptian;

import java.util.*;

import nederhof.interlinear.*;
import nederhof.interlinear.labels.*;
import nederhof.res.*;
import nederhof.util.*;

public class PhraseEditHelper {

    // Name of label for position in phrase of tier.
    // Return null if failed. If labelled position does not exist, create
    // one (if allowed).
    public static String positionId(Vector<ResourcePart> tier, int pos, TreeSet<String> ids,
	    boolean mayCreate) {
	int nSymbols = 0;
	for (int i = 0; i < tier.size(); i++) {
	    ResourcePart part = tier.get(i);
	    if (part instanceof PosPart) {
		PosPart posPart = (PosPart) part;
		int symbol = Math.max(posPart.symbol, 0);
		String id = posPart.id;
		if (nSymbols + symbol == pos)
		    return id;
	    } else if (part instanceof HiPart) {
		HiPart hi = (HiPart) part;
		if (hi.nSymbols() > 0) {
		    if (pos < nSymbols + hi.nSymbols()) {
			if (mayCreate) {
			    int symbol = pos - nSymbols;
			    String newId = freshId(ids);
			    PosPart newPos = new PosPart(symbol, newId);
			    tier.insertElementAt(newPos, i);
			    return newId;
			} else 
			    return null;
		    }
		    nSymbols += hi.nSymbols();
		}
	    } else if (part instanceof EgyptianTierPart) {
		EgyptianTierPart ePart = (EgyptianTierPart) part;
		if (ePart.nSymbols() > 0) {
		    if (nSymbols == pos) {
			if (mayCreate) {
			    String newId = freshId(ids);
			    PosPart newPos = new PosPart(-1, newId);
			    tier.insertElementAt(newPos, i);
			    return newId;
			} else
			    return null;
		    }
		    if (part instanceof StringPart) {
			StringPart str = (StringPart) part;
			if (pos < nSymbols + str.nSymbols()) {
			    if (mayCreate) {
				int symbol = pos - nSymbols;
				StringPart prefixPart = str.prefixPart(symbol);
				StringPart suffixPart = str.suffixPart(symbol);
				String newId = freshId(ids);
				PosPart newPos = new PosPart(-1, newId);
				tier.setElementAt(suffixPart, i);
				tier.insertElementAt(newPos, i);
				tier.insertElementAt(prefixPart, i);
				return newId;
			    } else
				return null;
			}
		    } 
		    nSymbols += ePart.nSymbols();
		}
	    }
	}
	return null;
    }

    // Name of label for position in phrase of tier.
    // If labelled position does not exist, create
    // one. Record whether label was created.
    // If exact position cannot be found or created,
    // return set of positions with offsets.
    public static TreeSet<LabelOffset> positionIdOffset(Vector tier, int pos, TreeSet<String> ids,
	    boolean mayCreate, WrappedBool changed) {
	TreeSet<LabelOffset> positions = new TreeSet<LabelOffset>();
	int nSymbols = 0;
	for (int i = 0; i < tier.size(); i++) {
	    ResourcePart part = (ResourcePart) tier.get(i);
	    if (part instanceof PosPart) {
		PosPart posPart = (PosPart) part;
		int symbol = Math.max(posPart.symbol, 0);
		String id = posPart.id;
		if (nSymbols + symbol == pos && mayCreate)
		    return singleton(id);
		else 
		    positions.add(new LabelOffset(id, pos - (nSymbols + symbol)));
	    } else if (part instanceof HiPart) {
		HiPart hi = (HiPart) part;
		if (hi.nSymbols() > 0) {
		    if (pos < nSymbols + hi.nSymbols()) {
			if (mayCreate) {
			    int symbol = pos - nSymbols;
			    String newId = freshId(ids);
			    PosPart newPos = new PosPart(symbol, newId);
			    tier.insertElementAt(newPos, i);
			    changed.set(true);
			    return singleton(newId);
			} 
		    }
		    nSymbols += hi.nSymbols();
		}
	    } else if (part instanceof EgyptianTierPart) {
		EgyptianTierPart ePart = (EgyptianTierPart) part;
		if (ePart.nSymbols() > 0) {
		    if (nSymbols == pos) {
			if (mayCreate) {
			    String newId = freshId(ids);
			    PosPart newPos = new PosPart(-1, newId);
			    tier.insertElementAt(newPos, i);
			    changed.set(true);
			    return singleton(newId);
			}
		    }
		    if (part instanceof StringPart) {
			StringPart str = (StringPart) part;
			if (pos < nSymbols + str.nSymbols()) {
			    if (mayCreate) {
				int symbol = pos - nSymbols;
				StringPart prefixPart = str.prefixPart(symbol);
				StringPart suffixPart = str.suffixPart(symbol);
				String newId = freshId(ids);
				PosPart newPos = new PosPart(-1, newId);
				tier.setElementAt(suffixPart, i);
				tier.insertElementAt(newPos, i);
				tier.insertElementAt(prefixPart, i);
				changed.set(true);
				return singleton(newId);
			    } 
			}
		    } 
		    nSymbols += ePart.nSymbols();
		}
	    }
	}
	return positions;
    }

    // Make single label-offset pair, as singleton set.
    private static TreeSet<LabelOffset> singleton(String id) {
	TreeSet<LabelOffset> singleton = new TreeSet<LabelOffset>();
	singleton.add(new LabelOffset(id, 0));
	return singleton;
    }

    // Get id that is new.
    private static String freshId(TreeSet<String> ids) {
	int i = 0;
	while (ids.contains("" + i))
	    i++;
	String id = "" + i;
	ids.add(id);
	return id;
    }

    // Normalise hieroglyphic tier, making sure consecutive hieroglyphic code
    // is joined. Each note and pos in between needs to be moved 
    // before first part.
    public static void normalizeHieroTier(Vector parts) {
	Vector newParts = new Vector();
	Vector notes = new Vector();
	Vector posses = new Vector();
	HiPart hiPart = null;
	for (int i = 0; i < parts.size(); i++) {
	    ResourcePart part = (ResourcePart) parts.get(i);
	    if (part instanceof NotePart) {
		if (hiPart == null)
		    notes.add(part);
		else {
		    int size = hiPart.nSymbols();
		    NotePart notePart = (NotePart) part;
		    int sym = Math.max(0, notePart.symbol());
		    NotePart moved = 
			new NotePart(notePart.text(), sym + size);
		    notes.add(moved);
		}
	    } else if (part instanceof PosPart) {
		if (hiPart == null)
		    posses.add(part);
		else {
		    int size = hiPart.nSymbols();
		    PosPart posPart = (PosPart) part;
		    int sym = Math.max(0, posPart.symbol);
		    PosPart moved = 
			new PosPart(sym + size, posPart.id);
		    posses.add(moved);
		}
	    } else if (part instanceof HiPart) {
		HiPart nextPart = (HiPart) part;
		if (hiPart == null)
		    hiPart = nextPart;
		else {
		    ResFragment frag1 = hiPart.parsed;
		    ResFragment frag2 = nextPart.parsed;
		    if (frag1 != null && frag2 != null) {
			ResFragment combined =
			    ResComposer.append(frag1, frag2);
			hiPart = new HiPart(combined.toString(), false);
		    }
		}
	    } else {
		newParts.addAll(notes);
		newParts.addAll(posses);
		if (hiPart != null)
		    newParts.add(hiPart);
		notes.clear();
		posses.clear();
		hiPart = null;
		newParts.add(part);
	    }
	}
	newParts.addAll(notes);
	newParts.addAll(posses);
	if (hiPart != null)
	    newParts.add(hiPart);
	parts.clear();
	parts.addAll(newParts);
    }

    // Change space symbols into transliteration font.
    public static void normalizeTranslitTier(Vector parts) {
	Vector newParts = new Vector();
	for (int i = 0; i < parts.size(); i++) {
	    ResourcePart part = (ResourcePart) parts.get(i);
	    if (part instanceof NoPart) {
		NoPart noPart = (NoPart) part;
		if (noPart.str().matches("\\s+")) {
		    AlPart alPart = 
			new AlPart(" ", false, false);
		    newParts.add(alPart);
		} else
		    newParts.add(noPart);
	    } else
		newParts.add(part);
	}
	parts.clear();
	parts.addAll(newParts);
    }

    // Transfer text from original before position to left,
    // and put the rest in right. Certain material that does not 
    // correspond to positions, should go before the first position 
    // in the right part, but 'post' must go after the left part.
    // Special treatment is needed for hieroglyphic,
    // as breaking up one fragment may also affect where
    // posses and notes go.
    public static void cutPhrase(Vector original, int pos, 
	    Vector left, Vector right, 
	    boolean hiero) {
	int nSymbols = 0;
	Vector buffer = new Vector();
	int i;
	for (i = 0; i < original.size(); i++) {
	    ResourcePart part = (ResourcePart) original.get(i);
	    if (part instanceof NotePart) {
		NotePart note = (NotePart) part;
		if (hiero) 
		    buffer.add(note);
		else {
		    left.add(note);
		    nSymbols += note.nSymbols();
		    if (nSymbols >= pos) 
			break;
		}
	    } else if (part instanceof HiPart) {
		HiPart hi = (HiPart) part;
		if (nSymbols + hi.nSymbols() <= pos) {
		    left.addAll(buffer);
		    buffer.clear();
		    left.add(hi);
		    nSymbols += hi.nSymbols();
		    if (nSymbols >= pos)
			break;
		} else {
		    HiPart prefixPart = hi.prefixPart(pos - nSymbols);
		    HiPart suffixPart = hi.suffixPart(pos - nSymbols);
		    if (prefixPart == null || suffixPart == null)
			return;
		    Vector leftBuffer = new Vector();
		    Vector rightBuffer = new Vector();
		    splitBuffers(buffer, prefixPart.nSymbols(), 
			    leftBuffer, rightBuffer);
		    left.addAll(leftBuffer);
		    left.add(prefixPart);
		    right.addAll(rightBuffer);
		    right.add(suffixPart);
		    break;
		}
	    } else if (part instanceof PrePart || 
		    part instanceof PostPart) {
		left.add(part);
	    } else if (part instanceof PosPart) {
		if (hiero) 
		    buffer.add(part);
		else 
		    left.add(part);
	    } else if (part instanceof StringPart) {
		StringPart str = (StringPart) part;
		if (nSymbols + str.nSymbols() <= pos) {
		    left.add(str);
		    nSymbols += str.nSymbols();
		    if (nSymbols >= pos)
			break;
		} else {
		    StringPart prefixPart = str.prefixPart(pos - nSymbols);
		    StringPart suffixPart = str.suffixPart(pos - nSymbols);
		    left.add(prefixPart);
		    right.add(suffixPart);
		    break;
		}
	    } else if (part instanceof EgyptianTierPart) {
		EgyptianTierPart ePart = (EgyptianTierPart) part;
		if (ePart.nSymbols() > 0) {
		    left.addAll(buffer);
		    buffer.clear();
		}
		left.add(ePart);
		nSymbols += ePart.nSymbols();
		if (nSymbols >= pos)
		    break;
	    }
	}
	i++;
	while (right.isEmpty() &&
		i < original.size() && 
		original.get(i) instanceof PostPart) {
	    left.add(original.get(i));
	    i++;
	}
	while (i < original.size()) {
	    right.add(original.get(i));
	    i++;
	}
    }

    // For pos and note elements, divide them according to whether they are
    // before or after position. Adjust symbol for the right part.
    private static void splitBuffers(Vector buffer, int pos,
	    Vector leftBuffer, Vector rightBuffer) {
	for (int i = 0; i < buffer.size(); i++) {
	    ResourcePart part = (ResourcePart) buffer.get(i);
	    if (part instanceof NotePart) {
		NotePart note = (NotePart) part;
		if (note.symbol() < pos)
		    leftBuffer.add(note);
		else {
		    note = new NotePart(note.text(), 
			    note.symbol() - pos);
		    rightBuffer.add(note);
		}
	    } else if (part instanceof PosPart) {
		PosPart posPart = (PosPart) part;
		if (posPart.symbol < pos)
		    leftBuffer.add(posPart);
		else {
		    posPart = new PosPart(posPart.symbol - pos,
			    posPart.id);
		    rightBuffer.add(posPart);
		}
	    }
	}
    }

}
