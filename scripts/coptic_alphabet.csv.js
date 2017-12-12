#!/usr/bin/env node
// fs=require('fs')
// rl = read_lines = readlines = read_list = cat = loads = read_array = lines = function (path) {
// 	return fs.readFileSync(path).toString().split('\n')
// }
// read_tsv = load_csv = x => read_lines(x).map(x => x.split("\t"))

require('/me/dev/js/extensions.js')()
map={}
file='coptic_alphabet.csv'
for(line of read_tsv(file))
	map[line[0]]=line[2]

s=""
for(c of process.argv.slice(2).join(" "))
	s+=map[c]||" "
console.log(s)
return 
my_map={e:'w|au|ou'}
greek={ai:"ὄ",a:"α",b:"β",c:"ζ",d:"δ",e:"ε",f:"ƒ",wh:"ϝ",g:"γ",hi:"η",i:"ι",j:"ῖ",k:"κ",l:"λ",m:"μ",n:"ν",o:"o",p:"π",q:"ϐ",r:"ρ",s:"σ",t:"θ",T:"τ",u:"υ",v:"φ",w:"ω",x:"ξ",y:"γ",z:"ζ","th":"θ",sh:"ϡ","ß":"ς",tsh:"γ","dh":"ð",dz:"ҙ",dj:"γ",ps:"ψ",õ:"ὦ",ch:"χ",ɦra:"ἁ",fi:"ἰ",y:"ύ"},
greek['y']='θ' //fix
greek['c']='ς' //fix
greek['/']='η' //fix
greek[']']='τ' //fix
greek['u']='γ' //fix


dict="coptic_dict.orig"
for(line of read_lines(dict)){
	line=line.replace("---","").lower()
	line=line.replace(" v."," ↔")
	line=line.replace(/\.$/,"")

	line=line.replace("(2)","")
	line=line.replace("(2a)","")
	line=line.replace("(2b)","")
	line=line.replace("(1)","")
	line=line.replace("(1a)","")
	line=line.replace("(1b)","")
	line=line.replace("vi. to ","")
	line=line.replace("vt. to ","")
	line=line.replace("vb. to ","")
	line=line.replace("vb. ","")
	line=line.replace(/;$/,"")
	line=line.replace("(gk?); ","🇬🇷 ")
	line=line.replace("(gk); ","🇬🇷 ")
	line=line.replace("(gk)","🇬🇷")
	line=line.replace(";:",":")
	line=line.replace(":;",":")
	let [copt,en]=line.split(":")
	if(!en)[copt,en]=line.split("-")
	if(!copt)continue 
		// ||line.match("---")
	if(!en){print(line);continue }
	print("\n"+copt.map(x=>map[x]||x)+"\t"+en)
	// if(line.match(/🇬🇷/))
	// print("\t"+copt.map(x=>greek[x]||x))
	// if(line.length==1){print(line);continue }
}

