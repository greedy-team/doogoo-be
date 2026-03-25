#!/bin/sh
set -e

BLUE_DIR=/opt/doogoo/blue
ENV_FILE="${DOOGOO_ENV_FILE:-/opt/doogoo/shared/.env}"
JAR="$BLUE_DIR/app.jar"
PID_FILE="$BLUE_DIR/app.pid"
LOG_FILE="$BLUE_DIR/app.log"
JAVA_OPTS="${JAVA_OPTS:--Xmx384m}"

log() { echo "[start-blue] $*"; }

[ -f "$ENV_FILE" ] && set -a && . "$ENV_FILE" && set +a

BLUE_PORT="${DOOGOO_BLUE_PORT:-}"

if [ -z "$BLUE_PORT" ]; then
  log "missing required env: DOOGOO_BLUE_PORT"
  exit 1
fi

if [ ! -f "$JAR" ]; then
  log "missing $JAR"
  exit 1
fi

if [ -f "$PID_FILE" ]; then
  PID=$(cat "$PID_FILE" 2>/dev/null || true)
  if [ -n "$PID" ] && kill -0 "$PID" 2>/dev/null; then
    log "already running pid=$PID"
    exit 0
  fi
  rm -f "$PID_FILE"
fi

install -d -m 755 "$BLUE_DIR"
cd "$BLUE_DIR" || exit 1

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-prod}"

nohup java $JAVA_OPTS -jar "$JAR" --server.port="$BLUE_PORT" >>"$LOG_FILE" 2>&1 &
echo $! > "$PID_FILE"
log "started pid=$(cat "$PID_FILE") port=$BLUE_PORT log=$LOG_FILE"
exit 0
