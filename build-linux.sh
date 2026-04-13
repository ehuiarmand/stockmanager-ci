#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

APP_NAME="StockManager-CI-Linux"
APP_VERSION="1.0.0"
VENDOR="INP-HB"
MAIN_JAR="stockmanager-ci-1.0.jar"
MAIN_CLASS="com.inphb.icgl.stocks.MainApp"
PACKAGE_TYPE="${1:-app-image}"

resolve_jpackage() {
  if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/jpackage" ]]; then
    echo "$JAVA_HOME/bin/jpackage"
    return
  fi

  if command -v jpackage >/dev/null 2>&1; then
    command -v jpackage
    return
  fi

  echo "jpackage introuvable. Installez un JDK 21 complet puis definissez JAVA_HOME." >&2
  exit 1
}

prepare_input() {
  local input_dir="$PROJECT_ROOT/target/linux-input"
  rm -rf "$input_dir"
  mkdir -p "$input_dir"
  cp "$PROJECT_ROOT/target/$MAIN_JAR" "$input_dir/"
  cp "$PROJECT_ROOT"/target/jpackage-input/*.jar "$input_dir/"
  echo "$input_dir"
}

case "$PACKAGE_TYPE" in
  app-image|deb|rpm) ;;
  *)
    echo "Type non supporte: $PACKAGE_TYPE. Utilisez app-image, deb ou rpm." >&2
    exit 1
    ;;
esac

JPACKAGE_BIN="$(resolve_jpackage)"

echo "Compilation et tests..."
./mvnw package

INPUT_DIR="$(prepare_input)"
OUTPUT_DIR="$PROJECT_ROOT/target/linux-$PACKAGE_TYPE"
rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

echo "Generation Linux ($PACKAGE_TYPE)..."
"$JPACKAGE_BIN" \
  --type "$PACKAGE_TYPE" \
  --name "$APP_NAME" \
  --app-version "$APP_VERSION" \
  --vendor "$VENDOR" \
  --dest "$OUTPUT_DIR" \
  --input "$INPUT_DIR" \
  --main-jar "$MAIN_JAR" \
  --main-class "$MAIN_CLASS" \
  --java-options "--module-path=\$APPDIR" \
  --java-options "--add-modules=javafx.controls,javafx.fxml" \
  --java-options "--enable-native-access=ALL-UNNAMED" \
  --java-options "--enable-native-access=javafx.graphics"

echo "Package Linux genere dans $OUTPUT_DIR"
