# ✅ ГОТОВО! Теперь разверните на Railway

## 🎯 GitHub репозиторий готов

✅ Код загружен: https://github.com/vitya55519-alt/observer-app
✅ Панель подготовлена для Railway
✅ Все исправления применены

---

## 🚂 Шаги развертывания на Railway (5 минут)

### Шаг 1: Зайти на Railway

1. Откройте https://railway.app
2. **Sign up** → **Continue with GitHub**
3. Авторизуйте Railway для доступа к репозиториям

### Шаг 2: Создать новый проект

1. **+ New Project**
2. **Deploy from GitHub repo**
3. Выберите `vitya55519-alt/observer-app`
4. Railway начнет автоматический деплой

⏳ **Подождите 1-2 минуты...**

### Шаг 3: Настроить Root Directory

**Важно!** Railway должен запускать приложение из `server/app/`:

1. Нажмите на созданный сервис (должен называться `observer-app`)
2. **Settings** → **Root Directory**
3. Введите: `server/app`
4. **Deploy** (Railway перезагрузится)

### Шаг 4: Добавить PostgreSQL

1. В проекте нажмите **+ New**
2. **Database** → **Add PostgreSQL**
3. Подождите создания (~30 секунд)

✅ Railway автоматически создаст переменную `DATABASE_URL`

### Шаг 5: Настроить переменные окружения

1. Нажмите на сервис `observer-app` (не на БД!)
2. **Variables** → **+ New Variable**

Добавьте по одной:

```
ADMIN_PASSWORD=YourStrongPassword123
SECRET=your-super-secret-key-at-least-32-characters-long-for-security
DOMAIN=
TG_TOKEN=
TG_CHAT=
```

**Важно:** Поле `DOMAIN` оставьте пустым пока!

### Шаг 6: Получить публичный домен

1. На сервисе `observer-app` → **Settings**
2. **Networking** → **Public Networking**
3. **Generate Domain**
4. Скопируйте полученный URL (например: `observer-app-production-1a2b.up.railway.app`)

### Шаг 7: Обновить DOMAIN

1. Вернитесь в **Variables**
2. Найдите переменную `DOMAIN`
3. Установите значение: `observer-app-production-1a2b.up.railway.app`
   *(БЕЗ https://, только домен!)*
4. Сохраните → Railway автоматически передеплоит

⏳ **Подождите перезапуска (~30 секунд)**

---

## ✅ Проверка работоспособности

### 1. Проверить API

Откройте PowerShell:

```powershell
$url = "https://observer-app-production-1a2b.up.railway.app/api/devices"
$headers = @{"X-Admin-Token" = "YourStrongPassword123"}
Invoke-RestMethod -Uri $url -Headers $headers
```

**Ожидаемый результат:** `[]` (пустой массив)

### 2. Открыть веб-панель

```
https://observer-app-production-1a2b.up.railway.app/panel/
```

**Логин:**
- Пароль: `YourStrongPassword123` (из ADMIN_PASSWORD)

✅ Если панель открылась - **всё работает!**

---

## 🔐 Создать код привязки устройства

В PowerShell:

```powershell
$url = "https://observer-app-production-1a2b.up.railway.app/api/codes"
$headers = @{"X-Admin-Token" = "YourStrongPassword123"}
$response = Invoke-RestMethod -Uri $url -Method POST -Headers $headers
$response
```

**Результат:**
```json
{"code": "A1B2C3"}
```

**Этот код используйте в Android приложении для привязки устройства!**

---

## 📱 Следующий шаг: Собрать APK

Теперь нужно собрать Android приложение.

### Вариант 1: Android Studio (рекомендуется)

1. Скачать: https://developer.android.com/studio
2. Установить (~20 минут)
3. **File** → **Open** → выбрать папку `android/`
4. Дождаться Gradle Sync (~5-10 минут)
5. **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
6. Готово! APK в `android\app\build\outputs\apk\release\app-release.apk`

### Вариант 2: Онлайн сборка (если не хотите ставить Android Studio)

Можно использовать GitHub Actions для автоматической сборки APK в облаке.

Хотите, чтобы я настроил автоматическую сборку через GitHub Actions?

---

## 🎉 Что дальше?

После сборки APK:

1. Установить APK на Android устройство
2. В приложении ввести:
   - **Адрес сервера:** `https://observer-app-production-1a2b.up.railway.app`
   - **Код привязки:** `A1B2C3` (из команды выше)
   - **Имя устройства:** `TestDevice`
3. Нажать **Привязать**
4. Устройство появится в веб-панели!

---

## 🆘 Помощь

**Не получается развернуть?**
- Проверьте логи: Railway → сервис → **Deployments** → последний → **View Logs**

**Панель не открывается?**
- Убедитесь, что Root Directory = `server/app`
- Проверьте, что переменная DOMAIN заполнена

**API возвращает ошибку?**
- Проверьте, что PostgreSQL добавлена
- Проверьте, что DATABASE_URL создалась автоматически

---

## 📊 Готово!

✅ Сервер в облаке  
✅ PostgreSQL база данных  
✅ Веб-панель  
✅ Публичный HTTPS домен  
✅ Бесплатно!  

**Осталось только собрать APK!**

Хотите, чтобы я помог с:
1. Установкой Android Studio?
2. Настройкой автоматической сборки APK через GitHub Actions?
3. Чем-то еще?
