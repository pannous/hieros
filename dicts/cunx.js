#!/usr/bin/env node
exec=require('child_process').execSync
// const runes = require('runes') // for split chars…
require('/Users/me/dev/js/extensions.js')()
map={}

lowers={
	'₀':'0','₁':'1','₂':'2','₃':'3','₄':'4','₅':'5','₆':'6','₇':'7','₈':'7','₉':'9',
}


function norm(text){
	for([dig,big] of lowers){
		text=text.replace(dig,big)
	}
	text=text.replace(/".*/g,'')
	text=text.replace(/(\d+)/g,'$1 ')
	text=text.replace(/~/g,' ')
	text=text.replace(/;/g,' ')
	text=text.replace(/:/g,' ')
	text=text.replace(/_/g,' ')
	text=text.replace(/-/g,' ')
	text=text.replace(/ -/g,'-')

	text=text.replace(/\s+@t/,'@t')
	// text=text.replace(/\s+@t/,'/tenu/')
	text=text.replace(/\./g,' ')
	text=text.replace(/ x /g,'×')
	text=text.replace(/ \* /g,'×')
	text=text.replace(/\*/g,'×')
	text=text.replace(/ TIMES /g,'×')
	text=text.replace(/sh/g,'š') // !
	text=text.replace(/Ḫ/g,'H') // !
	text=text.replace(/ḫ/g,'h')
	return text
}


function add_variants(trans,glyph,ignore_duplicates=0){
		trans=trans.trim()
		trans=trans.replace("_","")
		trans=trans.replace(" x ","×")
		trans=trans.replace(" /tenu/","@t") //without space!
		trans=trans.replace(" /tenû/","@t")
		trans=trans.replace(" /gunu/","@g")
		trans=trans.replace(" /gunû/","@g")
		trans=trans.replace(/Ḫ/g,'H') // !
		trans=trans.replace(/ḫ/g,'h')
		trans=trans.trim()
		if(!trans)return
		// console.log(glyph,trans)
		if(map[trans]){
			// if(map[trans]!=glyph) throw trans+" <- "+map[trans]+" ≠ "+glyph
		}
		else map[trans]=glyph.trim()
}

function load_signs(){
	csv=read_csv("/Users/me/uruk_egypt/abc/cuneiform.csv.full")
	// 𒀉       U+12009         A_2     560     334     ID
	for(line of csv){
		glyph=line[0]
		trans=line[2]
		alts=line[5]
		if(!trans)continue
		add_variants(trans,glyph)
		add_variants(trans.lower(),glyph)
		add_variants(trans.lower().replace(/i2/,"í"),glyph,1)
		add_variants(trans.lower().replace(/i3/,"ì"),glyph,1)
		add_variants(trans.replace(/\d+/,""),glyph,1)
		add_variants(trans.lower().replace(/\d+/,""),glyph,1)
		if(!alts)continue
		for(alt of alts.split(",")){
		add_variants(alt,glyph)
		add_variants(alt.lower(),glyph)
		}
		// break
	}
}


function load_signs2(){
	csv=read_lines("/Users/me/uruk_egypt/abc/cuneiform.list")
	for(line of csv){
		if(!line || line=="")continue
		cols=line.split("\t") 
		trans=cols[0]
		glyph=cols[1]
		if(!trans || !glyph){
		// console.log("ERROR")
		// console.log(line)
		// 	exit(1)
			continue
		}
		if(trans.contains('Note'))break;
		glyph=glyph.replace("?","")
		glyph=glyph.trim()
		trans=trans.trim()
		if(line.contains("ráš"))
			console.log(glyph,"=>",trans)
		add_variants(trans,glyph)
		add_variants(trans.lower(),glyph)
		add_variants(trans.lower().replace(/i2/,"í"),glyph,1)
		add_variants(trans.lower().replace(/i3/,"ì"),glyph,1)
		add_variants(trans.replace(/\d+/,""),glyph,1)
		add_variants(trans.lower().replace(/\d+/,""),glyph,1)
	}
}



// text_to_cuneiform
function cuneiformize(text){
	text=text.replace(/(\d+)/g,'$1 ')
	text=text.replace(/cuneiform/g,"")
	text=text.replace(/mušen/g," mušen ")
	text=text.replace(/ĝeštin/g,"ĝeshtin")

	// text=text.replace(/ĝeštin/g,"𒃾")
	text=text.replace(/gia/g,"gi a")
	text=text.replace(/ĝeš/g," ĝeš ")
	text=text.replace(/kuš1/g," kush1 ")
	text=text.replace(/kuš2/g," kush2 ")
	text=text.replace(/kuš3/g," kush3 ")
	text=text.replace(/kuš4/g," kush4 ")
	text=text.replace(/kuš/g," kuš ")
	text=text.replace(/urud/g," urud ")
	text=text.replace(/ -/g,'-')
	text=text.replace(/-$/,'')
	text=text.replace(/;/g,'')
	text=text.replace('\n',' ')
	text=text.replace("Akk. ","#")
	text=text.replace("wr. ","")
	text=text.replace(/\(\|(.*?)\|\)/g," $1 ") //  adx(|BAD.LU2|); adx(|LU2×GAM|);
	text=text.replace(/\[.*?\]/g,"")
	text=text.replace(/\(.*?\)/g,"")


	chars = text.split('-')
	trans=""
	for(char of chars){
		glyph=map[char]||"?"
		trans+=glyph
	}
	if(trans=="?" && ( text.endsWith("-") || !text.contains("-")))return ""
	return trans
}

load_signs()
load_signs2()
map['UM']='𒌝'
map['LUB']='𒈜'

file="Elamite.tsv"
for(line of lines(file)){
	cols=line.split("\t")
	words=cols[2]
	if(!words)continue
	cun=""
	for(word of words.split(";")){
		word=word.replace(".","-")
		word=word.replace("*","")
 			cun+=cuneiformize(word.trim())+";"
 	}
 	cun=cun.replace(/;$/,"")
 	cun=cun.replace(/^\\?/g,"")
 	cun=cun.replace(/^;/,"")
 	cun=cun.replace(/^;/,"")
 	// if(cun.contains("\\?"))
 		console.log(line,"\t",cun)
}



