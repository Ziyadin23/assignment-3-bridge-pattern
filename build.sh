#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
compiler="${JAVA_HOME:+$JAVA_HOME/bin/}javac"
mkdir -p build/classes
find src/main/java src/test/java -name '*.java' | sort > build/sources.txt
"$compiler" --release 17 -encoding UTF-8 -Xlint:all -Werror -d build/classes @build/sources.txt
