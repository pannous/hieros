#!/usr/local/bin/node
require('./extensions.js')
lines=read_lines("Sumerian_Cuneiform_Dictionary.txt")
for(line of lines){
  while(true){
  m=line.match(/12\w\w\w/)
  if(m){
    nr=m[0]
    z=chr(parseInt(nr,16))
    line=line.replace(nr,z)
  }else break
  }
  console.log(line)
}