package nederhof.lexicon.egyptian;

import java.util.*;

// Optional alternatives in dictionary.
public class DictOpt implements DictUsePart {
    public Vector<DictUse> uses;

    public DictOpt(Vector<DictUse> uses) {
	this.uses = uses;
    }
    public DictOpt() {
	this(new Vector<DictUse>());
    }
    public boolean isEmpty() {
        return uses.isEmpty();
    }
}
