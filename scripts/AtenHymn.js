#!/usr/bin/env nodemon
require("./extensions.js")()
txt=load("AtenHymn.txt")
xs=load("AtenHymn.vocab").split("\n")//.map(x=>x.split("\t")

// xs.sort((a,b)=>len(a)<len(b))
j=0
for (lie of txt.split("\n")){
if(j++<0)continue;
if(j>2000)break;
// console.log(j +" "+lie);
txt=lie
txt=txt.replace(/>/gi," >")
txt=txt.replace(/</gi," <")
txt=txt.replace(/𓇋 𓏏 𓈖 𓇳/g,"𓇋𓏏𓈖𓇳_AdonRay")
for ( line of xs){
	line=line.replace("		","	")  
 	line=line.split("\t")
	w=line[0].trim()
	t=line[1]||""
	if(t=="")continue
	ws=w.replace(/ /g,"")
	i=0
	// do{
	// 	txt2=txt
	// 	txt=txt2.replace(w," "+ws+"_"+t+" ")
	// }while(len(w)>3&&txt!=txt2 && i++<100)
	txt=txt.replace(new RegExp(w+" ","g")," "+ws+"_"+t+" ")
	txt=txt.replace(new RegExp(ws+" ","g")," "+ws+"_"+t+" ")
}
	// txt=txt.replaceAll(w,  ws+t)||txt

// 𓉐𓂋𓂻_partiere  𓆑_sin 𓐝  𓋴𓅱𓎛𓏏𓆇_Generteii 
txt=txt.replace(/  [ ]+/g,"  ")
txt=txt.replace(/𓎟	/gi,"𓎟 all ")
txt=txt.replace(/𓂝	/gi,"𓂝 or ")
txt=txt.replace(/𓈖	/gi,"𓈖 an ")
txt=txt.replace(/𓊃	/gi,"𓊃 es ")
txt=txt.replace(/𓐝	/gi,"𓐝 im ")
txt=txt.replace("𓎟	","𓎟 all ")
txt=txt.replace("𓂝	","𓂝 or ")
txt=txt.replace("𓈖	","𓈖 an ")
txt=txt.replace("𓊃	","𓊃 es ")
txt=txt.replace("𓐝 ","𓐝 im ")
txt=txt.replace(/_/gi," ")
// txt=txt.replace(/𓏪 /g,"en𓏪     ")
txt=txt.replace(/\s𓏏𓏻 -ty/g,"ty𓏏𓏻 ")
txt=txt.replace(/s\s𓏪 -n/g,"s𓏪")
txt=txt.replace(/s  en 𓏪/g,"s𓏪")

txt=txt.replace(/\s𓏪 -n/g,"en 𓏪")
txt=txt.replace(/\s𓏤  𓏪 -n/g,"en 𓏤𓏪")
txt=txt.replace(/ 𓏤 en 𓏪/g,"en 𓏤𓏪")
txt=txt.replace(/en en 𓏪/g,"en𓏪")
txt=txt.replace(/er en 𓏪/g,"ern𓏪")
txt=txt.replace(/ 𓍘𓇋 -ty/g,"-ty 𓍘𓇋")
txt=txt.replace(/s en 𓏪/g,"s 𓏪")
console.log(j + " " +txt);
}
