#!/bin/sh

JRUBY_EXECUTABLE=`which jruby`
JRUBY_BIN=`dirname $JRUBY_EXECUTABLE`

if ! [ -f lib/js.jar ]; then
    echo
    echo Rhino JavaScript has not been downloaded.
    echo Review the contents of
    echo "   lib/fetch_dependencies.sh"
    echo then run it from this directory to fetch the dependencies.
    echo
    exit 1
fi

mkdir -p built/template
javac -classpath built:lib/*:$JRUBY_BIN/../lib/jruby.jar -d built -Xlint:unchecked `find template -name *.java | xargs echo`
