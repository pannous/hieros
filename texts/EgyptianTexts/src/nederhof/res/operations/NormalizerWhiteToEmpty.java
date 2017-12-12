package nederhof.res.operations;

import java.util.*;

import nederhof.res.*;

// Changing ResFragment by replacing white glyphs by
// empty.

public class NormalizerWhiteToEmpty extends ResNormalizer {

    protected ResTopgroup normalize(ResNamedglyph glyph) {
	glyph = (ResNamedglyph) super.normalize(glyph);
	if (glyph.color.isWhite()) {
	    float width = 1f;
	    float height = 1f;
	    Boolean shade = glyph.shade;
	    Vector<String> shades = glyph.shades;
	    boolean firm = true;
	    ResNote note = null;
	    ResSwitch switchs = glyph.switchs;
	    return new ResEmptyglyph(width, height, shade, shades, firm, note, switchs);
	} else
	    return glyph;
    }

}
