#!/usr/local/bin/python  # <<<<<<<< for TEXTMATE !!!!!!
# -*- coding: utf-8 -*-  
#!/usr/bin/env PYTHONIOENCODING="utf-8" python
import sys
mapping_file="Linear_B.csv"
dic={}
for line in open(mapping_file):
    import re
    line=re.sub(r'#.*','',line)
    if len(line.strip()) == 0 :
      continue
    l=line.strip().replace("\t"," ").replace("  "," ").split(" ")
    sign, rest = l[0], l[1:] 
    trans="".join(rest).decode('utf8','ignore')
    if len(trans.strip()) == 0 :
      continue
    dic[sign]=trans
    dic[sign.lower()]=trans
    dic[trans]=sign
    dic[trans.lower()]=sign

debug=False
# debug=True

file="Linear-B-Lexicon.txt.orig"
text=open(file).read()
for line in map(str.strip,text.split("\n")):
  if "-" in line:
    print ""
  print line
  if not "-" in line:
    continue
  if "http" in line:
    continue
  signs=line.strip().replace("\t"," ").replace(",","-,").replace("(","-(").replace(")","-)").replace("/","-/").replace(" ","- -").split("-")
  for s in signs:
    if s in dic:
      if debug: 
        print " - ",s,dic[s],#" - ",
      else: 
        print dic[s],
    else:
      print s,
  print ""