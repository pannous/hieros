#!/usr/bin/env node

require('/me/dev/js/extensions.js')()
for(line of read_lines("hungarian.txt")){
	if(line.match(/^ŠL/))continue 
	if(line.match(/^Gost/))continue 
	if(line.match(/^MSL/))continue 
	if(line.match(/^LM/))continue 
	// if(line.match(/^\d/))console.log()
	console.log(line)
}