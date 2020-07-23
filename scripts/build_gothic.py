#!/usr/local/bin/python3

import sys
sys.path.append('/me/dev/script/python/')

from WiktionaryParser.wiktionaryimporter import search, query, all, titles
from WiktionaryParser.wiktionet import describe, word

from extensions import * 
# file='gothic.lemmas'
# old=read_text('gothic.tsv')

file='coptic.lemmas'
old=read_text('coptic.tsv')

gothic={ # ᵛ  𐌴:thi hi ie
"𐌰":"a", "𐌱":"b", "𐌾":"ɕ", "𐌳":"d", "𐌴":"e", "𐍆":"f", "𐌲":"g", "𐌷":"ḫ", "𐌹":"ι", "𐌺":"k", "𐌻":"l", "𐌼":"m", "𐌽":"n", "𐍈":"wh", "𐍀":"p", "𐌵":"c", "𐍂":"r", "𐍃":"s", "𐍄":"t", "𐍊":"ṯ", "𐌿":"u", "𐍁":"V", "𐍅":"v", "𐍇":"x", "𐍉":"ō", "𐌶":"z", "𐌸":"ƌ", " ":" ", "-":"-" # ɕ ᵗ/ᵍ tun/gehn
}
coptic={"Ⲁ":"A", "Ⲃ":"B", "Ⲥ":"C", "Ⲇ":"D", "Ⲉ":"E", "Ⲋ":"F", "Ⲅ":"G","Ⲏ":"H", "Ϩ":"CH", "Ϫ":"J", "Ⲓ":"I", "Ϧ":"K","Ⲕ":"K","ⳤ":"Kr", "Ⲗ":"L", "Ⲙ":"M", "Ⲛ":"N", "Ⲟ":"O", "Ⲡ":"P", "Ϥ":"Q","Ⲣ":"R", "ⳁ":"PR", "Ϭ":"S", "Ϯ":"T","Ⲧ":"T", "Ⲩ":"U", "Ⲫ":"V", "Ⲱ":"W", "Ⲭ":"X","Ⲝ":"Xr", "Ⲯ":"Y", "Ⳉ":"Z", "Ⲍ":"Z","Ⲑ":"TH", "Ϣ":"SH", "Ⲋ":"ß", "ⲁ":"a", "ⲃ":"b", "ⲥ":"c", "ⲇ":"d", "ⲉ":"e", "ⲋ":"f", "ⲅ":"g", "ϩ":"ch","ⲏ":"h", "ϫ":"j", "ⲓ":"i", "ϧ":"k", "ⲕ":"k", "ⲗ":"l", "ⲙ":"m", "ⲛ":"n", "ⲟ":"o", "ⲡ":"p", "ϥ":"q","ⲣ":"r", "ⳁ":"pr", "ϭ":"s", "ϯ":"t","ⲧ":"t", "ⲩ":"u", "ⲫ":"v", "ⲱ":"w","ⲭ":"x", "ⲝ":"xr", "ⲯ":"y", "ⳉ":"z","ⲍ":"z", "ⲑ":"th", "ϣ":"sh", "ⲋ":"ß"," ":" ","-":"-","⸗":"⸗","ⳋ":"ⳋ","ⳃ":"SH","ç":"Ch"}
# ⲳⲕⲧⲟⲕ

# w=word('𐌲𐌰𐍆𐌹𐌻𐌷𐌰𐌽')
# print(w.describe())
import os


def english_reading(ok):
	ok=ok.replace("^afaιr","after")
	ok=ok.replace("^afar","after")
	ok=ok.replace("^ufar","over")
	ok=ok.replace("^faura","fore-")
	ok=ok.replace("^faur","fore-")
	ok=ok.replace("^fra","for") #pro
	ok=ok.replace("gg","ng")
	ok=ok.replace("gk","nk")
	ok=ok.replace("^ga","") # german but ok
	ok=ok.replace("ɕan$","e")
	ok=ok.replace("an$","e")
	ok=ok.replace("ōn$","e")
	ok=ok.replace("^ana"	,"en")
	ok=ok.replace("^and","anti-")
	# ok=ok.replace("^and","un-") # ad(mit) in(vest,take)
	ok=ok.replace("^af","off-")
	ok=ok.replace("^uf","of-")
	ok=ok.replace("^bι","")
	ok=ok.replace("^dιs","dis")
	ok=ok.replace("^ut","out")
	ok=ok.replace("^us","") # ex out off
	ok=ok.replace("sk","sh")
	ok=ok.replace("z","sh")
	ok=ok.replace("de","dth") # 𐌳𐌴 fix earlier!
	ok=ok.replace("ƌ","ƒ")
	return ok

def german_reading(ok):
	ok=ok.replace("t","ƫ") # tz tß ŧ ţ ƫ
	ok=ok.replace("d","t")
	ok=ok.replace("c","k")
	ok=ok.replace("s","t")
	ok=ok.replace("va","we")
	ok=ok.replace("vι","we")
	ok=ok.replace("^ve","^we")
	ok=ok.replace("v","w")
	ok=ok.replace("gg","ng")
	ok=ok.replace("gk","nk")
	ok=ok.replace("ufar","über")
	# ok=ok.replace("ai","e") # stain sten
	# ok=ok.replace("aι","e")
	ok=ok.replace("an$","en")
	ok=ok.replace("ōn$","en")
	ok=ok.replace("ns$","ng")
	# ok=ok.replace("s$","ng")
	ok=ok.replace("^dιs","des")
	ok=ok.replace("^ga","ge")
	ok=ok.replace("^faurga","vorge") # ver
	ok=ok.replace("^faura","vor") # ver(ge)
	ok=ok.replace("^faurb","vorb") 
	ok=ok.replace("^faur","ver") 
	ok=ok.replace("^fra","ver") #pro
	ok=ok.replace("^faιr","ver") # vor/für
	ok=ok.replace("^ana","ange")
	ok=ok.replace("^and","ent")
	ok=ok.replace("^af","ab")
	ok=ok.replace("^uf","ab") # aus
	ok=ok.replace("^us","^aus")
	ok=ok.replace("bι","be") # bιleιben bleiben  bιlaιgōn belecken / blecken
	ok=ok.replace("gc","nk")
	ok=ok.replace("nɕ","nig")
	ok=ok.replace("𐌳𐌴","ß") # dth @ EN 
	ok=ok.replace("𐌽𐍉$","nung")
	ok=ok.replace("𐌽𐌹$","nung")
	ok=ok.replace("𐌹𐌽𐍃$","ung")
	ok=ok.replace("sk","sch")
	ok=ok.replace("wh","w") # b
	ok=ok.replace("au","ø")
	ok=ok.replace("ō","u")
	# 𐌰𐍄 : abt ab-, ant:an-
	# ok=ok.replace("t","ß") #NO
	ok=ok.replace("^øs","aus") # fix
	return ok

for line in read_lines(file):
	l=line.strip()
	if l in old:
		continue 
	w=word(l)
	p="^"+"".join(list(map(lambda it: coptic[it],l)))+"$"
	# p="^"+"".join(list(map(lambda it: gothic[it],l)))+"$"
	g=german_reading(p)
	e=english_reading(p)
	if g!=e: p=g+"\t"+e
	else: p=g+"\t"+p
	p=p.replace("$","")
	p=p.replace("^","")
	trans=w.translations()
	# if not trans:
	# 	trans=w.etymology()
	# 	trans=list(filter(lambda it:not it[0] in "abcdefghijklmnopqrstuvwxyz",trans))
	if len(trans)>0 and isinstance(trans[0],tuple):
		trans=filter(lambda it:'de' in it[0],trans) #[trans[0][1]] #map(lambda it: it[1], trans)
		trans=map(lambda it: it[1], trans)
	trans=filter(lambda it:not ':' in it,trans)
	trans=",".join(trans).strip()
	print(l,"\t",p,"\t","\t", trans)



