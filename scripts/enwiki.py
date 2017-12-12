#!/usr/local/bin/python
# encoding: utf-8
import extensions
from extensions import *
from xml.dom import minidom, Node # USE cElementTree for FAST stream or BeautifulSoup for html !!™

# https://en.wiktionary.org/w/api.php?action=query&titles=money&prop=revisions&rvprop=content&format=xml
base_url="https://en.wiktionary.org/w/api.php?action=query&prop=revisions&rvprop=content&format=xml&titles="
base_url_xml="https://en.wiktionary.org/w/api.php?action=query&prop=revisions&rvprop=content&format=xml&rvexpandtemplates=true&titles="
http_base="https://en.wiktionary.org/w/index.php?title="

title="Category:Ugaritic_nouns"

html=wget(http_base+title)
link_ex=regex("a href=\"/wiki/(.*)\"")
# str.matches=regex_matches
# links=html.matches(link_ex)
# print(links)
links=match(html,link_ex)
# links=matches(html,link_ex)
print(links)

quit()

def getChild(self,name):
	if name=="__str__": return self.__repr__
	if name=="text": return self.firstChild.nodeValue
	# if name=="__str__": return lambda:self.localName# self.toxml
	if name=="xml": return self.toxml()
	xs=self.getElementsByTagName(name)
	return self.getAttribute(name) or len(xs)==1 and xs[0] or xs

minidom.Element.__getitem__=getChild
minidom.Element.__getattr__=getChild
import xml.dom.minidom
# xmldoc = minidom.parseString(wget(base_url+title))
xmldoc = minidom.parseString(wget(base_url_xml+title))

root = xmldoc.documentElement
# print(root)
# print(root.xml)
page=root['query']['pages']['page']
# minidom.Element.__getattr__=minidom.Element.getAttribute
# minidom.Element.__getattr__=getChild
# print(root.query)
# print(root.toxml())
# print(page.title)
print(page['title'])
text=page['revisions']['rev']
print(text)
print(text.text)