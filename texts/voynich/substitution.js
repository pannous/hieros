#!/usr/bin/env node
require('/me/dev/js/extensions.js')()

file="voynich.txt"
// abc="abcdefghijklmnopqrstuvwxyz.ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
// def="abcdefgkijhlmnopqrstuvwxrz.ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890" //äöüßáàñèéêç"
// for(i in abc){
// 	c=abc[i]
// 	// console.log(i,c)
// 	hash[c]=def[i]
// }
hash={
	'??':'Al`',
	'???':'Pro-',
	'????':'Anti-',
	'?????':'Super',
	'h':'k',
	'k':'He',
	'y':'r',
	'c':'i',
	'n':'yr',
	'm':'r',
	'K':'cH',
	'g':'P',
	'H':'M',
	// 'H':'w',
	// 'H':'Pr',
	// '8':'Θ',
	'8':'δ',
	// '8':'t',
	'9':'en',
	// '2':'ch',
	'2':'l',
	'4':'q',
	// '4':'ge',
	// '1':'c',
	'1':'s',
	'I':'u',
	'(':'we',
	// 'e':'l',
	// 'e':'s',
}
// qoHesen gewesen? <<
// <f3r.1,@P0>        Helios.gejae.soe.schoe.δaP* P=>ß?
// <f3r.2,+P0>        ensior.sor.δap.gewesap.sap  p=>r/f/n?
// <f3r.3,+P0>        osor.gesior.soe.δar.schen 
// <f3r.4,+P0>        ssien.sor.sae.sa*.sap.so 
// <f3r.5,+P0>        geko'e.soeo'een.s.sap.schoe   schoe:schoen?

// <f4r.5,+P0>        Penδar.qoHesen.δen,Heenδen # Pentar/Pensar? den Händen
// <f4r.6,+P0>        sor.lenHesen.δenHesCen  # dänischen?
// <f4r.7,+P0>        qoHear.cHoe.δar.cHop  # der Kopp?
// <f4r.8,+P0>        !or.3oe.#oe.cHen.Joeδen   # JUDEN
// <f4r.9,+P0>        δar.Hosen.Hesen.kor.ar           

// <f4r.12,+P0>       sour.saur.saur 
// <f4r.13,+P0>       taur.cHjen<$>
// <f18r.1,@P0>       Petrozyten.tarotEj.enoar.enksoe.tar,A,p.sHen.oHAr.tae
// // 
// <f6r.4,+P0>        δar.i@senδ;o,s.lior.soVen soVen souvent/sieben/saufen? ---

// REIME:
// <f6v.10,+P0>       qoksoδ.en,siar.ksδen 
// <f6v.11,+P0>       ear.sar.oHea*.cHo,p,δen 
// <f6v.12,+P0>       enHesos.3en.qokap.cHen 
// <f6v.13,+P0>       enoδar.cHen.s.sor.oiis.Ar 
// <f6v.14,+P0>       qokor.so,e.cHoe.Hesaeoδen 
// <f6v.15,+P0>       soHen.s.or.sen.s.ayr.ar 
// <f6v.16,+P0>       osen.cHar.cHor.cHen 
// <f6v.17,+P0>       en,saZ.Hae.cHoδap.δen 
// <f6v.18,+P0>       enHesocHoe.sis.cHor 
// <f6v.19,+P0>       osoeen.ksor.sen.7or 
// <f6v.20,+P0>       δsor.soeδar.okoe.δar 


print = x => process.stdout.write(x) // supports \r

text=read_text(file)
ignore=0
for(letter of text){
	if(!is_string(letter))continue 
	if (!text.hasOwnProperty(letter) || letter>='0')

	if(letter=="<")ignore=1
	if(letter==">")ignore=0
	if(ignore)
		print(letter)
	else
		print(hash[letter]||letter)
}