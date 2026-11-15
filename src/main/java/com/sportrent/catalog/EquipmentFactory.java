package com.sportrent.catalog;

import com.sportrent.shared.error.ValidationException;
import com.sportrent.shared.model.Equipment;
import com.sportrent.shared.model.EquipmentType;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class EquipmentFactory {
    private final Map<EquipmentType, EquipmentCreator> creators = Map.of(
            EquipmentType.BICYCLE, new BicycleCreator(),
            EquipmentType.BALL, new BallCreator(),
            EquipmentType.RACKET, new RacketCreator());

    public Equipment create(UUID id, EquipmentType type, String name, BigDecimal dailyRate) {
        if (type == null) {
            throw new ValidationException("Выберите тип предмета.", Map.of("type", "Укажите тип."));
        }
        return creators.get(type).create(id, name, dailyRate);
    }
}

