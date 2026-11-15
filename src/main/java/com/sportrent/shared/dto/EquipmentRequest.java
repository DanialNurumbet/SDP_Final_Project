package com.sportrent.shared.dto;

import com.sportrent.shared.model.EquipmentType;

import java.math.BigDecimal;

/** POST/PUT /api/equipment. Тип при PUT менять нельзя. */
public record EquipmentRequest(EquipmentType type, String name, BigDecimal dailyRate) {
}
