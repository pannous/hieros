#!/usr/bin/env node
require('/Users/me/dev/js/extensions.js')()
last={}
for(line of read_csv("swadesh.tsv")){
	nr=int(line[0])
	word=line[1]
	if(nr<91 || nr>146)continue 
	if(!word)continue 
	word=word.replace(/^to /,"").trim()
	word=word.replace(/ .*/,"").trim()
	for(eg of read_csv("../../my_egyptian_dictionary.csv")){
		glyph=eg[0]
		trans=eg[1]
		comment=eg[2]
		if(!trans||!glyph)continue 
		ok = trans.match(new RegExp("\\b"+word+"\\b"))
		// ok = ok || comment && comment.match(new RegExp("\\b"+word+"\\b"))
		if(ok){
			console.log(line[0]+"\t"+word+"\t"+eg.join("\t"))
			last[word]=glyph	
		}

	}
	if(!last[word])
			console.log(line[0]+"\t"+word)
}