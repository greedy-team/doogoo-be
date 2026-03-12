#!/bin/sh
GREEN_DIR=/opt/doogoo/green
PID_FILE="$GREEN_DIR/app.pid"

log() { echo "[stop-green] $*"; }

if [ ! -f "$PID_FILE" ]; then
  log "no pid file, nothing to stop"
  exit 0
fi

PID=$(cat "$PID_FILE" 2>/dev/null || true)
if [ -z "$PID" ]; then
  rm -f "$PID_FILE"
  exit 0
fi

if ! kill -0 "$PID" 2>/dev/null; then
  log "pid $PID not running, removing stale $PID_FILE"
  rm -f "$PID_FILE"
  exit 0
fi

kill "$PID" 2>/dev/null || true
for _ in 1 2 3 4 5 6 7 8 9 10; do
  if ! kill -0 "$PID" 2>/dev/null; then
    break
  fi
  sleep 1
done
if kill -0 "$PID" 2>/dev/null; then
  kill -9 "$PID" 2>/dev/null || true
fi
rm -f "$PID_FILE"
log "stopped pid=$PID"
exit 0
