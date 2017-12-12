/***************************************************************************/
/*                                                                         */
/*  Penalties.java                                                         */
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

// Penalties for formatting.

package nederhof.interlinear.egyptian;

public class Penalties {

    // Penalty that is veto.
    public static final double maxPenalty = 10000000;

    // Penalty for breaking after space.
    public static final double spacePenalty = 10;

    // Penalty for breaking at phrase boundary.
    public static final double phrasePenalty = 0;

    // Penalty of breaking up hieroglyhic.
    public static final double hierobreakPenalty = 0;

}
