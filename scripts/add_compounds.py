#!/usr/local/bin/python3
import sys
sys.path.append('/me/dev/script/python/')

from extensions import * 
from WiktionaryParser.wiktionet import word
sounds={}

def fix_sound(r):
		r=r.replace("a1","ā")
		r=r.replace("a2","á")
		r=r.replace("a3","ǎ")
		r=r.replace("a4","à")
		r=r.replace("an1","ān")
		r=r.replace("an2","án")
		r=r.replace("an3","ǎn")
		r=r.replace("an4","àn")
		r=r.replace("ang1","āng")
		r=r.replace("ang2","áng")
		r=r.replace("ang3","ǎng")
		r=r.replace("ang4","àng")
		r=r.replace("u1","ū")
		r=r.replace("u2","ú")
		r=r.replace("u3","ǔ")
		r=r.replace("u4","ù")
		r=r.replace("un1","ūn")
		r=r.replace("un2","ún")
		r=r.replace("un3","ǔn")
		r=r.replace("un4","ùn")
		r=r.replace("e1","ē")
		r=r.replace("e2","é")
		r=r.replace("e3","ě")
		r=r.replace("e4","è")
		r=r.replace("er1","ēr")
		r=r.replace("er2","ér")
		r=r.replace("er3","ěr")
		r=r.replace("er4","èr")
		r=r.replace("en1","ēn")
		r=r.replace("en2","én")
		r=r.replace("en3","ěn")
		r=r.replace("en4","èn")
		r=r.replace("eng1","ēng")
		r=r.replace("eng2","éng")
		r=r.replace("eng3","ěng")
		r=r.replace("eng4","èng")
		r=r.replace("i1","ī")
		r=r.replace("i2","í")
		r=r.replace("i3","ǐ")
		r=r.replace("i4","ì")
		r=r.replace("in1","īn")
		r=r.replace("in2","ín")
		r=r.replace("in3","ǐn")
		r=r.replace("in4","ìn")
		r=r.replace("ing1","īng")
		r=r.replace("ing2","íng")
		r=r.replace("ing3","ǐng")
		r=r.replace("ing4","ìng")
		r=r.replace("o1","ō")
		r=r.replace("o2","ó")
		r=r.replace("o3","ǒ")
		r=r.replace("o4","ò")
		r=r.replace("on1","ōn")
		r=r.replace("on2","ón")
		r=r.replace("on3","ǒn")
		r=r.replace("on4","òn")
		r=r.replace("ong1","ōng")
		r=r.replace("ong2","óng")
		r=r.replace("ong3","ǒng")
		r=r.replace("ong4","òng")
		return r


for row in tsv("/data/words/chinese.freq4000-1000.tsv"):
	sounds[row[1]]=fix_sound(row[4])

for row in tsv("./Chinese_radicals.tsv"):
	if(len(row))<5:continue 
	for s in row[1]:
		sounds[s]=row[4].split("/")[0]

for row in tsv("chinese.freq.tsv"):
	arg=row[1]
	phon=row[2].split("/")[0]
	sounds[arg.strip()]=fix_sound(phon)

sounds['']=''
sounds['⿰']=''
sounds['⿱']=''
sounds['⿰']=''
sounds['⿷']=''
sounds['⿵']=''
sounds['⿸']=''
sounds['⿺']=''
sounds['⿻']=''
sounds['⿴']=''
sounds['⿲']=''
sounds['⿹']=''
sounds['⿳']=''
sounds['⿶']=''
sounds['⿳']=''

sounds['[']=''
sounds[']']=''
sounds['|']=''
sounds['𠂌']=''
sounds['𠂋']=''
sounds['𢀖']=''
sounds['𠮛']=''
sounds['𠁤']=''
sounds['𠃜']=''
sounds['𬺰']=''

sounds[' ']=''

sounds['']=''
sounds['']=''
sounds['一']='yī'
sounds['䖵']='~chóng'
sounds['劦']='lì'
sounds['畺']='jiāng'
sounds['習']='xiyǔ'
sounds['屯']='dùnb'
sounds['呙']='*gwo'
sounds['畏']='wei'
sounds['更']='geng'
sounds['亦']='yì'
sounds['艮']='gēn'
# sounds['𤴓']=sounds['']
sounds['饣']=sounds['食']
sounds['纟']=sounds['糸']='mì'
sounds['犭']=sounds['豸']='zhì'

sounds['龺']=sounds['𠦝']=sounds['卓']#zhuó stand
sounds['𧾷']=sounds['足']
sounds['𥫗']=sounds['竹']
sounds['氵']=sounds['水']
sounds['⺮']=sounds['竹']
sounds['衤']=sounds['衣']
sounds['⺭']=sounds['衣']
sounds['⻂']=sounds['衣']
sounds['巜']=sounds['巛']
sounds['乛']=sounds['乙']
sounds['𠂇']=sounds['又'] #!
sounds['䒑']=sounds['艸']
sounds['𠄌']=sounds['亅']
sounds['𠃌']=sounds['亅']
sounds['𠂉']=sounds['亻']
sounds['䏍']=sounds['肙']='yuàn' #worm/wound!
sounds['㝵']='*day' #sounds['得']
sounds['亍']=sounds['行'] # 彳 / Simplified
sounds['𦍌']=sounds['羊']
sounds['𧘇']=sounds['衣']
sounds['𠃊']=sounds['乙']
sounds['龰']=sounds['止']
sounds['𠬝']=sounds['服']
sounds['咼']=sounds['咼']='kuāi'
sounds['𤣩']=sounds['玉'] #!
sounds['𤴓']=sounds['正'] 
sounds['戉']='~成' #sounds['成'] # axe /*ɢʷad/
sounds['䖝']=sounds['蟲'] 
sounds['']=sounds[''] 


sounds['戋']=sounds['戔']='jiān' #㦮 Simplified
sounds['猗']='yī!' #sounds['奇']# yī! interj. 
sounds['夬']='guài' # cut/certain

sounds['勹']='bāo'
sounds['畐']='fú' # full/width
sounds['丆']='myeon?'
sounds['乜']='miē' # 'what'
sounds['冄']='*hairy' #     枏, 蚦, 衻, 㚩, 耼, 䑙, 䛁, 䒣, 髥, 䫇
sounds['肰']='rán' # 肉 rou ~ 肰 rantanplan=meat
sounds['咅']='*pǒuke'
sounds['昷']='*mong'
sounds['臣']='chén'
sounds['髟']='biāo' # hair
sounds['隹']='zhuī'
sounds['钅']='jin1'
sounds['氵']='shui'
sounds['匚']='fāng'
sounds['⻗']='yǔ'
sounds['刖']='yuèt' # cut
sounds['弔']='dì' # /吊 diao
sounds['冋']='jiōng'# ~ jian
sounds['𭕄']='?'
sounds['龶']='?'
sounds['龸']='?'
sounds['曲']='qū®' # curve@JP!!
sounds['𠃋']='?arm/gong' #
sounds['乂']='yì' # 刈 / ài 
sounds['亼']='*shūƫ'
sounds['咢']='*gaku/kao/è'
sounds['彖']='*Sau' # tan
sounds['㒸']='*Säue' # 彖
sounds['卂']='*shin'
sounds['民']='mín'
sounds['果']='guǒ'
sounds['甫']='pu!'
sounds['末']='mò'
sounds['']=''
sounds['']=''
sounds['']=''
sounds['钅']=sounds['⻐']='jīn' # wtf UTF # 金⻐
sounds['艹']=sounds['艸']='cǎo' # 艹⺾⺿⻀


for row in tsv("chinese.freq.tsv"):
	arg=row[1]
	w=word(arg)
	cs=w.compounds()
	if cs:
		compound=cs[0].replace("[","").replace("]","")
		compound=compound.split("(")[0].replace("<small>","")
		compound=compound.split(",")[0].replace("<small>","")
		ps=w.pronounciations()
		if len(ps)>=1 and not "lang=" in ps[0]:
			ps=ps[0]
		else:
			ps=fix_sound(sounds[row[1]])
		ps = ps.split(" ")[0]
		ps = ps.split(",")[0]
		if not row[1] in sounds: 
			sounds[row[1]]=fix_sound(ps)
		# phon=" ".join(map(lambda x:sounds[x],compound))
		# phon=" ".join(map(lambda x:(sounds[x] if x in sounds else '?'),compound))
		neu=ps+row[1]+"="+compound
		phon=" ".join(map(lambda x:(sounds[x] if x in sounds else '?'),compound))
		# neu="+".join(cs)
		r=neu+" "+phon
		r=r.replace('  ',' ')
		# row[4]=row[4]+"\t"+r
		row[5]=fix_sound(r)
	print("\t".join(row))

	# for col in row