package nederhof.web;

import java.util.*;

import nederhof.alignment.*;
import nederhof.alignment.egyptian.*;
import nederhof.alignment.generic.*;
import nederhof.interlinear.*;
import nederhof.interlinear.egyptian.*;
import nederhof.interlinear.egyptian.pdf.*;
import nederhof.interlinear.egyptian.image.*;
import nederhof.res.*;

// Aligned lexical/image information (where signs are in images).
public class AlignedLexPlaces {

    // Lexical information plus corresponding image information.
    public LxPdfPart lxPart;
    public Vector<ImagePlace> places;

    public AlignedLexPlaces(LxPdfPart lxPart, Vector<ImagePlace> places) {
	this.lxPart = lxPart;
	this.places = places;
    }

    public static Vector<AlignedLexPlaces> align(
	    Vector<LxPdfPart> lxParts, Vector<ImagePlacePdfPart> iParts) {
	Vector<AlignedLexPlaces> combined = new Vector<AlignedLexPlaces>();
	Vector<String> lxNames = new Vector<String>();
	Vector<String> iNames = new Vector<String>();
	Vector<Integer> lxNameLengths = new Vector<Integer>();
	for (LxPdfPart lxPart : lxParts) {
	    ResFragment res = lxPart.texthiParsed;
	    Vector<String> names = res.glyphNames();
	    lxNames.addAll(names);
	    lxNameLengths.add(names.size());
	}
	for (ImagePlacePdfPart iPart : iParts) {
	    ImageSign info = iPart.info;
	    iNames.add(info.getName());
	}
	MinimumEdit updater = new MinimumEdit(lxNames, iNames);
	int lPrefLen = 0;
	for (int i = 0; i < lxParts.size(); i++) {
	    LxPdfPart lxPart = lxParts.get(i);
	    int len = lxNameLengths.get(i);
	    int lowPos = updater.map(lPrefLen);
	    int highPos = updater.map(lPrefLen + len);
	    Vector<ImagePlace> places = new Vector<ImagePlace>();
	    for (int j = lowPos; j < highPos; j++) {
		ImagePlacePdfPart iPart = iParts.get(j);
		ImageSign info = iPart.info;
		places.addAll(info.getPlaces());
	    }
	    combined.add(new AlignedLexPlaces(lxPart, places));
	    lPrefLen += len;
	}
	return combined;
    }
}
