# Участник №3: оформление и возврат

Модуль `rental` оформляет аренду только после серверной проверки доступности и повторного расчёта стоимости через `PricingService`. При создании сохраняется снимок инвентаря, а при возврате статус меняется на `COMPLETED`.

## Реализованные паттерны

- `RentalBuilder` проверяет обязательные поля перед созданием неизменяемого `Rental`.
- `RentalEventPublisher` публикует события после успешного сохранения.
- `RentalOperationLogger` журналирует создание и возврат.
- `LatestRentalNotificationStore` хранит последнее уведомление для `GET /api/rental-notifications/latest`.

Команды API:

- `POST /api/rentals`
- `POST /api/rentals/{id}/return`
- `GET /api/rental-notifications/latest`
