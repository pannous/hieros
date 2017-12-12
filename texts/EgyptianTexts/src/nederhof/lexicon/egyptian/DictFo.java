package nederhof.lexicon.egyptian;

import java.util.*;

// Form in dictionary.
public class DictFo implements DictUsePart {
    public String fo;
    public DictFo(String fo) {
	this.fo = fo;
    }
    public boolean isEmpty() {
        return fo.equals("");
    }
}
