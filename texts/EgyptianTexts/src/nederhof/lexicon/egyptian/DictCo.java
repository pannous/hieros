package nederhof.lexicon.egyptian;

import java.util.*;

// Comment in dictionary.
public class DictCo implements DictUsePart {
    public String co;
    public DictCo(String co) {
	this.co = co;
    }
    public boolean isEmpty() {
        return co.equals("");
    }
}
