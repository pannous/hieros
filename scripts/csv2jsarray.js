#!/usr/bin/env nodemon
require("./extensions.js")()
csv=read_csv("cognet.csv")
head=csv[0]
data=csv.splice(1)
// console.log(head);
// console.log(data);
function print(argument) {
	process.stdout.write(argument)
}
console.log('data=[');
for(line of data){
	print("{")
	for (var i = 0; i <head.length; i++) {
		col=head[i]
		dat=line[i]
		if(dat)
		print("	"+col+':  "'+(dat?dat:"")+'",');
	}
	console.log ("},")

	}
console.log(']');


