#!/usr/bin/env node
// fs=require('fs')
// rl = read_lines = readlines = read_list = cat = loads = read_array = lines = function (path) {
// 	return fs.readFileSync(path).toString().split('\n')
// }
// read_tsv = load_csv = x => read_lines(x).map(x => x.split("\t"))

require('/me/dev/js/extensions.js')()
map={}
file='/me/uruk_egypt/abc/coptic_alphabet.csv'
for(line of read_tsv(file)){
	map[line[0]]=line[2]
	map[upper(line[0])]=line[2]
	map[line[2]]=line[0]
}
dict='/me/uruk_egypt/dicts/coptic.txt'
coptic={}
for(line of read_tsv(file)){
	coptic[line[0]]=line[2]
}
s=""
line = process.argv.slice(2).join(" ").strip()
// console.log("BETTER USE ORIGINAL COPT! DONT LOSE SPECIAL READING")
console.log("<"+line+">")
if(isFile(line)){
  for(line of readlines(line)){
  	s= ""
		console.log(line)
		for(c of line)
			s+=map[c]||c
		console.log(s)
	}
}else{
	console.log(line)
	for(c of line)
		s+=map[c]||" "
	console.log(s)
}