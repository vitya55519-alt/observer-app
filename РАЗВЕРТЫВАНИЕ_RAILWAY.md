# 🚂 Развертывание на Railway + GitHub (БЕСПЛАТНО)

## ✨ Почему Railway идеален для теста?

- ✅ **Бесплатно:** $5 кредитов/месяц (достаточно для теста)
- ✅ **PostgreSQL включена:** не нужно настраивать
- ✅ **Автоматический HTTPS:** готовый домен вида `yourapp.railway.app`
- ✅ **Deploy за 2 минуты:** push на GitHub → автодеплой
- ✅ **Логи в реальном времени:** удобная отладка

---

## 📝 Шаг 1: Подготовка GitHub репозитория

### 1.1 Создать репозиторий на GitHub

1. Зайти на https://github.com/new
2. Название: `observer-app` (или любое другое)
3. **Private** (важно для безопасности!)
4. Create repository

### 1.2 Загрузить код

```bash
cd "C:\Users\Woterson\Documents\Проект по умолчанию"

# Инициализировать git (если еще не инициализирован)
git init

# Добавить remote
git remote add origin https://github.com/YOUR_USERNAME/observer-app.git

# Добавить файлы
git add .

# Коммит
git commit -m "Initial commit - Observer app"

# Загрузить на GitHub
git branch -M main
git push -u origin main
```

**Если нужна аутентификация:**
```bash
# Использовать Personal Access Token
# Settings → Developer settings → Personal access tokens → Generate new token
# Скопировать токен и использовать вместо пароля
```

---

## 🚂 Шаг 2: Развертывание на Railway

### 2.1 Создать аккаунт

1. Зайти на https://railway.app
2. **Sign up with GitHub** (рекомендуется)
3. Авторизовать доступ к репозиториям

### 2.2 Создать новый проект

1. **New Project**
2. **Deploy from GitHub repo**
3. Выбрать `observer-app`
4. Railway автоматически определит Python приложение

### 2.3 Настроить путь к приложению

**Важно!** Railway должен запускать файл из `server/app/`:

1. Нажать на созданный сервис
2. **Settings** → **Root Directory**
3. Установить: `server/app`
4. **Save**

### 2.4 Добавить PostgreSQL

1. В проекте нажать **+ New**
2. **Database** → **Add PostgreSQL**
3. Railway автоматически создаст переменную `DATABASE_URL`

### 2.5 Настроить переменные окружения

1. Нажать на сервис (не на БД)
2. **Variables** → **RAW Editor**
3. Добавить:

```env
ADMIN_PASSWORD=YourStrongPassword123
SECRET=your-secret-key-minimum-32-characters-long
DOMAIN=yourapp.railway.app
TG_TOKEN=
TG_CHAT=
```

**Важно:** `DATABASE_URL` уже добавлена автоматически Railway!

### 2.6 Deploy

1. **Deployments** → Последний деплой должен быть успешным
2. Если есть ошибки - смотреть **View Logs**

---

## 🌐 Шаг 3: Получить публичный URL

### 3.1 Включить публичный доступ

1. Нажать на сервис
2. **Settings** → **Networking**
3. **Generate Domain**
4. Скопировать URL: `https://yourapp-production-xxxx.up.railway.app`

### 3.2 Обновить переменную DOMAIN

1. **Variables** → Найти `DOMAIN`
2. Изменить на: `yourapp-production-xxxx.up.railway.app` (без https://)
3. **Save** → Railway автоматически передеплоит

---

## 🔧 Шаг 4: Инициализация БД

Railway автоматически создаст таблицы при первом запуске, но нужно убедиться:

### Вариант A: Через Railway CLI (рекомендуется)

```bash
# Установить Railway CLI
npm i -g @railway/cli

# Или через PowerShell
iwr https://railway.app/install.ps1 | iex

# Логин
railway login

# Подключиться к проекту
railway link

# Выполнить SQL
railway run psql $DATABASE_URL -f server/app/schema.sql
```

### Вариант B: Через веб-интерфейс

1. Открыть PostgreSQL в Railway
2. **Data** → **Query**
3. Скопировать содержимое `server/app/schema.sql`
4. Вставить и выполнить

---

## ✅ Шаг 5: Проверка работоспособности

### 5.1 Проверить API

```bash
curl https://yourapp-production-xxxx.up.railway.app/api/devices \
  -H "X-Admin-Token: YourStrongPassword123"
```

**Ожидаемый ответ:** `[]` (пустой массив)

### 5.2 Открыть веб-панель

```
https://yourapp-production-xxxx.up.railway.app/panel/
```

**Проблема:** Панель не будет работать, так как она в отдельной папке!

---

## 🔧 Шаг 6: Исправить структуру для Railway

Нужно объединить API и панель. Создам обновленный main.py:

### Изменения в коде:

Railway ожидает, что всё в одной папке. Нужно:

1. Скопировать `server/panel/` в `server/app/panel/`
2. Создать `server/app/dist/` для APK

```bash
# Выполнить локально
mkdir server\app\panel
mkdir server\app\dist
copy server\panel\* server\app\panel\
```

### Обновить main.py:

Я уже настроил пути в main.py правильно:
```python
app.mount("/panel", StaticFiles(directory="/srv/panel", html=True), name="panel")
app.mount("/app", StaticFiles(directory="/srv/dist", html=True), name="dist")
```

Но для Railway нужно изменить на:
```python
app.mount("/panel", StaticFiles(directory="panel", html=True), name="panel")
app.mount("/app", StaticFiles(directory="dist", html=True), name="dist")
```

Также удалить строки создания `/srv/` директорий.

Я создам патч...
