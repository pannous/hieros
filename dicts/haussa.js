#!/usr/bin/env node
require('/me/dev/js/extensions.js')()
print=x=>process.stdout.write(x)
// levenshtein=require('levenshtein')
// let {pronounce}=require('/me/dev/script/javascript/pronounce.js')
// file="dan-eng.dic"
file="haussa.dic"
lines=readlines(file)
i=0
for(var l of lines){
	i++
	// if(l.match(/^\s*\.\s*$/)||l.match(/^\s*$/))
	if(l.match(/\sv.t./))
		console.log(l)
	// else 
		// print(l)
	// [xeno,_,en]=l.split("\t")
	// ok=pronounce(hi,1)
	// if(!en)continue 
	// if(levenshtein(en,xeno)<=en.length/1.5)
		// console.log(xeno,':',en)
		// console.log("^^^^^^^^^^")
}