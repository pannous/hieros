#!/usr/local/bin/python3

from bs4 import BeautifulSoup # pipi BeautifulSoup4 4!!!



from extensions import * 

books=["Mark","Luke","Matthew","John"]

book="Mark"

bible="https://www.stepbible.org/?q=version=Wulfila|reference=%s.%d"

old={}

def parse(html):
	print("OK")
	# print(html)
	soup = BeautifulSoup(html,features="html.parser")
	data=soup.find_all('span', {"class":"verse"})
	for verse in data:
		nr=verse.find('span', {"class":"verseNumber"})
		# print("Mark"nr.text)
		nr.decompose()
		# print(verse.text)
		for word in verse.text.split(" "):
			if not word in old:
				print(word.replace(",","").replace(";","").replace(".","").replace(":",""))
				old[word]=1

for i in range(1,17):
	file = "bible/%s.%d"%(book,i)
	try:
		parse(read_text(file))
	except Exception as e:
		download(bible%(book,i),file,true)
		parse(read_text(file))
	# if exists(file): parse(read_text(file))


