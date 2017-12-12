package nederhof.res.operations;

import java.util.*;

import nederhof.res.*;

// Changing ResFragment by removing all shading.

public class NormalizerRemoveShade extends ResNormalizer {

    protected Vector<String> normalizeShades(Vector<String> shades) {
	return new Vector<String>(1,1);
    }

    protected Boolean normalizeShade(Boolean shade) {
	return Boolean.FALSE;
    }

}
