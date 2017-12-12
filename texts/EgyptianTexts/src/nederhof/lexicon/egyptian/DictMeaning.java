package nederhof.lexicon.egyptian;

import java.util.*;

// Meaning in lemma.
public class DictMeaning {
    public String rank;
    public Vector<DictUse> uses;

    public DictMeaning(String rank, Vector<DictUse> uses) {
	this.rank = rank;
	this.uses = uses;
    }
    public DictMeaning() {
	this("", new Vector<DictUse>());
    }
    public boolean isEmpty() {
	return rank.equals("") && uses.isEmpty();
    }
}
