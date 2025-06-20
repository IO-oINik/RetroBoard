# Retrospective Boards API

## Описание

Приложение для создания ретроспективных досок с real-time обновлениями и системой голосования.

**Возможности:**

- Создавать интерактивные доски ретроспектив
- 3 типа доступа (Владелец/Редактор/Зритель)
- Добавлять компоненты (карточки) с типом POSITIVE/NEGATIVE/IDEA
- Анонимные голосования и карточки
- Голосовать за важные пункты
- Совместно редактировать в реальном времени (REST API + SSE)
- Систему доступа через JWT + инвайт-токены

## Запуск проекта

### Требования
- Docker 20.10+
- Docker Compose 1.29+

### Команды

Запуск всех сервисов (фоновый режим):
```bash
docker-compose up --build -d
```

Проверка статуса:
```bash
docker-compose ps
```


Остановка:
```bash
docker-compose down
```

Логи API:
```bash
docker-compose logs -f api-service
```

## Технологии

- Spring Boot
- Spring Jpa
- Spring Security
- PostgreSql
- JWT
- SpringDoc
- Kafka