#!/usr/bin/env node
require('/me/dev/js/extensions.js')()

file='hebrew.dict.txt'
file2='hebrew.tsv.csv'
m={}
t={}
for(l of read_lines(file)){
	if(l.match(/^\d/)){
		[nr,w,tr,...note]=l.split(' ')
		// console.log(nr,note.join(' '))
		m[nr]=note.join(' ')
		t[nr]=tr
	}
}
for(l of read_lines(file2)){
	[nr,w,tr,...note]=l.split('\t')
	if(tr=='?')tr=m[nr]
	tr=tr||'?'
  tr=tr.replaceAll('\t'," ").replaceAll('   ','  ')
	console.log([nr,w,tr,note.join('\t')].join('\t'))
}