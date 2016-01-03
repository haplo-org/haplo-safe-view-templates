#!/bin/sh

export CLASSPATH=target/classes:target/lib/*
jruby test/test.rb $@
