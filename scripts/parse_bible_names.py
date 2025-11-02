#!/usr/bin/env python3
import nltk
from collections import defaultdict

# Ensure that the necessary NLTK packages are downloaded
nltk.download('punkt')
nltk.download('averaged_perceptron_tagger')
nltk.download('maxent_ne_chunker')
nltk.download('words')

def extract_names(text):
    """
    Extract proper names from a text using NLTK.
    """
    for sentence in nltk.sent_tokenize(text):
        for chunk in nltk.ne_chunk(nltk.pos_tag(nltk.word_tokenize(sentence))):
            if hasattr(chunk, 'label') and chunk.label() == 'PERSON':
                yield ' '.join(c[0] for c in chunk)

def parse_bible(bible_text):
    """
    Parse the Bible text file and extract names from each chapter.
    """
    chapter_names = defaultdict(set)
    current_chapter = ""
    chapter_text = ""

    for line in bible_text.splitlines():
        if line.strip():
            # Check if the line indicates a new chapter
            if ":" in line and line.split(":")[0] != current_chapter:
                # Process the previous chapter
                if current_chapter:
                    names = set(extract_names(chapter_text))
                    chapter_names[current_chapter].update(names)

                # Start a new chapter
                current_chapter = line.split(":")[0]
                chapter_text = line
            else:
                chapter_text += " " + line

    # Process the last chapter
    if current_chapter:
        names = set(extract_names(chapter_text))
        chapter_names[current_chapter].update(names)

    return chapter_names

# Example usage
bible_text = """Genesis 1:1 In the beginning God created the heaven and the earth.
Genesis 1:2 And the earth was without form, and void; and darkness was upon the face of the deep...
...
Revelation 22:21 The grace of our Lord Jesus Christ be with you all. Peter, Paul, and the Other James. Amen."""

import requests

url = "https://raw.githubusercontent.com/dabignerd/bible.txt/main/bible.txt"
print("download from url" , url)
response = requests.get(url)
bible_text = response.text if response.status_code == 200 else None

print("download DONE from url" , url)

print("parse_bible... ")
chapter_names = parse_bible(bible_text)
print("parse_bible DONE")

for chapter, names in chapter_names.items():
    print(f"{chapter}: {', '.join(names)}")