require('/me/dev/js/extensions.js')()
g1 = fs.readFileSync("gardiner.full.csv").toString().split('\n').map(x=>x.split("\t"))
g2 = fs.readFileSync("Gardiner-Bedeutungen.csv").toString().replaceAll('-',"").split('\n').map(x=>x.split("\t"))
g3 = fs.readFileSync("gardiner_representations.txt").toString().split('\n').map(x=>x.split("\t"))
g4=  fs.readFileSync("gardiner_code_manual_de_codage.tsv").toString().split('\n').map(x=>x.split("\t"))

Bedeutungen={}
representations={}
codes={}
signs={}
for(x of g1) signs[x[0]]=x[3]
for(x of g2) Bedeutungen[x[1]]=x[2]
for(x of g3) representations[x[0]]=x[1]
for(x of g4) codes[x[0]]=x[2]

for(g of g1){
	g0=g[0]
	if(!g0)continue 
	code=(Bedeutungen[g0]||"").replace(/.*\((.*)\)/,"$1")
	g.push(codes[g0]||code)
	text=representations[g0]+" "||''
	if(text.contains("combination"))
	for([ga,s] of signs)
		if(ga && text.match(" "+ga+" "))
			text=text.replace(Regex(". "+ga),ga+" "+s)
	g.push(text)
	g.push(Bedeutungen[g0]||'')
	// if(g0=='D17')
	// if(g0=='A1')
	if(text.contains("combination"))

	console.log(g)||quit()
}
