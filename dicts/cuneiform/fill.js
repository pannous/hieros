#!/usr/bin/env node
x=require('/Users/me/uruk_egypt/scripts/cuneiformize.js')
require('/Users/me/dev/js/extensions.js')()


args=process.argv.slice(2)
// file = args[0] || "/Users/me/uruk_egypt/texts/sumerian/lament_of_ur.txt"
file = args[0] || "/Users/me/uruk_egypt/texts/sumerian/praise_of_shulgi.txt"

lines=read_lines(file)
// console.log("Processing file: "+file, ""+lines.length+" lines found.")

for(line0 of lines){
	line=line0.strip()
  // remove line numbers like 438.
  line=line.replaceAll(/^\d+\.\s+/,'')
	let cun=x.cuneiformize(line)
	cun=cun.replaceAll(/^\?/g,'')
	cun=cun.replaceAll(/\?$/g,'')
	console.log(line0)
	console.log(cun.strip())
  console.log()
}

