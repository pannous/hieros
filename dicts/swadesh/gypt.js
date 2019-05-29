#!/usr/bin/env node
require('/me/dev/js/extensions.js')()
last={}
for(line of read(
	if(!line.English)continue 
	word=line.English.replace(/ .*/,"").trim()
	// console.log(word)
	// continue 
	for(eg of read_csv("../../my_egyptian_dictionary.csv")){
		trans=eg[2]
		glyph=eg[0]
		if(!trans||!glyph)continue 
		// if(len(last[word])>=glyph.length)continue 
			// console.log(trans)
		// if(str(trans).contains(word))
		// if(trans.contains(word))
		// 	console.log("~~~~~",word,eg.join("\t"))
		if(trans.match(new RegExp("\\b"+word+"\\b"))){
			console.log(line[0]+"\t"+word+"\t"+eg.join("\t"))
			last[word]=glyph	
		}

	}
	if(!last[word])
		for(eg of read_csv("../../my_egyptian_dictionary.csv")){
		trans=eg[2]
		if(!trans)continue 
		if(trans.contains(word)){
			console.log(line[0]+"\t"+word+"\t"+eg.join("\t"))
			break
		}
	}
	if(!last[word])
			console.log(line[0]+"\t"+word)


}