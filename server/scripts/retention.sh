#!/bin/sh
set -e
psql "$DATABASE_URL" -c "DELETE FROM events WHERE ts < now() - interval '60 days' AND kind IN ('notif','screen','sms','call','loc')"
psql "$DATABASE_URL" -c "DELETE FROM media WHERE ts < now() - interval '30 days'"
find /data/media -type f -mtime +30 -delete
U=$(df /data | tail -1 | awk '{print $5}' | tr -d '%')
if [ "$U" -gt 85 ] && [ -n "$TG_TOKEN" ]; then
  curl -s "https://api.telegram.org/bot$TG_TOKEN/sendMessage" -d "chat_id=$TG_CHAT&text=Диск ${U}%"
fi
