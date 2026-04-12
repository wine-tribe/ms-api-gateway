# 🚪 ms-api-backend

`ms-api-backend` — единая точка входа в систему (API Gateway) на базе **Spring Cloud Gateway**.  
Проксирует запросы во все микросервисы по префиксам, агрегирует Swagger-документацию и готов к централизованному логированию/аудиту.

---

## 🧭 Маршрутизация (единый вход)

Gateway стартует на:

👉 `http://localhost:8080`

Префиксы сервисов:

- **Warehouse** → `http://localhost:8080/warehouse/**`  → `http://localhost:8081/**`
- **Delivery**  → `http://localhost:8080/delivery/**`   → `http://localhost:8082/**`
- **Customer**  → `http://localhost:8080/customer/**`   → `http://localhost:8083/**`
- **Payment**   → `http://localhost:8080/payment/**`    → `http://localhost:8084/**`

> Внутри backend используется `StripPrefix=1`, поэтому сервисы получают “родные” пути без префикса.

---

## 📚 Swagger UI (агрегированный)

Единый Swagger UI на backend:

👉 `http://localhost:8080/swagger-ui.html`

Там доступны спецификации всех сервисов (выбор через выпадающий список).

---

## 🚀 Запуск сервиса

Перед запуском убедитесь, что установлены **JDK 21** и проект собирается через **Gradle Wrapper**.

### 🔨 Сборка

```bash
.\gradlew clean build -x test
```

### ▶️ Запуск локально

```bash
.\gradlew bootRun
```

---

## ⚙️ Конфигурация (порты/URL)

По умолчанию backend слушает `8080`, а сервисы доступны по:

- warehouse: `http://localhost:8081`
- delivery: `http://localhost:8082`
- customer: `http://localhost:8083`
- payment: `http://localhost:8084`

Порт backend можно изменить через переменную окружения:

- `SERVER_PORT` (по умолчанию `8080`)

---

## 🔎 Actuator / Metrics

- Health: `http://localhost:8080/actuator/health`
- Prometheus: `http://localhost:8080/actuator/prometheus`

---

## 🧾 Логирование / Корреляция запросов

Gateway генерирует и/или прокидывает `X-Request-Id` и пишет access-логи вида:

- requestId
- method
- path
- status
- durationMs

Это база для будущего:
- централизованного аудита,
- распределённого трейсинга,
- журналирования действий пользователей.

---

## 🌐 CORS

Включён глобальный CORS для всех путей (`[/**]`) — разрешены методы/заголовки `*`,
а также `X-Request-Id` добавлен в `exposedHeaders`.

---

## 🧩 Используемые технологии

- Java 21
- Spring Boot 3
- Spring Cloud Gateway (WebFlux)
- Springdoc OpenAPI (WebFlux UI)
- Actuator + Prometheus
- Docker (опционально)

---

## 🛠️ Troubleshooting

### Swagger UI “Try it out” бьёт не туда / 404

Проверь:

- routes в backend настроены с `StripPrefix=1`
- Swagger UI открыт именно на `http://localhost:8080/swagger-ui.html`
- сервисы отдают корректный `servers.url` для работы за префиксом backend (`/warehouse`, `/delivery`, `/customer`, `/payment`)

### Ошибка Gradle Wrapper

Если `./gradlew` падает (например, `NoClassDefFoundError`), пересоздай wrapper командой `wrapper` из любого рабочего сервиса
или установи Gradle и выполни `gradle wrapper`.

---

## ✅ Пример вызова через backend (PowerShell)

Получить JWT (если auth реализован в warehouse):

```powershell
$body = @{ username="demo"; password="demo" } | ConvertTo-Json
Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/warehouse/api/auth/authenticate" `
  -ContentType "application/json" `
  -Body $body
```
