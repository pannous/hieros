#!/usr/bin/env node
// fs=require('fs')
// rl = read_lines = readlines = read_list = cat = loads = read_array = lines = function (path) {
// 	return fs.readFileSync(path).toString().split('\n')
// }
// read_tsv = load_csv = x => read_lines(x).map(x => x.split("\t"))

require('/me/dev/js/extensions.js')()
map={}
file='/me/uruk_egypt/abc/coptic_alphabet.csv'
for(line of read_tsv(file))
	map[line[0]]=line[2]
  map[line[2]]=line[0]

s=""
for(c of process.argv.slice(2).join(" "))
	s+=map[c]||" "
console.log(s)
return 

// dict="coptic_dict.orig"
// for(line of read_lines(dict)){