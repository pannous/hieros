#!/usr/bin/env python3
import re
# lines=open("voynich_v101.txt").readlines()
lines=open("voynich_eva.txt").readlines()

# since 'daiin' / '8am' is the most frequent word, the maximal reading is unlikely; unless meiyou

# IPA like reading chíḫøiŋ ≈ qihuan , chøngkchiŋ chong-qing ? íïⁿ yi-yin
# thats all cute, but we are completely missing L- series! r- í- ï- ≈ l- ? ok?  ( h- = M-  ok? )
# t- d- only in bound forms ítar yi-dianr !?
# ḫair like forms ok 孩儿 keep r in ren!  arí airíḫ problematic

def common(l):
	l=l.replace(".", " ")
	l=l.replace("\n", "")
	return l

def remap_common(l):
	l=l.replace("4o", "•")	
	# l=l.replace("8", "ḫ")	
	l=l.replace("o", "í") # 1 = yi QED 2 = r QED 3 = 9 ƒ
	# l=l.replace("9", "ˢū")
	l=l.replace("9", "sã") # san
	# l=re.sub("^o","yi-",l)
	# l=re.sub("o","yi-",l)
	l=l.replace("v", "wô")
	# l=l.replace("e", "ø") # ye
	return l


def remap(l):
	l=l.replace("4o", "•") # yi / qo enigma
	l=l.replace("1", "t") # combines with c tc7ū tcoe tcḫū': 331, 'tcū': 332, 'toe toy
	l=l.replace("2", "z") # combines with c 2cū
	# l=l.replace("8", "ḫ")	
	l=l.replace("m", "you")	

	# B- P- <<<<<<<<<<<,
	l=l.replace("7", "b")	

	l=l.replace("8", "m")	

	# l=l.replace("a", "bu")
	# l=l.replace("a", "µ") # ḫań otherwise ok  ḫµń <> Sanskrit 
	# l=l.replace("a", "'") # ḫań otherwise ok  ḫµń <> Sanskrit 

	# l=l.replace("d", "n")
	# l=l.replace("o", "y")
	# l=l.replace("8", "j") # combines with c tc8ū
	# l=l.replace("8", "d")

	# l=l.replace("s", "?") 
	# l=l.replace("3", "z")

	# l=l.replace("4", "ḿ")
	l=l.replace("íe", "ÿe")
	# l=l.replace("oe", "mei")
	# l=l.replace("oy", "mei")


	# MISSING INITIAL L!

	# MISSING INITIAL M!
	
	# l=l.replace(",", "m")
	# l=l.replace("7", "m") # but tmū
	# # l=l.replace("k", "m") # but qok'cḫū mcḫū ímtū
	# l=l.replace("K", "m") # but tmū
	# l=l.replace("e", "man")
	# l=l.replace("m", "ń") # hem => hen com 

	return l

def remap_long(l):
	# l=l.replace("qo", "you ") # why though?
	l=l.replace("qo", "是 ") # qi≈shi AFTER 4->q mapping ?
	# l=l.replace("qo", "在") # qi≈zai 
	# l=l.replace("qo", "有") # why though?

	l=l.replace("ds", "µbu ") # ;) mei why?
	l=l.replace("s", "不 ") # ;) why?
	l=l.replace("sb", "sh") 
	l=l.replace("o", "í")
	# 1 = yi QED 2 = r QED 3 = 9 ƒ
	l=l.replace("9", "ˢū")
	l=l.replace("e", "ø")
	l=l.replace("dy", "ng") # maybe inverse orthography gg => ng ≈ ḫg
	# l=l.replace("d", "µ") 8am / daiin => com vs ḫen vs men vs nam ?
	l=l.replace("d", "ḫ")

	# l=l.replace("y", "g") # Goog nice but shog ?
	l=l.replace("y", "iŋ") # maybe inverse orthography gn => ng

	# l=l.replace("k", "G") # íkeng => yigong / keep kong!
	# l=l.replace("l", "ƞ") #  appears in front and end => n / g 
	l=l.replace("l", "ïⁿ") #  appears in front and end => n / g  vs o=>í
	l=l.replace("pch", "Ph")
	l=l.replace("fch", "fu")
	l=l.replace("cfh", "b")
	l=l.replace("aii", "e")
	l=l.replace("øø", "u")

	l=re.sub("\s+h"," m",l) # sh ch are very different


	return l

counts={}
for line in lines:
	if line[0]=="#":
		continue
	l=re.sub("<.*?>\s*","",line)
	l=common(l)
	l=remap_common(l) # o->i
	# l=remap(l)
	l=remap_long(l)

	# l=re.sub("^í","yi-",l)

	words=l.split(" ")
	for word in words:
		if word in counts:
			counts[word]=counts[word]+1
		else:
			counts[word]=1

	# print(l)
# sort by value
x=dict(sorted(counts.items(),key=lambda item: item[1]))
print(x)

# 1	的	de/di2/di4	DE(von:der) 的 地 底	DE/(possessive particle)/of/really and truly/aim/clear
# 2	一	yi1	EYIN	EYIN/one/1/single/a(n)
# 3	是	shi4	SEI	SEI/is/are/am/yes/to be
# 4	不	bu4/bu2	AB¬	(negative prefix)/not/no
# 6	在	zai4	ZU/ZOO:SEI	(located) at/in/exist
# 7	人	ren2	𓂋RENMIN民(mouths)	man/person/people/men
# 8	有	you3	Y+ 又 YAD 🖑 YUD™  𠂇月 yoù yuè 	®/to have/there is/there are/to exist/to be
# 9	我	wo3	WE/WIR	I/me/myself
# 11	这	zhe4/zhei4	CE	CE/this/these/this/these/(sometimes used before a measure word/especially in Beijing)
# 12	个	ge4	je 1 geMENge	®/(a measure word)/individual
# 13	们	men	MENge	®/(plural marker for pronouns and a few animate nouns)
# 14	中	zhong1/zhong4	CIN/CENTER	CIN/center/within/among/in/middle/while (doing sth)/during/China/Chinese/hit (the mark)
# 15	来	lai2	allaive	al/arrive/to come 粒 li:𓂋
# 16	上	SHANG4	SHAN 山 上 升	®/on/on top/upon/first (of two parts)/previous or last (week/etc.)/upper/higher/above/previous/to climb/to go into/above/to go up
# 17	大	da4/dai4	DAR/TALL	™/big/huge/large/major/great/wide/deep/oldest/eldest/doctor
# 18	为	wei2/wei4	WHY/WEIL/WAY/WERD/BE	BE/act as/take...to be/to be/to do/to serve as/to become/because of/for/to
# 20	国	guo2	GAU/KUR	KUR/country/state/nation
# 21	地	de/di4	DE/的/-TY/底	-TY/(subor. part. adverbial)/-ly/earth/ground/field/place/land
# 22	到	dao4	TO	TO/to (a place)/until (a time)/up to/to go/to arrive
# 23	以	yi3	USE / YN	USE/BY/WITH/IN…/according to/so as to/in order to/by/with/because/Israel (abbrev.)
# 24	说	shui4/shuo1	SAY	SAY/persuade (politically)/to speak/to say
# 25	时	shi2	season 🌣 ZEIt 	SEASON/ZEIt/Schicht/o'clock/time/when/hour/period
# 26	要	yao1/yao4	YEARN	YEARN/want/demand/ask/request/coerce/important/vital/to want/to be going to/must
# 27	就	jiu4	JUST JOURN -	JUST/JOURN/at once/then/right away/only/(emphasis)/to approach/to move towards/to undertake
# 29	会	hui4/kuai4	CAN KOINE	CAN/be possible/be able to/to assemble/to meet/to gather/to see/union/group/association/to balance an account/accounting
# 30	可	ke3/*ken	CAN	CAN/may/able to/certain(ly)/to suit/(particle used for emphasis)
# 31	也	ye3	Y+	Y/also/too
# 32	你	ni3	你 ni:er 尔	you