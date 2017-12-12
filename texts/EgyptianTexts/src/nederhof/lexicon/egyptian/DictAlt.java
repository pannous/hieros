package nederhof.lexicon.egyptian;

import java.util.*;

// Alternatives in dictionary.
public class DictAlt implements DictUsePart {
    public Vector<DictUse> uses;

    public DictAlt(Vector<DictUse> uses) {
	this.uses = uses;
    }
    public DictAlt() {
	this(new Vector<DictUse>());
    }
    public boolean isEmpty() {
        return uses.isEmpty();
    }
}
