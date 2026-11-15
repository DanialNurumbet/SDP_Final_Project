package com.sportrent.storage;

import com.sportrent.shared.model.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class RentalFixtures {
    private RentalFixtures() { }

    public static Rental active(Equipment equipment) {
        BigDecimal price = equipment.getDailyRate();
        return new Rental(UUID.randomUUID(), equipment.getId(), new Customer("Тестовый клиент", "+77001234567"),
                new EquipmentSnapshot(equipment.getName(), equipment.getType(), price), 1, Tariff.STANDARD,
                List.of(), new PriceBreakdown(price, new BigDecimal("0.00"), price, List.of(), price),
                RentalStatus.ACTIVE, Instant.parse("2026-11-15T10:00:00Z"), null);
    }
}
