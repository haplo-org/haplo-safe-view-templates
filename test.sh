#!/bin/sh

if ! [ -d target/classes/org/haplo/template/html ]; then
    echo
    echo Before running the tests, fetch dependencies and compile with
    echo "   mvn package"
    exit 1
fi

export CLASSPATH=target/classes:target/lib/*
java -cp $CLASSPATH org.jruby.Main test/test.rb $@
