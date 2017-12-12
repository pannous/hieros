/***************************************************************************/
/*                                                                         */
/*  ResFirstop.java                                                        */
/*                                                                         */
/*  Copyright (c) 2008 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

package nederhof.res;

import java.util.*;

public class ResFirstop extends ResOp {

    // Constructor from parser.
    public ResFirstop(Collection args, IParsingContext context) {
	super(context);
	for (Iterator iter = args.iterator(); iter.hasNext(); ) {
	    ResArg arg = (ResArg) iter.next();
	    if (!processOparg(arg)) {
		if (arg.hasLhs("size") && arg.hasRhsNonZeroReal())
		    size = arg.getRhsReal();
		else if (arg.hasLhs("size") && arg.hasRhs("inf"))
		    size = Float.MAX_VALUE;
		else 
		    context.reportError("Wrong first_op_arg", arg.left, arg.right);
	    }
	}
    }

}
