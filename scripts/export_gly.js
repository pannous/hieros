#!/usr/bin/env nodemon
// Manuel de Codage
// https://en.wikipedia.org/wiki/Manuel_de_Codage

// RES (Revised Encoding Scheme) 
// https://mjn.host.cs.st-andrews.ac.uk/egyptian/res/
require("./app/gardiner_map.js")
// require("./app/gardiner_map2.js")
require("./gardiner_map_manuel_de_codage.js")
require("./extensions.js")()

Gardiner_Map2={deprecated:1}

function print_Coda(argument) {
	for(a of Manuel_De_Codage_Map.keys()){
		g=Manuel_De_Codage_Map[a]
		m=Gardiner_Map[g]||Gardiner_Map2[g]
		console.log(g+"	"+m+"	"+a);
	}
}
// print_Coda()

// text=load("./EgyptianTexts/P_Harris_I_Discours aux dieux, Thebes.gly")
// text=load("./EgyptianTexts/AtenHymn.gly")
text=load("./EgyptianTexts/corpus/resources/ShipwreckedHi.xml")
// text=load("./EgyptianTexts/amenemope.hie")

text=text.replace(/\^+/g,"-")
text=text.replaceAll(":","-")
text=text.replaceAll("_","-")
text=text.replace(/=/,"")
text=text.replace(/\(/g,"")
text=text.replace(/\)/g,"")
text=text.replace(/#e/g,"")
text=text.replace(/\*/g,"-")
text=text.replace(/\&/g,"-")
text=text.replace(/\./g,"")
text=text.replace(/3\\90/g,"𓏼")
text=text.replace(/1\\r1/g,"1r")
text=text.replace(/Z2\\r1/g,"𓐆")
text=text.replace(/(-Z1){10}/g,"-𓎆")//𓐃/𓐃")
text=text.replace(/(-1){10}/g,"-𓎆")//𓐃/𓐃")
text=text.replace(/(-1){9}/g,"-𓐂")
text=text.replace(/(-1){8}/g,"-𓐁")
text=text.replace(/(-1){7}/g,"-𓐀")
text=text.replace(/(-1){6}/g,"-𓏿")
text=text.replace(/(-1){5}/g,"-𓏾")
text=text.replace(/Ff303-Ff302/g,"-𓏾")
text=text.replace(/(-1){4}/g,"-𓏽")
text=text.replace(/(-1){3}/g,"-𓏼")
text=text.replace(/(-1){2}/g,"-𓏻")
text=text.replace(/(-Z15){2}/g,"-𓏻")
// text=text.replace(/Z15/g,"-𓏺")

// text=text.replace(/(-1-){1}/g,"-𓏺-")
text=text.replace(/(1r-){9}/g,"𓐌-")
text=text.replace(/(1r-){8}/g,"𓐋-")
text=text.replace(/(1r-){7}/g,"𓐊-")
text=text.replace(/(1r-){6}/g,"𓐉-")
text=text.replace(/(1r-){5}/g,"𓐈-")
text=text.replace(/(1r-){4}/g,"𓐇-")
text=text.replace(/(1r-){3}/g,"𓐆-")
text=text.replace(/(1r-){2}/g,"𓐅-")

text=text.replace(/,/g,"-")
text=text.replace(/\$b/g,"")
text=text.replace(/\$r/g,"")
text=text.replace(/\#\d+/g,"#")
text=text.replace(/\{\{.*?\}\}/g,"#")
text=text.replace(/k\\/g,"𓎡")
text=text.replace(/O\\30/g,".")//?
// text=text.replace(/O\\30/g,"𓈒")//?

text=text.replace(/v\//g,"#")
text=text.replace(/\\l/g,"")
text=text.replace(/\+l/g,"...")
text=text.replace(/\+s/g,"...")

// text=text.replace(/(-1r-){1}/g,"-𓐄-")
// Z1XV	𓐃

// text=text.replace(/\d+\\r\d*/g,"")
// text=text.replace(/<\d+/g,"<")
// Hmt??
text=text.replace(/<b/g,"<")// blank
text=text.replace(/<0/g,"")//| open
text=text.replace(/<e/g,"<")//|
text=text.replace(/0>/g,"")
text=text.replace(/<1/g,"<") // normal
text=text.replace(/2>/g,">|") // normal
// text=text.replace(/<S/g,"|<")// inversed
text=text.replace(/<2/g,"|<")// inversed
text=text.replace(/1>/g,">")// inversed
// <h1 open hut h1> <h3 h3>
text=text.replace(/\!\n/g,"!-")
text=text.replace(/O\\10/g,".")
text=text.replace(/O\\50/g,".")
text=text.replace(/\\50/g,"")
text=text.replace(/-10\\r1/g,"-𓎭")
text=text.replace(/V10\\R90/g,"𓎆")// BIG! 𓎅
text=text.replace(/10\\70/g,"-𓎆-")// 𓎅
text=text.replace(/\\+t1/g,"")// 45` UPRIGHT 
text=text.replace(/\\+r1/g,"")// 45` UPRIGHT 
text=text.replace(/\\+r3/g,"")// 45` UPRIGHT 
text=text.replace(/\\+R45/g,"")// 45` UPRIGHT 
text=text.replace(/\\+R90/g,"")// UPRIGHT REST 
text=text.replace(/\\+R270/g,"")// UPRIGHT REST 
text=text.replace(/\\+R180/g,"")// UPRIGHT REST 
// text=text.replace(/\\+144/g,"")//
// text=text.replace(/\\+200/g,"")//
// text=text.replace(/\\+102/g,"")//
// text=text.replace(/\\+60/g,"")// 
// text=text.replace(/\\+70/g,"")// 
// text=text.replace(/\\+80/g,"")// 
text=text.replace(/\\+\d+/g,"")// 
text=text.replace(/\\\-/g,"-")// 


// text=text.toUpper()
GLY_Map={
'!':"\n",
'!\n':"\n",
'!!':"\n",
t:"𓏏",
T:"𓍿",// C
AXT:'𓈌',//#-
O:"●",// 🌑● ⚈⚫ ⢀ ․ ⋅ ᐧ ˙𝆺 𝅘 𝅕 𝅓	̇ ◍ vs 𓐍? ! ;)
ir:"𓁹",
Ff1:"𓏯",//?
M4:'𓆳',//?
m:"𓅓",
M:'𓐝',
'-':'',
D:"𓆓",
W:"𓍢",
w:"𓅱",
H:"𓎛",
Hm:"𓍛",
nn:"𓇒",// 𓇒 𓊹𓊹𓊹 nature QED <> 𓃃 𓂻 ?
x:"𓐍",
aH:'𓉗',
nTrw: "𓊹𓊹𓊹",// sign?
sn:"𓌢",
tA:"𓇾",
TA:"𓅷",//? G47B ?? looks different with open mouth
Axt:"𓈌",
Ff100:"𝅘",
Ff101:"↽",//harpoon barb
S:"𓈙",
	// '<':"𓍸",
	// '>':")", wrong way!
'#e':"",// ?
'a#3':"𓂝",//?
'$B':"",// black
'$R':"",// black
'$r':"",// black
'$b':"",// black
'#b':"#",// 
A:"𓄿"
}

text=text.replace(/#/g,"-  #")
for(line of text.split("\n")){
	if(line.startsWith("<") && !line.contains("/>"))
		continue

	if(line.startsWith("<"))
		line=line.replace(/.*id="(.*?)"\/>/,"$1-")
	fixed=""
	line=line.replaceAll("!","")
	line=line.replaceAll("insert","")
	line=line.replaceAll(/\[.*?\]/,"")
	chars= line.split("-")
	ok=""
	for(c of chars){
		c=c.trim()
		c2=c.toUpper()
		if(c.startsWith("++"))continue;
		c2=Manuel_De_Codage_Map[c]||c2
		ok+=GLY_Map[c]||Gardiner_Map[c2]||Gardiner_Map2[c2]||c //||">"+c+"<"
		ok+=" "
	}
	line=line.replaceAll("𓈖 𓈖 𓈖","𓈗")
	line=line.replaceAll("𓈖-𓈖-𓈖","𓈗")

	// console.log(line);
	console.log(ok);
}
