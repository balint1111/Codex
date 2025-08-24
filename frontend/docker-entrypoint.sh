#!/bin/sh
set -e
: "${API_URL}"
: "${KEYCLOAK_URL}"
: "${KEYCLOAK_REALM}"
: "${KEYCLOAK_CLIENT_ID}"
cat <<EOF > /usr/share/nginx/html/env.js
window.API_URL = '${API_URL}';
window.KEYCLOAK_URL = '${KEYCLOAK_URL}';
window.KEYCLOAK_REALM = '${KEYCLOAK_REALM}';
window.KEYCLOAK_CLIENT_ID = '${KEYCLOAK_CLIENT_ID}';
EOF
exec "$@"
