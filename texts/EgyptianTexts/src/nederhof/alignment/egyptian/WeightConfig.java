/***************************************************************************/
/*                                                                         */
/*  WeightConfig.java                                                      */
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

// Weight plus configuration.

package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.util.*;

public class WeightConfig<Configuration extends Comparable> implements Comparable {

    private double weight;
    private Configuration config;

    public WeightConfig(double weight, Configuration config) {
	this.weight = weight;
	this.config = config;
    }

    public double getWeight() {
	return weight;
    }

    public Configuration getConfig() {
	return config;
    }

    public boolean equals(Object o) {
	if (o instanceof WeightConfig) {
	    WeightConfig other = (WeightConfig) o;
	    return compareTo(other) == 0;
	} else
	    return false;
    }

    // Smaller than any difference between weights.
    // If two weights differ by less, they are considered equal.
    private static final double epsilon = 0.0001;

    // Order first by weight, then by configuration.
    public int compareTo(Object o) {
	if (o instanceof WeightConfig) {
	    WeightConfig other = (WeightConfig) o;
	    if (Math.abs(weight - other.weight) < epsilon)
		return config.compareTo(other.config);
	    else if (weight < other.weight)
		return -1;
	    else 
		return 1;
	} else
	    return 1;
    }

    // For testing.
    public String toString() {
	return "weight=" + weight + " " + config;
    }

}
