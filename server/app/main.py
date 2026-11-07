import os, hmac, time, json, asyncio, secrets, hashlib
from typing import Optional
from fastapi import FastAPI, Header, HTTPException, UploadFile, WebSocket, WebSocketDisconnect, Depends
from fastapi.responses import FileResponse, RedirectResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel
import asyncpg

DB = os.environ["DATABASE_URL"]
ADMIN = os.environ["ADMIN_PASSWORD"]
SECRET = os.environ["SECRET"].encode()

# Use /data/media for Railway, fallback to local for development
MEDIA = os.environ.get("MEDIA_PATH", "/data/media")
os.makedirs(MEDIA, exist_ok=True)

# Ensure panel and dist directories exist
os.makedirs("panel", exist_ok=True)
os.makedirs("dist", exist_ok=True)
os.makedirs("/srv/dist", exist_ok=True)
os.makedirs("/srv/panel", exist_ok=True)

app = FastAPI()
pool = None

class Bus:
    def __init__(self): self.subs = {}
    def sub(self, ch, ws): self.subs.setdefault(ch, set()).add(ws)
    def unsub(self, ch, ws): self.subs.get(ch, set()).discard(ws)
    async def pub(self, ch, data):
        for ws in list(self.subs.get(ch, ())):
            try: await ws.send_text(json.dumps(data))
            except Exception: self.unsub(ch, ws)

bus = Bus()

@app.on_event("startup")
async def _up():
    global pool
    pool = await asyncpg.create_pool(DB, min_size=1, max_size=8)
    asyncio.create_task(keepalive())

async def keepalive():
    while True:
        await asyncio.sleep(25)
        for ch in list(bus.subs):
            for ws in list(bus.subs[ch]):
                try: await ws.send_text('{"ping":1}')
                except Exception: bus.unsub(ch, ws)

def sign(key):
    e = int(time.time()) + 3600
    s = hmac.new(SECRET, f"{key}:{e}".encode(), hashlib.sha256).hexdigest()[:32]
    return f"/m/{key}?e={e}&s={s}"

def admin(t: Optional[str] = Header(None, alias="X-Admin-Token")):
    if not t or not secrets.compare_digest(t, ADMIN): raise HTTPException(401)

async def device(t: Optional[str] = Header(None, alias="X-Device-Token")) -> str:
    async with pool.acquire() as c:
        r = await c.fetchrow("SELECT id FROM devices WHERE token=$1 AND NOT revoked", t)
    if not r: raise HTTPException(401)
    return str(r["id"])

class Enroll(BaseModel):
    code: str; name: str; model: str = ""; android: str = ""
class Loc(BaseModel):
    lat: float; lon: float; acc: float = 0; speed: float = 0
class Ev(BaseModel):
    kind: str; app: str = ""; contact: str = ""; title: str = ""; body: str = ""; src: str = ""
class Login(BaseModel):
    password: str

@app.post("/api/login")
async def login(l: Login):
    if not secrets.compare_digest(l.password, ADMIN): raise HTTPException(401)
    return {"token": ADMIN}

@app.post("/api/enroll")
async def enroll(e: Enroll):
    async with pool.acquire() as c:
        r = await c.fetchrow("SELECT code FROM codes WHERE code=$1 AND NOT used", e.code)
        if not r: raise HTTPException(403, "код неверный")
        await c.execute("UPDATE codes SET used=true WHERE code=$1", e.code)
        tok = secrets.token_urlsafe(32)
        did = await c.fetchval(
            "INSERT INTO devices (name,model,android,token) VALUES ($1,$2,$3,$4) RETURNING id",
            e.name, e.model, e.android, tok)
    await bus.pub("panel", {"t": "device", "id": str(did), "name": e.name})
    return {"device_id": str(did), "token": tok}

@app.post("/api/loc")
async def loc(l: Loc, d: str = Depends(device)):
    async with pool.acquire() as c:
        await c.execute("INSERT INTO events (device_id,kind,body,src) VALUES ($1,'loc',$2,'gps')",
                        d, f"{l.lat},{l.lon}")
        await c.execute("UPDATE devices SET last_seen=now() WHERE id=$1", d)
    await bus.pub("panel", {"t": "loc", "device": d})
    return {"ok": True}

@app.post("/api/events")
async def events(items: list[Ev], d: str = Depends(device)):
    async with pool.acquire() as c:
        for e in items:
            await c.execute(
                "INSERT INTO events (device_id,kind,app,contact,title,body,src) VALUES ($1,$2,$3,$4,$5,$6,$7)",
                d, e.kind, e.app, e.contact, e.title, e.body, e.src)
        await c.execute("UPDATE devices SET last_seen=now() WHERE id=$1", d)
    for e in items:
        await bus.pub("panel", {"t": "event", "device": d, "contact": e.contact, "body": e.body})
    return {"ok": True}

@app.post("/api/media")
async def media(kind: str, file: UploadFile, dur: int = 0, d: str = Depends(device)):
    key = f"{d}/{secrets.token_hex(8)}_{kind}"
    full = os.path.join(MEDIA, key)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    data = await file.read()
    with open(full, "wb") as f: f.write(data)
    async with pool.acquire() as c:
        mid = await c.fetchval(
            "INSERT INTO media (device_id,kind,path,size,dur) VALUES ($1,$2,$3,$4,$5) RETURNING id",
            d, kind, key, len(data), dur)
    await bus.pub("panel", {"t": "media", "device": d})
    return {"ok": True, "id": mid}

@app.get("/m/{key:path}")
async def media_file(key: str, e: int, s: str):
    exp = hmac.new(SECRET, f"{key}:{e}".encode(), hashlib.sha256).hexdigest()[:32]
    if s != exp or int(e) < time.time(): raise HTTPException(403)
    p = os.path.join(MEDIA, key)
    if not os.path.exists(p): raise HTTPException(404)
    if key.endswith("photo_front") or key.endswith("photo_back"):
        mt = "image/jpeg"
    elif key.endswith("voice"):
        mt = "audio/wav"
    else:
        mt = "application/octet-stream"
    return FileResponse(p, media_type=mt)

@app.get("/api/devices", dependencies=[Depends(admin)])
async def devices():
    async with pool.acquire() as c:
        rows = await c.fetch("SELECT id,name,model,last_seen,online FROM devices WHERE NOT revoked ORDER BY last_seen DESC NULLS LAST")
    return [dict(r) for r in rows]

@app.post("/api/codes", dependencies=[Depends(admin)])
async def new_code():
    code = secrets.token_hex(3).upper()
    async with pool.acquire() as c:
        await c.execute("INSERT INTO codes (code) VALUES ($1)", code)
    return {"code": code}

@app.get("/api/devices/{did}/chats", dependencies=[Depends(admin)])
async def chats(did: str):
    async with pool.acquire() as c:
        rows = await c.fetch("""SELECT contact, MAX(ts) ts, COUNT(*) n,
            (SELECT body FROM events e2 WHERE e2.device_id=$1 AND e2.contact=e.contact
             ORDER BY ts DESC LIMIT 1) last
            FROM events e WHERE device_id=$1 AND contact<>'' GROUP BY contact ORDER BY ts DESC""", did)
    return [dict(r) for r in rows]

@app.get("/api/devices/{did}/chat", dependencies=[Depends(admin)])
async def chat(did: str, contact: str):
    async with pool.acquire() as c:
        rows = await c.fetch("SELECT id,ts,kind,title,body,src FROM events WHERE device_id=$1 AND contact=$2 ORDER BY ts ASC LIMIT 500", did, contact)
    return [dict(r) for r in rows]

@app.get("/api/devices/{did}/locations", dependencies=[Depends(admin)])
async def locations(did: str):
    async with pool.acquire() as c:
        rows = await c.fetch("SELECT ts, body FROM events WHERE device_id=$1 AND kind='loc' ORDER BY ts DESC LIMIT 200", did)
    return [dict(r) for r in rows]

@app.get("/api/devices/{did}/media", dependencies=[Depends(admin)])
async def media_list(did: str):
    async with pool.acquire() as c:
        rows = await c.fetch("SELECT id,ts,kind,dur,path FROM media WHERE device_id=$1 ORDER BY ts DESC LIMIT 200", did)
    return [{"id": r["id"], "ts": r["ts"], "kind": r["kind"], "dur": r["dur"], "url": sign(r["path"])} for r in rows]

@app.post("/api/devices/{did}/cmd", dependencies=[Depends(admin)])
async def cmd(did: str, kind: str):
    async with pool.acquire() as c:
        cid = await c.fetchval("INSERT INTO commands (device_id,kind) VALUES ($1,$2) RETURNING id", did, kind)
    await bus.pub(f"dev:{did}", {"id": cid, "kind": kind})
    return {"ok": True}

@app.websocket("/ws/device")
async def ws_device(ws: WebSocket):
    tok = ws.query_params.get("token")
    async with pool.acquire() as c:
        r = await c.fetchrow("SELECT id FROM devices WHERE token=$1 AND NOT revoked", tok)
        if not r: await ws.close(); return
        did = str(r["id"])
        await c.execute("UPDATE devices SET online=true, last_seen=now() WHERE id=$1", did)
    await ws.accept()
    ch = f"dev:{did}"
    bus.sub(ch, ws)
    try:
        while True: await ws.receive_text()
    except WebSocketDisconnect: pass
    finally:
        bus.unsub(ch, ws)
        async with pool.acquire() as c:
            await c.execute("UPDATE devices SET online=false WHERE id=$1", did)

@app.websocket("/ws/panel")
async def ws_panel(ws: WebSocket):
    t = ws.query_params.get("token")
    if not t or t != ADMIN: await ws.close(); return
    await ws.accept()
    bus.sub("panel", ws)
    try:
        while True: await ws.receive_text()
    except WebSocketDisconnect: pass
    finally: bus.unsub("panel", ws)

@app.get("/")
async def root(): return RedirectResponse("/panel/")

# Try /srv paths first (Docker), fallback to local paths (Railway/local dev)
panel_dir = "/srv/panel" if os.path.exists("/srv/panel") else "panel"
dist_dir = "/srv/dist" if os.path.exists("/srv/dist") else "dist"

app.mount("/panel", StaticFiles(directory=panel_dir, html=True), name="panel")
app.mount("/app", StaticFiles(directory=dist_dir, html=True), name="dist")
