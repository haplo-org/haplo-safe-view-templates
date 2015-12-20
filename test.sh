#!/bin/sh

export CLASSPATH=built:lib/*
jruby test/test.rb $@
