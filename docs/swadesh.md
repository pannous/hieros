[[https://en.wiktionary.org/wiki/Appendix:Swadesh_lists]]  
```  
import nltk  
nltk.download('swadesh')  
from nltk.corpus import swadesh  
print(swadesh.entries(['de']))  