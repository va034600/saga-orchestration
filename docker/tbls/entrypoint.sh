#!/bin/bash
set -e

SERVICES="orchestrator:orchestrator_db order-service:order_db payment-service:payment_db compensation-service:compensation_db"

echo "=== Applying migrations ==="
for pair in $SERVICES; do
  service="${pair%%:*}"
  db="${pair##*:}"
  for sql in $(ls /migration/"$service"/V*.sql 2>/dev/null | sort); do
    echo "  $sql -> $db"
    psql "postgres://saga:saga@postgres:5432/$db?sslmode=disable" -f "$sql"
  done
done

echo "=== Generating schema docs ==="
for pair in $SERVICES; do
  service="${pair%%:*}"
  db="${pair##*:}"
  cat > "/tmp/.tbls-${service}.yml" <<EOF
dsn: postgres://saga:saga@postgres:5432/${db}?sslmode=disable
docPath: /docs/${service}
EOF
  tbls doc -c "/tmp/.tbls-${service}.yml" --rm-dist
done

echo "=== Converting Markdown to HTML ==="
for service_dir in /docs/*/; do
  service=$(basename "$service_dir")
  for md in "$service_dir"*.md; do
    [ -f "$md" ] || continue
    # Fix links: README.md -> index.html, *.md -> *.html
    sed -i 's/README\.md/index.html/g; s/\.md)/\.html)/g' "$md"
    base=$(basename "$md" .md)
    if [ "$base" = "README" ]; then
      out="${service_dir}index.html"
    else
      out="${service_dir}${base}.html"
    fi
    pandoc "$md" -f gfm -t html5 --standalone --metadata title="$service / $base" -o "$out"
  done
  rm -f "$service_dir"*.md
done

# Top-level index
cat > /docs/index.html <<'HTML'
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Schema Documentation</title>
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif; max-width: 600px; margin: 60px auto; color: #24292f; }
    h1 { border-bottom: 1px solid #d0d7de; padding-bottom: 8px; }
    ul { list-style: none; padding: 0; }
    li { margin: 12px 0; }
    a { color: #0969da; text-decoration: none; font-size: 18px; }
    a:hover { text-decoration: underline; }
  </style>
</head>
<body>
  <h1>Schema Documentation</h1>
  <ul>
    <li><a href="orchestrator/">Orchestrator</a></li>
    <li><a href="order-service/">Order Service</a></li>
    <li><a href="payment-service/">Payment Service</a></li>
    <li><a href="compensation-service/">Compensation Service</a></li>
  </ul>
</body>
</html>
HTML

echo "=== Schema docs ready: http://localhost:3000 ==="
cd /docs && python3 -m http.server 3000
