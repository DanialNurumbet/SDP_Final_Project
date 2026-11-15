# contracts SportRent

Фиксирует Даниал. Изменения только по согласованию с ним и затронутыми участниками.
Пакет: `com.sportrent`. Деньги: `BigDecimal`, KZT, scale 2, `HALF_UP`. Время: `Instant` (ISO-8601). Идентификаторы: `UUID`.
----- Роли -----
1 - Дамир К
2 - Арыстанали
3 - Артем
4 - Даниал
## Enum (`shared.model`)
| Enum | Значения |
|---|---|
| EquipmentType | BICYCLE, BALL, RACKET |
| Tariff | STANDARD, LONG_TERM |
| ExtraService | INSURANCE, ACCESSORY_KIT |
| RentalStatus | ACTIVE, COMPLETED |

## Models (`shared.model`)
- `abstract class Equipment` — `getId()`, `getName()`, `getDailyRate()`, `getType()`, `getUsageHint()`. Подклассы Bicycle/Ball/Racket и создатели — №1 (`catalog`). Флага `available` нет.
- `record Customer(String name, String phone)`
- `record EquipmentSnapshot(String name, EquipmentType type, BigDecimal dailyRate)` — `ReportElement`
- `record ExtraLine(ExtraService extra, BigDecimal cost)`
- `record PriceBreakdown(BigDecimal baseCost, BigDecimal discount, BigDecimal discountedCost, List<ExtraLine> extras, BigDecimal total)` — `ReportElement`
- `record RentalTerms(int days, Tariff tariff, List<ExtraService> extras)`
- `record Rental(UUID id, UUID equipmentId, Customer customer, EquipmentSnapshot equipmentSnapshot, int days, Tariff tariff, List<ExtraService> extras, PriceBreakdown priceBreakdown, RentalStatus status, Instant createdAt, Instant returnedAt)` — `ReportElement`; `completedAt(Instant)` возвращает завершённую копию.

## Interfaces (`shared.contract`)
| Интерфейс                                                                 | Методы | Реализует |
|---------------------------------------------------------------------------|---|---|
| EquipmentRepository                                                       | `List<Equipment> findAll()`; `Optional<Equipment> findById(UUID)`; `Equipment save(Equipment)`; `void deleteById(UUID)` | №1 |
| RentalRepository                                                          | `List<Rental> findAll()`; `Optional<Rental> findById(UUID)`; `Rental save(Rental)` | №1 |
| EquipmentAvailabilityService *(предложение, согласовать Дамиру и Артему)* | `boolean isAvailable(UUID equipmentId)` | №1 |
| PricingService                                                            | `PriceBreakdown calculate(Equipment, RentalTerms)` | №2 |
| RentalService                                                             | `Rental createRental(RentalRequest)`; `Rental returnRental(UUID)` | №3 |
| RentalQueryService                                                        | `List<Rental> findRentals(RentalStatus status)`; `Rental getRental(UUID)`; `ReportSummary getSummary()`; `String buildTextReport()` | №4 |
| ReportElement / ReportVisitor                                             | `accept(ReportVisitor)`; `visit(Rental)`, `visit(EquipmentSnapshot)`, `visit(PriceBreakdown)` | №4 (паттерн Visitor) |

## DTO (`shared.dto`)
Внутренние технические поля хранения в DTO не попадают.
- `EquipmentRequest { type, name, dailyRate }`
- `EquipmentResponse { id, type, name, dailyRate, available, usageHint }`
- `QuoteRequest { equipmentId, days (Integer), tariff, extras }`
- `RentalRequest { equipmentId, customer {name, phone}, days (Integer), tariff, extras }` — цену клиент не присылает
- `RentalResponse { id, equipmentId, customer, equipmentSnapshot, days, tariff, extras, priceBreakdown, status, createdAt, returnedAt }`
- `ReportSummary { totalCount, activeCount, completedCount, totalRentalsAmount }` — это сумма стоимости всех оформленных аренд, не выручка

## Errors (`shared.error`)
Формат: `{"code": "...", "message": "...", "fieldErrors": {}}`.
Модули бросают `ValidationException` (400, `VALIDATION_ERROR`), `NotFoundException` (404), `ConflictException` (409). Контроллеры ошибки не ловят: их обрабатывает `GlobalExceptionHandler`. Неизвестное исключение → 500 `INTERNAL_ERROR` без stack trace.

Рекомендуемые коды: `EQUIPMENT_NOT_FOUND`, `RENTAL_NOT_FOUND`, `EQUIPMENT_UNAVAILABLE`, `EQUIPMENT_IN_USE`, `RENTAL_ALREADY_COMPLETED`.
Статусы: 200 чтение/изменение/возврат, 201 создание, 204 удаление, 400, 404, 409.

## REST-paths
| Метод и путь | Кто | Контроллер | Ответ |
|---|-----|---|---|
| GET /api/equipment | №1  | EquipmentController | 200 `EquipmentResponse[]` |
| GET /api/equipment/{id} | №1  | EquipmentController | 200 `EquipmentResponse` |
| POST /api/equipment | №1  | EquipmentController | 201 `EquipmentResponse` |
| PUT /api/equipment/{id} | №1  | EquipmentController | 200 `EquipmentResponse` |
| DELETE /api/equipment/{id} | №1  | EquipmentController | 204 |
| POST /api/pricing/quote | №2  | PricingController | 200 `PriceBreakdown` |
| POST /api/rentals | №3  | RentalCommandController | 201 `RentalResponse` |
| POST /api/rentals/{id}/return | №3  | RentalCommandController | 200 `RentalResponse` |
| GET /api/rental-notifications/latest | №3  | (по выбору №3) | 200 уведомление |
| GET /api/rentals?status=ACTIVE | №4  | RentalQueryController | 200 `RentalResponse[]`, новые первыми |
| GET /api/rentals/{id} | №4  | RentalQueryController | 200 `RentalResponse` |
| GET /api/reports/summary | №4  | RentalQueryController | 200 `ReportSummary` |
| GET /api/reports/rentals.txt | №4  | RentalQueryController | 200 `text/plain; charset=UTF-8`, скачивание |

`RentalCommandController` и `RentalQueryController` используют префикс `/api/rentals`, но не объявляют одинаковых маршрутов (POST у Артем, GET у Даниала).

## Settings (`application.properties`)
- `sportrent.storage.path=data/sportrent.json` — читает модуль storage (Дамир)
- `spring.jackson.deserialization.accept-float-as-int=false` — срок `7.5` даёт 400, а не округляется

## Questions
1. `EquipmentAvailabilityService` — как предложение. Подтвердить Дамиру и Артему или убрать.
2. DTO уведомления для `/api/rental-notifications/latest` определяет Артем
