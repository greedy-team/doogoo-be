#!/bin/sh
set -e

GREEN_DIR=/opt/doogoo/green
GREEN_PORT=50010
JAR="$GREEN_DIR/app.jar"
PID_FILE="$GREEN_DIR/app.pid"
LOG_FILE="$GREEN_DIR/app.log"
JAVA_OPTS="-Xmx512m"

log() { echo "[start-green] $*"; }

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

install -d -m 755 "$GREEN_DIR"
cd "$GREEN_DIR" || exit 1

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-prod}"

nohup java $JAVA_OPTS -jar "$JAR" --server.port="$GREEN_PORT" >>"$LOG_FILE" 2>&1 &
echo $! > "$PID_FILE"
log "started pid=$(cat "$PID_FILE") port=$GREEN_PORT log=$LOG_FILE"
exit 0
