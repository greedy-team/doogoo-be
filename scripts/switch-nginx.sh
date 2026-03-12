#!/bin/sh
set -e

BLUE_HOST=10.0.2.130
GREEN_HOST=10.0.0.146
BLUE_PORT=50009
GREEN_PORT=50010

ACTIVE_FILE=/etc/doogoo/active
PROXY_FILE=/etc/nginx/doogoo-proxy-active.conf
PROXY_BAK="${PROXY_FILE}.bak"

log() { echo "[switch-nginx] $*"; }

restore_proxy_from_backup() {
  if [ -f "$PROXY_BAK" ]; then
    cp -f "$PROXY_BAK" "$PROXY_FILE"
    log "restored $PROXY_FILE from $PROXY_BAK"
  else
    log "no backup at $PROXY_BAK"
  fi
}

COLOR=$(echo "${1:-}" | tr '[:upper:]' '[:lower:]')

case "$COLOR" in
  blue)
    LINE="proxy_pass http://${BLUE_HOST}:${BLUE_PORT};"
    ;;
  green)
    LINE="proxy_pass http://${GREEN_HOST}:${GREEN_PORT};"
    ;;
  *)
    log "usage: $0 blue|green"
    exit 1
    ;;
esac

if [ -f "$PROXY_FILE" ]; then
  cp -f "$PROXY_FILE" "$PROXY_BAK"
  log "backed up $PROXY_FILE -> $PROXY_BAK"
fi

echo "$LINE" > "$PROXY_FILE"
log "wrote $PROXY_FILE -> $LINE"

if ! nginx -t; then
  restore_proxy_from_backup
  nginx -t || true
  log "nginx -t failed — restored"
  exit 1
fi

if ! nginx -s reload; then
  log "nginx -s reload failed — restoring"
  restore_proxy_from_backup
  if nginx -t; then
    log "nginx -t OK after restore"
  else
    log "nginx -t still failing after restore"
  fi
  exit 1
fi

echo "$COLOR" > "$ACTIVE_FILE"
log "active is now $COLOR (reload OK)"
exit 0
