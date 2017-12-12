package nederhof.lexicon.egyptian;

import java.util.*;

// Translation in dictionary.
public class DictTr implements DictUsePart {
    public String tr;
    public DictTr(String tr) {
	this.tr = tr;
    }
    public boolean isEmpty() {
	return tr.equals("");
    }   
}
