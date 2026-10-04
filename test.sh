#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
./build.sh
runtime="${JAVA_HOME:+$JAVA_HOME/bin/}java"
exec "$runtime" -Djava.awt.headless=true -cp build/classes edu.aitu.bridge.BridgeTests
