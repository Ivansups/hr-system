# HR System

Учебный проект: система учёта сотрудников, реализованная в три стадии
(in-memory → JDBC → сервлеты). Подробности архитектуры — в
[`docs/architecture.md`](docs/architecture.md) ([RU](docs/architecture.ru.md)).

## Запуск

- `make up` — поднять Postgres и Tomcat с приложением на `localhost:8080`.
- `make test` — прогнать тесты (включая интеграционные, через
  Testcontainers).
- `make down` — остановить и удалить контейнеры и volume с данными БД.
- `make db-shell` — открыть psql внутри контейнера с БД.

Схема БД: `src/main/resources/db/schema.sql`, применяется вручную (нет
фреймворка миграций — см. спецификацию).

## HTTP API (стадия 3)

Консоль удалена — вся работа через HTTP API (Postman/curl), фронтенда нет.

```
curl -X POST localhost:8080/departments -d '{"name":"Engineering"}'
curl localhost:8080/departments
curl -X POST localhost:8080/positions -d '{"type":"DEVELOPER","title":"Backend Developer","baseSalary":2000,"techStack":"Java","grade":3}'
curl -X POST localhost:8080/employees -d '{"fullName":"Ivan Petrov","hireDate":"2024-01-01","departmentId":1,"positionId":1}'
curl localhost:8080/employees/1
curl -X DELETE localhost:8080/employees/1
```
