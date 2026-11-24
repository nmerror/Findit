#!/usr/bin/env bash
# ---------------------------------------------------------------------------
#  FindIt - one-time Java installer
#
#    bash setup-java.sh
#
#  Downloads the official Eclipse Temurin JDK 17 into this project folder
#  (./.jdk) and points the launcher at it.  Nothing is installed system-wide,
#  no administrator rights are needed, and your PATH is left untouched.
#
#  It also fetches the MySQL JDBC driver into ./lib so that the application can
#  talk to a real database without Maven.
#
#  Needs only: bash, curl or wget, tar.
# ---------------------------------------------------------------------------
set -e
SCRIPT_PATH="${BASH_SOURCE[0]}"
case "$SCRIPT_PATH" in
  */*) cd "${SCRIPT_PATH%/*}" ;;
esac

JDK_DIR="$PWD/.jdk"
MARKER="$JDK_DIR/java-home"
LIB_DIR="$PWD/lib"
API="https://api.adoptium.net/v3/binary/latest/17/ga"
DRIVER_URL="https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar"
DRIVER_JAR="$LIB_DIR/mysql-connector-j-8.4.0.jar"

# --- already installed? ----------------------------------------------------
if [ -f "$MARKER" ] && [ -x "$(cat "$MARKER")/bin/java" ]; then
  echo "A private JDK 17 is already installed in this project:"
  echo "  $(cat "$MARKER")"
  "$(cat "$MARKER")/bin/java" -version 2>&1 | head -1
  echo ""
  echo "Next:  bash run.sh"
  exit 0
fi

# --- detect platform -------------------------------------------------------
OS_RAW="$(uname -s 2>/dev/null || echo unknown)"
case "$OS_RAW" in
  Darwin) OS="mac" ;;
  Linux)  OS="linux" ;;
  *)
    echo "This script supports macOS and Linux."
    echo "On Windows please run  setup-java.bat  instead."
    exit 1 ;;
esac

ARCH_RAW="$(uname -m 2>/dev/null || echo unknown)"
case "$ARCH_RAW" in
  arm64|aarch64) ARCH="aarch64" ;;
  x86_64|amd64)  ARCH="x64" ;;
  *)
    echo "Unsupported architecture: $ARCH_RAW"
    exit 1 ;;
esac

echo "=============================================="
echo "  FindIt - Java 17 installer"
echo "=============================================="
echo "Platform : $OS_RAW / $ARCH_RAW"
echo "Target   : $JDK_DIR"
echo ""

# --- download --------------------------------------------------------------
TARBALL="$PWD/jdk17-$OS-$ARCH.tar.gz"
URL="$API/$OS/$ARCH/jdk/hotspot/normal/eclipse"

echo "Downloading Eclipse Temurin JDK 17 (about 190 MB)..."
echo "  $URL"
echo ""
if command -v curl >/dev/null 2>&1; then
  curl -fL --retry 3 --connect-timeout 30 -# -o "$TARBALL" "$URL"
elif command -v wget >/dev/null 2>&1; then
  wget -q --show-progress -O "$TARBALL" "$URL"
else
  echo "ERROR: neither curl nor wget is available."
  echo "       Install one of them, or download the JDK manually from"
  echo "       https://adoptium.net/temurin/releases/?version=17"
  exit 1
fi

# --- extract ---------------------------------------------------------------
echo ""
echo "Extracting..."
mkdir -p "$JDK_DIR"
tar -xzf "$TARBALL" -C "$JDK_DIR" --strip-components=1
rm -f "$TARBALL"

if [ -x "$JDK_DIR/Contents/Home/bin/java" ]; then
  JAVA_HOME_DIR="$JDK_DIR/Contents/Home"
elif [ -x "$JDK_DIR/bin/java" ]; then
  JAVA_HOME_DIR="$JDK_DIR"
else
  FOUND="$(find "$JDK_DIR" -maxdepth 4 -type f -path '*/bin/java' | head -1)"
  if [ -z "$FOUND" ]; then
    echo "ERROR: the downloaded archive did not contain a java executable."
    exit 1
  fi
  JAVA_HOME_DIR="$(dirname "$(dirname "$FOUND")")"
fi

printf '%s' "$JAVA_HOME_DIR" > "$MARKER"

echo ""
echo "=============================================="
echo "  Java 17 is ready"
echo "=============================================="
"$JAVA_HOME_DIR/bin/java" -version 2>&1 | head -2
"$JAVA_HOME_DIR/bin/javac" -version 2>&1 | head -1
# --- the MySQL driver ------------------------------------------------------
echo ""
echo "Fetching the MySQL JDBC driver..."
mkdir -p "$LIB_DIR"
if command -v curl >/dev/null 2>&1; then
  curl -fL --retry 3 --connect-timeout 30 -# -o "$DRIVER_JAR" "$DRIVER_URL"
else
  wget -q -O "$DRIVER_JAR" "$DRIVER_URL"
fi
echo "  saved to lib/$(basename "$DRIVER_JAR")"

echo ""
echo "=============================================="
echo "  Everything is ready"
echo "=============================================="
echo ""
echo "Next:  bash run.sh"
echo ""
echo "The JDK lives in this project folder only, so nothing outside it changed."
echo "Delete the .jdk and lib folders to remove them completely."
