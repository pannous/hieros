/***************************************************************************/
/*                                                                         */
/*  ResArg.java                                                            */
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

// An argument in RES.
// Can be:
// NAME '=' NAME
// NAME '=' NAT_NUM
// NAME '=' REAL
// NAME
// NAT_NUM
// REAL

package nederhof.res;

import java.text.*;
import java.util.*;

import nederhof.res.*;

// Auxiliary class for reading and writing RES arguments.
public class ResArg {

    private String lhs;
    private String rhs;

    // Left and right values of lexer, used for error reporting.
    public int left;
    public int right;

    // LHS '=' RHS
    public ResArg(String lhs, String rhs, int left, int right) {
	this.lhs = lhs;
	this.rhs = rhs;
	this.left = left;
	this.right = right;
    }

    // LHS only. RHS is null.
    public ResArg(String lhs, int left, int right) {
	this.lhs = lhs;
	this.rhs = null;
	this.left = left;
	this.right = right;
    }

    // As above, but with dummy values for left and right.
    public ResArg(String lhs, String rhs) {
	this(lhs, rhs, -1, -1);
    }

    // As above, but rhs is null.
    public ResArg(String lhs) {
	this(lhs, null);
    }

    // Tests on kinds of arg.

    public boolean is(String lhs) {
	return this.lhs.equals(lhs) && rhs == null;
    }

    public boolean isColor() {
	return rhs == null && Color16.isColor(lhs);
    }

    public boolean isPattern() {
	return lhs.matches("[tbse]+") && rhs == null;
    }

    public boolean hasLhs(String lhs) {
	return this.lhs.equals(lhs);
    }

    public boolean hasLhsNatnum() {
	return isNatNum(lhs);
    }

    public boolean hasLhsNonZeroNatnum() {
	return isNonZeroNatNum(lhs);
    }

    public boolean hasLhsReal() {
	return isReal(lhs);
    }

    public boolean hasLhsNonZeroReal() {
	return isNonZeroReal(lhs);
    }

    public boolean hasLhsLowReal() {
	return isLowReal(lhs);
    }

    public boolean hasRhs() {
	return rhs != null;
    }

    public boolean hasRhs(String rhs) {
	return rhs != null && this.rhs.equals(rhs);
    }

    public boolean hasRhsNatNum() {
	return rhs != null && isNatNum(rhs);
    }

    public boolean hasRhsNonZeroNatNum() {
	return rhs != null && isNonZeroNatNum(rhs);
    }

    public boolean hasRhsReal() {
	return rhs != null && isReal(rhs);
    }

    public boolean hasRhsNonZeroReal() {
	return rhs != null && isNonZeroReal(rhs);
    }

    public boolean hasRhsLowReal() {
	return rhs != null && isLowReal(rhs);
    }

    // Helpers.

    private static boolean isNatNum(String s) {
	return s.matches("([1-9][0-9]?)?[0-9]");
    }

    private static boolean isNonZeroNatNum(String s) {
	return s.matches("[1-9]([0-9][0-9]?)?");
    }

    private static boolean isReal(String s) {
	return s.matches("[0-9]") || s.matches("[0-9]?\\.[0-9][0-9]?");
    }

    private static boolean isNonZeroReal(String s) {
	return s.matches("[1-9](\\.[0-9][0-9]?)?") || 
	    s.matches("0?\\.([1-9][0-9]?|0[1-9])");
    }

    private static boolean isLowReal(String s) {
	return s.matches("0") || s.matches("0?\\.[0-9][0-9]?") ||
	    s.matches("1(\\.00?)?");
    }

    // Getting values. Methods below do not check well-formedness.
    // Apply above methods to verify this first.
    
    public String getLhs() {
	return lhs;
    }

    public int getLhsNatNum() {
	return Integer.parseInt(lhs);
    }

    public float getLhsReal() {
	return Float.parseFloat(lhs);
    }

    public int getRhsNatNum() {
	return Integer.parseInt(rhs);
    }

    public float getRhsReal() {
	return Float.parseFloat(rhs);
    }

    public String toString() {
	if (rhs == null)
	    return lhs;
	else
	    return lhs + "=" + rhs;
    }

    // Print list of arguments.
    public static String toString(Vector<String> args) {
	if (args.size() > 0) {
	    String s = "[" + args.get(0);
	    for (int i = 1; i < args.size(); i++)
		s += "," + args.get(i);
	    return s + "]";
	} else
	    return "";
    }

    // Format for small floats.
    private static DecimalFormatSymbols decimalSymbols = new DecimalFormatSymbols(Locale.UK);
    private static final NumberFormat nf = new DecimalFormat("0.0#", decimalSymbols);

    // Auxiliary for printing.
    public static String realString(float r) {
	return nf.format(r);
    }
}
