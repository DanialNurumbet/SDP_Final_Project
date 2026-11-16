package com.sportrent.reporting;

import com.sportrent.shared.model.Customer;
import com.sportrent.shared.model.EquipmentSnapshot;
import com.sportrent.shared.model.EquipmentType;
import com.sportrent.shared.model.ExtraLine;
import com.sportrent.shared.model.ExtraService;
import com.sportrent.shared.model.PriceBreakdown;
import com.sportrent.shared.model.Rental;
import com.sportrent.shared.model.RentalStatus;
import com.sportrent.shared.model.Tariff;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

// spec example: 1000 x 7, LONG_TERM, both extras = 8000)

final class ReportTestData {

    static final Instant OLD = Instant.parse("2026-11-01T10:00:00Z");
    static final Instant NEW = Instant.parse("2026-11-10T09:30:00Z");

    private ReportTestData() {
    }

    // 1000 KZT/day, 7 days, LONG_TERM: 7000 - 700 + 1400 + 300 = 8000. Active
    static Rental longTermBicycle() {
        return new Rental(UUID.randomUUID(), UUID.randomUUID(),
                new Customer("Алексей", "+77001234567"),
                new EquipmentSnapshot("Велосипед №1", EquipmentType.BICYCLE, money("1000.00")),
                7, Tariff.LONG_TERM, List.of(ExtraService.INSURANCE, ExtraService.ACCESSORY_KIT),
                new PriceBreakdown(money("7000.00"), money("700.00"), money("6300.00"),
                        List.of(new ExtraLine(ExtraService.INSURANCE, money("1400.00")),
                                new ExtraLine(ExtraService.ACCESSORY_KIT, money("300.00"))),
                        money("8000.00")),
                RentalStatus.ACTIVE, OLD, null);
    }

    // 500 KZT/day, 3 days, STANDARD, no extras: total 1500. Completed
    static Rental standardBall() {
        return new Rental(UUID.randomUUID(), UUID.randomUUID(),
                new Customer("Мария", "87001112233"),
                new EquipmentSnapshot("Мяч футбольный", EquipmentType.BALL, money("500.00")),
                3, Tariff.STANDARD, List.of(),
                new PriceBreakdown(money("1500.00"), money("0.00"), money("1500.00"), List.of(), money("1500.00")),
                RentalStatus.ACTIVE, NEW, null).completedAt(Instant.parse("2026-11-12T12:00:00Z"));
    }

    static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
