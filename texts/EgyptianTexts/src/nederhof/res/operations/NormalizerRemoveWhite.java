package nederhof.res.operations;

import java.util.*;

import nederhof.res.*;

// Changing ResFragment by removing named glyphs that are white.

public class NormalizerRemoveWhite extends NormalizerRemoveSpecial {
    
    protected boolean isSpecial(ResNamedglyph glyph) {
	return glyph.color.isWhite();
    }

    protected ResTopgroup makeSpecial(ResSwitch after) {
	ResNamedglyph named = new ResNamedglyph("\"?\"", after);
	named.color = Color16.WHITE;
	return named;
    }
}
