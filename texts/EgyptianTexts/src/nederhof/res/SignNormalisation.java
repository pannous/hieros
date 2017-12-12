/***************************************************************************/
/*                                                                         */
/*  SignNormalisation.java                                                 */
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

package nederhof.res;

import java.util.*;

import nederhof.util.ArrayAux;

// Dealing with deprecated signs and sign names.
class SignNormalisation {

    // May be mnemonic.
    public static boolean maybeMnemonic(String name) {
        return name.matches("^[a-zA-Z]+$") || name.matches("^[0-9]+$");
    }

    // Normalize with rotation.
    // O37 --> O36[rotate=60]
    // etc.
    private static Map rotateNormal = null;

    public static Map rotatedMapping() {
	if (rotateNormal == null) { // Construct only once
	    String[] map = {
		"C2c", "C2[mirror]",
		// "O37", "O36[rotate=120]",
		// "T2", "T3[rotate=310]",
		"T15", "T14[rotate=330]",
		"U6", "U7[rotate=45]",
		"U6a", "U7[rotate=75]",
		"U6b", "U7[mirror,rotate=285]",
		"V31a", "V31[mirror]",
		"Y1a", "Y1[rotate=270]"};
	    rotateNormal = ArrayAux.arrayToMap(map);
	}
	return rotateNormal;
    }

    public static String rotatedMap(String name) {
	return (String) rotatedMapping().get(name);
    }

    // Normalize with repeated signs versus atomic signs.
    // N19 --> N18:[fix,sep=0.5]N18
    // etc.
    private static Map repeatNormal = null;

    public static Map repeatedMapping() {
        if (repeatNormal == null) { // Construct only once
	    String[] map = {
	        "D50a", "D50*[fix,sep=0.2]D50",
	        "D50b", "D50*[fix,sep=0.2]D50*[fix,sep=0.2]D50",
	        "D50c", "D50*[fix,sep=0.2]D50*[fix,sep=0.2]D50*[fix,sep=0.2]D50",
	        "D50d", "D50*[fix,sep=0.2]D50*[fix,sep=0.2]D50" +
			    ":[fix,sep=0.2]" +
			    ".*[sep=0]D50*[fix,sep=0.2]D50*[sep=0].",
	        "D50e", "D50*[fix,sep=0.2]D50*[fix,sep=0.2]D50" +
			    ":[fix,sep=0.2]" +
			    "D50*[fix,sep=0.2]D50*[fix,sep=0.2]D50",
	        "D50f", "D50*[fix,sep=0.2]D50*[fix,sep=0.2]D50*[fix,sep=0.2]D50" +
			    ":[fix,sep=0.2]" +
			    ".*[sep=0]D50*[fix,sep=0.2]D50*[fix,sep=0.2]D50*[sep=0].",
	        "D50g", "D50*[fix,sep=0.2]D50*[fix,sep=0.2]D50*[fix,sep=0.2]D50" +
			    ":[fix,sep=0.2]" +
			    "D50*[fix,sep=0.2]D50*[fix,sep=0.2]D50*[fix,sep=0.2]D50",
	        "D50h", "D50*[fix,sep=0.2]D50*[fix,sep=0.2]D50" +
			    ":[fix,sep=0.2]" +
			    "D50*[fix,sep=0.2]D50*[fix,sep=0.2]D50" +
			    ":[fix,sep=0.2]" +
			    "D50*[fix,sep=0.2]D50*[fix,sep=0.2]D50",
	        "D50i", "D50*[fix,sep=0.2]D50*[fix,sep=0.2]D50*[fix,sep=0.2]D50*[fix,sep=0.2]D50",

	        "D67a", "D67:[fix,sep=0.3]D67",
	        "D67b", "D67*[fix,sep=0.3]D67:[fit,fix,sep=0.3]D67",
	        "D67c", "D67*[fix,sep=0.3]D67*[fix,sep=0.3]D67:[fix,sep=0.3]D67",
	        "D67d", "D67*[fix,sep=0.3]D67*[fix,sep=0.3]D67" +
			    ":[fit,fix,sep=0.3]" +
			    ".*[sep=0]D67*[fix,sep=0.3]D67*[sep=0].",
	        "D67e", "D67*[fix,sep=0.3]D67*[fix,sep=0.3]D67" +
			    ":[fix,sep=0.3]" +
			    "D67*[fix,sep=0.3]D67*[fix,sep=0.3]D67",
	        "D67f", "D67*[fix,sep=0.3]D67*[fix,sep=0.3]D67*[fix,sep=0.3]D67" +
			    ":[fit,fix,sep=0.3]" +
			    ".*[sep=0]D67*[fix,sep=0.3]D67*[fix,sep=0.3]D67*[sep=0].",
	        "D67g", "D67*[fix,sep=0.3]D67*[fix,sep=0.3]D67*[fix,sep=0.3]D67" +
			    ":[fix,sep=0.3]" +
			    "D67*[fix,sep=0.3]D67*[fix,sep=0.3]D67*[fix,sep=0.3]D67",
	        "D67h", "D67*[fix,sep=0.3]D67*[fix,sep=0.3]D67" +
			    ":[fix,sep=0.3]" +
			    "D67*[fix,sep=0.3]D67*[fix,sep=0.3]D67" +
			    ":[fix,sep=0.3]" +
			    "D67*[fix,sep=0.3]D67*[fix,sep=0.3]D67",

	        "F51a", "F51*[fix,sep=0.2]F51*[fix,sep=0.2]F51",
	        "F51b", "F51:[fit,fix,sep=0.2]F51:[fit,fix,sep=0.2]F51",

	        "M12a", "M12*[fix,sep=0.3]M12",
	        "M12b", "M12*[fix,sep=0.3]M12*[fit,fix,sep=0.3]M12",
	        "M12c", "M12*[fix,sep=0.3]M12*[fix,sep=0.3]M12*[fix,sep=0.3]M12",
	        "M12d", "M12*[fix,sep=0.3]M12*[fix,sep=0.3]M12*[fit,fix,sep=0.3]M12*[fix,sep=0.3]M12",
	        "M12e", "M12*[fix,sep=0.3]M12*[fix,sep=0.3]M12" +
			    ":[fix,sep=0.3]" +
			    "M12*[fix,sep=0.3]M12*[fix,sep=0.3]M12",
	        "M12f", "M12*[fix,sep=0.3]M12*[fix,sep=0.3]M12*[fix,sep=0.3]M12" +
			    ":[fit,fix,sep=0.3]" +
			    ".*[sep=0]M12*[fix,sep=0.3]M12*[fix,sep=0.3]M12*[sep=0].",
	        "M12g", "M12*[fix,sep=0.3]M12*[fix,sep=0.3]M12*[fix,sep=0.3]M12" +
			    ":[fix,sep=0.3]" +
			    "M12*[fix,sep=0.3]M12*[fix,sep=0.3]M12*[fix,sep=0.3]M12",
	        "M12h", "M12*[fix,sep=0.3]M12*[fix,sep=0.3]M12" +
			    ":[fix,sep=0.3]" +
			    "M12*[fix,sep=0.3]M12*[fix,sep=0.3]M12" +
			    ":[fix,sep=0.3]" +
			    "M12*[fix,sep=0.3]M12*[fix,sep=0.3]M12",

	        "M17a", "M17*[fix,sep=0.2]M17",
	        "M22a", "M22*[sep=0.5]M22",
	        "N19", "N18:[fix,sep=0.5]N18",
	        "N35a", "N35:N35:N35",

	        "V1a", "V1*[fix,sep=0.1]V1",
	        "V1b", "V1*[fix,sep=0.1]V1*[fit,fix,sep=0.1]V1",
	        "V1c", "V1*[fix,sep=0.1]V1*[fix,sep=0.1]V1*[fix,sep=0.1]V1",
	        "V1d", "V1*[fix,sep=0.1]V1*[fix,sep=0.1]V1" +
			    ":[fix,sep=0.1]" +
			    ".*[sep=0]V1*[fix,sep=0.1]V1*[sep=0].",
	        "V1e", "V1*[fix,sep=0.1]V1*[fix,sep=0.1]V1" +
			    ":[fix,sep=0.1]" +
			    "V1*[fix,sep=0.1]V1*[fix,sep=0.1]V1",
	        "V1f", "V1*[fix,sep=0.1]V1*[fix,sep=0.1]V1*[fix,sep=0.1]V1" +
			    ":[fit,fix,sep=0.1]" +
			    ".*[sep=0]V1*[fix,sep=0.1]V1*[fix,sep=0.1]V1*[sep=0].",
	        "V1g", "V1*[fix,sep=0.1]V1*[fix,sep=0.1]V1*[fix,sep=0.1]V1" +
			    ":[fix,sep=0.1]" +
			    "V1*[fix,sep=0.1]V1*[fix,sep=0.1]V1*[fix,sep=0.1]V1",
	        "V1h", "V1*[fix,sep=0.1]V1*[fix,sep=0.1]V1*[fix,sep=0.1]V1*[fix,sep=0.1]V1" +
			    ":[fix,sep=0.1]" +
			    "V1*[fix,sep=0.1]V1*[fix,sep=0.1]V1*[fix,sep=0.1]V1",
	        "V1i", "V1*[fix,sep=0.1]V1*[fix,sep=0.1]V1*[fix,sep=0.1]V1*[fix,sep=0.1]V1",

	        "V20a", "V20:[fix,sep=0.3]V20",
	        "V20b", "V20*[fix,sep=0.3]V20:[fix,sep=0.3]V20",
	        "V20c", "V20*[fix,sep=0.3]V20:[fix,sep=0.3]V20*[fix,sep=0.3]V20",
	        "V20d", "V20*[fix,sep=0.3]V20*[fix,sep=0.3]V20" +
			    ":[fix,sep=0.3]" +
			    ".*[sep=0]V20*[fix,sep=0.3]V20*[sep=0].",
	        "V20e", "V20*[fix,sep=0.3]V20*[fix,sep=0.3]V20" +
			    ":[fix,sep=0.3]" +
			    "V20*[fix,sep=0.3]V20*[fix,sep=0.3]V20",
	        "V20f", "V20*[fix,sep=0.1]V20*[fix,sep=0.1]V20*[fix,sep=0.1]V20" +
			    ":[fix,sep=0.3]" +
			    "V20*[fix,sep=0.3]V20*[fix,sep=0.3]V20",
	        "V20g", "V20*[fix,sep=0.1]V20*[fix,sep=0.1]V20*[fix,sep=0.1]V20" +
			    ":[fix,sep=0.3]" +
			    "V20*[fix,sep=0.1]V20*[fix,sep=0.1]V20*[fix,sep=0.1]V20",
	        "V20h", "V20*[fix,sep=0.1]V20*[fix,sep=0.1]V20" +
			    ":[fix,sep=0.1]" +
			    "V20*[fix,sep=0.1]V20*[fix,sep=0.1]V20" +
			    ":[fix,sep=0.1]" +
			    "V20*[fix,sep=0.1]V20*[fix,sep=0.1]V20",
	        "V20i", "V20*[fix,sep=0.3]V20",
	        "V20j", "V20*[fix,sep=0.3]V20*[fix,sep=0.3]V20",
	        "V20k", "V20*[fix,sep=0.1]V20*[fix,sep=0.1]V20*[fix,sep=0.1]V20",
	        "V20l", "V20*[fix,sep=0.1]V20*[fix,sep=0.1]V20*[fix,sep=0.1]V20*[fix,sep=0.1]V20",

	        "V40a", "V40*[fix,sep=0.1]V40",
	        "W14a", "V28*W14:[sep=0.3,fit]O34",

	        "Z2", "Z1*Z1*Z1",
	        "Z2a", "Z1*[sep=0.3,fit]Z1*[sep=0.3,fit]Z1",
	        "Z2b", "N33*[sep=0.5,fit]N33*[sep=0.5,fit]N33",
	        "Z3", "Z1:Z1:Z1",
	        "Z3a", "Z1[rotate=90]:Z1[rotate=90]:Z1[rotate=90]",
	        "Z3b", "N33:N33:N33",
	        "Z4", "Z1*Z1",

	        "Z15a", "Z15*[fix,sep=0.3]Z15",
	        "Z15b", "Z15*[fix,sep=0.3]Z15*[fix,sep=0.3]Z15",
	        "Z15c", "Z15*[fix,sep=0.3]Z15*[fix,sep=0.3]Z15*[fix,sep=0.3]Z15",
	        "Z15d", "Z15*[fix,sep=0.3]Z15*[fix,sep=0.3]Z15" +
			    ":[fix,sep=0.3]" +
			    ".*[sep=0]Z15*[fix,sep=0.3]Z15*[sep=0].",
	        "Z15e", "Z15*[fix,sep=0.3]Z15*[fix,sep=0.3]Z15" +
			    ":[fix,sep=0.3]" +
			    "Z15*[fix,sep=0.3]Z15*[fix,sep=0.3]Z15",
	        "Z15f", "Z15*[fix,sep=0.3]Z15*[fix,sep=0.3]Z15*[fix,sep=0.3]Z15" +
			    ":[fix,sep=0.3]" +
			    ".*[sep=0]Z15*[fix,sep=0.3]Z15*[fix,sep=0.3]Z15*[sep=0].",
	        "Z15g", "Z15*[fix,sep=0.3]Z15*[fix,sep=0.3]Z15*[fix,sep=0.3]Z15" +
			    ":[fix,sep=0.3]" +
			    "Z15*[fix,sep=0.3]Z15*[fix,sep=0.3]Z15*[fix,sep=0.3]Z15",
	        "Z15h", "Z15*[fix,sep=0.3]Z15*[fix,sep=0.3]Z15" +
			    ":[fix,sep=0.3]" +
			    "Z15*[fix,sep=0.3]Z15*[fix,sep=0.3]Z15" +
			    ":[fix,sep=0.3]" +
			    "Z15*[fix,sep=0.3]Z15*[fix,sep=0.3]Z15",
	        "Z15i", "Z15*[fix,sep=0.3]Z15*[fix,sep=0.3]Z15*[fix,sep=0.3]Z15*[fix,sep=0.3]Z15",

	        "Z16a", "Z16:[fix,sep=0.3]Z16",
	        "Z16b", "Z16:[fix,sep=0.3]Z16:[fix,sep=0.3]Z16",
	        "Z16c", "Z16:[fix,sep=0.3]Z16:[fix,sep=0.3]Z16:[fix,sep=0.3]Z16",
	        "Z16d", "(Z16:[fix,sep=0.3]Z16:[fix,sep=0.3]Z16)" +
			    "*[fix,sep=0.3]" +
			    "(.:[sep=0]Z16:[fix,sep=0.3]Z16:[sep=0].)",
	        "Z16e", "(Z16:[fix,sep=0.3]Z16:[fix,sep=0.3]Z16)" +
			    "*[fix,sep=0.3]" +
			    "(Z16:[fix,sep=0.3]Z16:[fix,sep=0.3]Z16)",
	        "Z16f", "(Z16:[fix,sep=0.3]Z16:[fix,sep=0.3]Z16:[fix,sep=0.3]Z16)" +
			    "*[fix,sep=0.3]" +
			    "(.:[sep=0]Z16:[fix,sep=0.3]Z16:[fix,sep=0.3]Z16:[sep=0].)",
	        "Z16g", "(Z16:[fix,sep=0.3]Z16:[fix,sep=0.3]Z16:[fix,sep=0.3]Z16)" +
			    "*[fix,sep=0.3]" +
			    "(Z16:[fix,sep=0.3]Z16:[fix,sep=0.3]Z16:[fix,sep=0.3]Z16)",
	        "Z16h", "(Z16:[fix,sep=0.3]Z16:[fix,sep=0.3]Z16)" +
			    "*[fix,sep=0.3]" +
			    "(Z16:[fix,sep=0.3]Z16:[fix,sep=0.3]Z16)" +
			    "*[fix,sep=0.3]" +
			    "(Z16:[fix,sep=0.3]Z16:[fix,sep=0.3]Z16)"};
	    repeatNormal = ArrayAux.arrayToMap(map);
	}
	return repeatNormal;
    }

    public static String repeatedMap(String name) {
	return (String) repeatedMapping().get(name);
    }

    // Replace aberrant mnemonics from MdC97.
    // Includes verse points.
    // etc.
    private static Map mdc97MnemNormal = null;

    public static Map mdc97MnemomicMapping() {
	if (mdc97MnemNormal == null) { // Construct only once
	    String[] map = {
	        "R", "D26", // D153 is not in official list
	        "nDs", "G37", 
	        "1000", "M12", 
	        "nn", "M22*M22", 
	        "qnbt", "O38[mirror]", 
	        "nTrw", "R8*[sep=0,fix]R8*[sep=0,fix]R8", 
	        "K", "S7", // S56 is not in official list
	        "wa", "T21", 
	        "M", "Aa15", // Conflict between MdC97 and MdC88.
	        "o", "\"o\"[red]", // Also present in many MdC dialects.
	        "O", "\"o\"[black]"};
	    mdc97MnemNormal = ArrayAux.arrayToMap(map);
	}
	return mdc97MnemNormal;
    }

    public static String mdc97MnemomicMap(String name) {
	return (String) mdc97MnemomicMapping().get(name);
    }

    // Replace MdC numbers to RES.
    private static Map numberNormal = null;

    public static Map numberMapping() {
	if (numberNormal == null) { // Construct only once
	    String[] map = {
	        "1", "Z1",
	        "2", "Z1*Z1",
	        "3", "Z1*Z1*Z1",
	        "4", "Z1*Z1*Z1*Z1",
	        "5", ".*[sep=0]Z1*Z1*[sep=0].:Z1*Z1*Z1",
	        "20", "10*10",
	        "30", "10*10*10",
	        "40", "10*10*10*10",
	        "50", ".*[sep=0]10*10*[sep=0].:10*10*10",
	        "200", "100*100",
	        "300", "100*100*100",
	        "400", "100*100*100*100",
	        "500", ".*[sep=0]100*100*[sep=0].:100*100*100"};
	    numberNormal = ArrayAux.arrayToMap(map);
	}
	return numberNormal;
    }

    public static String numberMap(String name) {
	return (String) numberMapping().get(name);
    }

    // Replace EGPZ names by RES.
    private static Map egpzNormal = null;

    public static Map egpzMapping() {
	if (egpzNormal == null) { // Construct only once
	    String[] map = {
		"A6m", "A6a",
		"A6n", "A6b",
		"A42b", "A42a",
		"A43d", "A43a",
		"A45b", "A45a",
		"A469", "A65",
		"A239", "A66",
		"A282", "A67",
		"A199a", "A68",
		"A68", "A69",
		"A73", "A10",
		"B106", "B5a",
		"B47", "B9",
		"C2ar", "C2b",
		"C2r", "C2c",
		"C12r", "C13",
		"C14r", "C15",
		"C33", "C21",
		"C49b", "C22",
		"C165", "C23",
		"C185b", "C24",
		"D50b", "D50a",
		"D50c", "D50b",
		"D50d", "D50c",
		"D50e", "D50d",
		"D50f", "D50e",
		"D50g", "D50f",
		"D50h", "D50g",
		"D50i", "D50h",
		"D50j", "D50i",
		"S125", "D52a",
		"D271", "D64",
		"D132", "D65",
		"D417", "D67",
		"D417a", "D67a",
		"D417b", "D67b",
		"D417c", "D67c",
		"D417d", "D67d",
		"D417e", "D67e",
		"D417f", "D67f",
		"D417g", "D67g",
		"D417h", "D67h",
		"E100", "E9a",
		"E16c", "E16a",
		"E141", "E17a",
		"E20b", "E20a",
		"E34c", "E34a",
		"E35", "E36",
		"E45", "E37",
		"E92", "E38",
		"F1b", "F1a",
		"F13b", "F13a",
		"F31b", "F31a",
		"F37b", "F37a",
		"F38c", "F38a",
		"F51g", "F51a",
		"F51gv", "F51b",
		"F51a", "F51c",
		"F59", "F53",
		"G139", "G6a",
		"G247", "G45a",
		"I24", "I9a",
		"I31", "I10a",
		"K13", "K8",
		"L35", "L8",
		"M48", "M1b",
		"M3g", "M3a",
		"M72", "M10a",
		"M12c", "M12a",
		"M12d", "M12b",
		"M12e", "M12c",
		"M12f", "M12d",
		"M12g", "M12e",
		"M12h", "M12f",
		"M12i", "M12g",
		"M12j", "M12h",
		"M139", "M15a",
		"M139b", "M16a",
		"M24b", "M24a",
		"M140b", "M28a",
		"M31d", "M31a",
		"M40d", "M40a",
		"N102", "N18a",
		"N34b", "N34a",
		"N37b", "N37a",
		"O54", "O1a",
		"O5u", "O5a",
		"O86a", "O6a",
		"O85c", "O6b",
		"O85d", "O6c",
		"O85a", "O6d",
		"O85", "O6e",
		"O86", "O6f",
		"O18f", "O10c",
		"O19d", "O19a",
		"O20c", "O20a",
		"O24b", "O24a",
		"O190", "O25a",
		"O29v", "O29a",
		"O119a", "O36a",
		"O119b", "O36b",
		"O119c", "O36c",
		"O119d", "O36d",
		"P3b", "P3a",
		"R2c", "R2a",
		"R3p", "R3a",
		"R3pa", "R3b",
		"R10i", "R10a",
		"R16b", "R16a",
		"R133", "R27",
		"R129", "R28",
		"O196", "R29",
		"S48", "S2a",
		"S50", "S6a",
		"S14d", "S14b",
		"S130a", "S26a",
		"S130b", "S26b",
		"S56", "S46",
		"T43", "T3a",
		"T60", "T11a",
		"S126", "T32a",
		"T33b", "T33a",
		"T141", "T36",
		"U23f", "U23a",
		"U32d", "U32a",
		"U116", "U42",
		"V2c", "V2a",
		"V23c", "V23a",
		"V71", "V28a",
		"V90a", "V29a",
		"V20m", "V40",
		"V20n", "V40a",
		"W3b", "W3a",
		"W9d", "W9a",
		"W14b", "W14a", // Apparent error in EGPZ spec. of nov. 2007.
		"W17e", "W17a",
		"W18c", "W18a",
		"X4h", "X4b",
		"X6b", "X6a",
		"Y1v", "Y1a",
		"Z2d", "Z2a",
		"Z2e", "Z2b",
		"Z2a", "Z2c",
		"Z2b", "Z2d",
		"Z8a", "Z13",
		"Z40", "Z14",
		"Z41", "Z15",
		"Z41a", "Z15a",
		"Z41b", "Z15b",
		"Z41c", "Z15c",
		"Z41d", "Z15d",
		"Z41e", "Z15e",
		"Z41f", "Z15f",
		"Z41g", "Z15g",
		"Z41h", "Z15h",
		"Z41i", "Z15i",
		"Z42", "Z16",
		"Z42a", "Z16a",
		"Z42b", "Z16b",
		"Z42c", "Z16c",
		"Z42d", "Z16d",
		"Z42e", "Z16e",
		"Z42f", "Z16f",
		"Z42g", "Z16g",
		"Z42h", "Z16h"};
	    egpzNormal = ArrayAux.arrayToMap(map);
	}
	return egpzNormal;
    }

    public static String egpzMap(String name) {
	return (String) egpzMapping().get(name);
    }

    // Replace EGPZ ligatures by RES.
    private static Map egpzLigatureNormal = null;

    public static Map egpzLigatureMapping() {
	if (egpzLigatureNormal == null) { // Construct only once
	    String[] map = {
		// TODO BE COMPLETED
                "A14&Z2", "insert[bs,sep=0.5](A14,Z1*[sep=1.5]Z1*[sep=1.5]Z1)",
                "A17&Z2", "insert[be](A17*[sep=0.0]empty[width=0.2,height=0.0],Z1*Z1*Z1)",
                "A24&Z2d", "insert[te,sep=0.5](A24*[sep=0.0]empty[width=0.3],Z1*[sep=0.5]Z1*[sep=0.5]Z1)",
                "A51&X1", "insert[b,sep=0.2](A51,X1)",
                "D17&N5", ".",
                "D17&N5&X1", ".",
                "D17&X1", ".",
                "D17&X1&N5", ".",
                "D26&Z2", "insert[be,sep=0.3](D26,Z1*Z1*Z1)",
                "D28&D52", ".",
                "D28&F12", ".",
                "D36&D58", ".",
                "D36&Z1", ".",
                "D46&U1", ".",
                "D46&D58", ".",
                "D52&X1", ".",
                "D54&G43", ".",
                "E1&Z2d", ".",
                "E1&R12", ".",
                "E6&Z2d", ".",
                "E6&X1", ".",
                "E6&Z1", ".",
                "E6a&Z2d", ".",
                "E7&Z1", ".",
                "E7&Z2d", ".",
                "E10&Z1", ".",
                "E20&X1", ".",
                "E26&Z2d", ".",
                "E34&Q3", ".",
                "F4&N5", "insert[s,sep=0.7](F4,N5)",
                "F4&X1", "insert[s,sep=0.7](F4,X1)",
                "F20&D21", "insert[bs](F20,D21)",
                "F20&F16", "insert[bs](F20,F16)",
                "F20&O34", "insert[bs](F20,O34)",
                "F20&R8&U36&Z2a", ".",
                "F20&S19", "insert[bs](F20,S19)",
                "F20&T7", "insert[bs](F20,T7)",
                "F20&X1", "insert[bs](F20,X1)",
                "F20&X1&Z4", "insert[bs](F20,X1*[sep=0.5]Z4)",
                "F20&Z1", "insert[bs](F20,Z1)",
                "F20&Z1&F51", ".",
                "F20&Z1&Aa20&N18", ".",
                "F28&T11&X1&X1", ".",
                "F29&X1", ".",
                "F30&D46", ".",
                "F35&D28", ".",
                "F35&R8", ".",
                "F39&Z1", ".",
                "F39&Aa1", ".",
                "F44&N5", ".",
                "F44&N5&X1", ".",
                "G1&D21&O34", ".",
                "G1&D46", ".",
                "G1&D58", ".",
                "G1&I64", ".",
                "G1&M17a", ".",
                "G1&N5", ".",
                "G1&N29", ".",
                "G1&N35", ".",
                "G1&O34", ".",
                "G1&S3", ".",
                "G1&S56", ".",
                "G1&V31", ".",
                "G1&W24", ".",
                "G1&X1", ".",
                "G1&X1&W11", ".",
                "G1&Z1", ".",
                "G1&Z2d", ".",
                "G1&Z4", ".",
                "G1&Z7", ".",
                "G1&Aa1", ".",
                "G1&Aa2", ".",
                "G2&X1", ".",
                "G4&X1", ".",
                "G4&Z2d", ".",
                "G4a&Z2d", ".",
                "G5&H8", ".",
                "G5&N6", ".",
                "G5&O49", ".",
                "G5&S3", ".",
                "G5&Z1", ".",
                "G9&N19", ".",
                "G11&X1", ".",
                "G11&Z1", ".",
                "G14&X1", ".",
                "G14&Z2d", ".",
                "G15&X1", ".",
                "G17&D21", ".",
                "G17&D35", ".",
                "G17&D36", ".",
                "G17&D37", ".",
                "G17&D38", ".",
                "G17&F21", ".",
                "G17&F34", ".",
                "G17&F51", ".",
                "G17&I9", ".",
                "G17&N28", ".",
                "G17&O1", ".",
                "G17&O45", ".",
                "G17&V13", ".",
                "G17&X1", ".",
                "G17&Z2d", ".",
                "G17&Z4", ".",
                "G17&Z7", ".",
                "G17&Aa1", ".",
                "G18&Z4", ".",
                "G21&Z1", ".",
                "G23&N29", ".",
                "G25&X1", ".",
                "G25&Z1", ".",
                "G25&Z7", ".",
                "G25&Aa1", ".",
                "G26&X1&Z4", ".",
                "G28&X1", ".",
                "G29&N29", ".",
                "G29&R7", ".",
                "G29&S56", ".",
                "G29&V31", ".",
                "G29&X1", ".",
                "G29&Z1", ".",
                "G29&Z7", ".",
                "G30&Z1", ".",
                "G30&Z2d", ".",
                "G31&Z1", ".",
                "G35&N29", ".",
                "G35&Z1", ".",
                "G36&X1", ".",
                "G36&Z1", ".",
                "G38&Z2d", ".",
                "G39&N5", ".",
                "G39&N29", ".",
                "G39&X1", ".",
                "G39&X1&N5", ".",
                "G39&X1&Z4", ".",
                "G39&Z1", ".",
                "G39&Z1&X1", ".",
                "G39&Z2d", ".",
                "G43&N5", ".",
                "G43&N5&Z1", ".",
                "G43&N21", ".",
                "G43&N29", ".",
                "G43&N42", ".",
                "G43&O4", ".",
                "G43&V1", ".",
                "G43&X1", ".",
                "G43&Z2d", ".",
                "G43&Z4", ".",
                "G43&Aa1", ".",
                "G47&X1", ".",
                "G47&Z1", ".",
                "G53&Z1", ".",
                "G106&N6", ".",
                "G106&N6a", ".",
                "G106a&N6", ".",
                "G106a&N6a", ".",
                "H8&D58", ".",
                "H8&G5&N14", ".",
                "I10&D2", "insert[bs,sep=0.5](I10,D2)",
                "I10&D2&Z1", ".",
                "I10&D21&X1", ".",
                "I10&D21&Y1&Z5", ".",
                "I10&D21&Y1&Z5a", ".",
                "I10&D36", "insert[bs,sep=0.5](I10,D36)",
                "I10&D36&D21", ".",
                "I10&D36&D36", ".",
                "I10&D46", "insert[bs,sep=0.5](I10,D46)",
                "I10&D46&D46", ".",
                "I10&D46&I9", ".",
                "I10&D46&N35", ".",
                "I10&D46&X1", ".",
                "I10&D54", "insert[bs,sep=0.5](I10,D54)",
                "I10&D58", "insert[bs,sep=0.5](I10,D58)",
                "I10&D58&W24", ".",
                "I10&F21", "insert[bs,sep=0.5](I10,F21)",
                "I10&G1&I9", ".",
                "I10&G17", "insert[bs,sep=0.5](I10,G17)",
                "I10&G36", "insert[bs,sep=0.5](I10,G36)",
                "I10&G37", "insert[bs,sep=0.5](I10,G37)",
                "I10&G43", "insert[bs,sep=0.5](I10,G43)",
                "I10&I9", "insert[bs,sep=0.5](I10,I9)",
                "I10&I9&I9&I9", ".",
                "I10&I9&X1", ".",
                "I10&I10", "insert[bs,sep=0.5](I10,I10)",
                "I10&I10&X1", ".",
                "I10&M13", ".",
                "I10&M13a", ".",
                "I10&N5", "insert[bs](I10,N5)",
                "I10&N8", "insert[bs,sep=0.5](I10,N8)",
                "I10&N14", "insert[bs,sep=0.5](I10,N14)",
                "I10&N23&Z1", ".",
                "I10&N26", "insert[bs,sep=0.5](I10,N26)",
                "I10&N33", ".",
                "I10&N33&N33&N33", ".",
                "I10&N33&X1", ".",
                "I10&N33&Z2", ".",
                "I10&N35", "insert[bs,sep=0.5](I10,N35)",
                "I10&N35&I9", ".",
                "I10&N35&N35", ".",
                "I10&O26", "insert[b,sep=0.5](I10,O26)",
                "I10&O34", ".",
                "I10&O34&I9", ".",
                "I10&Q3", "insert[b,sep=0.5](I10,Q3)",
                "I10&R7", "insert[b](I10,R7)",
                "I10&S29", "insert[bs,sep=0.5](I10,S29)",
                "I10&S43", "insert[bs,sep=0.5](I10,S43)",
                "I10&S43&M17", ".",
                "I10&S43&M17&N35", ".",
                "I10&S43&S3", ".",
                "I10&S43&S43&S43", ".",
                "I10&S43&Z1", ".",
                "I10&T9", "insert[bs,sep=0.5](I10,T9)",
                "I10&V28", "insert[bs,sep=0.5](I10,V28)",
                "I10&V28&X1&Z4", ".",
                "I10&W24", "insert[b](I10,W24)",
                "I10&X1", "insert[b](I10,X1)",
                "I10&X1&D19", ".",
                "I10&X1&G43", ".",
                "I10&X1&N5", ".",
                "I10&X1&N16", ".",
                "I10&X1&N18", ".",
                "I10&X1&N33a", ".",
                "I10&X1&N35", ".",
                "I10&X1&O34", ".",
                "I10&X1&W3", ".",
                "I10&X1&X1", ".",
                "I10&X1&Z1", ".",
                "I10&X1&Z2", ".",
                "I10&X1&Z4", ".",
                "I10&X1&Z7", ".",
                "I10&Y1", "insert[bs,sep=0.5](I10,Y1)",
                "I10&Y24", "insert[b](I10,Y24)",
                "I10&Z1", "insert[b](I10,Z1)",
                "I10&Z4", "insert[bs](I10,Z4)",
                "I10&Z9", "insert[bs](I10,Z9)",
                "I11&X1", ".",
                "I64&Z2B", ".",
                "L2&X1", ".",
                "M22&O49&D58", ".",
                "M23&X1", ".",
                "M23&X1&T22", ".",
                "M27&X1&Z1", ".",
                "N5&G25", ".",
                "N5&T21", ".",
                "N8&G43", ".",
                "N8&I10", ".",
                "N21&G25&Z1", ".",
                "N28&D36", ".",
                "N28&G26", ".",
                "N29&A28", ".",
                "N29&D58", ".",
                "N29&G1", ".",
                "N29&R8", ".",
                "N29&U1", ".",
                "N29&U2", ".",
                "N37&U1", ".",
                "N37a&D58", ".",
                "N42&D58", ".",
                "N42&G17", ".",
                "N104&D40", ".",
                "O1&N14", ".",
                "O11&I9", ".",
                "O34&G39&X1", ".",
                "O42&D36", ".",
                "O44&X1&Z1", ".",
                "O49&G43", ".",
                "P4b&Z7", ".",
                "P4g&Z7", ".",
                "P5&Z2D&G43", ".",
                "P6&D36&N5&Z1", ".",
                "P6&V62", ".",
                "P8&I9", ".",
                "Q3&D36", ".",
                "Q3&G1", ".",
                "Q3&G43", ".",
                "R7&E10", ".",
                "R7&E11", ".",
                "R7&G29&Z1", ".",
                "R7&R8", ".",
                "R8&S2", ".",
                "R8&X1", ".",
                "R8&Z2a", ".",
                "S10&X1", ".",
                "S22&X1&X1", ".",
                "S29&T30", ".",
                "S29&X1", ".",
                "S29&X1&F29", ".",
                "S29&Z4", ".",
                "S34&U28&S29", ".",
                "S42&G5&S12a", ".",
                "S42&N23", ".",
                "T3b&V12", ".",
                "T14&Z4", ".",
                "T17&Z1", ".",
                "T17&Z2", ".",
                "T17&Z2d", ".",
                "T28&D36", ".",
                "T28&D58", ".",
                "T28&F4", ".",
                "T28&R8", ".",
                "U1&Aa11&D36", ".",
                "U1&Aa11&X1", ".",
                "U1&M17", ".",
                "U2&N29", "insert[s,sep=0.7](U2,N29)",
                "U5&D36", ".",
                "U9&M33", ".",
                "U9&Z2d", ".",
                "U10&Z2d", ".",
                "U19&W24", ".",
                "U21&N35&N5", ".",
                "U21&N5", ".",
                "U21&N5&Z1", ".",
                "U21&Q3&Y1", ".",
                "U22&R8", ".",
                "U36&R8", ".",
                "U36&X1&B1", ".",
                "V1&D40", ".",
                "V6&M14", ".",
                "V13&U1", ".",
                "V15&X1&X1", ".",
                "V22&D40", "insert[bs](V22:[sep=0.0]empty[width=0.0,height=0.2],D40)",
                "V22&D46", "insert[bs](V22:[sep=0.0]empty[width=0.0,height=0.2],D46)",
                "V22&V28&N35a", ".",
                "V22&W23&Z1", ".",
                "V22&X1&S42", ".",
                "V22&X1&Y1", ".",
                "V22&Y2", "insert[bs](V22,Y2)",
                "V23&D40", "insert[bs](V23:[sep=0.0]empty[width=0.0,height=0.2],D40)",
                "V23&F34", "insert[b](V23:[sep=0.0]empty[width=0.0,height=0.2],F34)",
                "V23&V28", "insert[b,sep=0.4](V23:[sep=0.0]empty[width=0.0,height=0.2],V28)",
                "V23&X1", "insert[b](V23,X1)",
                "V23&X1&&Z4", ".",
                "V23&Y2", "insert[bs](V23,Y2)",
                "V23a&X1&Z4a", ".",
                "V25&I10", ".",
                "V28&D36&D36", ".",
                "V28&T28&D58", ".",
                "V31&D58", ".",
                "W9&F4", ".",
                "W10&R8", ".",
                "W11&D58", ".",
                "W14&I9", ".",
                "W22&G43", ".",
                "W22&G43&X1", ".",
                "W24&G43", ".",
                "W24&G43&X1", ".",
                "X1&A51", ".",
                "X1&B1", ".",
                "X1&D17", ".",
                "X1&D33", ".",
                "X1&D40", ".",
                "X1&D58", ".",
                "X1&F4", ".",
                "X1&F29&X1", ".",
                "X1&G1", ".",
                "X1&G1&H8", ".",
                "X1&G1&Z7", ".",
                "X1&G4a", ".",
                "X1&G7", ".",
                "X1&G14", ".",
                "X1&G14&F51b", ".",
                "X1&G14&Z1", ".",
                "X1&G15", ".",
                "X1&G25&Aa1", ".",
                "X1&G28", ".",
                "X1&G39&X1&Z1&X1", ".",
                "X1&G39&Z1&X1", ".",
                "X1&G39&Z1&X1&X1", ".",
                "X1&G43&X1", ".",
                "X1&M17", ".",
                "X1&O289a", ".",
                "X1&R8", ".",
                "X1&R12", ".",
                "X1&U1", ".",
                "X1&Z4&G43", ".",
                "X1&Z6", ".",
                "X2&R8", ".",
                "Z1&D36", ".",
                "Z1&G43", ".",
                "Z1&R8", ".",
                "Z1&T31", ".",
                "Z2&G43", ".",
                "Z2&R8", ".",
                "Z2a&R8", ".",
                "Z3&R8", ".",
                "Z4&D40", ".",
                "Z4&G43", ".",
                "Z4&G43&Z4", ".",
                "Z7&G1", ".",
                "Z7&G1&Z4", ".",
                "Z7&M17", ".",
                "Z9&D40", ".",
                "Z9&G43&N21", ".",
                "Z10&G43", ".",
                "Aa1&D43", ".",
                "Aa1&G25", ".",
                "Aa1&G43", ".",
                "Aa1&X1&D58", ".",
                "Aa1&X1&D58&X1", ".",
                "Aa2&G43", ".",
                "Aa21&V12", "."};
	    egpzLigatureNormal = ArrayAux.arrayToMap(map);
	}
	return egpzLigatureNormal;
    }

}
