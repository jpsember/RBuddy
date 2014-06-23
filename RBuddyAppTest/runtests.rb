#!/usr/bin/env ruby

require 'js_base'

# Ruby script:  runtests.rb

if true
  puts "(skipping clean; do 'ant clean' beforehand if desired)"
else
  puts "ant clean..."
  scall('ant clean')
end

puts "ant debug..."
scall('ant debug')

puts "ant debug install..."
scall('ant debug install')

puts "ant test..."
result,success = scall('ant test')

if (!success) || result.include?("FAILURES!!!")
  puts result
  die "\n\n...test failures detected, aborting!"
end

puts "...done"
