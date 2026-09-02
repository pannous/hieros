#!/usr/bin/env node
let {convert}=require('/me/dev/script/javascript/pronounce.js')
path= 'Ugarit.txt'
lines = require('fs').readFileSync(path).toString().split('\n')
good=0

Ugarit={
  '𐎀': 'φ',
  '𐎁': 'ƀ',
  '𐎋': 'c',
  '𐎏': 'δ',
  '𐎅': 'ḫ',
  '𐎛': 'ᵛí',
  // '𐎛': 'βí',!
  // '𐎛': 'bí',!
  '𐎙': 'x',
  '𐎙': 'ġ',
  '𐎊': 'ᵛע',
  '𐎍': 'l',
  '𐎎': 'm',
  '𐎐': 'ŉ',
  // '𐎐': 'ന',// na
  // '𐎓': 'voi',
  '𐎓': 'ᵓî',
  '𐎔': 'p',
  '𐎖': 'q',// qr
  '𐎗': 'r',
  '𐎒': 'sh',
  '𐎚': 'ᵎ',
  '𐎆': 'w',
  '𐎉': 'x',
  '𐎕': 'z',// ϛ st 𐋃
  '𐎑': 'dh',// LOST as z '𐎕' :(
  '𐎌': 'ß',
  '𐎘': 'th',
  '𐎃': 'ḫ',
  '𐎝': 'td',
  '𐎇': 'dz',
  '𐎄': 'dl',
  '𐎄': 'br',// lost as 𐎏 'd' !
  '𐎈': 'cr',
  '𐎜': 'vḫ',
  '𐎑': 'Z',// LOST as z '𐎕' :(
  // '𐎜': 'vr',
  '𐎟': ' '
}

function pronounce(word){
	trans=""
	for(c of word)
		trans+=Ugarit[c] || "XXXXX>>"+c+"<<<<XXXX"
	return trans
}

for(var l of lines){
	[xeno,text,_]=l.split("\t")
	if(xeno=='i' || xeno=='𐎛')good=1
	if(xeno.indexOf(" ")>0 || !good){
		console.log(l)
}else{
	// ug=convert(xeno,'ugarit')
	// ug=ug.replaceAll('/','')
	// ug=ug.replaceAll('-','')
	pro=pronounce(xeno)
	console.log(pro,'\t', l)
}
}