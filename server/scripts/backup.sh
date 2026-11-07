#!/bin/sh
set -e
mkdir -p /backup
pg_dump "$DATABASE_URL" | gzip > /backup/db_$(date +%F).sql.gz
find /backup -name 'db_*.sql.gz' -mtime +14 -delete
