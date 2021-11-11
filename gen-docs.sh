#!/bin/env bash

find ggc/core -type f -name "*.java" \
	| xargs javadoc \
		--show-members private \
		--show-types private \
		-cp po-uilib.jar:. \
		-d docs \
	2>&1 1>/dev/null