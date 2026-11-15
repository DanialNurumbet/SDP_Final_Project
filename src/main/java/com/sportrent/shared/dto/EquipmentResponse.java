package com.sportrent.shared.dto;

import com.sportrent.shared.model.Equipment;
import com.sportrent.shared.model.EquipmentType;

import java.math.BigDecimal;
import java.util.UUID;

public record EquipmentResponse(
        UUID id,
        EquipmentType type,
        String name,
        BigDecimal dailyRate,
        boolean available,
        String usageHint) {

    public static EquipmentResponse from(Equipment e, boolean available) {
        return new EquipmentResponse(e.getId(), e.getType(), e.getName(), e.getDailyRate(),
                available, e.getUsageHint());
    }
}
