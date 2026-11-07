CREATE TABLE devices (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name text NOT NULL,
  model text,
  android text,
  token text UNIQUE NOT NULL,
  enrolled_at timestamptz NOT NULL DEFAULT now(),
  last_seen timestamptz,
  online boolean NOT NULL DEFAULT false,
  revoked boolean NOT NULL DEFAULT false
);
CREATE TABLE codes (
  code text PRIMARY KEY,
  used boolean NOT NULL DEFAULT false,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE events (
  id bigserial PRIMARY KEY,
  device_id uuid NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
  ts timestamptz NOT NULL DEFAULT now(),
  kind text NOT NULL,
  app text DEFAULT '',
  contact text DEFAULT '',
  title text DEFAULT '',
  body text DEFAULT '',
  src text DEFAULT ''
);
CREATE INDEX ON events (device_id, ts DESC);
CREATE TABLE media (
  id bigserial PRIMARY KEY,
  device_id uuid NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
  ts timestamptz NOT NULL DEFAULT now(),
  kind text,
  path text,
  size int,
  dur int DEFAULT 0
);
CREATE TABLE commands (
  id bigserial PRIMARY KEY,
  device_id uuid NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
  ts timestamptz NOT NULL DEFAULT now(),
  kind text NOT NULL,
  done_at timestamptz
);
