#!/bin/env bash

set -e

# Compile all files
javac -cp po-uilib.jar $(find ggc/ -iname "*.java")