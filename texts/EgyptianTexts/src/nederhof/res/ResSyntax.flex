/***************************************************************************/
/*                                                                         */
/*  ResSyntax.flex                                                         */
/*                                                                         */
/*  Copyright (c) 2005 Mark-Jan Nederhof                                   */
/*                                                                         */
/*  This file is part of the implementation of AELalign, and may only be   */
/*  used, modified, and distributed under the terms of the                 */
/*  GNU General Public License (see doc/GPL.TXT).                          */
/*  By continuing to use, modify, or distribute this file you indicate     */
/*  that you have read the license and understand and accept it fully.     */
/*                                                                         */
/***************************************************************************/

// JFlex specification of RES.

package nederhof.res;

import java_cup.runtime.*;

%%

%class lexer
%unicode
%cup
%char
%line

%{
	private Symbol symbol(int type) {
            return new Symbol(type, yychar, yyline);
	}

	private Symbol symbol(int type, Object value) {
            return new Symbol(type, yychar, yyline, value);
	}
%}

category 	 = ([A-I]|[K-Z]|(Aa)|(NL)|(NU))
nat_num 	 = (0|([1-9]([0-9][0-9]?)?))
non_zero_nat_num = ([1-9]([0-9][0-9]?)?)
printing 	 = ([^\t\n\r\f\b\"\\]|(\\\")|(\\\\))

%%

"empty"			{ return symbol(sym.EMPTY); }
"stack"			{ return symbol(sym.STACK); }
"insert"		{ return symbol(sym.INSERT); }
"modify"		{ return symbol(sym.MODIFY); }

{category}{non_zero_nat_num}[a-z]? 	
			{ return symbol(sym.GLYPH_NAME, yytext()); }
[a-zA-Z]+ 		{ return symbol(sym.NAME, yytext()); }
\"{printing}+\"		{ return symbol(sym.STRING, yytext()); }
[0-9]?\.[0-9][0-9]?	{ return symbol(sym.REAL, yytext()); }
{nat_num} 		{ return symbol(sym.NAT_NUM, yytext()); }

"-"			{ return symbol(sym.MINUS); }
":"			{ return symbol(sym.COLON); }
"("			{ return symbol(sym.OPEN); }
")"			{ return symbol(sym.CLOSE); }
"*"			{ return symbol(sym.ASTERISK); }
"."			{ return symbol(sym.PERIOD); }
","			{ return symbol(sym.COMMA); }
"^"			{ return symbol(sym.CARET); }
"!"			{ return symbol(sym.EXCLAM); }
"["			{ return symbol(sym.SQ_OPEN); }
"]"			{ return symbol(sym.SQ_CLOSE); }
"="			{ return symbol(sym.EQUALS); }
[ \t\n\r\f]		{ return symbol(sym.WHITESPACE); }

/* error fallback */
.			{ return symbol(sym.ERROR); }

