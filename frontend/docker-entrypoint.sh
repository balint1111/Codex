#!/bin/sh
set -e
: "${API_URL}"
echo "window.API_URL = '${API_URL}';" > /usr/share/nginx/html/env.js
exec "$@"
