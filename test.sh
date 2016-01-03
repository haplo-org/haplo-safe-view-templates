#!/bin/sh

export CLASSPATH=target/classes:target/lib/*
java -cp $CLASSPATH org.jruby.Main test/test.rb $@
