앱 서버: `deploy-app.sh <jar> blue|green`  
nginx 서버: `switch-nginx.sh blue|green`

GitHub Actions `deploy-bg.yml` 시크릿:

| Secret | 용도 |
|--------|------|
| APP_BLUE_HOST | Blue 앱 서버 호스트 |
| APP_GREEN_HOST | Green 앱 서버 호스트 |
| NGINX_HOST | nginx 서버 호스트 |
| APP_SSH_USER | 앱 서버 SSH 사용자 |
| APP_SSH_KEY | 앱 서버 SSH private key |
| NGINX_SSH_USER | nginx 서버 SSH 사용자 |
| NGINX_SSH_KEY | nginx 서버 SSH private key |

앱·nginx 모두 동일 키/유저면 NGINX_* 를 APP_* 와 같은 값으로 등록.

서버에 `/opt/doogoo/incoming` 존재, 스크립트 `/usr/local/bin/deploy-app.sh`, `/usr/local/bin/switch-nginx.sh` 배치 및 실행 권한.
