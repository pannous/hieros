package nederhof.interlinear.egyptian.ortho;

// Suggestion for orthographic annotation. Contains frequency information.

public class AnnotationSuggestion {

    public String fun;
    public String arg;
    public String val;
    public String lemma;
    public int count;
    // For a suggestion for a particular word, the index where it is located.
    public int index;

    public AnnotationSuggestion(String fun, String arg, String val, String lemma, int count) {
	this.fun = fun;
	this.arg = arg;
	this.val = val;
	this.lemma = lemma;
	this.count = count;
	this.index = -1; // value should never be used
    }

    public String toString() {
	return fun + " " + arg + " " + val + " " + lemma + " " + count;
    }

    public String toShortString() {
	return fun + " " + arg + "=" + val;
    }


    // Clone for index in particular word, at index.
    public AnnotationSuggestion clone(int index) {
	AnnotationSuggestion cl = new AnnotationSuggestion(fun, arg, val, lemma, count);
	cl.index = index;
	return cl;
    }

}
