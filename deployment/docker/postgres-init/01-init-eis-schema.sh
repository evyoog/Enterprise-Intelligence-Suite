#!/bin/sh
# Runs once, on first start of an empty Postgres volume (docker-entrypoint-initdb.d).
# Creates the eis_platform schema and loads the backend's schema.sql into it.
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
  -c "CREATE SCHEMA IF NOT EXISTS eis_platform AUTHORIZATION \"$POSTGRES_USER\";"

PGOPTIONS='-c search_path=eis_platform' \
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
  -f /eis-schema/schema.sql
