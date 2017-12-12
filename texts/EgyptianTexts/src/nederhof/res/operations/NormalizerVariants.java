package nederhof.res.operations;

import java.util.*;

public class NormalizerVariants extends NormalizerFromFile {

    // At most one copy loaded.
    private static TreeMap<String,String> mapping = null;

    public NormalizerVariants() {
	super("data/ortho/variants.xml");
    }

    protected TreeMap<String,String> getMapping() {
        return mapping;
    }
    protected void initializeMapping() {
        mapping = new TreeMap<String,String>();
    }

}
