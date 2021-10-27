#!/bin/env bash

set -e

# Compile all files
javac -cp po-uilib-v15.jar $(find ggc/ -iname "*.java")