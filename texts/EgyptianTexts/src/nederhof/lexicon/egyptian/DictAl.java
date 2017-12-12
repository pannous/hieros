package nederhof.lexicon.egyptian;

import java.util.*;

// Transliteration in dictionary.
public class DictAl implements DictUsePart {
    public String al;
    public DictAl(String al) {
	this.al = al;
    }
    public boolean isEmpty() {
	return al.equals("");
    }
}
