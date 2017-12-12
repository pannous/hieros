package nederhof.util.ngram;

import java.util.*;

import nederhof.alignment.egyptian.*;

// N-gram model of symbols S. Frequencies are in the first instance floating
// point numbers, not integers, as they may be gathered in the process of EM.
public class FloatNGram<S> {

	// N-grams.
    protected int N;

    // Model start and end of string.
    protected S start;
    protected S end;

    // Maps grams to frequencies. One for each n.
    protected ArrayList<HashMap<Gram,Double>> nFreq = new ArrayList<HashMap<Gram,Double>>();

    // Initialize.
    public FloatNGram(int N, S start, S end) {
        this.N = N;
        this.start = start;
        this.end = end;
        for (int i = 0; i <= N; i++)
            nFreq.add(new HashMap<Gram, Double>());
	}

    // Get N.
    public int getN() {
        return N;
    }

	// Add grams for list of symbols, occurring with frequency f.
	public void addGrams(List<S> syms, double f) {
		List<S> extended  = extend(syms, N);
		System.out.println("added ----------");
		for (int i = 0; i < extended.size(); i++)
			System.out.println(extended.get(i));
		Gram obj = new Gram(extended);
		for (int i = 0; i <= N; i++) {
			HashMap<Gram,Double> freq = nFreq.get(N);
			if (!freq.containsKey(obj))
				freq.put(obj, 0.0);
			freq.put(obj, freq.get(obj) + f);
			if (i > 0)
				obj = obj.after();
		}
	}

	// N-gram probability without smoothing.
	public double prob(S[] history, S sym) {
		S[] extended = extend(history, N-1);
		Gram obj = new Gram(extended);
		Gram prev = obj.prev();
		HashMap<Gram,Double> freq = nFreq.get(N);
		HashMap<Gram,Double> freqPrev = nFreq.get(N-1);
		double n = freq.containsKey(obj) ? freq.get(obj) : 0;
		double nPrev = freqPrev.containsKey(prev) ? freqPrev.get(prev) : 0;
		// System.out.println(" " + n + " " + nPrev);
		if (n == 0 || nPrev == 0)
			return 0;
		else
			return n / nPrev;
	}

	// N-gram probability of ending input in history.
	public double prob(S[] history) {
		return prob(history, end);
	}

	// Override frequencies to give uniform probability.
	// Frequencies are divided equally over symbols that could follow
	// history.
	public void estimateUniform() {
		HashMap<Gram,Double> freq = nFreq.get(N);
		HashMap<Gram,Double> freqPrev = nFreq.get(N-1);
		HashMap<Gram,Integer> nNext = new HashMap<Gram,Integer>();
		for (Map.Entry<Gram,Double> entry : freq.entrySet()) {
			Gram obj = entry.getKey();
			Gram prev = obj.prev();
			if (!nNext.containsKey(prev))	
				nNext.put(prev, 0);
			nNext.put(prev, nNext.get(prev) + 1);
		}
		HashMap<Gram,Double> uniform = new HashMap<Gram,Double>();
		for (Map.Entry<Gram,Double> entry : freq.entrySet()) {
			Gram obj = entry.getKey();
			Gram prev = obj.prev();
			int n = nNext.get(prev);
			uniform.put(obj, 1.0 / n);
		}
		nFreq.set(N, uniform);
		HashMap<Gram,Double> ones = new HashMap<Gram,Double>();
		for (Gram prev : nNext.keySet()) {
			ones.put(prev, 1.0);
		}
		nFreq.set(N-1, ones);
	}

    /**
     * Class to represent NGram object. An NGram object contains one or more
     * symbols.
     *
     */
    private class Gram {
        protected S[] syms;

        public Gram(S[] syms) {
            this.syms = syms;
        }
        public Gram(List<S> syms) {
			this(toArray(syms));
        }

        public S[] getSymbols() {
            return syms;
        }

        public int hashCode() {
            return Arrays.hashCode(syms);
        }

        // Last function is start.
        public boolean isStart() {
            return syms.length > 0 && syms[syms.length-1] == start;
        }
        // Last function is end.
        public boolean isEnd() {
            return syms.length > 0 && syms[syms.length-1] == end;
        }

        // Copy without last.
        public Gram prev() {
            S[] histFuns = (S[]) new Object[syms.length-1];
            for (int i = 0; i < syms.length-1; i++)
                histFuns[i] = syms[i];
            return new Gram(histFuns);
        }
        // Copy without first.
        public Gram after() {
            S[] afterFuns = (S[]) new Object[syms.length - 1];
            for (int i = 1; i < syms.length; i++)
                afterFuns[i - 1] = syms[i];
            return new Gram(afterFuns);
        }
        // Last.
        public S last() {
            return syms[syms.length - 1];
        }

        public boolean equals(Object obj) {
            return this == obj ||
                obj != null && obj instanceof FloatNGram.Gram &&
                Arrays.equals(syms, ((Gram) obj).syms);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < syms.length; i++) {
                sb.append("f" + (i + 1) + "=" + syms[i]);
                if (i+1 < syms.length)
                    sb.append(" ");
            }
            sb.append("]");
            return sb.toString();
        }
    }

	//////////////////////////////////////////////////////
	// Auxiliary.

	private S[] toArray(List<S> syms) {
		S[] ar = (S[]) new Object[syms.size()];
		for (int i = 0; i < syms.size(); i++) 
			ar[i] = syms.get(i);
		return ar;
	}

	// Extend to length n.
	private List<S> extend(List<S> syms, int n) {
		List<S> extended  = new LinkedList<S>(syms);
		while (extended.size() < n)
			extended.add(0, start);
		return extended;
	}
	private S[] extend(S[] syms, int n) {
		if (syms.length == n)
			return syms;
		S[] extended = (S[]) new Object[n];
		for (int i = 0; i < n - syms.length; i++)
			extended[i] = start;
		for (int i = 0; i < syms.length; i++)
			extended[i + n - syms.length] = syms[i];
		return extended;
	}

}

