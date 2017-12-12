/***************************************************************************/
/*                                                                         */
/*  ResourceId.java                                                        */
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

// Combination of resource (path) and id of position in resource.

package nederhof.interlinear.labels;

import java.util.*;

public class ResourceId implements Comparable {

    public String resource;
    public String id;

    public ResourceId(String resource, String id) {
	this.resource = resource;
	this.id = id;
    }

    public int compareTo(Object o) {
	if (o instanceof ResourceId) {
	    ResourceId other = (ResourceId) o;
	    if (resource.compareTo(other.resource) != 0)
		return resource.compareTo(other.resource);
	    else
		return id.compareTo(other.id);
	} else
	    return 1;
    }

    // For debugging.
    public String toString() {
	return resource + "--->" + id;
    }

}
