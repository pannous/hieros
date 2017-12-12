/***************************************************************************/
/*                                                                         */
/*  MdcSyntax.flex                                                         */
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

// JFlex specification of MDC-88 (Buurman et al., 1988).
// See http://www.catchpenny.org/codage/ for online MDC documentation (MDC-97).
// See also the MDC parser in JSesh by Rosmorduc.

package nederhof.res.mdc;

import java_cup.runtime.*;

%%

%class lexer
%unicode
%cup
%char
%line

%state PROPERTIES

%{
        private Symbol symbol(int type) {
            return new Symbol(type, yychar, yyline);
        }

        private Symbol symbol(int type, Object value) {
            return new Symbol(type, yychar, yyline, value);
        }
%}

integer		  = ([0-9]+)
space		  = ([ \t\n\r\f])

%%

<YYINITIAL>"-"
	{ return symbol(sym.MINUS); }

<YYINITIAL>"^"  
	{ return symbol(sym.CARET); }
<YYINITIAL>"?"  
	{ return symbol(sym.QUESTION); }
<YYINITIAL>"??" 
	{ return symbol(sym.QUESTIONQUESTION); }
<YYINITIAL>"|"[^-]*
	{ return symbol(sym.TEXTSUPER, yytext().substring(1)); }

<YYINITIAL>"!"("="{integer}"%")?
	{ /* See MDC-97; MDC-88 has only "!" */
	  return symbol(sym.EXCLAM);  }
<YYINITIAL>"!!"
	{ return symbol(sym.EXCLAMEXCLAM); }

<YYINITIAL>"+s" 
	{ return symbol(sym.HIERO); }
<YYINITIAL>"+t"(\\\+|[^+]|"+"[^a-z+])*
	{ return symbol(sym.ALPHA, yytext().substring(2)); }
<YYINITIAL>"+"[a-ru-z+](\\\+|[^+]|"+"[^a-z+])*  
	{ /* catch-all for ++, +l, +i, etc. */
	  return symbol(sym.EXTRA, yytext().substring(2)); }

<YYINITIAL>"{l"{integer},{integer}"}"   
	{ /* JSesh, ignored */ }
<YYINITIAL>"{L"{integer},{integer}"}"
	{ /* JSesh, ignored */ }
<YYINITIAL>"?"{integer}         
	{ /* JSesh, ignored */ }

<YYINITIAL>":"  
	{ return symbol(sym.COLON, ""); }
<YYINITIAL>"*"  
	{ return symbol(sym.ASTERISK, ""); }
<YYINITIAL>"("  
	{ return symbol(sym.OPEN); }
<YYINITIAL>")"  
	{ return symbol(sym.CLOSE); }

<YYINITIAL>"^^^"        
	{ /* undocumented, but seems to mean roughly :[fit] */ 
	  return symbol(sym.COLON, "fit"); }
<YYINITIAL>"&&&"  
	{ /* undocumented, but seems to mean roughly *[fit] */ 
	  return symbol(sym.ASTERISK, "fit"); }

<YYINITIAL>("#")       
	{ return symbol(sym.STACK); }
<YYINITIAL>("##")       
	{ /* JSesh only. */
	  return symbol(sym.STACK); }

<YYINITIAL>"&"  
	{ /* JSesh only. */ 
	  return symbol(sym.LIGATURE, yytext()); }
<YYINITIAL>"&&"  
	{ /* JSesh only. */ 
	  return symbol(sym.LIGATURE, yytext()); }

<YYINITIAL>"**" 
	{ /* undocumented */ 
	  return symbol(sym.LIGATURE, yytext()); }
<YYINITIAL>"^^" 
	{ /* undocumented */ 
	  return symbol(sym.LIGATURE, yytext()); }

<YYINITIAL>"{{" 
	{ /* This and all the below involving state PROPERTIES
	     is not part of MDC-88 nor MDC-97, but of JSesh. */
	  yybegin(PROPERTIES); }
<PROPERTIES>"}}" 
	{ yybegin(YYINITIAL); }
<YYINITIAL>"["  
	{ /* Undocumented */ 
	  yybegin(PROPERTIES); }
<PROPERTIES>"]" 
	{ yybegin(YYINITIAL); }

<PROPERTIES>"," 
	{ /* ignored */ }
<PROPERTIES>"=" 
	{ /* ignored */ }
<PROPERTIES>{integer} 
	{ /* ignored */ }
<PROPERTIES>[a-zA-Z_][a-zA-Z0-9_]* 
	{ /* ignored */ }
<PROPERTIES>[ \t\n\015] 
	{ /* ignored */ }

<YYINITIAL>(([A-Z]|"Aa")[0-9]+[A-Z]*|[a-zA-Z]+|[0-9]+)    
	{ /* Captures Gardiner codes, mnemonics, and numbers, 
	     and @ from JSesh. */
	  return symbol(sym.SIGN, yytext()); }

<YYINITIAL>"\\"
	{ return symbol(sym.MIRROR); }
<YYINITIAL>"\\r1" 
	{ /* MDC-97 only. */
	  return symbol(sym.ROTATE, "270"); }
<YYINITIAL>"\\r2" 
	{ /* MDC-97 only. */
	  return symbol(sym.ROTATE, "180"); }
<YYINITIAL>"\\r3" 
	{ /* MDC-97 only. */
	  return symbol(sym.ROTATE, "90"); }
<YYINITIAL>"\\t1" 
	{ /* MDC-97 only. */
	  return symbol(sym.MIRRORROTATE, "90"); }
<YYINITIAL>"\\t2" 
	{ /* MDC-97 only. */
	  return symbol(sym.MIRRORROTATE, "180"); }
<YYINITIAL>"\\t3" 
	{ /* MDC-97 only. */
	  return symbol(sym.MIRRORROTATE, "270"); }
<YYINITIAL>"\\"{integer}
	{ /* MDC-97 only. */
	  return symbol(sym.SCALE, yytext().substring(1)); }

<YYINITIAL>"\\R"-?{integer}
	{ /* JSesh */ 
	  return symbol(sym.ROTATE, yytext().substring(2)); }
<YYINITIAL>"\\red"       
	{ /* JSesh */ 
	  return symbol(sym.REDGLYPH); }
<YYINITIAL>"\\i"       
	{ /* JSesh */ 
	  return symbol(sym.GRAYGLYPH); }
<YYINITIAL>"\\l"       
	{ /* JSesh, ignored */ }
<YYINITIAL>"\\"[a-zA-Z][a-zA-Z]*[0-9]*     
	{ /* JSesh, ignored */ }

<YYINITIAL>"//" 
	{ return symbol(sym.WHOLESHADE); }
<YYINITIAL>"h/" 
	{ return symbol(sym.HORIZONTALSHADE); }
<YYINITIAL>"v/" 
	{ return symbol(sym.VERTICALSHADE); }
<YYINITIAL>"/"  
	{ return symbol(sym.QUARTERSHADE); }

<YYINITIAL>"."  
	{ return symbol(sym.QUARTERBLANK); }
<YYINITIAL>".." 
	{ return symbol(sym.WHOLEBLANK); }

<YYINITIAL>"<"[SFHsfh]?[bme]?[0123]?
	{  /* Generalised from MDC-88 and MDC-97. */
	   return symbol(sym.BEGINBOX, yytext().substring(1)); }
<YYINITIAL>[SFHsfh]?[0123]?">"  
	{ return symbol(sym.ENDBOX, yytext().substring(0, yytext().length()-1)); }

<YYINITIAL>"#"[1234]*
	{ /* MDC-97, not MDC-88 . */
	  return symbol(sym.QUADRATSHADE, yytext().substring(1)); }

<YYINITIAL>"$"  
	{ return symbol(sym.COLORTOGGLE); }
<YYINITIAL>"$r" 
	{ /* MDC-97 only. */
	  return symbol(sym.RED); }
<YYINITIAL>"$b" 
	{ /* MDC-97 only. */
	  return symbol(sym.BLACK); }

<YYINITIAL>"-#"
	{ return symbol(sym.SHADINGTOGGLE); }
<YYINITIAL>"-#b"
	{ /* MDC-97 only. */
	  return symbol(sym.SHADINGON); }
<YYINITIAL>"-#e"       
	{ /* MDC-97 only. */
	  return symbol(sym.SHADINGOFF); }

<YYINITIAL>"[&" 
	{ return symbol(sym.BEGINPHIL, "<"); }
<YYINITIAL>"&]" 
	{ return symbol(sym.ENDPHIL, ">"); }
<YYINITIAL>"[{" 
	{ return symbol(sym.BEGINPHIL, "{"); }
<YYINITIAL>"}]" 
	{ return symbol(sym.ENDPHIL, "}"); }
<YYINITIAL>"[[" 
	{ return symbol(sym.BEGINPHIL, "["); }
<YYINITIAL>"]]" 
	{ return symbol(sym.ENDPHIL, "]"); }
<YYINITIAL>"[\""        
	{ return symbol(sym.BEGINPHIL, "[|"); }
<YYINITIAL>"\"]"        
	{ return symbol(sym.ENDPHIL, "|]"); }
<YYINITIAL>"['" 
	{ return symbol(sym.BEGINPHIL, "'"); }
<YYINITIAL>"']" 
	{ return symbol(sym.ENDPHIL, "'"); }

<YYINITIAL>({space}|"_"|"=")
	{ /* grammar ignored */ }

.       
	{ /* catch-all */ }
