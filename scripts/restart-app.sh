#!/bin/sh
set -e

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
COLOR=$(echo "${1:-}" | tr '[:upper:]' '[:lower:]')

case "$COLOR" in
  blue)
    "$SCRIPT_DIR/stop-blue.sh"  || true
    sleep 1
    "$SCRIPT_DIR/start-blue.sh"
    ;;
  green)
    "$SCRIPT_DIR/stop-green.sh" || true
    sleep 1
    "$SCRIPT_DIR/start-green.sh"
    ;;
  *)
    echo "usage: $0 blue|green"
    exit 1
    ;;
esac
exit 0
