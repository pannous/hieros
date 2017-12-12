#!/usr/bin/env node
var fs = require('fs');
var read=(file)=>fs.readFileSync(file,{encoding: 'utf-8'})
text="HULLO"
if(process.argv.length>2) text=process.argv.join(" ")
if(fs.exists(text))text=read(text)
maps={}
try{
	maps=read("abc.map.json",'utf-8')
	console.log("maps parsed from json!")
}catch(x){
	console.log(x)
}
maps['script']={A:"𝒜", B:"𝓑", C:"𝒞", D:"𝒟", E:"𝓔", F:"𝓕", G:"𝒢", H:"𝓗", I:"𝓘",J:"𝒥", K:"𝒦", L:"𝓛", M:"𝓜", N:"𝒩", O:"𝒪", P:"𝒫", Q:"𝒬", R:"𝓡", S:"𝒮", T:"𝒯", U:"𝒰", V:"𝒱", W:"𝒲", X:"𝒳", Y:"𝒴", Z:"𝒵", a:"𝒶", b:"𝒷", c:"𝒸", d:"𝒹", f:"𝒻", g:"𝓰", h:"𝒽", i:"𝒾" , j:"𝒿", k:"𝓀", l:"𝓁", m:"𝓂", n:"𝓃", o:"𝓸", p:"𝓅", q:"𝓆", r:"𝓇", s:"𝓈", t:"𝓉", u:"𝓊", v:"𝓋", w:"𝓌", x:"𝓍", y:"𝓎", z:"𝓏" }
maps['fullwidth']={A:"Ａ", B:"Ｂ", C:"Ｃ", D:"Ｄ", E:"Ｅ", F:"Ｆ", G:"Ｇ", H:"Ｈ", I:"Ｉ", J:"Ｊ", K:"Ｋ", L:"Ｌ", M:"Ｍ", N:"Ｎ", O:"Ｏ", P:"Ｐ", Q:"Ｑ", R:"Ｒ", S:"Ｓ", T:"Ｔ", U:"Ｕ", V:"Ｖ", W:"Ｗ", X:"Ｘ", Y:"Ｙ", Z:"Ｚ",a:"ａ", b:"ｂ", c:"ｃ", d:"ｄ", e:"ｅ", f:"ｆ", g:"ｇ", h:"ｈ", i:"ｉ", j:"ｊ", k:"ｋ", l:"ｌ", m:"ｍ", n:"ｎ", o:"ｏ", p:"ｐ", q:"ｑ", r:"ｒ", s:"ｓ", t:"ｔ", u:"ｕ", v:"ｖ", w:"ｗ", x:"ｘ", y:"ｙ", z:"ｚ", } // ℊ ℋ ℐ ℒ ℘ ℛ ℞ ℬ ℰ ℱ ℳ
console.log(text)
// map=maps["script"]
map=maps["fullwidth"]
// map=maps["greek"]
for(c of text){
	// process.stdout.write(c||" ")
	C=c.toUpperCase()
	process.stdout.write(map[c]||map[C]||" ")
}
console.log()
// console.log(map["B"])