package nederhof.lexicon.egyptian;

import java.util.*;

// Hieroglyphic in dictionary.
public class DictHi implements DictUsePart {
    public String hi;
    public DictHi(String hi) {
	this.hi = hi;
    }
    public boolean isEmpty() {
	return hi.equals("");
    }
}
