# 🚂 Railway Deploy БЕЗ домена (используем Railway домен)

## ⚡ За 5 минут до рабочего сервера

Railway автоматически дает вам публичный домен вида:
```
observer-app-production-xxxx.up.railway.app
```

Он меняется при перезагрузке, но для тестирования идеален!

---

## 🎬 Быстрый деплой (4 шага)

### Шаг 1: Откройте Railway
```
https://railway.app/dashboard
```

Если не залогинены:
1. **Sign up** → **Continue with GitHub**
2. Авторизуйте доступ

### Шаг 2: Создайте проект

1. **+ New Project**
2. **Deploy from GitHub repo**
3. Найдите `vitya55519-alt/observer-app`
4. Выберите и дождитесь деплоя (~1 минута)

### Шаг 3: Добавьте PostgreSQL

1. В проекте нажмите **+ New Service**
2. **Database** → **PostgreSQL**
3. Дождитесь создания (~30 сек)

**Railway автоматически создаст переменную `DATABASE_URL`!**

### Шаг 4: Установите переменные окружения

1. Нажмите на сервис `observer-app`
2. **Variables** → добавьте:

```
ADMIN_PASSWORD=TestPass123
SECRET=ThisIsASecretKeyFor32CharsMinimumLength12345
DOMAIN=observer-app-production.up.railway.app
```

(Значение DOMAIN будет автоматически обновлено Railway)

---

## ✅ Готово! Проверка

### 1. Откройте панель

```
https://observer-app-production-xxxx.up.railway.app/panel/
```

**Пароль:** `TestPass123`

### 2. Создайте код привязки

В браузере (DevTools → Console):

```javascript
fetch('https://observer-app-production-xxxx.up.railway.app/api/codes', {
  method: 'POST',
  headers: {
    'X-Admin-Token': 'TestPass123'
  }
})
.then(r => r.json())
.then(d => console.log(d))
```

**Скопируйте код из ответа**

---

## 📱 Собрать APK автоматически

### Способ 1: GitHub Actions (рекомендуется)

APK собирается **автоматически** при каждом push!

1. Откройте: https://github.com/vitya55519-alt/observer-app/actions
2. Найдите последний workflow "Build APK"
3. Если он успешен ✅ → скачайте APK:
   - **Build APK** (workflow)
   - **Artifacts** → `app-release.apk`

**Готово!** APK собран в облаке, никакой Android Studio не нужна!

### Способ 2: Локально (если GitHub Actions не сработал)

```bash
# Установить Android Studio
# https://developer.android.com/studio

# В папке проекта
cd android
./gradlew.bat assembleRelease

# APK в: android\app\build\outputs\apk\release\app-release.apk
```

---

## 🔌 Установить APK на устройство

### Через ADB (USB):

```bash
# 1. Включить Developer Mode на телефоне
# Settings → About → Build Number (нажать 7 раз)

# 2. Включить USB Debug
# Settings → Developer Options → USB Debugging

# 3. Подключить через USB

# 4. Установить
adb install path\to\app-release.apk
```

### Вручную:

1. Скачать APK на компьютер
2. Перекинуть на телефон (USB, Telegram, облако)
3. Открыть файловый менеджер
4. Нажать на APK → Установить

---

## 🎯 Первый запуск приложения

После установки:

1. Открыть Observer
2. **Адрес сервера:** `https://observer-app-production-xxxx.up.railway.app`
3. **Код привязки:** из JavaScript кода выше
4. **Имя устройства:** `TestDevice`
5. **Привязать**

✅ Устройство должно появиться в панели!

---

## 🌐 Если нужен постоянный домен

Railway домен меняется при перезагрузке. Для продакшена:

### Вариант 1: Railway Custom Domain (платно)
- Railway → Domain → Custom Domain
- Потребуется HTTPS сертификат

### Вариант 2: Купить домен + настроить DNS
1. Купить домен на Namecheap/GoDaddy/etc
2. Railway → Domain → Add Custom Domain
3. Настроить DNS CNAME

### Вариант 3: Использовать ngrok (бесплатно, временно)
```bash
# Установить
choco install ngrok

# Запустить туннель
ngrok http 443

# Получите публичный URL: https://xxxxx.ngrok.io
```

---

## 🔍 Если что-то не работает

### Проверить логи Railway

1. Railway → проект → сервис `observer-app`
2. **Deployments** → последний деплой
3. **View Logs** → смотреть ошибки

### Типичные ошибки

**Ошибка:** `DATABASE_URL not found`
```
✓ Решение: Убедитесь, что PostgreSQL добавлена
```

**Ошибка:** `Timeout`
```
✓ Решение: Railway может быть на бесплатном плане
Подождите 1-2 минуты и попробуйте еще раз
```

**Ошибка:** `Panel not found`
```
✓ Решение: Убедитесь, что Root Directory = server/app
```

---

## 📊 Архитектура

```
GitHub (ваш код)
    ↓
GitHub Actions (автосборка APK)
    ↓
Railway (сервер + БД)
    ↓
Android приложение ← → Веб-панель
```

---

## ✅ Контрольный список

- [ ] GitHub Actions успешно собрал APK
- [ ] Railway развернул приложение
- [ ] PostgreSQL добавлена
- [ ] Переменные окружения установлены
- [ ] Панель открывается: https://observer-app-production-xxxx.up.railway.app/panel/
- [ ] Код привязки создан
- [ ] APK установлен на устройство
- [ ] Устройство появилось в панели

---

## 🎉 Готово!

Сервер работает! Теперь просто:

1. Для изменений:
```bash
git push origin master
```
GitHub Actions автоматически пересоберет APK

2. Railway автоматически перезагружается при обновлении

3. Всё работает без докера, без локальной установки!

---

**Вопросы? Проблемы? Пишите в логи Railway - там вся информация!** 🚀
