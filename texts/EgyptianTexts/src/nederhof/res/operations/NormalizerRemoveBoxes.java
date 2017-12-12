package nederhof.res.operations;

import java.util.*;

import nederhof.res.*;

// Changing ResFragment by removing named glyphs that represent parts of
// boxes.

public class NormalizerRemoveBoxes extends NormalizerRemoveFromFile {

    // At most one copy loaded.
    protected static TreeSet<String> nameSet = null;

    public NormalizerRemoveBoxes() {
	super("data/ortho/boxparts.xml");
    }
    
    protected TreeSet<String> getSet() {
	return nameSet;
    }
    protected void initializeSet() {
	nameSet = new TreeSet<String>();
    }

}
