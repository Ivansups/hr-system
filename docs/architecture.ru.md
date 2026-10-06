# HR System — обзор архитектуры

Учебный проект в три стадии, каждая закоммичена отдельно (см. `git log`):

| Стадия | Задание | Результат |
|--------|---------|-----------|
| 1 | Модель, репозитории, сервисы, консоль, хранение в памяти | `3d9bad4` |
| 2 | Те же интерфейсы репозиториев, но реализация в PostgreSQL через чистый JDBC (без JPA/ORM) | `92a5ccf` |
| 3 | Консоль заменена сервлетами, деплой в Tomcat (проверка через Postman/curl) | `ef190a0` |

Каждый юнит сделан по TDD: коммит `test:` (красный) → коммит `feat:`.

## 1. Слои

Каждый слой зависит только от нижележащего. Репозитории — интерфейсы, поэтому замена in-memory на JDBC на стадии 2 не затронула сервисы.

```mermaid
flowchart TD
    C[web: сервлеты + JsonMapper] --> S[service: Department / Position / Employee]
    S --> R[repository: интерфейсы]
    R --> J[repository.jdbc: JDBC + HikariCP]
    J --> DB[(PostgreSQL)]
    S -.использует.-> M[mapper + dto]
    S -.использует.-> D[model]
```

## 2. Модель предметной области

`Position` — `sealed`-иерархия: три разрешённых подтипа, у каждого свои поля.

```mermaid
classDiagram
    class Department { Long id; String name }
    class Employee { Long id; String fullName; LocalDate hireDate }
    class Position { <<sealed abstract>> Long id; String title; BigDecimal baseSalary; getType() }
    class DeveloperPosition { String techStack; int grade }
    class ManagerPosition { int teamSize }
    class SalespersonPosition { BigDecimal salesPercent }

    Position <|-- DeveloperPosition
    Position <|-- ManagerPosition
    Position <|-- SalespersonPosition
    Employee "*" --> "1" Department
    Employee "*" --> "1" Position
```

## 3. Схема БД

Позиции хранятся по схеме **table-per-subclass**: общая таблица `positions` и по одной таблице на подтип, связанные через `position_id` (PK = FK, `ON DELETE CASCADE`). Колонка `type` подсказывает репозиторию, из какой таблицы подтипа читать.

```mermaid
erDiagram
    departments ||--o{ employees : has
    positions ||--o{ employees : "held by"
    positions ||--o| developer_positions : "type=DEVELOPER"
    positions ||--o| manager_positions : "type=MANAGER"
    positions ||--o| salesperson_positions : "type=SALESPERSON"

    departments { bigint id PK }
    positions { bigint id PK "type, title, base_salary" }
    employees { bigint id PK "full_name, hire_date, department_id FK, position_id FK" }
    developer_positions { bigint position_id PK "tech_stack, grade" }
    manager_positions { bigint position_id PK "team_size" }
    salesperson_positions { bigint position_id PK "sales_percent" }
```

Файл схемы: `src/main/resources/db/schema.sql` (применяется вручную, фреймворка миграций нет).

## 4. Поток запроса (POST /employees)

```mermaid
sequenceDiagram
    participant P as Postman
    participant S as EmployeeServlet
    participant Svc as EmployeeService
    participant Repo as JdbcEmployeeRepository
    participant DB as PostgreSQL

    P->>S: POST /employees (JSON)
    S->>Svc: create(EmployeeDto)
    Svc->>Svc: валидация (имя, hireDate)
    Svc->>Repo: найти отдел и позицию по id
    Svc->>Repo: save(Employee)
    Repo->>DB: INSERT ... RETURNING id
    Svc-->>S: EmployeeDto
    S-->>P: 201 Created (JSON)
```

## 5. Обработка ошибок

Сервисы бросают доменные исключения, сервлеты переводят их в HTTP-коды.

```mermaid
flowchart LR
    V[ValidationException] --> 400[400 Bad Request]
    N[EntityNotFoundException] --> 404[404 Not Found]
    OK[успех] --> 2xx[200 / 201 / 204]
```

Тело ошибки: `{"error": "<сообщение>"}`.

## 6. Запуск и сборка зависимостей

DI-фреймворка нет. Tomcat находит `AppInitializer` через `META-INF/services/jakarta.servlet.ServletContainerInitializer`, и тот вручную собирает всё приложение.

```mermaid
flowchart LR
    T[Старт Tomcat] --> A[AppInitializer.onStartup]
    A --> DS[DataSourceFactory: пул Hikari из DB_URL / DB_USER / DB_PASSWORD]
    DS --> Repos[Jdbc-репозитории]
    Repos --> Svcs[Сервисы]
    Svcs --> Srv["Сервлеты: /departments/*, /positions/*, /employees/*"]
```

## 7. Деплой

```mermaid
flowchart LR
    subgraph docker compose
        APP[app: Tomcat 10 + ROOT.war :8080] -->|JDBC| DB[(db: postgres 16)]
    end
    U[Postman / curl] -->|HTTP| APP
```

- `make up` — собрать и поднять оба контейнера
- `make test` — прогнать все тесты в Docker (интеграционные — через Testcontainers с настоящим Postgres)
- `make down` — остановить контейнеры и удалить volume с данными БД
- `make db-shell` — открыть `psql` в контейнере с БД

## 8. HTTP API

| Ресурс | Эндпоинты |
|--------|-----------|
| `/departments` | `GET` список, `GET /{id}`, `POST`, `DELETE /{id}` |
| `/positions` | `GET` список, `GET /{id}`, `POST`, `DELETE /{id}`; `type` в теле: `DEVELOPER` / `MANAGER` / `SALESPERSON` |
| `/employees` | `GET` список, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`; в теле `fullName`, `hireDate`, `departmentId`, `positionId` |

Сервис сотрудников также умеет `getByDepartment` / `getByPositionType`, но ни один маршрут сервлета их пока не использует.

## 9. Тесты

| Вид | Что проверяет | Инструменты |
|-----|---------------|-------------|
| Unit | модель, мапперы, сервисы | JUnit 5, Mockito |
| Интеграционные | JDBC-репозитории на настоящей БД | Testcontainers (Postgres) |

## 10. Общий flow и роль мапперов

`X` — любая сущность (Department, Position, Employee). На каждой границе данные меняют форму: строка JSON → DTO → модель → строки БД, и обратно.

```mermaid
sequenceDiagram
    participant C as Клиент
    participant Srv as XServlet
    participant J as JsonMapper
    participant Svc as XService
    participant M as XMapper
    participant R as XRepository (Jdbc)
    participant DB as PostgreSQL

    C->>Srv: HTTP, тело JSON (строка)
    Srv->>J: fromJson(строка)
    J-->>Srv: XDto
    Srv->>Svc: create(XDto)
    Svc->>M: toEntity(XDto)
    M-->>Svc: X (model)
    Svc->>R: save(X)
    R->>DB: INSERT
    R-->>Svc: X с id
    Svc->>M: toDto(X)
    M-->>Svc: XDto
    Svc-->>Srv: XDto
    Srv->>J: toJson(XDto)
    Srv-->>C: 201 + JSON
```

| Шаг | Где | Что происходит | Формат |
|-----|-----|----------------|--------|
| 1-2 | `XServlet` + `JsonMapper.fromJson` | Читает тело, разбирает JSON | строка → **DTO** |
| 3 | `XService` | Валидация, бизнес-правила | DTO |
| 4 | `XMapper.toEntity` | Собирает объект модели | **DTO → model** |
| 5 | `JdbcXRepository.save` | SQL `INSERT`, проставляет `id` | model → строки БД |
| 6 | `XMapper.toDto` | Собирает объект для ответа | **model → DTO** |
| 7-8 | `JsonMapper.toJson` + `XServlet.writeJson` | Сериализация, статус | DTO → строка |

Чтение (`GET /xs/{id}`) — то же самое без шагов 1-4: `findById` → `XMapper.toDto` → JSON.

**Два разных маппера:**
- `JsonMapper` (`web/`) переводит строку JSON ↔ DTO (граница HTTP).
- `XMapper` (`mapper/`) переводит DTO ↔ модель (граница сервис ↔ домен).

**Employee — особый случай.** У `EmployeeMapper` есть только `toDto`: в `EmployeeDto` лежат `departmentId` / `positionId`, а модели нужны реальные `Department` / `Position`. Поэтому `EmployeeService.create` сам достаёт их из репозиториев и вызывает `new Employee(...)`; маппер нужен только на выходе.
