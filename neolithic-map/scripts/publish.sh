#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

echo "Checking and building the site..."
npm test

wrangler_config="dist/server/wrangler.json"
if [[ ! -f "$wrangler_config" || ! -f "dist/server/index.js" ]]; then
  echo "Build output is incomplete; refusing to publish." >&2
  exit 1
fi

echo "Publishing the site..."
npx --no-install wrangler deploy --config "$wrangler_config" "$@"
