#!/usr/bin/env node
require("/me/js/ext.js")();
// ls=read_lines("greek.txt.bak")
// ls=read_lines("greek.phi.orig")
ls=read_lines("test")
all=true
// map={ἐ:'e',ἔ:'e',έ:'e', Ἕ:'He',ὲ:'er',ὔ:"gy", ὑ:"hy", ὕ:"hy", η:"e", ή:"e", ῆ:"i",ὴ:"o", ῄ:"i", ῃ:"i",ῇ:"i",ἦ:"ei",
//  ἄ:"a", ἀ:"a",ὰ:"a", έ:"e", ἱ:"hi",ἶ:"i",ὶ:"i", ἵ:"hi",  ι:"i", ῖ:"j", ί:"i", ἴ:"j", ἷ:"j", ἰ:"j",
//  ὄ:"ai", ό:"o", ὸ:"o",ὁ:'o',ο:'o',
//  ω:"o", ω:"o", Ὡ:"Ho", ὠ:"ju", ὡ:"ju", ὥ:"ju", ὦ:"jo", ᾠ:"jo", ὧ:"ju", ῶ:"o", ώ:"o", ῳ:"u",
//   β:"b", χ:"ch", ζ:"z", ρ:"r", ξ:"x", λ:"l", μ:"m", ν:"n", π:"ƥ", φ:"sh", Φ:"Ph", ψ:"ps", θ:"th", σ:"s",τ:"dth",α:"a",ô:"o",ø:"o",ī:'i',
//   υ:'y',ῦ:'u',ὺ:'u',
//   γ:'g',ƥ:'p',ά:'a',ė:'e',ύ:'u',κ:'k',ε:'e',δ:'d',ὐ:'y',ἁ:'a',ἅ:'a',ᾶ:'a'}
map={ἐ:'e',ἔ:'e',έ:'e', Ἕ:'He',ὲ:'er',ὔ:"gy", ὑ:"hy", ὕ:"hy", η:"e", ή:"e", ῆ:"i",ὴ:"o", ῄ:"i", ῃ:"i",ῇ:"i",ἦ:"ei",
 ἄ:"a", ἀ:"a",ὰ:"a", έ:"e", ἱ:"hi",ἶ:"i",ὶ:"i", ἵ:"hi",  ι:"i", ῖ:"j", ί:"i", ἴ:"j", ἷ:"j", ἰ:"j",
 ὄ:"ai", ό:"o", ὸ:"o",ὁ:'o',ο:'o',
 ω:"o", ω:"o", Ὡ:"Ho", ὠ:"ju", ὡ:"ju", ὥ:"ju", ὦ:"jo", ᾠ:"jo", ὧ:"ju", ῶ:"o", ώ:"o", ῳ:"u",
  β:"b", χ:"ch", ζ:"z", ρ:"r", ξ:"x", λ:"l", μ:"m", ν:"n", π:"ƥ", φ:"sh", Φ:"Ph", ψ:"ps", θ:"th", σ:"s",τ:"dth",α:"a",ô:"o",ø:"o",ī:'i',
  υ:'y',ῦ:'u',ὺ:'u',
  γ:'g',ƥ:'p',ά:'a',ė:'e',ύ:'u',κ:'k',ε:'e',δ:'d',ὐ:'y',ἁ:'a',ἅ:'a',ᾶ:'a'}

i=0
for(line of ls){
  i++
   if(!all)  console.log(line)
  for(k in map){
    v=map[k]
    word=word.replace(new RegExp(k,"gi"),v)  
  }

  console.log(word)
 
}