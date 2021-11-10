#!/bin/env bash

set -e

# Build
echo "Building"
./build.sh

# Then run
echo "Running"
java "$@" -ea -cp po-uilib.jar:. ggc.app.App