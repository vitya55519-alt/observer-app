#!/bin/sh
set -e
P=$(openssl rand -hex 12)
S=$(openssl rand -hex 32)
sed -i "s|^ADMIN_PASSWORD=.*|ADMIN_PASSWORD=$P|" .env
sed -i "s|^SECRET=.*|SECRET=$S|" .env
sed -i "s|^DB_PASS=.*|DB_PASS=$P|" .env
sed -i "s|^DATABASE_URL=.*|DATABASE_URL=postgresql://obs:$P@db:5432/obs|" .env
echo "Секреты созданы. Впишите DOMAIN."
