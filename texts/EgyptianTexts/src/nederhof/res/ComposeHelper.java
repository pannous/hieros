package nederhof.res;

import java.util.*;

import nederhof.res.*;
import nederhof.res.format.*;

// Helps with constructing RES expressions.
public class ComposeHelper {

    // Given two or more topgroups and ops and switches between
    // them, combine then into horgroup.
    public static ResHorgroup toHorgroup(Vector<ResTopgroup> groups, 
	    Vector<ResOp> ops, Vector<ResSwitch> switches, 
	    float size) {
        Vector<ResHorsubgroup> newGroups = new Vector<ResHorsubgroup>(4,3);
        Vector<ResOp> newOps = new Vector<ResOp>(3,3);
        Vector<ResSwitch> newSwitches = new Vector<ResSwitch>(3,3);
        for (int i = 0; i < groups.size(); i++) {
            ResTopgroup top = groups.get(i);
            if (top instanceof ResHorsubgroupPart) {
                ResHorsubgroupPart part = (ResHorsubgroupPart) top;
                ResHorsubgroup sub = new ResHorsubgroup(part);
                newGroups.add(sub);
            } else {
                ResHorgroup nested = (ResHorgroup) top;
                for (int j = 0; j < nested.nGroups(); j++) {
                    newGroups.add(nested.group(j));
                    if (j < nested.nOps()) {
                        newOps.add(nested.op(j));
                        newSwitches.add(nested.switchs(j));
                    }
                }
            }
            if (i < ops.size()) {
                newOps.add(ops.get(i));
                newSwitches.add(switches.get(i));
            }
        }
        return new ResHorgroup(newGroups, newOps, newSwitches, size);
    }
    public static ResVertgroup toVertgroup(Vector<ResTopgroup> groups, 
	    Vector<ResOp> ops, Vector<ResSwitch> switches, 
	    float size) {
        Vector<ResVertsubgroup> newGroups = new Vector<ResVertsubgroup>(4,3);
        Vector<ResOp> newOps = new Vector<ResOp>(3,3);
        Vector<ResSwitch> newSwitches = new Vector<ResSwitch>(3,3);
        for (int i = 0; i < groups.size(); i++) {
            ResTopgroup top = groups.get(i);
            if (top instanceof ResVertsubgroupPart) {
                ResVertsubgroupPart part = (ResVertsubgroupPart) top;
                ResVertsubgroup sub = new ResVertsubgroup(part);
                newGroups.add(sub);
            } else {
                ResVertgroup nested = (ResVertgroup) top;
                for (int j = 0; j < nested.nGroups(); j++) {
                    newGroups.add(nested.group(j));
                    if (j < nested.nOps()) {
                        newOps.add(nested.op(j));
                        newSwitches.add(nested.switchs(j));
                    }
                }
            }
            if (i < ops.size()) {
                newOps.add(ops.get(i));
                newSwitches.add(switches.get(i));
            }
        }
        return new ResVertgroup(newGroups, newOps, newSwitches, size);
    }

    // Given zero or more topgroups and ops and switches between
    // them, combine then into horgroup. Or to other topgroup if there
    // is only one topgroup. Or to special if there are none.
    public static ResTopgroup toHorgroupOrTopgroup(Vector<ResTopgroup> groups,
	                Vector<ResOp> ops, Vector<ResSwitch> switches, float size,
			ResTopgroup special) {
	if (groups.size() > 1)
	    return toHorgroup(groups, ops, switches, size);
	else if (groups.size() == 1)
	    return groups.get(0);
	else 
	    return special;
    }
    public static ResTopgroup toVertgroupOrTopgroup(Vector<ResTopgroup> groups,
	                Vector<ResOp> ops, Vector<ResSwitch> switches, float size,
			ResTopgroup special) {
	if (groups.size() > 1)
	    return toVertgroup(groups, ops, switches, size);
	else if (groups.size() == 1)
	    return groups.get(0);
	else 
	    return special;
    }

}
