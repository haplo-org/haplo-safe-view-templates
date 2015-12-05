#!/bin/sh

export CLASSPATH=built
jruby test.rb $@
