import string
import urllib.request
import os
import time
from bs4 import BeautifulSoup # pipi BeautifulSoup4 4!!!

abc = "L"+string.ascii_uppercase+"ĜŞŚŢŠ"

def fix_img_alts(soup):
	# Find all the img tags in the HTML
	img_tags = soup.find_all('img')

	# Loop through each img tag and replace it with a text tag
	for img in img_tags:
	    alt_text = img.get('alt', '')
	    if alt_text:
	        new_tag = soup.new_tag('p')
	        new_tag.string = "\n"+alt_text
	        img.replace_with(new_tag)

import re


for x in abc:
	if x=="Ţ": x="TT"
	if x=="Š": x="SH"
	if x=="Ĝ": x="NG"
	try:
	# if 1>0:
		# download(f"http://psd.museum.upenn.edu/epsd/espd-{x}.html")
		# html_doc=open(f"cf-toc-{x}.html").read()
		# html_doc=open(f"{x}.html").read()
		html_doc=open(f"epsd-{x}.html").read()
		soup = BeautifulSoup(html_doc, features="html.parser")

		usages_divs = soup.find_all('div', {'class': 'usages'})
		for usages_div in usages_divs:
			usages_div.remove()
		usages_divs = soup.find_all('span', {'class': 'cont'})
		for usages_div in usages_divs:
			usages_div.remove()

		fix_img_alts(soup)
		text = soup.get_text(separator=' ')
		text = re.sub(r"\s*(\d+)", r"\1", text)
		text = re.sub(r"(\d+\.)", r" \1", text)
		text = text.replace(" - ", "-")
		text = text.replace(" -", "-")
		text = text.replace("- ", "-")
		text = text.replace("[ ", "[")
		text = text.replace(" ]", "]")


		# print(soup.text)
		# open(f"{x}.txt","w").write(text)
		# open(f"cf-toc-{x}.txt","w").write(text)
		# open(f"cf-toc-{x}.txt","w").write(text)
		open(f"epsd-{x}.txt","w").write(text)
		# exit()
	except:
		pass