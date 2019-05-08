#!/usr/bin/env node
require('/me/dev/js/extensions.js')()
files=ls("*.md")
docs=files.map(x=>x.replace(".md",""))
// console.log(files)

for(f of files){
	if(empty(f)||isDir(f))continue 
	if(f=="t.md")continue 
		// f="t.md"
	console.log(f)
	text0=read_text(f)
	console.log(text)
	text=text0.replace(/\[\[([^\]]+?)\]\]/g,"[$1]($1)")  // no textile links :(
	for(g of docs)
		text0=text.replace(new RegEx("\\("+g+"\\)","gi"),"("+g+")")
	if(text!=text0)
		write(f,text)
	// console.log(text)
	// quit()
}

// broken-link-checker "http://pannous.github.io/hieros/Home" -ro --get |gv 400|gv "─OK─"