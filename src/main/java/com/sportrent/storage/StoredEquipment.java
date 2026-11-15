package com.sportrent.storage;

import com.sportrent.catalog.EquipmentFactory;
import com.sportrent.shared.model.Equipment;
import com.sportrent.shared.model.EquipmentType;
import java.math.BigDecimal;
import java.util.UUID;

// Формат файла: явный type, без имён Java-классов и вычисляемых полей.
record StoredEquipment(UUID id, EquipmentType type, String name, BigDecimal dailyRate) {
    static StoredEquipment from(Equipment equipment) {
        return new StoredEquipment(equipment.getId(), equipment.getType(), equipment.getName(), equipment.getDailyRate());
    }

    Equipment toEquipment(EquipmentFactory factory) {
        return factory.create(id, type, name, dailyRate);
    }
}
