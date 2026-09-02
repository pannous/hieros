#!/usr/bin/env node
require('/Users/me/dev/js/extensions.js')()
require('/Users/me/uruk_egypt/scripts/cuneiformize.js')
last={}
for(line of read_csv("elamite.tsv")){
	nr=int(line[0])
	cun=line[3]
	// cun=cun.replaceAll(/, /,"")
	line[2]=cune(cun||"")
	console.log(line.join("\t"))
}
// console.log(cune("Si-mu-ut Ši-mu-ut-ta Ši-mu-ut Ši-mut "))