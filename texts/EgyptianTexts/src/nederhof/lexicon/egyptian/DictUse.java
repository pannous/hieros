package nederhof.lexicon.egyptian;

import java.util.*;

// Use in dictionary.
public class DictUse {
    public Vector<DictUsePart> parts;

    public DictUse(Vector<DictUsePart> parts) {
	this.parts = parts;
    }
    public DictUse() {
	this(new Vector<DictUsePart>());
    }
    public boolean isEmpty() {
	return parts.isEmpty();
    }
}
