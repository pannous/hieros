/***************************************************************************/
/*                                                                         */
/*  TierConstructor.java                                                   */
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

// Auxiliary methods to construct tiers.

package nederhof.interlinear.egyptian;

import java.util.*;

import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.pdf.*;
import nederhof.interlinear.frame.*;
import nederhof.interlinear.labels.*;

public class TierConstructor {

    // Location of relevant resource.
    private String location;

    // The tier constructed so far.
    private Vector tierParts = new Vector();
    // Get parts.
    public Vector parts() {
	return tierParts;
    }

    // The number of symbols so far.
    private int nSymbols;

    // The number of the tier.
    private int number;

    // The version and scheme of tier.
    private String version;
    private String scheme;

    // Mapping labels to positions.
    // Mapping labels to pre-positions and post-positions.
    private TreeMap<VersionSchemeLabel,Vector<int[]>> labelToPositions;
    private TreeMap<VersionSchemeLabel,Vector<int[]>> labelToPrePositions;
    private TreeMap<VersionSchemeLabel,Vector<int[]>> labelToPostPositions;

    // Mapping from phrases to the first position in the phrase.
    private TreeMap<Integer,Vector<int[]>> phraseToPositions;

    // Positions where phrases start.
    private Vector<Integer> phraseStart;

    // Mapping from resource and id to tier number and position.
    private TreeMap<ResourceId,int[]> resourceIdToPosition;

    // The set of ids.
    private TreeSet<String> ids;

    // Render parameters.
    private RenderParameters params;

    // To be taken for PDF?
    private boolean pdf;

    // Is for editing?
    private boolean edit;

    // Constructor.
    public TierConstructor(String location, int number, String version, String scheme,
	    TreeMap<VersionSchemeLabel,Vector<int[]>> labelToPositions, 
	    TreeMap<VersionSchemeLabel,Vector<int[]>> labelToPrePositions, 
	    TreeMap<VersionSchemeLabel,Vector<int[]>> labelToPostPositions, 
	    TreeMap<Integer,Vector<int[]>> phraseToPositions,
	    Vector<Integer> phraseStart,
	    TreeMap<ResourceId,int[]> resourceIdToPosition,
	    TreeSet<String> ids,
	    RenderParameters params, boolean pdf, boolean edit) {
	this.location = location;
	this.number = number;
	this.version = version;
	this.scheme = scheme;
	this.labelToPositions = labelToPositions;
	this.labelToPrePositions = labelToPrePositions;
	this.labelToPostPositions = labelToPostPositions;
	this.phraseToPositions = phraseToPositions;
	this.phraseStart = phraseStart;
	this.resourceIdToPosition = resourceIdToPosition;
	this.ids = ids;
	this.params = params;
	this.pdf = pdf;
	this.edit = edit;
    }

    // From resource parts select the tierparts.
    // Notes with symbols are gathered until next
    // hieroglyphic, to which they are attached.
    public void select(Vector<ResourcePart> tier, int phrase) {
	Vector notes = new Vector();
	EgyptianTierPart previous = null;
	boolean buffer = false; // space in empty parts
	boolean empty = true;
	if (edit) {
	    previous = new PhraseSeparatorEg();
	    empty = markPhrase(empty, previous, phrase);
	    tierParts.add(previous);
	    nSymbols += previous.nSymbols();
	}
	placeInitialPos();
	for (int i = 0; i < tier.size(); i++) {
	    ResourcePart part = tier.get(i);
	    if (part instanceof NotePart) {
		NotePart note = (NotePart) part;
		if (note.symbol() < 0) {
		    empty = markPhrase(empty, note, phrase);
		    previous = placePart(note, previous);
		    nSymbols += note.nSymbols();
		} else {
		    if (pdf) {
			NotePdfPart pdfNote = new NotePdfPart(note.text(), note.symbol());
			pdfNote.setParams(params);
			notes.add(pdfNote.footnote());
		    } else {
			note.setParams(params);
			notes.add(note.footnote());
		    }
		}
	    } else if (part instanceof HiPart) {
		HiPart hi = (HiPart) part;
		if (hi.nSymbols() > 0) {
		    hi.setNotes(notes);
		    notes = new Vector();
		    empty = markPhrase(empty, hi, phrase);
		    previous = placePart(hi, previous);
		    nSymbols += hi.nSymbols();
		}
	    } else if (part instanceof ImagePlacePart) {
		ImagePlacePart im = (ImagePlacePart) part;
		im.setEdit(edit);
		empty = markPhrase(empty, im, phrase);
		previous = placePart(im, previous);
		placePos(im.id);
		nSymbols += im.nSymbols();
	    } else if (part instanceof PrePart) {
		PrePart pre = (PrePart) part;
		placePreCoord(pre);
	    } else if (part instanceof PostPart) {
		PostPart post = (PostPart) part;
		placePostCoord(post);
	    } else if (part instanceof PosPart) {
		PosPart pos = (PosPart) part;
		placePos(pos);
	    } else if (part instanceof LxPart) {
		LxPart lx = (LxPart) part;
		empty = markPhrase(empty, lx, phrase);
		previous = placePart(lx, previous);
		placePos(lx.id);
		nSymbols += lx.nSymbols();
	    } else if (part instanceof OrthoPart) {
		OrthoPart op = (OrthoPart) part;
		empty = markPhrase(empty, op, phrase);
		previous = placePart(op, previous);
		placePos(op.id);
		nSymbols += op.nSymbols();
	    } else if (part instanceof EgyptianTierPart) {
		EgyptianTierPart ePart = (EgyptianTierPart) part;
		if (part instanceof CoordPart) {
		    CoordPart coord = (CoordPart) ePart;
		    placeCoord(coord);
		} else if (part instanceof StringPart) {
		    StringPart str = (StringPart) part;
		    if (buffer) 
			ePart = str = buffered(str);
		    if (str.string.matches("\\s\\s*")) {
			buffer = true;
			continue;
		    } 
		}
		empty = markPhrase(empty, ePart, phrase);
		previous = placePart(ePart, previous);
		nSymbols += ePart.nSymbols();
	    } 
	    buffer = false;
	}
    }

    // Make phrase for tier without phrases, to allow editing.
    public void selectEmpty() {
	EgyptianTierPart sep = new PhraseSeparatorEg();
	tierParts.add(sep);
	nSymbols += sep.nSymbols();
    }

    // Place part in series of parts.
    private EgyptianTierPart placePart(EgyptianTierPart part, 
	    EgyptianTierPart previous) {
	if (part.nSymbols() > 0) {
	    if (pdf)
		part = toPdf(part);
	    if (previous != null)
		previous.setNext(part);
	    part.setParams(params);
	    part.setEdit(edit);
	    tierParts.add(part);
	    return part;
	} else 
	    return previous;
    }

    // Move buffered space to beginning of part.
    private StringPart buffered(StringPart part) {
	if (part instanceof AlPart) {
	    AlPart p = (AlPart) part;
	    return new AlPart(" " + p.string, p.upper, p.footnote);
	} else if (part instanceof NoPart) {
	    NoPart p = (NoPart) part;
	    return new NoPart(" " + p.string);
	} else if (part instanceof IPart) {
	    IPart p = (IPart) part;
	    return new IPart(" " + p.string);
	} else
	    return part;
    }

    // Place coordinate.
    private void placeCoord(CoordPart coord) {
	String label = coord.id;
	VersionSchemeLabel fullLabel = new VersionSchemeLabel(version, scheme, label);
	if (labelToPositions.get(fullLabel) == null)
	    labelToPositions.put(fullLabel, new Vector());
	Vector<int[]> poss = labelToPositions.get(fullLabel);
	poss.add(new int[] {number, nSymbols});

    }

    // Process pre coordinate.
    private void placePreCoord(PrePart pre) {
	String label = pre.id;
	VersionSchemeLabel fullLabel = new VersionSchemeLabel(version, scheme, label);
	if (labelToPrePositions.get(fullLabel) == null)
	    labelToPrePositions.put(fullLabel, new Vector());
	Vector<int[]> poss = labelToPrePositions.get(fullLabel);
	poss.add(new int[] {number, nSymbols});
    }

    // Process post coordinate.
    private void placePostCoord(PostPart post) {
	String label = post.id;
	VersionSchemeLabel fullLabel = new VersionSchemeLabel(version, scheme, label);
	if (labelToPostPositions.get(fullLabel) == null)
	    labelToPostPositions.put(fullLabel, new Vector());
	Vector<int[]> poss = labelToPostPositions.get(fullLabel);
	poss.add(new int[] {number, nSymbols-1});
    }

    // Assume there is a pos with empty string at the beginning of the tier.
    private void placeInitialPos() {
	ResourceId resourceId = new ResourceId(location, "");
	resourceIdToPosition.put(resourceId, new int[] {number, 0});
    }

    // Process pos.
    private void placePos(PosPart pos) {
	int symbol = Math.max(pos.symbol, 0);
	String id = pos.id;
	ResourceId resourceId = new ResourceId(location, id);
	resourceIdToPosition.put(resourceId, new int[] {number, nSymbols + symbol});
	ids.add(id);
    }

    // Process id attached to e.g. lexical entry.
    private void placePos(String id) {
	if (!id.equals("")) {
	    ResourceId resourceId = new ResourceId(location, id);
	    resourceIdToPosition.put(resourceId, new int[] {number, nSymbols});
	    ids.add(id);
	}
    }

    // Prepare resource parts in footnote.
    public static Vector prepareFootnote(Vector<ResourcePart> tier, 
	    RenderParameters params, boolean pdf) {
	Vector tierParts = new Vector();
	EgyptianTierPart previous = null;
	for (int i = 0; i < tier.size(); i++) {
	    ResourcePart part = tier.get(i);
	    if (part instanceof EgyptianTierPart) {
		EgyptianTierPart ePart = (EgyptianTierPart) part;
		ePart.setFootnote(true);
		if (ePart.nSymbols() > 0) {
		    if (pdf) {
			ePart = toPdf(ePart);
			ePart.setFootnote(true);
		    }
		    if (previous != null)
			previous.setNext(ePart);
		    ePart.setParams(params);
		    tierParts.add(ePart);
		    previous = ePart;
		}
	    }
	}
	return tierParts;
    }

    // Record beginning of phrase.
    private boolean markPhrase(boolean empty, EgyptianTierPart part, int phrase) {
	if (!empty || part.nSymbols() == 0)
	    return empty;
	else {
	    Integer p = new Integer(phrase);
	    if (phraseToPositions.get(p) == null)
		phraseToPositions.put(p, new Vector<int[]>());
	    Vector<int[]> poss = (Vector<int[]>) phraseToPositions.get(p);
	    poss.add(new int[] {number, nSymbols});
	    phraseStart.add(new Integer(nSymbols));
	    return false;
	}
    }

    // Turn part into part for PDF.
    private static EgyptianTierPdfPart toPdf(EgyptianTierPart part) {
	if (part instanceof HiPart) {
	    HiPart p = (HiPart) part;
	    HiPdfPart pPdf = new HiPdfPart(p.hi, p.footnote);
	    pPdf.setNotes(p.getNotes());
	    return pPdf;
	} else if (part instanceof AlPart) {
	    AlPart p = (AlPart) part;
	    return new AlPdfPart(p.string, p.upper, p.footnote);
	} else if (part instanceof NoPart) {
	    NoPart p = (NoPart) part;
	    return new NoPdfPart(p.string);
	} else if (part instanceof IPart) {
	    IPart p = (IPart) part;
	    return new IPdfPart(p.string);
	} else if (part instanceof LxPart) {
	    LxPart p = (LxPart) part;
	    return new LxPdfPart(
		    p.texthi,
		    p.textal,
		    p.texttr,
		    p.textfo,
		    p.cite,
		    p.href,
		    p.keyhi,
		    p.keyal,
		    p.keytr,
		    p.keyfo,
		    p.dicthi,
		    p.dictal,
		    p.dicttr,
		    p.dictfo);
	} else if (part instanceof OrthoPart) {
	    OrthoPart p = (OrthoPart) part;
	    return new OrthoPdfPart(
		    p.texthi,
		    p.textal,
		    p.textortho);
	} else if (part instanceof ImagePlacePart) {
	    ImagePlacePart p = (ImagePlacePart) part;
	    return new ImagePlacePdfPart(p.info);
	} else if (part instanceof EtcPart) {
	    EtcPart p = (EtcPart) part;
	    return new EtcPdfPart();
	} else if (part instanceof NotePart) {
	    NotePart p = (NotePart) part;
	    return new NotePdfPart(p.text(), p.symbol());
	} else if (part instanceof CoordPart) {
	    CoordPart p = (CoordPart) part;
	    return new CoordPdfPart(p.id);
	} else if (part instanceof LinkPart) {
	    LinkPart p = (LinkPart) part;
	    return new LinkPdfPart(p.string, p.ref);
	} else if (part instanceof FootnoteMarker) {
	    FootnoteMarker p = (FootnoteMarker) part;
	    return new FootnoteMarkerPdf(p.marker);
	} else {
	    System.err.println("Strange element as argument of toPdf " +
		    "in nederhof.interlinear.egyptian.TierConstructor");
	    return null; // should not happen
	}
    }

}
