#!/usr/bin/env node
x=require('/Users/me/uruk_egypt/scripts/cuneiformize.js')
require('/Users/me/dev/js/extensions.js')()


file="e.txt"
lines=read_lines(file)
for(line0 of lines){
	line=line0
	i=line.indexOf("wr.")
	if(i<0)continue
	line=line.slice(i+4)
	j=line.indexOf("\"")
	if(j>0)line=line.slice(0,j)
	let cun=x.cuneiformize(line)
	cun=cun.replaceAll(/^\?/g,'')
	cun=cun.replaceAll(/\?$/g,'')
	// console.log(line0)
	console.log(line0.replace("wr. "+line,cun+" "+line))
}

