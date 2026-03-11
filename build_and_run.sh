#!/bin/bash
# ─────────────────────────────────────────────────────────────────
#  Library Management System - Build & Run Script (Linux/macOS)
# ─────────────────────────────────────────────────────────────────

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
LIB_DIR="$SCRIPT_DIR/lib"
SRC_DIR="$SCRIPT_DIR/src"
OUT_DIR="$SCRIPT_DIR/out"
JAR_NAME="LibraryManagement.jar"

# ── 1. Download sqlite-jdbc 3.36.0.3 (no SLF4J dependency) ──────
# NOTE: Versions 3.40+ require SLF4J on the classpath. 3.36.0.3 is
#       self-contained and works with Java 8+.
SQLITE_JAR="$LIB_DIR/sqlite-jdbc.jar"
if [ ! -f "$SQLITE_JAR" ]; then
    echo "Downloading SQLite JDBC driver (v3.36.0.3)..."
    mkdir -p "$LIB_DIR"
    SQLITE_URL="https://github.com/xerial/sqlite-jdbc/releases/download/3.36.0.3/sqlite-jdbc-3.36.0.3.jar"
    if command -v curl &> /dev/null; then
        curl -L "$SQLITE_URL" -o "$SQLITE_JAR"
    elif command -v wget &> /dev/null; then
        wget "$SQLITE_URL" -O "$SQLITE_JAR"
    else
        echo "ERROR: Please download the JAR manually from:"
        echo "  https://github.com/xerial/sqlite-jdbc/releases/tag/3.36.0.3"
        echo "  Rename it sqlite-jdbc.jar and place it in: $LIB_DIR/"
        exit 1
    fi
fi

# ── 2. Clean & compile ───────────────────────────────────────────
echo "Compiling..."
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"
find "$SRC_DIR" -name "*.java" > /tmp/sources.txt
javac -source 8 -target 8 -cp "$SQLITE_JAR" -d "$OUT_DIR" @/tmp/sources.txt
echo "Compilation successful."

# ── 3. Package fat JAR (sqlite-jdbc embedded) ───────────────────
echo "Packaging JAR..."
cd "$OUT_DIR"
jar xf "$SQLITE_JAR"
rm -f module-info.class   # avoid conflicts on older JVMs
cd "$SCRIPT_DIR"

jar cfe "$JAR_NAME" library.Main -C "$OUT_DIR" .
echo "JAR created: $JAR_NAME"

# ── 4. Run ───────────────────────────────────────────────────────
echo "Launching Library Management System..."
java -jar "$JAR_NAME"
