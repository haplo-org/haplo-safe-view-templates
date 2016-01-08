#!/bin/sh

set -e

BASE_DIR="`dirname $0`"
GEM_HOME="$BASE_DIR/gems"
GEM_PATH="$BASE_DIR/gems"
CLASSPATH='target/classes:target/lib/test/*:target/lib/compile/*'

if ! [ -d target/classes/org/haplo/template/html ]; then
    echo
    echo Before running the tests, fetch dependencies and compile with
    echo "   mvn package"
    exit 1
fi

function jruby {
    java -cp "$CLASSPATH" org.jruby.Main "$@"
}

jruby src/test/ruby/gems.rb
jruby "$GEM_HOME/bin/rspec" src/test/ruby/test.rb
jruby src/test/ruby/test.rb "$@"
