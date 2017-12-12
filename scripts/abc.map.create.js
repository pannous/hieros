var fs = require('fs');
var read=(file)=>fs.readFileSync(file,{encoding: 'utf-8'})
text="HULLO"
if(process.argv.length>2)
	text=process.argv[2]
if(fs.exists(text))text=read(text)
// function read(file) {
// // only works via 
// // <input type="file" onchange="read(this.files[0])" multiple>
//   var r = new FileReader();
//   r.onload = function(e) { 
//     var contents = e.target.result;
//     alert( "Got the file.n" 
//           +"name: " + f.name + "n"
//           +"type: " + f.type + "n"
//           +"size: " + f.size + " bytesn"
//           + "starts with: " + contents.substr(1, contents.indexOf("n"))
//     );  
//   }
//   r.readAsText(file);
// }

table=read('abc.map.csv','utf-8').toString('utf-8').split('\n').map(x=>x.split("\t"))
langs="deutsch	greek	greece	pronounce	coptic	number	egypt	phoenician	chinanr	hebrew	arameic	arabic	arabian	ethiopic	russian	russi	name	mean	meane	mein	sumerian	akkadia	chinese	rune	runam	runename".split("\t")
try{
	maps=read("abc.map.jsonx",'utf-8')
	console.log("maps parsed from json!")
}
catch(X){
	console.log("building maps...")
	maps={}
	j=0
	langs.each(l=>{maps[j]=maps[l]={}; j++ })// init
	for( xs of table){
		i=0
		langs.each(l=>{
			char=xs[i]
			if(char)maps[i][xs[0]]=char // phoenician A -> 𐤀
			if(char)maps[l][xs[0]]=char // phoenician A -> 𐤀
			// if(char)maps[i][xs[0].toLowerCase()]=char // phoenician A -> 𐤀
			// if(char)maps[l][xs[0].toLowerCase()]=char // phoenician A -> 𐤀
			i++
		})
	}
	fs.writeFileSync("abc.map.json",JSON.stringify(maps))
	dump="maps="+JSON5.stringify(maps)
	fs.writeFileSync("abc.map.json5",dump)
	// console.log(dump)
}
// console.log(maps["greek"])
console.log(text)
text=text.toUpperCase()
for(c of text){
	// process.stdout.write(c||" ")
	process.stdout.write(maps["greek"][c]||" ")
}
console.log()
// console.log(maps["greek"]["B"])