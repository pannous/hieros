// Write a js function to modify a tsv file in the following way:
// If the second column contains a ';' merge the 5th and sixth column into the second. Shift everything from the seventh column to the fifth. If the second column does not contain a ';' shift everything from the sixth column to the fourth.
// #!/usr/bin/env node
require('/Users/me/dev/js/extensions.js')()

function shift(line){
	let parts=line.split("\t")
    if(parts.length<2)return line
	if(parts[1].indexOf(";")<0){
		parts[1]=(parts[1]+" "+parts[4]+" "+parts[5]).trim()
		parts[4]=parts[6]
		parts[5]=parts[7]
		parts[6]=parts[8]
	}
	return parts.join("\t")
}


lines=read_lines("../sumerian.words.tsv")
for(line of lines) {
    console.log(shift(line))
}

