#!/usr/local/bin/node
require('./extensions.js')
lines=read_lines("cuneiform.csv")
for(line of lines){
  rows=line.split("\t")
  for(row of rows){
    print(hex(ord(row)))
  }
  console.log(line)
}