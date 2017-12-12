/***************************************************************************/
/*                                                                         */
/*  TreeNode.java                                                          */
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

// Node in tree structure of corpus.

package nederhof.corpus;

public class TreeNode implements Comparable {

    // The label at the node.
    public String label;

    // Key for sorting, overrides label.
    public String key;

    // Sorting string, derived from key or from label.
    private String sort;

    public TreeNode(String label, String key) {
	this.label = label;
	this.key = key;
	if (key.matches("\\s*")) 
	    sort = getKey(label);
	else
	    sort = getKey(key);
    }

    public boolean equals(Object o) {
        if (o instanceof TreeNode) {
            TreeNode other = (TreeNode) o;
            return sort.equalsIgnoreCase(other.sort);
        } else
            return false;
    }

    public int compareTo(Object o) {
        if (o instanceof TreeNode) {
            TreeNode other = (TreeNode) o;
	    return sort.compareToIgnoreCase(other.sort);
        } else
            return -1;
    }

    // Get key for sorting. Try to obtain up to 3 numbers from string. Place
    // these first, padded with zeros, place actual string afterwards.
    private static String getKey(String str) {
	final int N = 3;
	int[] nums = new int[N];
	for (int i = 0; i < N; i++)
	    nums[i] = 0;

	int pos = 0;
	for (int i = 0; i < N; i++) {
	    int fromPos = skipNonDigits(str, pos);
	    int toPos = skipDigits(str, fromPos);
	    if (toPos > fromPos) {
		String sub = str.substring(fromPos, toPos);
		try {
		    nums[i] = Integer.parseInt(sub);
		} catch (NumberFormatException e) {
		    break;
		}
		pos = toPos;
	    } else 
		break;
	}

	String key = "";
	for (int i = 0; i < N; i++) {
	    if (nums[i] != 0) {
		String k = Integer.toString(nums[i]);
		key += padding(k);
	    }
	}
	key += str;
	return key;
    }

    // Skip zero or more nondigits. Also zeros, which should not start decimal number.
    private static int skipNonDigits(String str, int pos) {
	while (pos < str.length() && 
		(!Character.isDigit(str.charAt(pos)) || str.charAt(pos) == '0'))
	    pos++;
	return pos;
    }

    // Skip one or more digits.
    private static int skipDigits(String str, int pos) {
	while (pos < str.length() && Character.isDigit(str.charAt(pos)))
	    pos++;
	return pos;
    }

    // Do padding with zeros to make string (representing number)
    // long enough.
    private static String padding(String str) {
	final int paddingLength = 10;
	while (str.length() < paddingLength)
	    str = "0" + str;
	return str;
    }

}
