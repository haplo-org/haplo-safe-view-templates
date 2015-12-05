#!/bin/sh

mkdir -p built/template
javac -classpath built -d built -Xlint:unchecked `find template -name *.java | xargs echo`
