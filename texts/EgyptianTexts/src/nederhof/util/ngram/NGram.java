package nederhof.util.ngram;

import java.util.*;

import nederhof.alignment.egyptian.*;

// N-gram model of symbols S.
public class NGram<S> {

	// N-grams.
	protected int N;

	// Model start and end of string.
	protected S start;
	protected S end;

	// Maps grams to frequencies. One for each n.
	protected ArrayList<HashMap<Gram<S>,Integer>> nFreq = new ArrayList<HashMap<Gram<S>,Integer>>();
	// Maps grams to mapping from frequencies to frequencies. One for each n, excluding N.
	private ArrayList<Map<Gram<S>,TreeMap<Integer,Integer>>> nFreqFreq = 
				new ArrayList<Map<Gram<S>,TreeMap<Integer,Integer>>>();
	// Maps grams to mapping from frequency to probability. One for each n, excluding N.
	// Used in Simple Good-Turing.
	protected ArrayList<Map<Gram<S>, TreeMap<Integer, Double>>> nFreqProb = 
				new ArrayList<Map<Gram<S>, TreeMap<Integer, Double>>>();

	// Unigram, bigram, ... probabilities. One for each n, excluding 0.
	protected ArrayList<HashMap<Gram<S>,Double>> nGram = new ArrayList<HashMap<Gram<S>,Double>>();
	// Smoothed by Good-Turing.
	protected ArrayList<HashMap<Gram<S>,Double>> nGramGT = new ArrayList<HashMap<Gram<S>,Double>>();

	// Weighting in Katz backoff. One for each n, excluding 0 and 1.
	protected ArrayList<HashMap<Gram<S>, Double>> nAlpha = new ArrayList<HashMap<Gram<S>, Double>>();

	// Initialize.
	public NGram(int n, S start, S end) {
		N = n;
		this.start = start;
		this.end = end;
		for (int i = 0; i <= n; i++) 
			nFreq.add(new HashMap<Gram<S>, Integer>());
		for (int i = 0; i < n; i++) {
			nGram.add(new HashMap<Gram<S>, Double>());
			nGramGT.add(new HashMap<Gram<S>, Double>());
		}
	}

	// Get N.
	public int getN() {
		return N;
	}

	/**
	 * Add grams for list of symbols.
	 * Extend with start and end markers, and count grams.
	 * @param syms the list of symbols
	 */
	public void addGrams(List<S> syms) {
		ArrayList<S> extended = new ArrayList<S>();
		for (int i = 1; i < N; i++) 
			extended.add(start);
		extended.addAll(syms);
		extended.add(end);
		for (int n = 1; n <= N; n++)
			for (int i = Math.max(0,N-1-n); i+n <= extended.size(); i++)
				countGram(extended, i, n);
		// artificial 0-gram
		S[] empty = (S[]) new Object[0];
		addFreq(0, empty, syms.size()+1);
	}

	// Add unigram without start and end markers.
	public void addUnigram(S sym) {
		final S[] empty = (S[]) new Object[0];
		S[] single = (S[]) new Object[1];
		single[0] = sym;
		addFreq(0, empty, 1);
		addFreq(1, single, 1);
	}

	// Take sub-array from index of given length and store.
	private void countGram(List<S> syms, int fromIndex, int length) {
		S[] gram = (S[]) new Object[length];
		for (int i = 0; i < length; i++)
			gram[i] = syms.get(fromIndex + i);
		addFreq(length, gram, 1);
	}

	// Add to frequency table, certain number of times.
	private void addFreq(int n, S[] syms, int c) {
		HashMap<Gram<S>,Integer> freq = nFreq.get(n); 
		Gram<S> obj = new Gram<S>(syms);
		if (!freq.containsKey(obj))
			freq.put(obj, 0);
		freq.put(obj, freq.get(obj) + c);
	}

	/**
	 * Do the estimation.
	 */
	public void estimate() {
		for (int n = 1; n <= N; n++) {
			estimateNGram(n);
			computeFreqFreq(n);
			computeFreqProb(n);
			estimateNGramGT(n);
		}
		for (int n = 2; n <= N; n++) {
			computeAlpha(n);
		}
	}

	// N-gram probability of symbol given history.
	public double probGT(S[] history, S sym) {
		S[] syms = (S[]) new Object[history.length + 1];
		System.arraycopy(history, 0, syms, 0, history.length);
		syms[history.length] = sym;
		int n = syms.length;
		Gram<S> obj = new Gram<S>(syms);
		HashMap<Gram<S>,Double> gramsGT = nGramGT.get(n-1);
		double prob = gramsGT.containsKey(obj) ? gramsGT.get(obj) : 0;
		if (prob > 0)
			return prob;
		Gram<S> prev = obj.prev();
		HashMap<Gram<S>,Integer> prevFreq = nFreq.get(n-1);
		Map<Gram<S>,TreeMap<Integer,Integer>> ffreq = nFreqFreq.get(n-1);
		int nTokens = prevFreq.containsKey(prev) ? prevFreq.get(prev) : 0;
		TreeMap<Integer,Integer> counts = ffreq.get(prev);
		int n1 = counts != null && counts.containsKey(1) ? counts.get(1) : 0;
		if (nTokens > 0 && n1 > 0)
			// Hack:
			// return 1.0 * n1 / nTokens;
			return 0.001 * n1 / nTokens;
		else
			return 0;
	}
	// Unigram probability without history.
	public double probGT(S sym) {
		S[] history = (S[]) new Object[0];
		return probGT(history, sym);
	}
	// N-gram probability of ending input in history.
	public double probGT(S[] history) {
		return probGT(history, end);
	}

	// Simple Good-Turing.
	public double probSimpleGT(S[] history, S sym) {
		S[] syms = (S[]) new Object[history.length + 1];
		System.arraycopy(history, 0, syms, 0, history.length);
		syms[history.length] = sym;
		int n = syms.length;
		Gram<S> obj = new Gram<S>(syms);
		Gram<S> prev = obj.prev();
		HashMap<Gram<S>,Integer> freq = nFreq.get(n);
		Map<Gram<S>,TreeMap<Integer,Double>> fprob = nFreqProb.get(n-1);
		int count = freq.containsKey(obj) ? freq.get(obj) : 0;
		if (fprob.get(prev) != null && fprob.get(prev).get(count) == null) {
			System.err.println("STRANG");
			System.err.println(n);
			System.err.println(obj);
		}
		double prob = fprob.get(prev) != null ? fprob.get(prev).get(count) : 0;
		// Hack: 
		if (count == 0)
			prob *= 0.001;
		return prob;
	}
	// Unigram probability without history.
	public double probSimpleGT(S sym) {
		S[] history = (S[]) new Object[0];
		return probSimpleGT(history, sym);
	}
	// N-gram probability of ending input in history.
	public double probSimpleGT(S[] history) {
		return probSimpleGT(history, end);
	}

	// Katz backoff.
	public double probKatz(S[] history, S sym) {
		S[] syms = (S[]) new Object[history.length + 1];
		System.arraycopy(history, 0, syms, 0, history.length);
		syms[history.length] = sym;
		int n = syms.length;
		if (n <= 1)
			return probSimpleGT(history, sym);
		else {
			Gram<S> obj = new Gram<S>(syms);
			HashMap<Gram<S>,Integer> freq = nFreq.get(n);
			if (freq.containsKey(obj))
				return probSimpleGT(history, sym);
			else {
				Gram<S> prev = obj.prev();
				HashMap<Gram<S>,Double> alphas = nAlpha.get(n-2);
				double alpha = alphas.containsKey(prev) ? alphas.get(prev) : 1;
				return alpha * probKatz(prev.after().getSymbols(), sym);
			}
		}
	}
	// Unigram probability without history.
	public double probKatz(S sym) {
		S[] history = (S[]) new Object[0];
		return probKatz(history, sym);
	}
	// N-gram probability of ending input in history.
	public double probKatz(S[] history) {
		return probKatz(history, end);
	}

	/**
	 * Estimate N-gram probabilities without smoothing.
	 * @param n number of ngram
	 */
	private void estimateNGram(int n) {
		HashMap<Gram<S>,Integer> freq = nFreq.get(n);
		HashMap<Gram<S>,Integer> prevFreq = nFreq.get(n-1);
		HashMap<Gram<S>,Double> grams = nGram.get(n-1);
		for (Map.Entry<Gram<S>, Integer> entry : freq.entrySet()) {
			Gram<S> obj = entry.getKey();
			if (!obj.isStart()) {
				int count = entry.getValue();
				Gram<S> prev = obj.prev();
				int prevCount = prevFreq.get(prev);
				double estimate = 1.0 * count / prevCount;
				grams.put(obj, estimate);
			}
		}
	}

	// Compute frequencies of frequencies.
	private void computeFreqFreq(int n) {
		HashMap<Gram<S>,Integer> freq = nFreq.get(n);
		Map<Gram<S>,TreeMap<Integer,Integer>> ffreq = 
			new HashMap<Gram<S>,TreeMap<Integer,Integer>>();
		for (Map.Entry<Gram<S>, Integer> entry : freq.entrySet()) {
			Gram<S> obj = entry.getKey();
			if (!obj.isStart()) {
				int count = entry.getValue();
				Gram<S> prev = obj.prev();
				if (ffreq.get(prev) == null)
					ffreq.put(prev, new TreeMap<Integer,Integer>());
				if (ffreq.get(prev).get(count) == null)
					ffreq.get(prev).put(count, 0);
				ffreq.get(prev).put(count, ffreq.get(prev).get(count) + 1);
			}
		}
		nFreqFreq.add(ffreq);
	}

	// Compute probabilities of frequencies. (Simple Good-Turing.)
	private void computeFreqProb(int n) {
		Map<Gram<S>, TreeMap<Integer, Integer>> ffreq = nFreqFreq.get(n-1);
		Map<Gram<S>, TreeMap<Integer, Double>> fprob = 
			new HashMap<Gram<S>, TreeMap<Integer, Double>>();
		for (Map.Entry<Gram<S>, TreeMap<Integer, Integer>> entry : ffreq.entrySet()) {
			Gram<S> prev = entry.getKey();
			TreeMap<Integer, Integer> counts = entry.getValue();
			SimpleGT sgt = new SafeSimpleGT(counts);
			fprob.put(prev, sgt.getProbs());
		}
		nFreqProb.add(fprob);
	}

	/**
	 * Estimation using Good-Turing discount.
	 * @param n number of ngram
	 */
	private void estimateNGramGT(int n) {
		// threshold below which Good-Turing is applied
		final int k = 5;

		HashMap<Gram<S>,Integer> freq = nFreq.get(n);
		HashMap<Gram<S>,Integer> prevFreq = nFreq.get(n-1);
		Map<Gram<S>,TreeMap<Integer,Integer>> ffreq = nFreqFreq.get(n-1);
		HashMap<Gram<S>, Double> grams = nGram.get(n - 1);
		HashMap<Gram<S>, Double> gramsGT = nGramGT.get(n - 1);

		for (Map.Entry<Gram<S>, Integer> entry : freq.entrySet()) {
			Gram<S> obj = entry.getKey();
			if (!obj.isStart()) {
				Gram<S> prev = obj.prev();
				int c = freq.get(obj);
				int nTokens = prevFreq.get(prev);
				TreeMap<Integer,Integer> counts = ffreq.get(prev);

				// Eq. 4.31 from SLP p. 137 
				int n1 = counts.containsKey(1) ? counts.get(1) : 0;
				int nc = counts.get(c) != null ? counts.get(c) : 0;
				int ncplus1 = counts.containsKey(c + 1) ? counts.get(c + 1) : 0;
				int nkplus1 = counts.containsKey(k + 1) ? counts.get(k + 1) : 0;
				if (1 <= c && c <= k && nc != 0 && n1 != 0) {
					double num = ((c + 1) * ncplus1 * 1.0 / nc) - (c * (k + 1) * nkplus1 / n1);
					double den = 1 - ((k + 1) * nkplus1 * 1.0 / n1);
					if (den > 0 && num > 0) {
						double cStar = num / den;
						double prob = cStar / nTokens;
						gramsGT.put(obj, prob);
					} else
						gramsGT.put(obj, grams.get(obj));
				} else {
					gramsGT.put(obj, grams.get(obj));
				}
			}
		}
	}

	// P_katz( last | <previous N-1> ) 
	//   = P_GT( last | <previous N-1> ),  if C( <previous N-1> last ) > m 
	//   = alpha( <previous N-1> ) * P_katz( last | <previous N-2> ),  otherwise
	// To make sure this is consistent, we sum the P_GT in the first case,
	// and subtract it from 1. This is the left-over probability mass.
	// The alpha is then chosen to let the sum of the backed-off model give
	// that left-over mass.
	private void computeAlpha(int n) {
		HashMap<Gram<S>, Integer> freq = nFreq.get(n);
		HashMap<Gram<S>, Double> sumGT = new HashMap<Gram<S>, Double>();
		HashMap<Gram<S>, Double> sumKatz = new HashMap<Gram<S>, Double>();
		for (Map.Entry<Gram<S>, Integer> entry : freq.entrySet()) {
			Gram<S> obj = entry.getKey();
			if (!obj.isStart()) {
				Gram<S> prev = obj.prev();
				S last = obj.last();
				if (sumGT.get(prev) == null)
					sumGT.put(prev, 0.0);
				if (sumKatz.get(prev) == null)
					sumKatz.put(prev, 0.0);
				sumGT.put(prev, sumGT.get(prev) + probSimpleGT(prev.getSymbols(), last));
				sumKatz.put(prev, sumKatz.get(prev) + probKatz(prev.getSymbols(), last));
			}
		}
		HashMap<Gram<S>, Integer> prevFreq = nFreq.get(n-1);
		HashMap<Gram<S>, Double> alpha = new HashMap<Gram<S>, Double>();
		for (Map.Entry<Gram<S>, Integer> entry : prevFreq.entrySet()) {
			Gram<S> prev = entry.getKey();
			if (!prev.isEnd()) {
				double remainGT = 1 - sumGT.get(prev);
				double remainKatz = 1 - sumKatz.get(prev);
				double weight = remainKatz > 0 ? remainGT / remainKatz : 1;
				alpha.put(prev, weight);
			}
		}
		nAlpha.add(alpha);
	}

	/**
	 * Class to represent NGram object. An NGram object contains one or more
	 * symbols.
	 *
	 */
	private class Gram<S> {
		protected S[] syms;

		public Gram(S[] syms) {
			this.syms = syms;
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
		public Gram<S> prev() {
			S[] histFuns = (S[]) new Object[syms.length-1];
			for (int i = 0; i < syms.length-1; i++)
				histFuns[i] = syms[i];
			return new Gram<S>(histFuns);
		}
		// Copy without first.
		public Gram<S> after() {
			S[] afterFuns = (S[]) new Object[syms.length - 1];
			for (int i = 1; i < syms.length; i++)
				afterFuns[i - 1] = syms[i];
			return new Gram<S>(afterFuns);
		}
		// Last.
		public S last() {
			return syms[syms.length - 1];
		}

		public boolean equals(Object obj) {
			return this == obj ||
				obj != null && obj instanceof Gram &&
				Arrays.equals(syms, ((Gram<S>) obj).syms);
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

	////////////////////////////////////////////////////////
	// For debugging.

	/**
	 * Print all frequencies of occurrences (for n = 1, 2, ...)
	 */
	public void printFrequency() {
		for (int n = 0; n <= N; n++) {
			System.out.println("=========FREQ " + n + "=============");
			for (Map.Entry<Gram<S>, Integer> entry : nFreq.get(n).entrySet()) {
				Gram<S> ngram = entry.getKey();
				System.out.println(ngram + "\t" + entry.getValue());
			}
		}
	}

	public void printNGram() {
		printNGram(N);
	}
	/**
	 * Print the table of ngram probability for a specific n
	 * @param n number of ngram
	 */
	private void printNGram(int n) {
		HashMap<Gram<S>, Double> grams = nGram.get(n - 1);
		HashMap<Gram<S>, Double> gramsGT = nGramGT.get(n - 1);
		for (Map.Entry<Gram<S>, Double> entry : grams.entrySet()) {
			Gram<S> gram = entry.getKey();
			double prob = entry.getValue();
			double probGT = gramsGT.get(gram);
			S[] syms = gram.getSymbols();

			String sb = "(s" + n + ":" + syms[n - 1];

			if (n > 1) {
				sb += "|";
				for (int i = 0; i+1 < syms.length; i++) 
					sb += "s" + (i+1) + ":" + syms[i] + " ";
				sb = sb.substring(0, sb.length() - 1);
			}

			sb += ") --> " + prob + "\t" + probGT;
			System.out.println(sb);
		}
	}

	// Print frequencies of frequencies.
	public void printFreqFreq() {
		for (int i = 0; i < nFreqFreq.size(); i++) {
			System.out.println("==FREQFREQ " + i + "==");
			Map<Gram<S>, TreeMap<Integer, Integer>> nfreq = nFreqFreq.get(i);
			for (Map.Entry<Gram<S>, TreeMap<Integer, Integer>> nEntry : nfreq.entrySet()) {
				System.out.println("\t" + nEntry.getKey());
				TreeMap<Integer, Integer> probEntry = nEntry.getValue();
				for (Map.Entry<Integer, Integer> pEntry : probEntry.entrySet()) 
					System.out.println("\t\t" + pEntry.getKey() + " : " + pEntry.getValue());
				System.out.println();
			}
			System.out.println();
		}
	}

	// Print the linear regression probability.
	public void printRegProb() {
		for (int i = 0; i < nFreqProb.size(); i++) {
			System.out.println("==REGRESSION " + i + "==");
			Map<Gram<S>, TreeMap<Integer, Double>> reg = nFreqProb.get(i);
			for (Map.Entry<Gram<S>, TreeMap<Integer, Double>> nEntry : reg.entrySet()) {
				System.out.println("\t" + nEntry.getKey());
				TreeMap<Integer, Double> probEntry = nEntry.getValue();
				for (Map.Entry<Integer, Double> pEntry : probEntry.entrySet()) 
					System.out.println("\t\t" + pEntry.getKey() + " : " + pEntry.getValue());
				System.out.println();
			}
			System.out.println();
		}
	}

	// Print alpha.
	public void printAlpha() {
		for (int i = 0; i < nAlpha.size(); i++) {
			System.out.println("==ALPHA " + i + "==");
			Map<Gram<S>, Double> alphas = nAlpha.get(i);
			for (Map.Entry<Gram<S>, Double> ent : alphas.entrySet()) 
				System.out.println("\t" + ent.getKey() + "\t" + ent.getValue());
			System.out.println();
		}
	}

}

