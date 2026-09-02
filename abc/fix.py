#!/usr/bin/env python3
nr=0
file="byblos.tsv"
lines=open(file).readlines()
for l in lines:
	print(nr,l,sep="\t")
	nr+=1