import string
import urllib.request
import os
import time

abc = string.ascii_uppercase+"ĜŞŚŢŠ"

def download(url):
	fname = url.split("/")[-1]
	if os.path.exists(fname): return
	print(url)
	try:
		print("OK "+url)
		urllib.request.urlretrieve(url, fname)
	except:
		print("Error "+url)

for x in abc:
	if x=="Ţ": x="TT"
	if x=="Š": x="SH"
	if x=="Ĝ": x="NG"
	download(f"http://psd.museum.upenn.edu/epsd/epsd-{x}.html")
	# download(f"http://psd.museum.upenn.edu/epsd/cf-toc-{x}.html")
	# download(f"http://psd.museum.upenn.edu/epsd/akkadian-toc-{x}.html")

