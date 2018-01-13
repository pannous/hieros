fs=require("fs")
require("./extensions.js")()
// path="./gardiner.tsv"
dictionary="my_egyptian_dictionary.txt"
require("./gardiner_map.js")
console.log(Gardiner_Map['N']);
console.log(Gardiner_Map['KOPP']);

lines=[]
gardiners=[]
// gardiners_tsv="/me/Documents/uruk_egypt/gardiner.csv"
gardiners_tsv="gardiner.csv"

loadVocab=function () {
	lines=fs.readFileSync(dictionary).toString().split('\n')
  lines.concat(fs.readFileSync("./changes.txt").toString().split('\n'))
	gardiners=fs.readFileSync(gardiners_tsv).toString().split('\n')
	// gardiners2=gardiners.map(x=>x.split("\t"))
	// for (var arr of gardiners2) {
	// 	hiero=arr[1]
	// 	name=arr[2]
	// 	speak=arr[3]
	// 	if(name && !gardiner_map[name])
	// 		gardiner_map[name]=hiero
	// 	if(speak && !gardiner_map[speak])
	// 		gardiner_map[speak]=hiero
	// }
}
// target="_none"
//  POST wtf
// submit()" onsubmit ??
comment_form=(l)=>`
<form action='add' method="GET" >
	<input name='h' value='${l}' type="hidden"/>
	<input name='x' id='x' autocomplete="off"/>
	<input type="submit" onClick="setTimeout(()=>x.value='',100)" value="+"/>
</form>`
/* */
next=false
find_word=function (q) { 
	q=q.replace(/\-/g," ")
	qi=new RegExp(q, "i")
  qg=q.replace(/[\w\s]*/g,'')
  glyphs_only=qg.len>0
	res=[]
  if(glyphs_only)
   res.push("glyphs_only")
	for(l of gardiners){
		if(l.has(qi)||glyphs_only&& l.has(qg))
     res.push(l)
	}
	res.push(comment_form(q))
	res.push("")
	for(l of lines){
    if(l.len<1){next=false;continue}
    if(glyphs_only)glyphs=l.replace(/[\w\s]*/g,'')
		if(res.length>200)break
		if(l.has(qi) || glyphs_only&&glyphs.has(qg)){
			l=l.replace("  "," ")
			if(!l.match(/\d/)){
				if(last)res.push(last)
				res.push("<b>"+l+"</b><br/>")
        // res.push(comment_form(last))
			}else{
				res.push(l) 
				next=true
			}
     if(!l.match(/\{/))//&&!l.match(/\d/))
        res.push(comment_form(l))
		}
		else if(next){
			if(!l.match(/\d/)){
				res.push("<b>"+l+"</b><br/>")
        // res.push(comment_form(last))
				next=false	
			} else{
        if(last.match(/\{/))
				res.push(l)
            // if(!l.match(/\{/)&&!l.match(/\d/))
            //    res.push(comment_form(last))
                  // if(!l.match(/\{/))
      //                res.push(comment_form(last))
					
			// } else{
				next=false	
			} 
			// return res
		}
		
		if(l.match(/\d/))
			last=l
	}
	if(res.len=0)
		return "NONE"
	else
		return res
}