#!/bin/sh
set -e

export TEMPLATE=$1
export VIEW=$2

export CLASSPATH=target/classes:target/lib/compile/*
java -cp $CLASSPATH org.jruby.Main <<__EOR
template_file = ENV['TEMPLATE']
view_file = ENV['VIEW']

def fail(msg); puts msg; exit 1; end
fail("No template specified on command line") unless template_file
require 'java'
require 'json'

template_source = File.open(template_file, "r:UTF-8") { |f| f.read }
view_source = (view_file && view_file.length > 0) ? File.open(view_file, "r:UTF-8") { |f| f.read } : nil

# See if it the template looks like a test file, and if so, pick out the template and view
template_source_split = template_source.split("\n---\n")
if template_source_split.length >= 3
  template_source = template_source_split[1]
  view_source = template_source_split[2] if view_source.nil?
end

template = Java::OrgHaploTemplateHtml::Parser.new(template_source, "tree").parse()
STDOUT.write template.dump()
if view_source
  view = JSON.parse(view_source)
  driver = Java::OrgHaploTemplateDriverJrubyjson::JRubyJSONDriver.new(view)
  puts template.renderString(driver)
end
__EOR
