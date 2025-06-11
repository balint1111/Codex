#!/bin/sh
set -e
: "${API_URL:=http://localhost:8081}"
echo "window.API_URL = '${API_URL}';" > /usr/share/nginx/html/env.js
exec "$@"
