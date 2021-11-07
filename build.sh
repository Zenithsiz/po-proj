#!/bin/env bash

set -e

# Compile all files
javac -Xlint:unchecked -cp po-uilib.jar $(find ggc/ -iname "*.java")