#!/bin/sh

if [ ! -e bin/ ]; then
  mkdir bin
fi

javac -cp lib/lucene-core-2.9.0.jar -source 1.5 -target 1.5 \
  -d bin/ `find src/ -name *.java`

