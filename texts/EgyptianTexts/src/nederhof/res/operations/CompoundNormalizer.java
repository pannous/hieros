package nederhof.res.operations;

import java.util.*;

import nederhof.res.*;

// Doing series of normalization steps.
public class CompoundNormalizer extends ResNormalizer {
    private Vector<ResNormalizer> normalizers = new Vector<ResNormalizer>();

    public ResFragment normalize(ResFragment res) {
	if (normalizers.isEmpty())
	    return super.normalize(res);
	else {
	    for (ResNormalizer normalizer : normalizers)
		res = normalizer.normalize(res);
	    return res;
	}
    }

    public void add(ResNormalizer normalizer) {
	normalizers.add(normalizer);
    }
}
