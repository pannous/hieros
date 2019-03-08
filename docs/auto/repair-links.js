#!/usr/bin/env node
require('/me/dev/js/extensions.js')()

files=dir("*.md")
docs=files.map(x=>x.replace(".md",""))

for(f of files){
	if(empty(f)||isDir(f))continue 
	if(f=="t.md")continue 
		// f="t.md"
	// console.log(f)
	text=read(f)
	text=text.replace(/\[\[([^\]]+?)\]\]/g,"[$1]($1)")  // no textile links :(
	for(g of docs)
		text=text.replace(new RegEx("\\("+g+"\\)","gi"),"("+g+")")
	write(f,text)
	// console.log(text)
	// quit()
}

// broken-link-checker "http://pannous.github.io/hieros/Home" -ro --get |gv 400|gv "─OK─"