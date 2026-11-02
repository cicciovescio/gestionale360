#!/bin/sh
set -e
cd "$(dirname "$0")"
chmod +x ./deploy.sh 2>/dev/null || true
exec ./deploy.sh
