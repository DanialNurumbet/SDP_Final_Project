# Тестовые сценарии SportRent

## Общий результат автоматических тестов

Команда `.\mvnw.cmd clean verify`:
`Tests run: 65, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`.

| Класс | Тестов |
|---|---|
| `EquipmentApiTest` | 14 |
| `EquipmentFactoryTest` | 2 |
| `PricingApiTest` | 8 |
| `RentalPricingServiceTest` | 9 |
| `RentalCommandServiceTest` | 3 |
| `RentalReportFacadeTest` | 5 |
| `ReportingApiTest` | 3 |
| `TextReportExporterTest` | 3 |
| `SportRentApplicationTests` | 1 |
| `JsonStorageAdapterTest` | 13 |
| `StorageConsistencyTest` | 4 |

Статус «Автотест пройден» ниже означает, что названный тест выполнился в этом запуске без ошибок.

## Сценарии

| № | Сценарий | Входные данные | Ожидаемый результат | Фактический результат                                                                                                                                                                                                                                                                | Статус |
|---|---|---|---|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---|
| 1 | Создание предмета | Тип `BALL`, название `  Новый мяч  `, цена `123.455` | 201, название без пробелов, цена `123.46`, есть подсказка, предмет свободен | Автотест `EquipmentApiTest.createsReadsUpdatesAndDeletesEquipment`. Ручная проверка: `Работает`                                                                                                                                                                                      | Автотест пройден |
| 2 | Редактирование свободного предмета | Предмет из сценария 1, новое название и цена `500.00` | 200, тот же `id`, новая цена; тип менять нельзя | Автотесты `createsReadsUpdatesAndDeletesEquipment`, `forbidsChangingType`. Ручная проверка: `Работает`                                                                                                                                                                               | Автотест пройден |
| 3 | Переключение STANDARD / LONG_TERM | Ставка 1 000 KZT, 7 дней, без услуг | Стандартный 7 000 KZT; долгосрочный 6 300 KZT, скидка 700 KZT | Автотесты `RentalPricingServiceTest` (в том числе `standardKeepsFullBaseCost`); результаты по `docs/member-2.md`: 7 000 и 6 300 KZT. Ручная проверка: `Работает`                                                                                                                     | Автотест пройден |
| 4 | Добавление двух услуг | Ставка 1 000 KZT, `LONG_TERM`, 7 дней, страховка и комплект аксессуаров | Скидка 700, страховка 1 400, комплект 300, итого 8 000 KZT | Автотесты `RentalPricingServiceTest.combinesExtrasAfterDiscount`, `PricingApiTest.quotesCatalogRateAndBothExtras`. Ручная проверка: `Работает`                                                                                                                                       | Автотест пройден |
| 5 | Полный путь оформления и возврата | Свободный предмет, клиент, срок, тариф; затем возврат | Аренда `ACTIVE` со снимком предмета и ценой; после возврата `COMPLETED` с датой возврата; предмет снова свободен | Автотесты `RentalCommandServiceTest.createsRentalWithSnapshotAndPublishesEvent`, `returnsRentalAndPublishesReturnEvent`. Ручная проверка в браузере: `Работает`                                                                                                                      | Автотест пройден |
| 6 | Реакция подписчиков на событие | Оформить, затем вернуть аренду | Последнее уведомление имеет тип `CREATED`, затем `RETURNED`; запись в логе приложения | Автотесты из сценария 5 проверяют `LatestRentalNotificationStore`. Лог и `GET /api/rental-notifications/latest`: `Работает`                                                                                                                                                                  | Автотест пройден |
| 7 | Сохранение и загрузка после перезапуска | Добавить предмет, перезапустить приложение | Предмет найден с прежним `id`; демоданные не создаются повторно | Автотест `JsonStorageAdapterTest.seedsNineItemsOnlyOnceAndReloadsBothRepositories`. Ручной перезапуск: `Работает`                                                                                                                                                                    | Автотест пройден |
| 8 | Неверные поля и срок | Название из 1 символа, цена 0, срок 0, 31, `7.5`, дубль услуги, неизвестный тариф | HTTP 400 с полями ошибок в общем формате | Автотесты `EquipmentApiTest.returnsFieldErrorsForInvalidValues`, `PricingApiTest.rejectsUnknownTariffAndDuplicateExtras`, `RentalPricingServiceTest.rejectsDuplicateExtras`. Ручная проверка: `Работает`                                                                             | Автотест пройден |
| 9a | Повторная выдача занятого предмета | Оформить аренду на уже занятый предмет | 409 `EQUIPMENT_UNAVAILABLE`, вторая активная запись не создаётся | Автотесты `RentalCommandServiceTest.rejectsSecondRentalForSameEquipment`, `StorageConsistencyTest.simultaneousRentalSavesAllowOnlyOneActiveRecord`. Ручная проверка: `Работает`                                                                                                      | Автотест пройден |
| 9b | Повторный возврат | Вернуть уже завершённую аренду | 409 `RENTAL_ALREADY_COMPLETED` | В списке автотестов отдельного теста на повторный возврат нет. Ручная проверка: `Работает`                                                                                                                                                                                           | Не покрыто автотестом |
| 10 | Экспорт отчёта | После оформления аренды скачать `GET /api/reports/rentals.txt` | 200, `text/plain`, вложение, в тексте сводка, клиент, снимок предмета, точные суммы | Автотесты `ReportingApiTest.textReportIsDownloadableUtf8Text`, `TextReportExporterTest.exportContainsCustomerSnapshotAndExactAmounts`. Ручное скачивание и открытие файла: `Работает`                                                                                                | Автотест пройден |
| 11 | История, подробности и сводка (участник 4) | Сохранить аренду; открыть `/api/rentals`, `?status=ACTIVE`, `?status=COMPLETED`, `/api/rentals/{id}`, `/api/reports/summary` | Аренда в общем списке и в `ACTIVE`, нет в `COMPLETED`; детали с клиентом и снимком; сводка выросла на 1 и на стоимость аренды; неизвестный id даёт 404, неверный `status` или не UUID даёт 400 | Автотесты `ReportingApiTest.savedActiveRentalAppearsInHistoryDetailsAndSummary`, `unknownOrMalformedRequestsReturnUnifiedErrors`, `RentalReportFacadeTest` (5 тестов). Страница «История и отчёты» в браузере: `Работает`                                                            | Автотест пройден |

## Пробел

Повторный возврат (сценарий 9b) не покрыт автотестом. Его можно добавить в `RentalCommandServiceTest`: вернуть аренду дважды и ожидать `ConflictException` с кодом `RENTAL_ALREADY_COMPLETED`. Модуль принадлежит участнику 3, поэтому добавление нужно согласовать с ним.
