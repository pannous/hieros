#!/usr/bin/env python3
import os
import sys
import subprocess

# read docs/epilogue.md to array
command = "/Users/me/uruk_egypt/scripts/cuneiformize.js"
filename="/Users/me/uruk_egypt/texts/oracc/AD-332B"
if len(sys.argv)>1:
	filename=sys.argv[1]
# else:
# 	print("ONLY ONE FILE to cuneiformize")
print(command)
print("AUTO cuneiformize.js of ", filename)

file = open(filename, "r")
lines = file.readlines()
file.close()
go = False
for line in lines:
	if line[0]=='#':
		print(line)
	else:
		output = subprocess.check_output([command,line])
		if output:
			print(output.decode('utf-8').strip())
		print(line.strip())
		go = False
