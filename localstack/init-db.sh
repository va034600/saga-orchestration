#!/bin/bash
set -e

echo "Waiting for PostgreSQL to be ready..."
export PGPASSWORD=saga

until psql -h postgres -U saga -d saga -c '\q' 2>/dev/null; do
  echo "PostgreSQL is not ready yet. Retrying in 2 seconds..."
  sleep 2
done

echo "PostgreSQL is ready. Creating databases..."

DATABASES=("order_db" "payment_db" "compensation_db" "orchestrator_db")

for DB in "${DATABASES[@]}"; do
  echo "Creating database: ${DB}"
  psql -h postgres -U saga -d saga -tc "SELECT 1 FROM pg_database WHERE datname = '${DB}'" | grep -q 1 \
    || psql -h postgres -U saga -d saga -c "CREATE DATABASE ${DB};"
  echo "Database ${DB} is ready."
done

echo "All databases created successfully."
