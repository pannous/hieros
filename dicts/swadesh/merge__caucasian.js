require('/Users/me/dev/js/extensions.js')()
file="caucasian.all.tsv"
for(worde of read_lines(file)){
	let rest=worde.split("\t")
	nr=rest[0]
	if(!nr>1)continue
		// console.log(rest)
found=0
	// console.log(nr,rest)
for(line of read_lines("zzz.tsv")){
	cols=line.split("\t")
	nr1=cols[0]
	// WORD=WORD.replaceAll(/\s\(.*/,"")
  if(found==0 && nr==nr1 && cols[2]){
  	console.log(worde+"\t"+cols[2]+"\t"+cols[3]+"\t"+cols[4])
  	found=1
  	break
	}  
// if(!found) console.log(nr+"\t"+rest) 
}
}
