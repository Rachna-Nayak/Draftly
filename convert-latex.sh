#!/bin/bash
# Simple LaTeX converter script
# Usage: ./convert-latex.sh --input <file.docx> --format <LNCS|IEEE> [--output <dir>]

cd "$(dirname "$0")" || exit 1

INPUT=""
FORMAT=""
OUTPUT="./latex-output"

while [[ $# -gt 0 ]]; do
    case $1 in
        --input) INPUT="$2"; shift 2 ;;
        --format) FORMAT="$2"; shift 2 ;;
        --output) OUTPUT="$2"; shift 2 ;;
        *) shift ;;
    esac
done

if [ -z "$INPUT" ] || [ -z "$FORMAT" ]; then
    echo "Usage: $0 --input <file.docx> --format <LNCS|IEEE> [--output <dir>]"
    exit 1
fi

# Build classpath - get ALL jars from gradle cache
CLASSPATH="build/classes/java/main:build/resources/main"

# Add all JAR files from gradle cache
if [ -d ~/.gradle/caches/modules-2/files-2.1 ]; then
    for jar in $(find ~/.gradle/caches/modules-2/files-2.1 -name "*.jar" -type f 2>/dev/null); do
        CLASSPATH="$CLASSPATH:$jar"
    done
fi

java -cp "$CLASSPATH" SimpleConverter --input "$INPUT" --format "$FORMAT" --output "$OUTPUT" 2>&1 | grep -v "SLF4J"
