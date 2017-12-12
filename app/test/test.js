#!/usr/bin/env node
require('/me/dev/script/javascript/extensions.js')()
require("./gardiner_map.js")
require("./gardiner_map2.js")
Object.assign(Gardiner_Map,Gardiner_Map2)
console.log(Gardiner_Map['OB']);
console.log(Gardiner_Map['KOP']);
console.log(Gardiner_Map['HEAD']);
console.log(Gardiner_Map['N']);
gardiners=read_csv("./gardiner.tsv")
// console.log(gardiners[1]);
// console.log("gardiner_table=[");

for (var arr of gardiners) {
	// console.log(arr);
	// if(Gardiner_Map['er'])break
	// console.log(arr)
	hiero=arr[1]
	if(hiero=='G'||hiero==undefined)continue
	gardiner=arr[0].up
	name=arr[2].up.trim().replace(/[^\w]/g,"_").replace(/_$/,"")
	speak=arr[3].up.trim().replace(/[^\w]/g,"_").replace(/_$/,"")
	descr=arr[4].up.replace(/[^\w]/g,"_")
	if(name && !Gardiner_Map[name])
		Gardiner_Map[name]=hiero;
	else if(Gardiner_Map[name]!=hiero) Gardiner_Map[name+"1"]=hiero;
	if(speak && !Gardiner_Map[speak])
		Gardiner_Map[speak]=hiero;
	else if(Gardiner_Map[speak]!=hiero) Gardiner_Map[speak+"1"]=hiero

}

console.log("Gardiner_Map2={");
for (let k in Gardiner_Map) {
	v=Gardiner_Map[k]
	// for (var [k,v] of Gardiner_Map) {
	// 	if(k.match(/\d/))
	if(k.match(/^[A-Z]/)){
		console.log(`\t${k}:'${v}',`);
	// console.log(`\t'${k}':'${v}',`);
	}
}
console.log("}");
// console.log(Gardiner_Map['ER1']);
