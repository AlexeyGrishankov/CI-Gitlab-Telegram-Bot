# CI BOT GITLAB TELEGRAM

### Фичи
- Android сборка проекта + автоинкрементация
- iOS (скоро)
- Desktop (скоро)

### Установка:

- Склонировать репозиторий
- Запустить таску для билда проекта `:shadowJar`
- Создать config/settings.json рядом с jar файлом
```json
{
  "botToken": "TELEGRAM_BOT_TOKEN",
  "gitlabLink": "gitlab.com"
}
```
- Создать `start.sh` и запустить
```bash
java -jar ci_bot-1.0.0.jar
```
