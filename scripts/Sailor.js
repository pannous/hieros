#!/usr/bin/env nodemon # -V -L -e txt,js # TYPE rs to restart!
// nodemon -V -L -e txt,js  # txt doesn't :(
Object.prototype[Symbol.iterator]=function*(){for(kv of Object.entries(this))yield kv}// enables:
// for([key, value] of map) {

var SortedMap = require("collections/sorted-map");
require("./extensions.js")()
txt=load("Sailor.txt")
by_length=(a,b)=>b.length - a.length
xs=load("Sailor.vocab").split("\n").sort(by_length)

ys=new SortedMap()
for ( voc of xs){
	if(voc[0]=="#")continue
	voc=voc.replace("		","	")  
 	voc=voc.split("\t")
	w=voc[0].trim()
	t=voc[1]||""
	if(t=="")continue
	ys[w]=t
	ws=w.replace(/ /g,"")
	ys[ws]=t
}
xs=ys

j=0
for (lie of txt.split("\n")){
	j++
	// if(j<20)continue
	// if(j>240)break
console.log("")
console.log(lie)// orig to debug

// console.log(j +" "+lie);
txt=lie+" "
txt=txt.replace("."," .")
txt=txt.replace(","," ,")
txt=txt.replace(/>/gi," >")
txt=txt.replace(/</gi," <")

// 𓉐𓂋𓂻_partiere  𓆑_sin 𓐝  𓋴𓅱𓎛𓏏𓆇_Generteii 
txt=txt.replace(/  [ ]+/g,"  ")

for ([w,t] of xs){
	ws=w.replace(/ /g,"")
	txt=txt.replace(new RegExp(w+" ","g")," "+ws+"_"+t+" ")
	txt=txt.replace(new RegExp(w+" ","g")," "+ws+"_"+t+" ")
 	txt=txt.replace(new RegExp(ws+" ","g")," "+ws+"_"+t+" ")
}

txt=txt.replace(/_/gi," ")
// txt=txt.replace(/𓏪 /g,"en𓏪     ")
txt=txt.replace(/\s𓏏𓏻 -ty/g,"ty𓏏𓏻 ")
txt=txt.replace(/s\s𓏪 -n/g,"s𓏪")
txt=txt.replace(/s  en 𓏪/g,"s𓏪")
txt=txt.replace(/\s𓏪 -n/g,"en 𓏪")
txt=txt.replace(/\s?𓅱 𓏮/g,"s𓏮")
txt=txt.replace(/\s𓏤  𓏪 -n/g,"en 𓏤𓏪")
txt=txt.replace(/ 𓏤 en 𓏪/g,"en 𓏤𓏪")
txt=txt.replace(/en en 𓏪/g,"en𓏪")
txt=txt.replace(/er en 𓏪/g,"ern𓏪")
txt=txt.replace(/ 𓍘𓇋 -ty/g,"ty𓍘𓇋")
txt=txt.replace(/s en 𓏪/g,"s 𓏪")
// console.log(j + " " +txt);// auto NR
console.log(txt);
}
// console.log(xs["𓂋 𓍿 𓀀 𓁐 𓏥"])