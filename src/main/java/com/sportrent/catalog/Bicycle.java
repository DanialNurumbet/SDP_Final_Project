package com.sportrent.catalog;

import com.sportrent.shared.model.Equipment;
import com.sportrent.shared.model.EquipmentType;
import java.math.BigDecimal;
import java.util.UUID;

public final class Bicycle extends Equipment {
    Bicycle(UUID id, String name, BigDecimal dailyRate) {
        super(id, name, dailyRate);
    }

    @Override
    public EquipmentType getType() { return EquipmentType.BICYCLE; }

    @Override
    public String getUsageHint() { return "Перед поездкой проверьте тормоза и высоту седла."; }
}

