#!/bin/sh

set -e

JRUBY_EXECUTABLE=`which jruby`
JRUBY_BIN=`dirname $JRUBY_EXECUTABLE`

if ! [ -f target/lib/rhino.jar ]; then
    echo
    echo Rhino JavaScript has not been downloaded.
    echo Review the contents of
    echo "   fetch_dependencies.sh"
    echo then run it from this directory to fetch the dependencies.
    echo
    exit 1
fi

mkdir -p target/classes
javac -classpath target/classes:target/lib/*:$JRUBY_BIN/../lib/jruby.jar -d target/classes -Xlint:unchecked `find src -name *.java | xargs echo`
