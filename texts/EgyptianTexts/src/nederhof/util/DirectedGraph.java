/***************************************************************************/
/*                                                                         */
/*  DirectedGraph.java                                                     */
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

package nederhof.util;

import java.util.*;

public class DirectedGraph<V extends Comparable> {

    // Mapping from vertices to set of vertices, in direction of edges,
    // and reverse direction.
    // The vertices are required to be comparable.
    private TreeMap<V,TreeSet<V>> forwardEdge = new TreeMap<V,TreeSet<V>>();
    private TreeMap<V,TreeSet<V>> backwardEdge = new TreeMap<V,TreeSet<V>>();

    // Create empty graph.
    public DirectedGraph() {
    }

    // Add vertex.
    public void addVertex(V vertex) {
	ensureExistence(vertex);
    }

    // Add edge in both directions.
    public void addEdge(V fromVertex, V toVertex) {
	ensureExistence(fromVertex);
	ensureExistence(toVertex);
	TreeSet<V> toVertices = forwardEdge.get(fromVertex);
	toVertices.add(toVertex);
	TreeSet<V> fromVertices = backwardEdge.get(toVertex);
	fromVertices.add(fromVertex);
    }

    // Make sure that vertex is in data structure.
    private void ensureExistence(V vertex) {
	if (!forwardEdge.containsKey(vertex))
	    forwardEdge.put(vertex, new TreeSet<V>());
	if (!backwardEdge.containsKey(vertex))
	    backwardEdge.put(vertex, new TreeSet<V>());
    }

    ///////////////////////////////////////////////////////////
    // Strongly connected components, by Kosaraju's algorithm.

    // The output components are sets arranged in a vector.
    public Vector<TreeSet<V>> stronglyConnectedComponents() {
	TreeSet<V> visited = new TreeSet<V>();
	Vector<V> finish = new Vector();
	for (Iterator<V> it = forwardEdge.keySet().iterator(); it.hasNext(); ) {
	    V vertex = it.next();
	    search(vertex, visited, finish);
	}
	visited.clear();
	Vector<TreeSet<V>> components = new Vector<TreeSet<V>>();
	for (int i = finish.size()-1; i >= 0; i--) {
	    V vertex = finish.get(i);
	    if (!visited.contains(vertex)) {
		TreeSet<V> component = new TreeSet<V>();
		searchBack(vertex, visited, component);
		components.add(component);
	    }
	}
	return components;
    }

    // Depth first search, not entering again visited nodes.
    // Record finish order.
    private void search(V vertex, TreeSet<V> visited, Vector<V> finish) {
	if (!visited.contains(vertex)) {
	    visited.add(vertex);
	    TreeSet<V> toVertices = forwardEdge.get(vertex);
	    for (Iterator<V> it = toVertices.iterator(); it.hasNext(); ) {
		V next = it.next();
		search(next, visited, finish);
	    }
	    finish.add(vertex);
	}
    }

    // Depth first search in reverse direction, 
    // not entering again visited nodes. 
    // Put every visited node in component.
    private void searchBack(V vertex, TreeSet<V> visited, TreeSet<V> component) {
	if (!visited.contains(vertex)) {
	    visited.add(vertex);
	    component.add(vertex);
	    TreeSet<V> fromVertices = backwardEdge.get(vertex);
	    for (Iterator<V> it = fromVertices.iterator(); it.hasNext(); ) {
		V next = it.next();
		searchBack(next, visited, component);
	    }
	}
    }

    // Components that are minimal, i.e. there is no edge to another
    // component.
    public Vector<TreeSet<V>> minimalComponents() {
	Vector<TreeSet<V>> components = stronglyConnectedComponents();
	for (int i = components.size() - 1; i >= 0; i--) {
	    TreeSet<V> component = components.get(i);
	    boolean isMinimal = true;
	    for (Iterator<V> it = component.iterator(); it.hasNext(); ) {
		V vertex = it.next();
		TreeSet<V> toVertices = forwardEdge.get(vertex);
		for (Iterator<V> itTo = toVertices.iterator(); itTo.hasNext(); ) {
		    V toVertex = itTo.next();
		    if (!component.contains(toVertex)) {
			isMinimal = false;
			break;
		    }
		}
		if (!isMinimal)
		    break;
	    }
	    if (!isMinimal)
		components.removeElementAt(i);
	}
	return components;
    }

    // Testing.
    public static void main(String[] args) {
	DirectedGraph<String> g = new DirectedGraph<String>();
	g.addVertex("Z");
	g.addEdge("A", "B");
	g.addEdge("B", "D");
	g.addEdge("D", "A");
	g.addEdge("C", "A");
	g.addEdge("D", "C");
	g.addEdge("E", "B");
	g.addEdge("X", "Y");
	g.addEdge("Y", "X");
	g.addVertex("1");
	Vector<TreeSet<String>> scc = g.minimalComponents();
	for (int i = 0; i < scc.size(); i++) {
	    TreeSet<String> comp = scc.get(i);
	    System.out.println(comp);
	}
    }

}
