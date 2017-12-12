#!/usr/bin/env ruby
# encoding: utf-8 # Basically, you have script encoding, internal encoding, and external encoding
Encoding.default_external="UTF-8"
Encoding.default_internal="UTF-8"
 # export LC_ALL=en_US.UTF-8
# $ export LANG=en_US.UTF-8
# ^^ gegen invalid multibyte char (US-ASCII)
file="/Users/me/dev/hieroglyph list.txt"
puts "A1    í Œí°€"
File.open(file, :encoding => "UTF-8").readlines.each  do |line|
# IO.readlines("/Users/me/dev/hieroglyph list.txt",:encoding => "UTF-8").each do |line|
  # puts line
end