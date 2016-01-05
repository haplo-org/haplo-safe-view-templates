#!/bin/sh

if ! [ -f target/lib/rhino.jar ]; then
    echo
    echo Dependencies have not been downloaded.
    echo Review the contents of
    echo "   fetch_dependencies.sh"
    echo then run it to fetch the dependencies.
    echo
    exit 1
fi

if ! [ -d target/classes/org/haplo/template/html ]; then
    echo
    echo Before running the tests, compile with
    echo "   mvn compile"
    exit 1
fi

export CLASSPATH=target/classes:target/lib/*
java -cp $CLASSPATH org.jruby.Main test/test.rb $@
