/***************************************************************************/
/*                                                                         */
/*  LeafNode.java                                                          */
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

// In tree structure of corpus, a leaf node.

package nederhof.corpus;

public class LeafNode extends TreeNode {

    // Several leaf nodes with same label and key should not be
    // collapsed. To keep them apart, each obtains unique id.
    private static int globalId = 0;

    // Get unique id.
    private int id = globalId++;

    // Name is in hyperlink, linking to text, with material after.
    public String name;
    public Text text;
    public String post;

    // Construct.
    public LeafNode(String label, String key, String name, Text text, String post) {
	super(label, key);
	this.name = name;
	this.text = text;
	this.post = post;
    }

    // With identical label, key and name.
    public LeafNode(String label, Text text, String post) {
	this(label, "", label, text, post);
    }

    // Leaf node and internal node are different.
    public boolean equals(Object o) {
	if (o instanceof LeafNode) {
	    if (super.equals(o)) {
		LeafNode other = (LeafNode) o;
		return id == other.id;
	    } else
		return false;
	} else
	    return false;
    }

    // Leaf nodes precede other nodes.
    public int compareTo(Object o) {
	if (super.compareTo(o) != 0) 
	    return super.compareTo(o);
	else if (o instanceof LeafNode) {
	    LeafNode other = (LeafNode) o;
	    if (id < other.id)
		return -1;
	    else if (id > other.id)
		return 1;
	    else
		return 0;
	} else
	    return -1;
    }

}
