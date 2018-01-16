#!/usr/bin/env node
require('/me/dev/js/extensions.js')()
ls=x=>run('ls '+(x||''))
console.log("OK")
for(file of ls("*.textile")){
	// file="test.textile"
	if(!file)continue // why??
	console.log("OK")
	console.log(file)
	// out = file.replace(".textile",".html")
	out = file.replace(".textile",".md")
	// 
	html=read(file)
	// console.log(html)
	html=html.replace(/\[\[([^\]]+)\|(.+?)\]\]/g,"[$1]($2)")
	// html=html.replace(/\[([^\[]+?jpg)/g,"$1") why not?
	html=html.replace(/\[\[([^\]]+?)\]\]/g,"[$1]($1)")
	html=html.replace(/(.*)\[(.*?png)/g,"$1![$2")
	html=html.replace(/(.*)\[(.*?jpg)/g,"$1![$2")
	// html=html.replaceAll(/\[\[(.*?.[png])\]\]/,"<img src='$1'/>")
	// html=html.replaceAll(/\[\[(.*?)\|(.*?)\]\]/,"<a href='$2' type='text/html'>$1</a>")
	// html=html.replaceAll(/\[\[(.*?)\]\]/,"<a href='$1' type='text/html'>$1</a>")
	// html=html.replaceAll(".html.html",".html") // :(
	html=html.replaceAll("↔","⇔")
	// html=html.replaceAll("```","") // todo
	html=html.split("\n").join("  \n") // '  ' ~ <br/> !
	// html=html.split("\n").join("<br/>\n")
	console.log(html)
	write(out,html)
	// write("../docs/"+out,html)
	// exit()
}
