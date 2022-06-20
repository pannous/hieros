require('/Users/me/dev/js/extensions.js')()
words="SWADESH_TEMPLATE.tsv"
file="caucasian.all.tsv"
for(concepts of read_lines(words)){
	let cols=concepts.split("\t")
  id=cols[0]
	WORD=cols[1].replaceAll(/\s\(.*/,"").upper()
	WORD=WORD.replaceAll(/^TO /,"").trim()

	if(!WORD)continue
	// console.log(WORD)
found =0
	for(line of read_lines(file)){
		cols=line.split("\t")
		CONCEPT=cols[1]
		if(!CONCEPT)continue
		CONCEPT=CONCEPT.replaceAll(/\s.*/,"").trim()
  	if(WORD==CONCEPT){
  		found=1
  		// console.log(id+"\t"+line)
  		cols[0]=id
  		console.log(cols.join("\t"))
  		break
		}
	}  
	if(!found)
		console.log(id+"\t"+WORD)
}
