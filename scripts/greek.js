#!/usr/bin/env node
require("/me/js/ext.js")();
// ls=read_lines("greek.txt.bak")
// ls=read_lines("greek.phi.orig")
ls=read_lines("test")
all=true
map={ἐ:'e',ἔ:'e',ὔ:"gy", ὑ:"hy", ὕ:"hy", η:"e", ή:"e", ῆ:"i", ῄ:"i", ῃ:"i", ἄ:"a", ἀ:"a", έ:"e", ἱ:"hi", ἵ:"hi", ὄ:"ai", ι:"i", ῖ:"j", ί:"i", ἴ:"j", ἷ:"j", ἰ:"j", ό:"o", ω:"o", ω:"o", Ὡ:"Ho", ὠ:"ju", ὡ:"ju", ὥ:"ju", ὦ:"jo", ᾠ:"jo", ὧ:"ju", ῶ:"o", ώ:"o", ῳ:"u", β:"b", χ:"ch", ζ:"z", ρ:"r", ξ:"x", λ:"l", μ:"m", ν:"n", π:"ƥ", φ:"sh", Φ:"Ph", ψ:"ps", θ:"th", σ:"s",τ:"t",α:"a",ô:"o",ø:"o",ī:'i',υ:'y',ῦ:'u',γ:'g',ƥ:'p',ά:'a',ė:'e',ύ:'u',κ:'k',ε:'e',δ:'d',ὐ:'y',ἁ:'a',ἅ:'a',ᾶ:'a'}
// ύ:'o',  vs δεύτεric
// ἐk:'ex',
i=0
for(line of ls){
  i++
  // if(i<1130)continue
  if(!all)  console.log(line)
    // if(i<130)continue
  line=line.replace(',',' ')
  if(!line.match(' '))continue
  if(!all) word=word2=line.split(' ')[1]
    else  word=word2=line
  // word=word.replace(/Ἀ/g,"A")
  // word=word.replace(/Ἅ/g,"Ha")
  
  // word=word.replace(/χρ/g,"kur")
  // word=word.replace(/κρ/g,"kur")
  word=word.replace(/άω/g,"ow")
  word=word.replace(/όω/g,"ow")
  word=word.replace(/ὁμ/g,"un")// hack!! <<
  word=word.replace(/εὐδ/g,'ɠuδ')// ju  δ
  word=word.replace(/εὐ/g,'eu')// juδ
  word=word.replace(/ἀπο/g,"apo.")
  word=word.replace(/ἄρ/g,"a'ra")
  word=word.replace(/$ἀ/g,"a'")
  
  
  word=word.replace(/μαι/g," moi")
  word=word.replace(/γραφ/g,"graph")  
  word=word.replace(/ὐγ/g,"ug")
  // word=word.replace(/\.ος/,"os")
  // word=word.replace(".ος","ôs")
  // word=word.replace(".ος","")
  // if(line.match("the ")){// Adj... !
    word=word.replace(/ός$/,"ic")
  word=word.replace(".ος","ic")
// }else{
//     word=word.replace(/ός$/,"us")
//     word=word.replace(".ος","us")
// }
    
  // word=word.replace(/υ/g,"y")
  // word=word.replace(/υ/g,"ü") // ~    3017 Λóyΐ      Levi
  // word=word.replace(/ύ/g,"oi")
  word=word.replace(/ὔ/g,"gy")
  // word=word.replace(/ὐ/g,"g") αὐτόματος  
  // word=word.replace(/ὑ/g,"hὑ")// hypo husband
  word=word.replace(/ὑ/g,"hύ")// hypo husband
  word=word.replace(/ὕ/g,"hy")
  
  
  // word=word.replace(/η/g,"i")
  // word=word.replace(/ή/g,"ie")
  word=word.replace(/η/g,"ė")
  word=word.replace(/ή/g,"ė")
  word=word.replace(/ῆ/g,"i")
   word=word.replace(/ῄ/g,"i")
    word=word.replace(/ῃ/g,"i")
   
  
       // word=word.replace(/ἄ/g,"an.")// un  ANGEL!
          // word=word.replace(/ἄ/g,"an")// un  ANGEL!
       // word=word.replace(/ἄ/g,"un-")// un
         // word=word.replace(/ἄ/g,"un.")// un
   // word=word.replace(/ἄ/g,"ain")
  word=word.replace(/ἀ/g,"a")
  // word=word.replace(/ά/g,"o") nee
  // word=word.replace(/α/g,"ra") 
  // word=word.replace(/α/g,"ra") α o
  // word=word.replace(/ε/g,"ė")// like o !!!! <<<
  // word=word.replace(/ε/g,"ó")// like o !!!! <<<
  // word=word.replace(/ε/g,"œ")  
  // word=word.replace(/ε/g,"er")
  word=word.replace(/έ/g,"øī") 
  word=word.replace(/ἱ/g,"hi")
  word=word.replace(/ἵ/g,"hi")
  // word=word.replace(/ἶ/g,"i")// ƒ !

  // word=word.replace(/ὀ/g,"a")  
  // word=word.replace(/ὁ/g,"ho")
  word=word.replace(/ὄ/g,"ai")
  
  
  // word=word.replace(/ι/g,"i")
  word=word.replace(/ῖ/g,"j")
  // word=word.replace(/ί/g,"i")
  word=word.replace(/ἴ/g,"j")
  word=word.replace(/ἷ/g,"j")
  // word=word.replace(/ἰ/g,"j")
  word=word.replace(/o/g,"ə")
  word=word.replace(/ό/g,"ə")
  // word=word.replace(/ό/g,"o")
  word=word.replace(/ω/g,"o")
  word=word.replace(/ω/g,"o")
  word=word.replace(/Ὡ/g,"Ho")
  
  word=word.replace(/ὠ/g,"ju")  
  word=word.replace(/ὡ/g,"ju")  // ja  
  word=word.replace(/ὥ/g,"ju")    
  word=word.replace(/ὦ/g,"jo")      
  word=word.replace(/ᾠ/g,"jo")

  word=word.replace(/ὧ/g,"ju")        
    word=word.replace(/ῶ/g,"u")        
        word=word.replace(/ώ/g,"o")
        word=word.replace(/ῳ/g,"u")
              
    
  
    word=word.replace(/β/g,"b") // foth fuß
    // word=word.replace(/β/g,"bh")
  // word=word.replace(/β/g,"ß")
  // word=word.replace(/γ/g,"g")
  // word=word.replace(/κ/g,"c")
  // word=word.replace(/χ/g,"x")
  word=word.replace(/χ/g,"ch")
  word=word.replace(/ζ/g,"c")
  word=word.replace(/ρ/g,"r")
  word=word.replace(/ξ/g,"x")
  // word=word.replace(/ξ/g,"xr")
  word=word.replace(/λ/g,"l")
  word=word.replace(/μ/g,"m")
  word=word.replace(/ν/g,"n")
  
  // word=word.replace(/π/g,"p")// ph f : πέμπτ.ος    the fifth phimpht pemptos pentos << !! will
  word=word.replace(/π/g,"ƥ")  
  // word=word.replace(/π/g,"ph")// p
  // word=word.replace(/φ/g,"ph")// f
  word=word.replace(/φ/g,"sh")// sh << Detect ancient words
  word=word.replace(/Φ/g,"Ph")// f
  
  word=word.replace(/ψ/g,"ps")
  
  // word=word.replace(/τ/g,"th")// th? sh?
  // word=word.replace(/δ/g,"d") // eighth
  word=word.replace(/θ/g,"th")
  word=word.replace(/σ/g,"s")
  // word=word.replace(/ς/g,"s")   tooth


  word2=word2.replace('.ος','os')
  word2=word2.replace(/άζ$/,'as')
  word2=word2.replace(/ίζ$/,'ic')
  word2=word2.replace(/έ$/,'ie')
  word2=word2.replace(/μαι$/,'')
  
  
  for(k in map){
    v=map[k]
    word2=word2.replace(new RegExp(k,"gi"),v)  
  }
  word2=word2.replace('.os','os')

      console.log(word+"    "+word2)
  console.log("")
 
}