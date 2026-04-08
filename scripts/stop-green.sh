#!/bin/sh
set -e

GREEN_DIR=/opt/doogoo/green
ENV_FILE="${DOOGOO_ENV_FILE:-/opt/doogoo/shared/.env}"
PID_FILE="$GREEN_DIR/app.pid"

log() { echo "[stop-green] $*"; }

[ -f "$ENV_FILE" ] && set -a && . "$ENV_FILE" && set +a

PORT="${DOOGOO_GREEN_PORT:-}"
if [ -z "$PORT" ]; then
  log "missing required env: DOOGOO_GREEN_PORT"
  exit 1
fi

kill_pid() {
  TARGET_PID="$1"
  [ -z "$TARGET_PID" ] && return 0
  if ! kill -0 "$TARGET_PID" 2>/dev/null; then
    return 0
  fi

  kill "$TARGET_PID" 2>/dev/null || true
  for _ in 1 2 3 4 5 6 7 8 9 10; do
    if ! kill -0 "$TARGET_PID" 2>/dev/null; then
      return 0
    fi
    sleep 1
  done
  kill -9 "$TARGET_PID" 2>/dev/null || true
}

port_pids() {
  if command -v lsof >/dev/null 2>&1; then
    lsof -tiTCP:"$PORT" -sTCP:LISTEN 2>/dev/null || true
    return
  fi

  if command -v ss >/dev/null 2>&1; then
    ss -ltnp "( sport = :$PORT )" 2>/dev/null | awk -F'pid=' 'NR>1 && NF>1 {split($2,a,","); print a[1]}' || true
    return
  fi
}

if [ -f "$PID_FILE" ]; then
  PID=$(cat "$PID_FILE" 2>/dev/null || true)
  if [ -n "$PID" ]; then
    kill_pid "$PID"
    log "stopped pid=$PID from pid file"
  fi
fi

for PID in $(port_pids); do
  kill_pid "$PID"
  log "stopped pid=$PID listening on port $PORT"
done

rm -f "$PID_FILE"

if [ -n "$(port_pids)" ]; then
  log "port $PORT is still in use after stop"
  exit 1
fi

log "port $PORT is free"
exit 0
