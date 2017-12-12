// Helper for clicks and keyboard events, connected to external viewers.

package nederhof.interlinear.frame;

import java.util.*;

import nederhof.interlinear.*;

abstract class ViewActionHelper {

    // Construct.
    public ViewActionHelper() {
    }

    // Record mouse click on position.
    public void recordClick(TierPos pos, Vector<TierPos> omittedTierPoss,
	    Vector<Integer> tierNums, Vector<TextResource> tierResources) {
	if (pos != null) {
	    Tier tier = pos.tier;
	    if (tier.id() < tierNums.size()) {
		Integer tierNum = tierNums.get(tier.id());
		TextResource resource = tierResources.get(tier.id());
		// TODO connect lexica and sign lists, etc.
	    }
	}
	for (TierPos omitPos : omittedTierPoss) {
	    Tier omitTier = omitPos.tier;
	    int p = omitPos.pos;
	    if (omitTier.id() < tierNums.size()) {
		Integer omitTierNum = tierNums.get(omitTier.id());
		TextResource omitRes = tierResources.get(omitTier.id());
		focusExternalViewer(omitRes, p);
	    }
	}
    }

    ///////////////////////////
    // Communication back to user.
    
    // Change focus of external viewer for resource to position.
    public abstract void focusExternalViewer(TextResource resource, int pos);
}

