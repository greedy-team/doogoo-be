#!/bin/sh
set -e

ENV_FILE="${DOOGOO_ENV_FILE:-/opt/doogoo/shared/.env}"

ACTIVE_FILE=/etc/doogoo/active
PROXY_FILE=/etc/nginx/doogoo-proxy-active.conf
PROXY_BAK="${PROXY_FILE}.bak"

log() { echo "[switch-nginx] $*"; }

[ -f "$ENV_FILE" ] && set -a && . "$ENV_FILE" && set +a

BLUE_HOST="${DOOGOO_BLUE_HOST:-}"
GREEN_HOST="${DOOGOO_GREEN_HOST:-}"
BLUE_PORT="${DOOGOO_BLUE_PORT:-}"
GREEN_PORT="${DOOGOO_GREEN_PORT:-}"

require_var() {
  VAR_NAME="$1"
  eval VAR_VALUE=\${$VAR_NAME:-}
  if [ -z "$VAR_VALUE" ]; then
    log "missing required env: $VAR_NAME"
    exit 1
  fi
}

require_var DOOGOO_BLUE_HOST
require_var DOOGOO_GREEN_HOST
require_var DOOGOO_BLUE_PORT
require_var DOOGOO_GREEN_PORT

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
