/***************************************************************************/
/*                                                                         */
/*  EuropeanLanguages.java                                                 */
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

package nederhof.corpus;

import java.util.*;

public class EuropeanLanguages {

    // Languages for translations, 2-letter codes, by ISO 639-1.
    private final static String[] languages2 = new String[]
        { "en", "de", "fr", "nl", "it", "es", "pt",
            "pl", "cs", "ro", "bg", "ca", "da", "sv", "no",
            "fi", "hu", "el" };

    // Vector of 3-letter code, by ISO 639-2/T.
    // Includes empty string.
    private final static Vector<String> languages3 = new Vector<String>();

    // Mapping from ISO 639-2/T codes to language names.
    private final static TreeMap<String,String> languageNames = 
	new TreeMap<String,String>();

    static {
        languages3.add("");
        languageNames.put("", "select");
        for (int i = 0; i < languages2.length; i++) {
            Locale locale = new Locale(languages2[i]);
            String alpha = locale.getISO3Language();
            String name = locale.getDisplayLanguage();
            languages3.add(alpha);
            languageNames.put(alpha, name);
        }
    }

    // Get European languages as 3-letter codes.
    public static Vector<String> getLanguages() {
        return (Vector<String>) languages3.clone();
    }

    // Mapping from ISO 639-2/T codes to language names.
    public static String getLanguageName(String alpha) {
	return languageNames.get(alpha);
    }

    // Mapping from 3-letter code to full name, if known, otherwise null.
    public static String getName(String code) {
        if (code == null || code.matches("\\s*"))
            return null;
        else
            return EuropeanLanguages.getLanguageName(code);
    }

}
