#!/usr/local/bin/python3
#ln /me/dev/python/extensions.py
from extensions import * 
import bs4 #html soup 
from bs4 import BeautifulSoup
base="https://en.wiktionary.org/"

link="wiki/Category:Coptic_lemmas"
alphabet="Ⲁ Ⲃ Ⲥ Ⲇ Ⲉ Ⲋ Ⲅ Ϩ Ϫ Ⲓ Ϧ Ⲗ Ⲙ Ⲛ Ⲟ Ⲡ Ϥ ⳁ Ϭ Ϯ Ⲩ Ⲫ Ⲱ Ⲝ Ⲯ Ⳉ Ⲑ Ϣ Ⲋ Ⲝ".split(" ")

# link="wiki/Category:Gothic_lemmas"
# alphabet="𐌰 𐌱 𐌲 𐌳 𐌴 𐌵 𐌶 𐌷 𐌸 𐌹 𐌺 𐌻 𐌼 𐌽 𐌾 𐌿 𐍀 𐍂 𐍃 𐍄 𐍅 𐍆 𐍇 𐍈 𐍉".split(" ")

while 1:
	print(link)	
	html=fetch(base+link)
	soup = BeautifulSoup(html,features="html.parser")
	words = columns = soup.findAll('a')
	for word in words:
		# if len(word.text)>1 and word.text[0] in alphabet:
			print(word.text)
	columns = soup.findAll('a', text = re.compile('next page')) #, attrs = {'class' : 'pos'}
	if not columns: break
	link=columns[0]['href'] # .href doesn't in python!!



# print(soup)
