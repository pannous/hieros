import re
import string
abc = "L"+string.ascii_uppercase+"ĜŞŚŢŠ"


for x in abc:
	if x=="Ţ": x="TT"
	if x=="Š": x="SH"
	if x=="Ĝ": x="NG"
	try:
		ls=open(f"epsd-{x}.txt").read()
	except:
		continue
	ls=ls.split("\n")
	for l in ls:
		if "cuneiform" in l:
			# if "la6" in l:
			# 	print(l)
			l=l.replace("cuneiform ","")
			l = re.sub(r"\+\-.*", "", l)
			l = re.sub(r"\(.*?\)", "", l)
			l = re.sub(r"\[\d+\]", "", l)
			l = re.sub(r"(\d\.)", " \1", l)
			l = re.sub(r"(\d\.)", " \1", l)
			l = re.sub(r"\. \|", ".|", l)
			l = re.sub(r"Akk...", "#", l)
			l = re.sub(r" x", "×", l)
			l = re.sub(r"See .*", "", l)
			l = re.sub(r"ĝeš ", "", l)
			l = re.sub(r"", "", l)
			l = re.sub(r"  ", " ", l)
			print(l)
			# if "la6" in l:
			# 	print(l)
