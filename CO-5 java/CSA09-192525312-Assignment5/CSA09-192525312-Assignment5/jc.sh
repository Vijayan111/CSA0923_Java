#!/bin/bash
# helper: javac via the jdk.compiler module (no separate javac binary in this sandbox)
java -m jdk.compiler/com.sun.tools.javac.Main "$@" 2>&1 | grep -v "Picked up"
exit ${PIPESTATUS[0]}
