#!/usr/local/bin/python3
# https://en.wiktionary.org/wiki/Category:Gothic_nouns
from wiktionaryparser import WiktionaryParser
parser = WiktionaryParser()
word = parser.fetch('test')
word = parser.fetch('𐌲𐌰𐍂𐌳𐍃')
# another_word = parser.fetch('test', 'french')
# parser.set_default_language('french')
print(word)
print(another_word)