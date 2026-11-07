# 🚂 Railway.app - Полная инструкция развертывания

## 🎯 За 10 минут до рабочего сервера в облаке

### Требования:
- ✅ GitHub аккаунт (бесплатно)
- ✅ Railway аккаунт (бесплатно, $5/месяц кредитов)
- ✅ Git установлен

---

## 📋 Шаг 1: Подготовить GitHub репозиторий

### 1.1 Создать репозиторий

1. Зайти на https://github.com/new
2. **Repository name:** `observer-app`
3. **Description:** Observer surveillance app
4. **Private** ← важно!
5. ✅ **Create repository**

### 1.2 Загрузить код

Откройте PowerShell в папке проекта:

```powershell
cd "C:\Users\Woterson\Documents\Проект по умолчанию"

# Инициализировать git
git init

# Добавить remote (замените USERNAME)
git remote add origin https://github.com/YOUR_USERNAME/observer-app.git

# Добавить все файлы
git add .

# Первый коммит
git commit -m "Initial commit"

# Загрузить на GitHub
git branch -M main
git push -u origin main
```

**Если просит аутентификацию:**
1. Зайти на GitHub
2. **Settings** → **Developer settings** → **Personal access tokens** → **Tokens (classic)**
3. **Generate new token (classic)**
4. Скопировать токен
5. Вставить вместо пароля в PowerShell

---

## 🚂 Шаг 2: Развертывание на Railway

### 2.1 Зарегистрироваться на Railway

1. Зайти на https://railway.app
2. **Sign up** → **GitHub** (рекомендуется)
3. Авторизировать доступ

### 2.2 Создать новый проект

1. Dashboard → **+ New Project**
2. **Deploy from GitHub repo**
3. Авторизировать GitHub если нужно
4. Выбрать `observer-app`

### 2.3 Выбрать сервисы

Railway автоматически определит Python. Нажать **Deploy Now**.

**Подождать 1-2 минуты пока загружается...**

---

## 🐘 Шаг 3: Добавить PostgreSQL

### 3.1 Добавить БД

1. В проекте нажать **+ New**
2. **Database** → **Add PostgreSQL**
3. Подождать создания (~30 секунд)

Railway **автоматически создаст** переменную `DATABASE_URL`!

---

## ⚙️ Шаг 4: Настроить переменные окружения

### 4.1 Добавить переменные

1. Нажать на сервис `api` (не на БД!)
2. **Variables** → **RAW Editor**
3. Добавить:

```env
ADMIN_PASSWORD=SuperSecret123!@#
SECRET=YourSecretKeyMinimum32CharactersLongForSecurity12345
DOMAIN=
TG_TOKEN=
TG_CHAT=
```

**Сохранить** → Railway автоматически перезагружает приложение

### 4.2 Получить публичный домен

1. На сервисе `api` нажать **Settings**
2. **Networking** → **Generate Domain**
3. Скопировать полученный URL
4. Вернуться в **Variables**
5. Заполнить `DOMAIN`:

```env
DOMAIN=observer-production-xxxx.up.railway.app
```

(Без `https://`, только домен!)

---

## 📁 Шаг 5: Загрузить файлы панели и APK

Railway создан, но панель и APK нужно загрузить.

### 5.1 Локально подготовить файлы

```bash
# Скопировать панель в app
mkdir -p server\app\panel
copy server\panel\* server\app\panel\

# Создать папку для APK
mkdir server\app\dist
```

### 5.2 Загрузить на GitHub

```bash
git add server/app/panel/
git add server/app/dist/
git commit -m "Add panel and dist directories"
git push
```

Railway автоматически перезагружается после push!

**Подождать деплой (~1 минута)**

---

## ✅ Шаг 6: Проверка работоспособности

### 6.1 Проверить API

```powershell
$url = "https://observer-production-xxxx.up.railway.app/api/devices"
$headers = @{"X-Admin-Token" = "SuperSecret123!@#"}
Invoke-WebRequest -Uri $url -Headers $headers
```

**Ожидаемый результат:** `[]` (пустой массив JSON)

### 6.2 Открыть веб-панель

```
https://observer-production-xxxx.up.railway.app/panel/
```

Логин: пароль из `ADMIN_PASSWORD`

---

## 🔌 Шаг 7: Получить код привязки устройства

### 7.1 Создать код через curl

```powershell
$url = "https://observer-production-xxxx.up.railway.app/api/codes"
$headers = @{"X-Admin-Token" = "SuperSecret123!@#"}
$response = Invoke-WebRequest -Uri $url -Method POST -Headers $headers
$response.Content | ConvertFrom-Json
```

**Ответ:**
```json
{"code": "A1B2C3"}
```

Этот код вводится в Android приложение!

---

## 📱 Шаг 8: Собрать и загрузить APK

### 8.1 Собрать APK локально

```bash
cd android
./gradlew.bat assembleRelease
```

Результат: `android\app\build\outputs\apk\release\app-release.apk`

### 8.2 Копировать на Railway

```bash
copy android\app\build\outputs\apk\release\app-release.apk server\app\dist\app.apk
git add server/app/dist/app.apk
git commit -m "Add APK for distribution"
git push
```

**Теперь доступно по ссылке:**
```
https://observer-production-xxxx.up.railway.app/app/app.apk
```

---

## 🎬 Первый запуск приложения

### На Android устройстве/эмуляторе:

1. Открыть приложение Observer
2. **Адрес сервера:** `https://observer-production-xxxx.up.railway.app`
3. **Код привязки:** `A1B2C3` (из шага 7.1)
4. **Имя устройства:** `TestPhone`
5. **Привязать**

Устройство должно появиться в веб-панели!

---

## 🔍 Отладка

### Проверить логи Railway

1. В проекте нажать на сервис `api`
2. **Deployments** → последний деплой
3. **View Logs** → смотреть ошибки в реальном времени

### Частые ошибки

**Ошибка:** `DATABASE_URL not found`
```
Решение: Убедитесь, что PostgreSQL добавлена в проект
```

**Ошибка:** `Panel not found`
```
Решение: Скопировать server/panel/ в server/app/panel/
И сделать git push
```

**Ошибка:** `Timeout при подключении`
```
Решение: Railway может быть на бесплатном плане
Попробовать еще раз через минуту
```

---

## 💰 Стоимость

**Бесплатный план:**
- $5 кредитов в месяц
- 1 приложение Python
- 1 PostgreSQL
- **Хватает на тестирование!**

**Если нужно больше:**
- Pay as you go: от $0.0007 за Гб памяти в час
- Pro план: $10/месяц с гарантией

---

## 🚀 Автоматизация обновлений

После первого развертывания, просто:

```bash
# Внести изменения в коде
# Загрузить на GitHub
git add .
git commit -m "Update description"
git push

# Railway автоматически перезагружается!
```

---

## 📊 Готово!

Теперь у вас:
- ✅ Работающий сервер в облаке
- ✅ PostgreSQL база данных
- ✅ Веб-панель администратора
- ✅ Публичный HTTPS домен
- ✅ Возможность подключать Android устройства

**Всё бесплатно и без локальной установки Docker/PostgreSQL!**

---

## 🆚 Сравнение: Railway vs Docker Локально

| Параметр | Railway | Docker Локально |
|----------|---------|-----------------|
| **Стоимость** | $5/мес бесплатно | Бесплатно |
| **Доступ** | Из интернета | Только локально |
| **HTTPS** | Автоматический | Нужен Caddy |
| **Простота** | Очень просто | Средне |
| **Масштабируемость** | Легко масштабировать | Нужно настраивать |
| **Для теста** | ✅ Идеально | ✅ Хорошо |
| **Для продакшена** | ✅ Да | ⚠️ Нужна настройка |

---

## 📞 Что дальше?

1. **Протестировать** все функции приложения
2. **Собрать статистику** использования
3. **Если нужен продакшен:**
   - Купить реальный домен
   - Использовать Railway Pro или другой хостер
   - Добавить бэкапы БД
   - Настроить мониторинг

---

**Готовы начать? Следуйте шагам 1-8!** 🎉
