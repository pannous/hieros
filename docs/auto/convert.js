#!/usr/bin/env node  
require('/me/dev/js/extensions.js')()  
ls=x=>run('ls '+(x||''))  
console.log("OK")  
for(file of ls("*.md")){  
	if(!file)continue // why??  
	html0=read(file)  
	// console.log(html)  
	html=html0.replace(/\[\[([^\]]+)\|(.+?)\]\]/g,"[$1]($2)")  
	// html=html.replace(/\[([^\[]+?jpg)/g,"$1") why not?  
	html=html.replace(/\[\[([^\]]+?)\]\]/g,"[$1]($1)")  
	html=html.replaceAll("↔","⇔")  
	// html=html.replaceAll("```","") // todo  
	html=html.split("\n").join("  \n") // '  ' ~ <br/> !  
	// html=html.split("\n").join("<br/>\n")  
	// console.log(html)
	if(html!=html0)
		write(file,html)  
	// write("../docs/"+out,html)  
	// exit()  
}  
