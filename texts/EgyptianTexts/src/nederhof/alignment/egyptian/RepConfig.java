// Configuration for recognizing repeated
// signs or repeated groups of signs.

package nederhof.alignment.egyptian;

import java.util.*;

public class RepConfig {

    // How long is the repeated sequence?
    private int k;
    public int length() {
	return k;
    }

    // Length of prefix of next repetition.
    private int i;

    // How often has the sequence been repeated.
    private int n;
    public int times() {
	return n;
    }

    // The sequence.
    private String[] signs;

    // Constructor.
    public RepConfig(int k) {
	this.k = k;
	this.i = 0;
	this.n = 0;
	this.signs = new String[k];
    }

    // Copy constructor.
    public RepConfig(int k, int i, int n, String[] signs) {
	this.k = k;
	this.i = i;
	this.n = n;
	this.signs = new String[k];
        System.arraycopy(signs, 0, this.signs, 0, signs.length);
    }

    // Are we at start of possible repetition?
    public boolean isStart() {
	return n == 1 && i == 0;
    }

    // Can we finish repetition here for dual?
    public boolean isDual() {
	return n == 2 && i == 0;
    }
    // Can we finish repetition here for plural?
    // We include cases of say 4 or 9 repetitions.
    public boolean isPlural() {
	return n > 2 && i == 0;
    }

    // Can next sign be added to repetition?
    public boolean applicable(String sign) {
	return n == 0 || signs[i].equals(sign);
    }

    // Add next sign to repetition.
    private void applyDestructive(String sign) {
	if (n == 0 && i < k) 
	    signs[i] = sign;
	i++;
	if (i == k) {
	    n++;
	    i = 0;
	} 
    }

    // Add next sign to repetition.
    public RepConfig apply(String sign) {
	RepConfig copy = new RepConfig(k, i, n, signs);
	copy.applyDestructive(sign);
	return copy;
    }

    // For debugging.
    public String toString() {
        return "(" + k + "," + i + "," + n + ")";
    }

    // Testing.
    public static void main(String[] args) {
        RepConfig config = new RepConfig(2);
        System.out.println(config.applicable("A1"));
	config = config.apply("A1");
        System.out.println(config.isStart());
	config = config.apply("B1");
        System.out.println(config.isStart());
        System.out.println(config);
        System.out.println(config.isDual());
	config = config.apply("A1");
	config = config.apply("B1");
        System.out.println(config);
        System.out.println(config.isDual());
        System.out.println(config.isPlural());
	config = config.apply("A1");
	config = config.apply("B1");
        System.out.println(config);
        System.out.println(config.isDual());
        System.out.println(config.isPlural());
    }

}
