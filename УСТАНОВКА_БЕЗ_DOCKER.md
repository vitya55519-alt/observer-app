# 🔧 Установка БЕЗ Docker

Если Docker не установлен, вот альтернативные способы:

## 📱 Сборка APK

### Вариант 1: Android Studio (Рекомендуется)

1. **Скачать Android Studio:**
   https://developer.android.com/studio

2. **Установить:**
   - Запустить установщик
   - Выбрать "Standard" установку
   - Дождаться загрузки SDK (может занять 30 минут)

3. **Открыть проект:**
   ```
   File → Open → выбрать папку android/
   ```

4. **Дождаться Gradle Sync:**
   - Внизу появится прогресс-бар
   - Может занять 5-10 минут при первом запуске

5. **Собрать APK:**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```

6. **Найти APK:**
   ```
   android\app\build\outputs\apk\release\app-release.apk
   ```

---

## 🖥️ Запуск сервера локально (без Docker)

### Требования:
- Python 3.11+
- PostgreSQL 14+

### Шаг 1: Установить PostgreSQL

```powershell
# Через Chocolatey
choco install postgresql

# Или скачать с https://www.postgresql.org/download/windows/
```

### Шаг 2: Создать базу данных

```powershell
# Открыть SQL Shell (psql)
# По умолчанию: Server=localhost, Database=postgres, Port=5432, Username=postgres

# Создать базу
CREATE DATABASE obs;
CREATE USER obs WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE obs TO obs;
```

### Шаг 3: Настроить .env

```powershell
cd server
copy .env.example .env
notepad .env
```

Изменить:
```ini
DOMAIN=localhost
ADMIN_PASSWORD=your_admin_password
SECRET=your_secret_key_32_chars_minimum
DB_PASS=your_password
DATABASE_URL=postgresql://obs:your_password@localhost:5432/obs
```

### Шаг 4: Установить зависимости Python

```powershell
cd server/app
python -m pip install -r req.txt
```

### Шаг 5: Инициализировать БД

```powershell
# В SQL Shell (psql)
\c obs
\i C:/path/to/server/app/schema.sql
```

### Шаг 6: Запустить сервер

```powershell
cd server/app
python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

Сервер будет доступен на `http://localhost:8000`

### Шаг 7: Настроить панель

Скопировать файлы панели в доступное место:
```powershell
# Можно использовать Python HTTP сервер для раздачи статики
cd server/panel
python -m http.server 8080
```

Панель будет на `http://localhost:8080`

---

## 🚀 Быстрый запуск с Python (упрощенный)

Создаю готовый скрипт:
