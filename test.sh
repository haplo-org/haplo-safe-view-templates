#!/bin/sh

set -e

BASE_DIR="`dirname $0`"
GEM_HOME="$BASE_DIR/target/rubygems"
CLASSPATH='target/classes:target/lib/test/*:target/lib/compile/*'

if ! [ -d target/classes/org/haplo/template/html ]; then
    echo
    echo Before running the tests, fetch dependencies and compile with
    echo "   mvn test-compile"
    exit 1
fi

function jruby {
    java -cp "$CLASSPATH" org.jruby.Main "$@"
}

jruby "$GEM_HOME/bin/rspec" src/test/ruby/test.rb
