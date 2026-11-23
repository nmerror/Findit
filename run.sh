#!/usr/bin/env bash
# ---------------------------------------------------------------------------
#  FindIt - Campus Lost & Found System
#  One-command launcher for Segment 1 (no database needed).
#
#    ./run.sh          -> start the app with demo data
#    ./run.sh --mysql  -> start the app against MySQL (see src/main/resources/app.properties)
#
#  Requirements: JDK 17 or newer.  Nothing else - no Maven, no MySQL.
# ---------------------------------------------------------------------------
set -e
cd "$(dirname "$0")"

echo "=============================================="
echo "  FindIt - Campus Lost & Found Management"
echo "=============================================="

# --- 1. check Java ---------------------------------------------------------
if ! command -v java >/dev/null 2>&1; then
  echo "ERROR: java was not found on your PATH."
  echo "       Install JDK 17 or newer: https://adoptium.net/"
  exit 1
fi

JAVA_MAJOR=$(java -version 2>&1 | head -1 | sed -E 's/.*version "([0-9]+).*/\1/')
if [ "${JAVA_MAJOR:-0}" -lt 17 ]; then
  echo "ERROR: Java 17 or newer is required (found: $(java -version 2>&1 | head -1))."
  exit 1
fi
echo "Java:  $(java -version 2>&1 | head -1)"

# --- 2. compile ------------------------------------------------------------
OUT=target/classes
mkdir -p "$OUT"
echo "Compiling..."
find src/main/java -name '*.java' > target/sources.txt
javac -encoding UTF-8 -d "$OUT" @target/sources.txt
cp -r src/main/resources/* "$OUT"/ 2>/dev/null || true
echo "Compiled $(wc -l < target/sources.txt) source files."

# --- 3. run ----------------------------------------------------------------
MODE="--memory --demo"
if [ "${1:-}" = "--mysql" ]; then
  MODE=""
  echo "Starting with MySQL (check app.properties for the credentials)..."
else
  echo "Starting in demo mode - no database required."
  echo "Login: admin@findit.edu / Admin@1234   (ADMIN)"
  echo "   or: ayesha@buet.edu / Student@123   (STUDENT)"
fi

java -cp "$OUT" com.bubt.findit.Main $MODE
