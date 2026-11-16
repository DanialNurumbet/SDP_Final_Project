# Паттерны проектирования SportRent

Каждый участник реализовал два паттерна. Подробности в `docs/member-1.md` ... `docs/member-4.md`.
Диаграмма классов с участниками паттернов: `docs/diagrams/class-diagram.puml` (PNG рядом).

Напоминание:

- Участник 1 - Дамир К
- Участник 2 - Арыстанали
- Участник 3 - Артем
- Участник 4 - Даниал

| Pattern | Problem | Classes/interfaces | How to demonstrate | Responsible student |
|---|---|---|---|---|
| Factory Method | При создании предмета и при чтении из JSON нужен правильный подкласс со своей подсказкой, а сервис каталога не должен создавать продукты напрямую | `EquipmentCreator`, `BicycleCreator`, `BallCreator`, `RacketCreator`, `Bicycle`, `Ball`, `Racket`, `EquipmentFactory` | В каталоге добавить по одному предмету каждого типа и сравнить подсказки в карточках; открыть `EquipmentCreator.createEquipment()` и три создателя | Участник 1 (Дамир К) |
| Adapter | Бизнес-модулям нужны операции репозиториев, а файловый слой работает с Jackson, путём и целым JSON-документом | `JsonStorageAdapter`, `JsonRentalRepository`, `JsonFileStorage`, `StoredEquipment`, `StorageData` | Добавить предмет, перезапустить приложение и найти его с прежним ID; показать JSON-файл без имён Java-классов | Участник 1 (Дамир К) |
| Strategy | Правило скидки зависит от тарифа и должно меняться без правки остального расчёта | `PricingStrategy`, `StandardPricingStrategy`, `LongTermPricingStrategy`, `RentalPricingService` | Рассчитать 7 дней по ставке 1 000 KZT: стандартный тариф 7 000 KZT, долгосрочный 6 300 KZT | Участник 2 |
| Decorator | Дополнительные услуги нужно сочетать и показывать отдельными строками стоимости | `RentalPrice`, `BaseRentalPrice`, `ExtraPriceDecorator`, `InsuranceDecorator`, `AccessoryKitDecorator` | Рассчитать с одной услугой, затем с обеими: цепочка `AccessoryKitDecorator(InsuranceDecorator(BaseRentalPrice))`, в разбивке две строки услуг | Участник 2 |
| Builder | Неизменяемая аренда содержит много полей, и её нельзя создавать неполной | `RentalBuilder`, `Rental`, `RentalCommandService` | Оформить аренду и открыть `RentalBuilder.build()`: проверка обязательных полей до создания записи | Участник 3 |
| Observer | Оформление и возврат должны вызывать реакции других компонентов без прямых зависимостей от них | `RentalEventPublisher`, `RentalEventListener`, `RentalOperationLogger`, `LatestRentalNotificationStore` | Оформить и вернуть аренду; увидеть запись в логе приложения и ответ `GET /api/rental-notifications/latest` | Участник 3 |
| Facade | Для истории и отчёта нужно читать аренды, фильтровать, сортировать, считать сводку и запускать экспорт; контроллер не должен знать об этом | `RentalQueryService`, `RentalReportFacade`, `RentalQueryController`, `TextReportExporter` | На странице «История и отчёты» открыть список, фильтр, сводку; показать `RentalReportFacade` и тонкий `RentalQueryController` | Участник 4 |
| Visitor | Форматирование отчёта не должно находиться в моделях, а новый формат не должен менять модели | `ReportElement`, `ReportVisitor`, `Rental`, `EquipmentSnapshot`, `PriceBreakdown`, `TextReportVisitor` | Скачать TXT-отчёт; показать, что `Rental.accept()` проходит по аренде, снимку и разбивке, а текст формирует только `TextReportVisitor` | Участник 4 |
