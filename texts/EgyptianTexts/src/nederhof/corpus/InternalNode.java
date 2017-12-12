/***************************************************************************/
/*                                                                         */
/*  InternalNode.java                                                      */
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

// In tree structure of corpus, an internal node.

package nederhof.corpus;

import java.util.*;

public class InternalNode extends TreeNode {

    // The children nodes.
    public TreeSet children;

    public InternalNode(String label, String key, TreeSet children) {
	super(label, key);
	this.children = children;
    }

    // Without children.
    public InternalNode(String label, String key) {
	this(label, key, new TreeSet());
    }

    // Leaf node and internal node are different.
    public boolean equals(Object o) {
        if (o instanceof InternalNode)
            return super.equals(o);
        else
            return false;
    }

    // Internal nodes follow other nodes.
    public int compareTo(Object o) {
	if (super.compareTo(o) != 0) 
	    return super.compareTo(o);
	else if (o instanceof InternalNode)
		return 0;
	else
	    return 1;
    }

}
