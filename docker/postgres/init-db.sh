#!/bin/bash
set -e

echo "Creating additional databases..."

DATABASES=("order_db" "payment_db" "compensation_db" "orchestrator_db")

for DB in "${DATABASES[@]}"; do
  echo "Creating database: ${DB}"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-SQL
    SELECT 'CREATE DATABASE ${DB}' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '${DB}')\gexec
SQL
  echo "Database ${DB} is ready."
done

echo "All databases created successfully."
