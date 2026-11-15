package com.sportrent.shared.model;

import java.util.List;

/** Условия аренды для расчёта. Корректность (1–30 дней, дубли услуг) проверяет PricingService. */
public record RentalTerms(int days, Tariff tariff, List<ExtraService> extras) {
}
