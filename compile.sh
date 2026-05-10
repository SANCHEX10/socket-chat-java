#!/bin/bash
set -e

echo "Compilando Socket Chat Java..."
javac src/*.java src/cli/*.java src/gui/*.java
echo "Compilación completada correctamente."
