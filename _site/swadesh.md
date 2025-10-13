-e ---
---
[https://en.wiktionary.org/wiki/Appendix:Swadesh_lists](https://en.wiktionary.org/wiki/Appendix:Swadesh_lists)  
[swadesh tsv lists](https://github.com/pannous/swadesh)

```  
import nltk  
nltk.download('swadesh')  
from nltk.corpus import swadesh  
print(swadesh.entries(['de']))  
