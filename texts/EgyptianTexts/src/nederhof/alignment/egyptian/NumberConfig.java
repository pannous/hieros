// Configuration for numbers.

package nederhof.alignment.egyptian;

import java.util.*;

import nederhof.egyptian.trans.*;

public class NumberConfig {

    // Hieroglyphs.
    private List<String> hiero;

    // Accumulated value of numerals.
    private int val;

    // Last added numeral.
    private int last;

    // Default constructor.
    public NumberConfig() {
	this(new ArrayList<String>(), 0, Integer.MAX_VALUE);
    }
    // Explicit constructor.
    public NumberConfig(List<String> hiero, int val, int last) {
	this.hiero = new ArrayList<String>(hiero);
	this.val = val;
	this.last = last;
    }

    // Copy constructor.
    public NumberConfig(NumberConfig old) {
	this(old.hiero, old.val, old.last);
    }

    // Get signs as array.
    public String[] getHiero() {
	String[] ar = new String[hiero.size()];
	for (int i = 0; i < hiero.size(); i++)
	    ar[i] = hiero.get(i);
	return ar;
    }

    // Get value.
    public int getVal() {
	return val;
    }
    // Get value as transliteration.
    public TransLow getTransLow() {
	return new TransLow("" + val);
    }

    // Concatenate new numeral.
    // Return next configuration.
    // Return null if not possible.
    public NumberConfig apply(String[] hiero, int num) {
	if (num > last)
	    return null;
	else {
	    NumberConfig next = new NumberConfig(this);
	    for (int i = 0; i < hiero.length; i++)
		next.hiero.add(hiero[i]);
	    next.val += num;
	    next.last = num;
	    return next;
	}
    }
    public NumberConfig apply(String hiero, int num) {
	if (num > last)
	    return null;
	else {
	    NumberConfig next = new NumberConfig(this);
	    next.hiero.add(hiero);
	    next.val += num;
	    next.last = num;
	    return next;
	}
    }

    // For debugging.
    public String toString() {
	return "(" + val + ")";
    }

    // Testing.
    public static void main(String[] args) {
	NumberConfig config = new NumberConfig();
	System.out.println(config);
	config = config.apply(new String[]{"A1"}, 100);
	System.out.println(config);
	config = config.apply(new String[]{"A1"}, 10);
	System.out.println(config);
	config = config.apply(new String[]{"A1"}, 1000);
	System.out.println(config);
    }

}
