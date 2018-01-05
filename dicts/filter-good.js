#!/usr/bin/env node
require('/me/dev/js/extensions.js')()
levenshtein=require('levenshtein')
// let {pronounce}=require('/me/dev/script/javascript/pronounce.js')
// file="dan-eng.dic"
file="nld-eng.dic"
lines=readlines(file)
for(var l of lines){
	[xeno,_,en]=l.split("\t")
	// ok=pronounce(hi,1)
	if(!en)continue 
	if(levenshtein(en,xeno)<=en.length/1.5)
		console.log(xeno,':',en)
		// console.log("^^^^^^^^^^")
}