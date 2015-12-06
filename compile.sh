#!/bin/sh

JRUBY_EXECUTABLE=`which jruby`
JRUBY_BIN=`dirname $JRUBY_EXECUTABLE`

mkdir -p built/template
javac -classpath built:$JRUBY_BIN/../lib/jruby.jar -d built -Xlint:unchecked `find template -name *.java | xargs echo`
