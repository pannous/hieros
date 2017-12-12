package nederhof.util.eval;

import java.util.*;

// Computing scores from recall and precision. Objects in class O.
public class Evaluation<O extends Comparable<O>> {

    private LinkedList<Double> recalls = new LinkedList<Double>();
    private LinkedList<Double> precisions = new LinkedList<Double>();
    private LinkedList<Double> fscores = new LinkedList<Double>();

    public void addObservation(Set<O> retrieved, Set<O> relevant) { 
	Set<O> intersection = new TreeSet<O>(retrieved);
	intersection.retainAll(relevant);
	if (relevant.size() > 0 && retrieved.size() > 0) {
	    double recall = 1.0 * intersection.size() / relevant.size();
	    double precision = 1.0 * intersection.size() / retrieved.size();
	    recalls.add(recall);
	    precisions.add(precision);
	    fscores.add(fscore(recall, precision));
	}
    }

    private static double fscore(double recall, double precision) {
	if (precision + recall > 0)
	    return 2.0 * precision * recall / (precision + recall);
	else
	    return 0;
    }

    // Averages.
    public double recall() {
	return average(recalls);
    }
    public double precision() {
	return average(precisions);
    }
    public double fscore() {
	return average(fscores);
    }

    private double average(LinkedList<Double> list) {
	return list.size() > 0 ? sum(list) / list.size() : 0;
    }

    private double sum(LinkedList<Double> list) {
	double acc = 0;
	for (double elem : list)
	    acc += elem;
	return acc;
    }

}
