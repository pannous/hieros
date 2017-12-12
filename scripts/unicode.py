#!/usr/bin/env PYTHONIOENCODING="utf-8" python
import sys
app=sys.argv[0]
arg=sys.argv[1]

try:
    from urllib2 import urlopen
    from urllib import urlretrieve
except ImportError:
    from urllib.request import urlopen, urlretrieve  # py3 HELL

def download(url): # to memory
  return urlopen(url).read()

base="http://www.fileformat.info/info/unicode/char/search.htm?q="
html=download(base+arg)

from bs4 import BeautifulSoup
soup = BeautifulSoup(html)
print soup.text

# from BeautifulSoup import *
# soup = BeautifulSoup(html) # parse
# data=soup.find('div', {"class":contentClass})
# heading = soup.h1
