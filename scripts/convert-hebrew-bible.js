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
	[nr,word,pie,phon,trans, ...rest]=line.split("\t")
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
	if(line[0]=='#' || len(line)<3){continue }
	[word,pie,trans]=line.trim().split("\t")
	trans=trans.trim()
	// trans=trans||pie
	// console.log([word,pie,trans])
	if(!word||!trans){console.log(line);exit();continue }
	word=word.trim()
	map[word]=trans
	pies[word]=pie
	if(!map["י"+word])map["י"+word]="he "+trans
	if(!pies["י"+word])pies["י"+word]="he "+pie

	if(!map[word+'ה'])map[word+'ה']=trans+"se"
	if(!pies[word+'ה'])pies[word+'ה']=pie+"se"
		
}

english={}
bibles="/me/uruk_egypt/texts/Bibles/"
// english_bible=bibles+"English__Basic_English_Bible__basicenglish__LTR.txt"
english_bible=bibles+"English__Young___s_Literal_Translation__ylt__LTR.txt"
for(line of readlines(english_bible)){
	[book,nr,phrase,...rest]=line.split("||")
	english[book+nr+"."+phrase]=rest[0]
}



i=0
for(line0 of read_lines("~/uruk_egypt/texts/Hebrew_Bible.me.txt")){
	let [book,nr,phrase,...bla]=line0.split("| ")
	line=line0.replace(/.*\|/,"").replace(/\s+/," ").trim()
	trans=english[book+nr+"."+phrase]
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
	console.log(trans)
	console.log()
	if(i++>69	)break
}
// console.log(map['א'])