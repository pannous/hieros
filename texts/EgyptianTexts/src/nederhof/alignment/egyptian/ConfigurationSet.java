/***************************************************************************/
/*                                                                         */
/*  ConfigurationSet.java                                                  */
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

// A set of weighted configurations.
// Acts much as priority queue, except that there is bound on number
// of elements that can be added. This bound is beam size.

package nederhof.alignment.egyptian;

import java.util.*;

public class ConfigurationSet<Configuration extends Comparable> {

    // Beam width.
    private int beam;
    // Number of elements thereof removed.
    private int removed = 0;

    // Mapping from included configurations to weights.
    private TreeMap configurationToWeight = new TreeMap();

    // Set of pairs each consisting of weight and configuration.
    private TreeSet configurations = new TreeSet();

    // Create empty set of configurations, for beam search of
    // indicated size.
    public ConfigurationSet(int beam) {
	this.beam = beam;
    }

    // Add configuration and weight. Do this only if the weight is smaller
    // than for existing element.
    public void add(Configuration config, double weight) {
	if (configurationToWeight.get(config) == null) {
	    configurationToWeight.put(config, new Double(weight));
	    configurations.add(new WeightConfig<Configuration>(weight, config));
	    prune();
	} else {
	    Double oldWeight = (Double) configurationToWeight.get(config);
	    double oldw = oldWeight.intValue();
	    if (weight < oldw) {
		configurationToWeight.put(config, new Double(weight));
		configurations.remove(new WeightConfig<Configuration>(oldw, config));
		configurations.add(new WeightConfig<Configuration>(weight, config));
	    }
	}
    }

    // Remove first element, with lowest weight.
    public WeightConfig pop() {
	WeightConfig<Configuration> first = (WeightConfig<Configuration>) configurations.first();
	configurationToWeight.remove(first.getConfig());
	configurations.remove(first);
	removed++;
	return first;
    }

    // Yield element with lowest weight.
    public WeightConfig<Configuration> best() {
	return (WeightConfig<Configuration>) configurations.first();
    }

    // Contains no unpopped elements?
    public boolean noUntreated() {
	return configurations.isEmpty();
    }

    // While size beyond beam, remove element with highest weight.
    private void prune() {
	while (configurations.size() > beam - removed) {
	    WeightConfig<Configuration> last = (WeightConfig<Configuration>) configurations.last();
	    configurationToWeight.remove(last.getConfig());
	    configurations.remove(last);
	}
    }

}
