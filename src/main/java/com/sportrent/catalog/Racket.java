package com.sportrent.catalog;

import com.sportrent.shared.model.Equipment;
import com.sportrent.shared.model.EquipmentType;
import java.math.BigDecimal;
import java.util.UUID;

public final class Racket extends Equipment {
    Racket(UUID id, String name, BigDecimal dailyRate) {
        super(id, name, dailyRate);
    }

    @Override
    public EquipmentType getType() { return EquipmentType.RACKET; }

    @Override
    public String getUsageHint() { return "Проверьте натяжение струн и держите ракетку за рукоятку."; }
}

