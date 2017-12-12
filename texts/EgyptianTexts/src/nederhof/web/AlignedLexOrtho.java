package nederhof.web;

import java.util.*;

import nederhof.alignment.*;
import nederhof.alignment.egyptian.*;
import nederhof.alignment.generic.*;
import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.egyptian.pdf.*;
import nederhof.interlinear.egyptian.ortho.*;

// Aligned lexical/orthographic information.
public class AlignedLexOrtho {

    // Lexical information plus corresponding orthographic information.
    public LxPdfPart lxPart;
    public Vector<OrthoPdfPart> orthoParts;

    public AlignedLexOrtho(LxPdfPart lxPart, Vector<OrthoPdfPart> orthoParts) {
	this.lxPart = lxPart;
	this.orthoParts = orthoParts;
    }

    // Align two vectors of things containing strings.
    public static Vector<AlignedLexOrtho> align(
	    Vector<LxPdfPart> lxParts, Vector<OrthoPdfPart> oParts) {
	Vector<AlignedLexOrtho> combined = new Vector<AlignedLexOrtho>();
	int oLength = 0;
	for (OrthoPdfPart oPart : oParts) 
	    oLength += oPart.textal.length() + 1;
	int[] nearestBound = new int[oLength + 1];
	Vector<Character> lxChars = new Vector<Character>();
	Vector<Character> oChars = new Vector<Character>();
	for (LxPdfPart lxPart : lxParts) {
	    for (char c : lxPart.textal.toCharArray())
		lxChars.add(c);
	     lxChars.add(' ');
	}
	int oIndex = 0;
	int oPrefLen = 0;
	for (OrthoPdfPart oPart : oParts) {
	    for (char c : oPart.textal.toCharArray())
		oChars.add(c);
	     oChars.add(' ');
	     int len = oPart.textal.length() + 1;
	     for (int i = 0; i <= len; i++)
		 nearestBound[oPrefLen + i] = i < len / 2 ? oIndex : oIndex + 1;
	     oPrefLen += len;
	     oIndex++;
	}
	MinimumEdit updater = new MinimumEdit(lxChars, oChars);
	int lPrefLen = 0;
	for (LxPdfPart lxPart : lxParts) {
	    int len = lxPart.textal.length() + 1;
	    int lowPos = updater.map(lPrefLen);
	    int highPos = updater.map(lPrefLen + len);
	    int low = nearestBound[lowPos];
	    int high = nearestBound[highPos];
	    combined.add(new AlignedLexOrtho(lxPart, 
			new Vector<OrthoPdfPart>(oParts.subList(low, high))));
	    lPrefLen += len;
	}
	return combined;
    }
}
