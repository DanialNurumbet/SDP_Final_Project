package com.sportrent.catalog;

import com.sportrent.shared.model.Equipment;
import com.sportrent.shared.model.EquipmentType;
import java.math.BigDecimal;
import java.util.UUID;

public final class Ball extends Equipment {
    Ball(UUID id, String name, BigDecimal dailyRate) {
        super(id, name, dailyRate);
    }

    @Override
    public EquipmentType getType() { return EquipmentType.BALL; }

    @Override
    public String getUsageHint() { return "Используйте мяч на подходящем покрытии и проверьте давление."; }
}

