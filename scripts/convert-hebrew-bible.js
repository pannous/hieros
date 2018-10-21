#!/usr/bin/env node
require('/me/dev/js/extensions.js')()

// ע:= Y Silent lost letter <> wildcard 
// ע:= γ DGR / dy / dj leder= תחש decer=עשור (decade)
// ע DANGER zone עבדה Arbeit / Robota / Dra.. ?


hebrew={a:"א",bv:"ב",c:"כ",d:"ד",h:"ה",f:"ו",g:"ג",ḫ:"ח",aĩ:"י",j:"ו",k:"כ",k:"ך",l:"ל",mn:"ם",m:"מ",n:"נ",on:"ן",Δ:"ע",p:"ף",p:"פ",q:"ק",r:"ר",th:"ץ",tz:"צ",t:"ת",δ:"ט",u:"ו",vo:"ו",w:".ו",x:"ס",z:"ז",sh:"ש",' ':' '}
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
	if(pie)pies[word]=pie
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
for(line0 of read_lines("~/uruk_egypt/texts/Hebrew_Bible.me.txt")){
	line=line0.replace(/.*\|/,"").replace(/\s+/," ").trim()
	console.log(line0.replace("| ו","\nו"))
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
	if(i++>22)break
}
console.log(map['א'])