#!/bin/bash
set -e

SERVICES="orchestrator:orchestrator_db order-service:order_db payment-service:payment_db compensation-service:compensation_db"

echo "=== Applying migrations ==="
for pair in $SERVICES; do
  service="${pair%%:*}"
  db="${pair##*:}"
  for sql in /migration/"$service"/V*.sql; do
    [ -f "$sql" ] || continue
    echo "  $sql -> $db"
    psql "postgres://saga:saga@postgres:5432/$db?sslmode=disable" -f "$sql"
  done
done

echo "=== Generating schema docs ==="
for pair in $SERVICES; do
  service="${pair%%:*}"
  db="${pair##*:}"
  sed \
    -e "s|localhost|postgres|" \
    -e "s|^docPath:.*|docPath: /docs/${service}|" \
    "/config/${service}/.tbls.yml" > "/tmp/.tbls-${service}.yml"
  tbls doc -c "/tmp/.tbls-${service}.yml" --rm-dist
done

echo "=== Converting Markdown to HTML ==="
cp /style/schema-style.css /docs/style.css
for service_dir in /docs/*/; do
  service=$(basename "$service_dir")
  for md in "$service_dir"*.md; do
    [ -f "$md" ] || continue
    sed -i 's/README\.md/index.html/g; s/\.md\([)#]\)/\.html\1/g' "$md"
    base=$(basename "$md" .md)
    if [ "$base" = "README" ]; then
      out="${service_dir}index.html"
    else
      out="${service_dir}${base}.html"
    fi
    pandoc "$md" -f gfm -t html5 --standalone \
      --metadata title="$service / $base" \
      --metadata lang=ja \
      --css ../style.css \
      -o "$out"
  done
  rm -f "$service_dir"*.md
done

cat > /docs/index.html <<'HTML'
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="utf-8">
  <title>スキーマドキュメント</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <h1>スキーマドキュメント</h1>
  <table>
    <thead>
      <tr><th>サービス</th><th>概要</th></tr>
    </thead>
    <tbody>
      <tr><td><a href="orchestrator/">orchestrator</a></td><td>Saga オーケストレーション</td></tr>
      <tr><td><a href="order-service/">order-service</a></td><td>注文管理</td></tr>
      <tr><td><a href="payment-service/">payment-service</a></td><td>決済管理</td></tr>
      <tr><td><a href="compensation-service/">compensation-service</a></td><td>補償処理</td></tr>
    </tbody>
  </table>
</body>
</html>
HTML

echo "=== Schema docs ready: http://localhost:3000 ==="
cd /docs && python3 -m http.server 3000
