#!/usr/bin/env bash
# ---------------------------------------------------------------------------
#  FindIt - Campus Lost & Found System
#  One-command launcher for Segment 1 (no database needed).
#
#    bash run.sh          -> start the app in memory mode (no database needed)
#    bash run.sh --mysql  -> start the app against MySQL (see app.properties)
#
#  Requirements: JDK 17 or newer (it is free). Nothing else - no Maven, no MySQL.
# ---------------------------------------------------------------------------
set -e
# Resolve the folder of this script without depending on external commands.
SCRIPT_PATH="${BASH_SOURCE[0]}"
case "$SCRIPT_PATH" in
  */*) cd "${SCRIPT_PATH%/*}" ;;
esac

echo "=============================================="
echo "  FindIt - Campus Lost & Found Management"
echo "=============================================="

# --- 0. use the project-local JDK if setup-java.sh installed one -----------
if [ -f "$PWD/.jdk/java-home" ]; then
  LOCAL_JAVA_HOME="$(cat "$PWD/.jdk/java-home")"
  if [ -x "$LOCAL_JAVA_HOME/bin/java" ]; then
    export JAVA_HOME="$LOCAL_JAVA_HOME"
    export PATH="$JAVA_HOME/bin:$PATH"
  fi
fi

# --- 1. check Java ---------------------------------------------------------
if ! command -v java >/dev/null 2>&1; then
  echo ""
  echo "ERROR: Java was not found on this computer."
  echo ""
  echo "  FindIt is a Java application, so it needs the JDK 17 (or newer)."
  echo "  It is free and takes about 3 minutes to install:"
  echo ""
  OS="$(uname -s 2>/dev/null || echo unknown)"
  case "$OS" in
    Darwin)
      echo "  macOS - easiest way (Homebrew):"
      echo "      brew install --cask temurin@17"
      echo ""
      echo "  macOS - without Homebrew:"
      echo "      1. open https://adoptium.net/temurin/releases/?version=17"
      echo "      2. pick macOS + your chip (aarch64 = Apple Silicon, x64 = Intel)"
      echo "      3. download the .pkg file and open it - double-click to install"
      echo "      4. close and reopen the terminal, then run: bash run.sh"
      ;;
    Linux)
      echo "  Linux:"
      echo "      sudo apt install openjdk-17-jdk"
      echo "  or download the .tar.gz from https://adoptium.net/temurin/releases/?version=17"
      ;;
    *)
      echo "  Windows - double-click run.bat instead of this script."
      echo "  Or download the .msi for Windows from:"
      echo "      https://adoptium.net/temurin/releases/?version=17&os=windows"
      echo "      install it, reopen the terminal, then run run.bat again."
      ;;
  esac
  echo ""
  echo "  Easiest - let the project install it for you (no admin rights):"
  echo "      bash setup-java.sh"
  echo ""
  echo "  After installing, check it with:  java -version"
  echo ""
  exit 1
fi

JAVA_MAJOR=$(java -version 2>&1 | head -1 | sed -E 's/.*version "([0-9]+).*/\1/')
if [ "${JAVA_MAJOR:-0}" -lt 17 ]; then
  echo ""
  echo "ERROR: Java 17 or newer is required, but this computer has:"
  echo "       $(java -version 2>&1 | head -1)"
  echo "       Install JDK 17+ from https://adoptium.net/temurin/releases/?version=17"
  echo ""
  exit 1
fi
echo "Java:  $(java -version 2>&1 | head -1)"

# A JRE can run the app but cannot compile it - catch that early.
if ! command -v javac >/dev/null 2>&1; then
  echo ""
  echo "ERROR: 'javac' was not found - you have a JRE, not a JDK."
  echo "       Install the full JDK 17+ from https://adoptium.net/temurin/releases/?version=17"
  echo ""
  exit 1
fi

# --- 2. compile ------------------------------------------------------------
OUT=target/classes
mkdir -p "$OUT"
echo "Compiling..."
find src/main/java -name '*.java' > target/sources.txt
javac -encoding UTF-8 -d "$OUT" @target/sources.txt
cp -r src/main/resources/* "$OUT"/ 2>/dev/null || true
echo "Compiled $(wc -l < target/sources.txt) source files."

# The MySQL driver lives in lib/ (fetched by setup-java.sh) - put it on the
# classpath when it is there.
CP="$OUT"
if [ -d lib ] && [ -n "$(ls -A lib 2>/dev/null)" ]; then
  CP="$OUT:lib/*"
  echo "MySQL driver: $(ls lib | head -1)"
fi

# --- 3. run ----------------------------------------------------------------
MODE="--memory"
if [ "${1:-}" = "--mysql" ]; then
  MODE=""
  echo "Starting with MySQL (check app.properties for the credentials)..."
else
  echo "Starting in memory mode - no database server needed."
  echo "Create your own account on the registration screen."
  echo "The first administrator is created automatically: admin@findit.edu / Admin@1234"
fi

java -cp "$CP" com.bubt.findit.Main $MODE
