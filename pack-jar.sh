#!/bin/env bash

set -e

echo "Building"
rm -f ggc/**/*.class
./build.sh

echo "Creating jar"
rm -f ggc.jar
jar --create --file ggc.jar ggc/