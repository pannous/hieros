#!/usr/bin/env node
require('/me/dev/js/extensions.js')()
levenshtein=require('levenshtein')
let {pronounce}=require('/me/dev/script/javascript/pronounce.js')
file="eng-rus.dic"
lines=readlines(file)
for(var l of lines){
	[en,_,xe]=l.split("\t")
	ok=pronounce(xe,1)
	if(!ok)continue 
	ok=ok.replaceAll("_","")
	ok=ok.replaceAll("~"," ")
	ok=ok.replaceAll("й","y")
	ok=ok.replaceAll("ф","th")
	easy=ok.replace("tskhy","")
	easy=ok.replace("skhy","")
	easy=ok.replace("tzh","z")
	if(levenshtein(en,easy)<=1+en.length/2)
		ok+=" ✓"//++" 
	else
		;//		ok+=" <<"
	console.log(l,ok)
		// console.log(l,ok,':',en)
		// console.log("^^^^^^^^^^")
}