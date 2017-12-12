/***************************************************************************/
/*                                                                         */
/*  InequalitySolver.java                                                  */
/*                                                                         */
/*  Copyright (c) 2009 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of PhilologEG, and may only be */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Solving systems of inequalities in a robust way, so that
// solutions always exist.

package nederhof.util;

import java.util.*;

public class InequalitySolver<V extends Comparable> {

    // Mapping from variables to indices.
    private TreeMap<V,Integer> variableToIndex = new TreeMap<V,Integer>();

    // Number of variables.
    private int nVars = 0;

    // Encodes x_i <= x_j + value. 
    // The value is Float.MAX_VALUE to indicate there is no bound.
    private float[][] precedesPlus;

    // To allow for rounding-off errors.
    private static final float errMargin = 0.0001f;

    // Construct empty system.
    // Assign numbers of variables, and make vacuous inqualities.
    public InequalitySolver(TreeSet<V> variables) {
	for (Iterator<V> it = variables.iterator(); it.hasNext(); )
	    variableToIndex.put(it.next(), new Integer(nVars++));
	precedesPlus = new float[nVars][nVars];
	for (int i = 0; i < nVars; i++)
	    for (int j = 0; j < nVars; j++)
		if (i == j)
		    precedesPlus[i][j] = 0;
		else
		    precedesPlus[i][j] = Float.MAX_VALUE;
    }

    // Get index of variable.
    private int index(V var) {
	Integer index = variableToIndex.get(var);
	return index.intValue();
    }

    // Add inequality x_i <= x_j + val.
    public void addInequality(V var1, V var2, float val) {
	int i = index(var1);
	int j = index(var2);
	addInequality(i, j, val);
    }

    // Add inequality x_i <= x_j + val.
    // An update is needed if val is smaller than before
    // and the value isn't already pinned down by having
    // x_i <= x_j + v and x_j <= x_i - v.
    private void addInequality(int i, int j, float val) {
	double old = precedesPlus[i][j];
	double oldReverse = precedesPlus[j][i];
	if (val + errMargin < old && old + errMargin > -oldReverse) {
	    if (val < -oldReverse)
		val = (float) -oldReverse;
	    precedesPlus[i][j] = val;
	    propagate(i, j, val);
	}
    }

    // x_i <= x_j + v1 & x_j <= x_k + v2 => x_i <= x_k + v1 + v2
    // x_k <= x_i + v3 & x_i <= x_j + v1 => x_k <= x_j + v1 + v3
    private void propagate(int i, int j, float v1) {
	for (int k = 0; k < nVars; k++) {
	    float v2 = precedesPlus[j][k];
	    if (v2 < Float.MAX_VALUE)
		addInequality(i, k, v1 + v2);
	    float v3 = precedesPlus[k][i];
	    if (v3 < Float.MAX_VALUE)
		addInequality(k, j, v1 + v3);
	}
    }

    // Map constraints to smallest solution, 
    // given minimum values for each of the variables.
    public TreeMap<V,Float> smallestSolution(TreeMap<V,Float> minimaMap) {
	float[] minima = new float[nVars];
	float[] solution = new float[nVars];
	for (Iterator<V> it = variableToIndex.keySet().iterator();
		it.hasNext(); ) {
	    V var = it.next();
	    Float minimum = minimaMap.get(var);
	    int i = index(var);
	    minima[i] = minimum.floatValue();
	    solution[i] = minimum.floatValue();
	}
	for (int i = 0; i < nVars; i++) 
	    for (int j = 0; j < nVars; j++) {
		// var1 <= var2 + v  =>  var2 >= var1 - v
		double diff = precedesPlus[i][j];
		solution[j] = (float) Math.max(solution[j], minima[i] - diff);
	    }
	TreeMap<V,Float> solutionMap = new TreeMap<V,Float>();
	for (Iterator<V> it = variableToIndex.keySet().iterator();
		it.hasNext(); ) {
	    V var = it.next();
	    int i = index(var);
	    solutionMap.put(var, new Float(solution[i]));
	}
	return solutionMap;
    }

    // For testing.
    public void print() {
	for (Iterator<V> it1 = variableToIndex.keySet().iterator(); 
		it1.hasNext(); )  {
	    V var1 = it1.next();
	    for (Iterator<V> it2 = variableToIndex.keySet().iterator(); 
		    it2.hasNext(); ) {
		V var2 = it2.next();
		int i = index(var1);
		int j = index(var2);
		float val = precedesPlus[i][j];
		if (val < Float.MAX_VALUE)
		    System.out.println("" + var1 + " <= " + var2 + " + " + val);
	    }
	}
    }

    // Testing.
    public static void main(String[] args) {
	TreeSet<String> set = new TreeSet<String>();
	set.add("a");
	set.add("b");
	set.add("c");
	set.add("d");
	set.add("e");
	InequalitySolver<String> solver = new InequalitySolver<String>(set);
	solver.addInequality("a", "b", -5);
	solver.addInequality("b", "c", -3);
	solver.addInequality("c", "d", -1);
	solver.addInequality("d", "b", 5);
	solver.addInequality("d", "a", 11);
	solver.addInequality("e", "a", -100);
	solver.print();

	TreeMap<String,Float> minima = new TreeMap<String,Float>();
	minima.put("a", new Float(0));
	minima.put("b", new Float(8));
	minima.put("c", new Float(8));
	minima.put("d", new Float(9));
	minima.put("e", new Float(10));
	TreeMap<String,Float> solution = solver.smallestSolution(minima);
	System.out.println("a " + solution.get("a"));
	System.out.println("b " + solution.get("b"));
	System.out.println("c " + solution.get("c"));
	System.out.println("d " + solution.get("d"));
	System.out.println("e " + solution.get("e"));
    }

}
