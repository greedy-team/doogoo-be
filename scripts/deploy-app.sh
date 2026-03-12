#!/bin/sh
set -e

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
RESTART_APP="$SCRIPT_DIR/restart-app.sh"

BLUE_PORT=50009
GREEN_PORT=50010
BLUE_DIR=/opt/doogoo/blue
GREEN_DIR=/opt/doogoo/green
HEALTH_PATH=/actuator/health
MAX_WAIT=90
SLEEP=2

log() { echo "[deploy-app] $*"; }

JAR_SRC="${1:-}"
COLOR=$(echo "${2:-}" | tr '[:upper:]' '[:lower:]')
SKIP_HEALTH="${3:-}"

case "$COLOR" in
  blue)
    TARGET_DIR=$BLUE_DIR
    PORT=$BLUE_PORT
    ;;
  green)
    TARGET_DIR=$GREEN_DIR
    PORT=$GREEN_PORT
    ;;
  *)
    log "usage: $0 /path/to/app.jar blue|green [skip-health]"
    exit 1
    ;;
esac

if [ -z "$JAR_SRC" ] || [ ! -f "$JAR_SRC" ]; then
  log "missing jar: $JAR_SRC"
  exit 1
fi

if [ ! -x "$RESTART_APP" ]; then
  log "not executable: $RESTART_APP"
  exit 1
fi

install -d -m 755 "$TARGET_DIR"
cp -f "$JAR_SRC" "$TARGET_DIR/app.jar"
log "installed $JAR_SRC -> $TARGET_DIR/app.jar"

"$RESTART_APP" "$COLOR"
log "restarted $COLOR"

# CI에서 호출 시 헬스체크는 워크플로 단계에서 함 (SSH 세션 길어지면 Broken pipe 방지)
if [ "$SKIP_HEALTH" = "skip-health" ]; then
  log "skip-health: health check is done by CI"
  exit 0
fi

HEALTH_URL="http://127.0.0.1:${PORT}${HEALTH_PATH}"
END=$(($(date +%s) + MAX_WAIT))
while [ "$(date +%s)" -lt "$END" ]; do
  if curl -sf "$HEALTH_URL" >/dev/null 2>&1; then
    log "health OK $HEALTH_URL"
    exit 0
  fi
  sleep "$SLEEP"
done

log "health FAILED $HEALTH_URL"
exit 1
