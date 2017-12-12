// Compute minimum edit distance.

package nederhof.alignment.generic;

import java.util.*;

public class MinimumEdit {

    // Minimum edit table.
    private short[][] dist;

    // Mapping from positions before to positions after.
    private int[] mapping;

    // First compute minimum edit distance.
    // Then trace back history.
    public MinimumEdit(Vector before, Vector after) {
	dist = new short[before.size() + 1][after.size() + 1];
	for (short i = 0; i <= before.size(); i++)
	    dist[i][0] = i;
	for (short j = 0; j <= after.size(); j++)
	    dist[0][j] = j;
	for (int j = 1; j <= after.size(); j++)
	    for (int i = 1; i <= before.size(); i++) {
		short d = distance(before.get(i-1), after.get(j-1));
		dist[i][j] = (short) Math.min(
			Math.min(dist[i-1][j] + 1, dist[i][j-1] + 1),
			dist[i-1][j-1] + d);
	    }
	mapping = new int[before.size()+1];
	traceBack(before, after);
    }

    // From minimum edit distance, trace back history.
    private void traceBack(Vector before, Vector after) {
	int i = before.size();
	int j = after.size();
	mapping[i] = j;
	while (i > 0 && j > 0) {
	    short d = distance(before.get(i-1), after.get(j-1));
	    if (dist[i][j] == dist[i-1][j-1] + d) {
		mapping[i-1] = j-1;
		i--;
		j--;
	    } else if (dist[i][j] == dist[i-1][j] + 1) {
		mapping[i-1] = j;
		i--;
	    } else
		j--;
	}
    }

    // Map position in old to position in new.
    // -1 if none.
    public int map(int i) {
	if (i >= mapping.length)
	    return -1;
	else
	    return mapping[i];
    }

    // Distance between two objects in input vectors.
    // Default 0 if equal and 1 if unequal.
    // Subclass may override.
    protected short distance(Object o1, Object o2) {
	if (o1.equals(o2))
	    return 0;
	else
	    return 1;
    }

    // For testing.
    public static void main(String[] args) {
	Vector v1 = new Vector();
	Vector v2 = new Vector();
	for (int i = 0; i < args[0].length(); i++)
	    v1.add(args[0].substring(i, i+1));
	for (int i = 0; i < args[1].length(); i++)
	    v2.add(args[1].substring(i, i+1));
	MinimumEdit updater = new MinimumEdit(v1, v2);
	for (int i = 0; i < args[0].length(); i++)
	    System.out.println(updater.map(i));
    }

}
