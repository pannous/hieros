/***************************************************************************/
/*                                                                         */
/*  CharAux.java                                                           */
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

// Characters.

package nederhof.util;

public class CharAux {

    // Pairs of symbols to be replaced by a single character.
    public static final String[] specialMapping = new String[]
         { "!!", "\u00A1",
           "||", "\u00A6",
           "ss", "\u00A7",
           "co", "\u00A9",
           "_a", "\u00AA",
           "<<", "\u00AB",
           "no", "\u00AC",
           "ro", "\u00AE",
           "oo", "\u00B0",
           "+-", "\u00B1",
           "pp", "\u00B6",
           "_o", "\u00BA",
           ">>", "\u00BB",
           "14", "\u00BC",
           "12", "\u00BD",
           "34", "\u00BE",
           "??", "\u00BF",

           "`A", "\u00C0",
           "'A", "\u00C1",
           "^A", "\u00C2",
           "~A", "\u00C3",
           "\"A", "\u00C4",
           "oA", "\u00C5",
           "AE", "\u00C6",
           "cC", "\u00C7",
           "`E", "\u00C8",
           "'E", "\u00C9",
           "^E", "\u00CA",
           "\"E", "\u00CB",
           "`I", "\u00CC",
           "'I", "\u00CD",
           "^I", "\u00CE",
           "\"I", "\u00CF",
           "-D", "\u00D0",
           "~N", "\u00D1",
           "`O", "\u00D2",
           "'O", "\u00D3",
           "^O", "\u00D4",
           "~O", "\u00D5",
           "\"O", "\u00D6",
           "xx", "\u00D7",
           "/O", "\u00D8",
           "`U", "\u00D9",
           "'U", "\u00DA",
           "^U", "\u00DB",
           "\"U", "\u00DC",
           "'Y", "\u00DD",
           "TH", "\u00DE",
           "sz", "\u00DF",
           "`a", "\u00E0",
           "'a", "\u00E1",
           "^a", "\u00E2",
           "~a", "\u00E3",
           "\"a", "\u00E4",
           "oa", "\u00E5",
           "ae", "\u00E6",
           "cc", "\u00E7",
           "`e", "\u00E8",
           "'e", "\u00E9",
           "^e", "\u00EA",
           "\"e", "\u00EB",
           "`i", "\u00EC",
           "'i", "\u00ED",
           "^i", "\u00EE",
           "\"i", "\u00EF",
           "-d", "\u00F0",
           "~n", "\u00F1",
           "`o", "\u00F2",
           "'o", "\u00F3",
           "^o", "\u00F4",
           "~o", "\u00F5",
           "\"o", "\u00F6",
           "-:", "\u00F7",
           "/o", "\u00F8",
           "`u", "\u00F9",
           "'u", "\u00FA",
           "^u", "\u00FB",
           "\"u", "\u00FC",
           "'y", "\u00FD",
           "th", "\u00FE",
           "\"y", "\u00FF" };

    // Examples of pairs of symbols to be replaced by a single character.
    public static final String[] exampleSpecialMapping = new String[] {
           "sz", "\u00DF",
           "cc", "\u00E7",
           "`o", "\u00F2",
           "'o", "\u00F3",
           "^o", "\u00F4",
           "~o", "\u00F5",
           "\"o", "\u00F6" };

}
