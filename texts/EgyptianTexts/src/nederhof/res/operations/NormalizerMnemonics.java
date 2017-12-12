package nederhof.res.operations;

import java.util.*;

public class NormalizerMnemonics extends NormalizerFromFile {

    // At most one copy loaded.
    private static TreeMap<String,String> mapping = null;

    public NormalizerMnemonics() {
	super("data/ortho/mnemonics.xml");
    }

    protected TreeMap<String,String> getMapping() {
	return mapping;
    }
    protected void initializeMapping() {
    	mapping = new TreeMap<String,String>();
    }

}
