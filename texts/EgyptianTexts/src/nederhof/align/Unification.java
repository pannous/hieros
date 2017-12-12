/***************************************************************************/
/*                                                                         */
/*  Unification.java                                                       */
/*                                                                         */
/*  Copyright (c) 2006 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// Deals with unification of versions and positions.

package nederhof.align;

import java.util.*;

final class Unification {

    // Graph leading to representatives of unification classes of versions.
    private HashMap versionMap;
    // List of positions that have to be made equal later. This list always
    // contains an even number of positions.
    private LinkedList posEquals;
    // Graph leading to representatives of unification classes of positions.
    private HashMap posMap;
    // Hash mapping (representatives of) positions to frequencies.
    private IntHashMap posToFreq;
    // Same as above, but only for open tags with breakable content.
    private IntHashMap posOpenToFreq;

    // Constructor.
    public Unification() {
	versionMap = new HashMap();
	posEquals = new LinkedList();
	posMap = new HashMap();
	posToFreq = new IntHashMap();
	posOpenToFreq = new IntHashMap();
    }

    ///////////////////////////////////////////////////////////////
    // versions

    // Add equality of versions. Find representatives, and then unify.
    public void addEqualVersion(String n1, String s1, String n2, String s2) {
	Version v1 = versionRepr(new Version(n1, s1));
	Version v2 = versionRepr(new Version(n2, s2));
	if (!v1.equals(v2))
	    versionMap.put(v1, v2);
    }

    // Find representatives in unification classes of versions.
    private Version versionRepr(Version v) {
	while (versionMap.containsKey(v))
	    v = (Version) versionMap.get(v);
	return v;
    }

    ///////////////////////////////////////////////////////////////
    // positions

    // Add equality of positions to list. This list is processed later.
    public void addEqualPos(int f, String n1, String s1, String p1,
	    String n2, String s2, String p2) {
	Pos pos1 = new Pos(n1, s1, p1, f);
	Pos pos2 = new Pos(n2, s2, p2, f);
	posEquals.addLast(pos1);
	posEquals.addLast(pos2);
    }

    // Finish unification. This was postponed because we first needed 
    // to gather all unifications on versions.
    public void finish() {
	while (!posEquals.isEmpty()) {
	    Pos pos1 = (Pos) posEquals.removeFirst();
	    Pos pos2 = (Pos) posEquals.removeFirst();
	    pos1 = deref(pos1);
	    pos2 = deref(pos2);
	    if (!pos1.equals(pos2))
		posMap.put(pos1, pos2);
	}
    }

    public Pos deref(Pos pos) {
	return posRepr(posToReprVersion(pos));
    }

    // Replace version in position by representative.
    private Pos posToReprVersion(Pos pos) {
	if (pos.isPhrasal())
	    return pos;
	else {
	    String n = pos.getVersion();
	    String s = pos.getScheme();
	    String t = pos.getTag();
	    int f = pos.getFile();
	    Version v = versionRepr(new Version(n, s));
	    return new Pos(v.getName(), v.getScheme(), t, f);
	}
    }

    // Find representatives in unification classes of positions.
    private Pos posRepr(Pos pos) {
	while (posMap.containsKey(pos))
	    pos = (Pos) posMap.get(pos);
	return pos;
    }

    // Count occurrence of position.
    public void countPos(Point point) {
	if (!(point instanceof ClosePoint)) {
	    Pos pos = point.getPos();
	    pos = deref(pos);
	    posToFreq.incr(pos);
	}
    }

    // Count occurrence of position at open tag.
    public void countPosOpen(Point point) {
	if (!(point instanceof ClosePoint)) {
	    Pos pos = point.getPos();
	    pos = deref(pos);
	    posOpenToFreq.incr(pos);
	}
    }

    // Get number of occurrences of pos.
    public int freq(Pos pos) {
	return posToFreq.get(pos);
    }

    // Get number of occurrences of pos at open tag.
    public int freqOpen(Pos pos) {
	return posOpenToFreq.get(pos);
    }
}
