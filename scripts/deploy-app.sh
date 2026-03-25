#!/bin/sh
set -e

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
RESTART_APP="$SCRIPT_DIR/restart-app.sh"
ENV_FILE="${DOOGOO_ENV_FILE:-/opt/doogoo/shared/.env}"

BLUE_DIR=/opt/doogoo/blue
GREEN_DIR=/opt/doogoo/green
HEALTH_PATH="${DOOGOO_HEALTH_PATH:-/actuator/health}"
HEALTH_HOST="${DOOGOO_HEALTH_HOST:-localhost}"
MAX_WAIT="${DOOGOO_HEALTH_MAX_WAIT:-90}"
SLEEP="${DOOGOO_HEALTH_SLEEP:-2}"

log() { echo "[deploy-app] $*"; }

[ -f "$ENV_FILE" ] && set -a && . "$ENV_FILE" && set +a

require_var() {
  VAR_NAME="$1"
  eval VAR_VALUE=\${$VAR_NAME:-}
  if [ -z "$VAR_VALUE" ]; then
    log "missing required env: $VAR_NAME"
    exit 1
  fi
}

require_var DOOGOO_BLUE_PORT
require_var DOOGOO_GREEN_PORT

JAR_SRC="${1:-}"
COLOR=$(echo "${2:-}" | tr '[:upper:]' '[:lower:]')
SKIP_HEALTH="${3:-}"

case "$COLOR" in
  blue)
    TARGET_DIR=$BLUE_DIR
    PORT=$DOOGOO_BLUE_PORT
    ;;
  green)
    TARGET_DIR=$GREEN_DIR
    PORT=$DOOGOO_GREEN_PORT
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

HEALTH_URL="http://${HEALTH_HOST}:${PORT}${HEALTH_PATH}"
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
