#!/usr/bin/env node
require('/me/dev/js/extensions.js')()

hebrew={a:"א",bv:"ב",c:"ג",d:"ד",h:"ה",f:"ו",g:"ג",ḫ:"ח",aĩ:"י",j:"ו",k:"כ",k:"ך",l:"ל",mn:"ם",m:"מ",n:"נ",on:"ן",y:"ע",p:"ף",p:"פ",q:"ק",r:"ר",th:"ץ",tz:"צ",t:"ת",δ:"ט",u:"ו",vo:"ו",w:".ו",x:"ס",z:"ז",sh:"ש",' ':' '}
roman=invert(hebrew) //flip()
pies={}
map={}
// file="/me/Documents/uruk_egypt/
file="/me/Documents/uruk_egypt/dicts/hebrew.tsv"
for(line of readlines(file)){
	[nr,word,pie,phon,trans,...rest]=line.split("\t")
	if(!word||!trans){console.log(line);break}
	word=word.trim()
	trans=trans.trim().replace("to ","").replace(/,.*/,"").replace("(plural)","").replace(/ .*/,"").replace(/&\s\w.*/,"")
	pie=(pie||"").trim().replace(/\++/,"").replace(/ .*/,"")
	if(pie=='?')pie=false
	map[word]=trans
	pies[word]=pie
}
file="/me/Documents/uruk_egypt/dicts/hebrew.me.tsv"
for(line of readlines(file)){
	[word,pie,trans]=line.trim().split("\t")
	// console.log([word,pie,trans])
	if(!word||!trans){console.log(line);continue }
	word=word.trim()
	map[word]=trans
	pies[word]=pie
}


i=0
for(line of read_lines("~/uruk_egypt/texts/Hebrew_Bible.me.txt")){
	line=line.replace(/.*\|/,"").trim()
	console.log(line)
	console.log(line.map(c=>roman[c]))
	for(word of line.split(" ")){
		print((pies[word]||word)+" ")
	}
	console.log()
	for(word of line.split(" ")){
		print((map[word]||word)+" ")
	}
	console.log()
	console.log()
	if(i++>4)break
}
console.log(map['א'])