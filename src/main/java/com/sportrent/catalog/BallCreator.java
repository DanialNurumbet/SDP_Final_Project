package com.sportrent.catalog;

import com.sportrent.shared.model.Equipment;
import java.math.BigDecimal;
import java.util.UUID;

public class BallCreator extends EquipmentCreator {
    @Override
    protected Equipment createEquipment(UUID id, String name, BigDecimal dailyRate) {
        return new Ball(id, name, dailyRate);
    }
}

