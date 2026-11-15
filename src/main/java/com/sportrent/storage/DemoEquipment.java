package com.sportrent.storage;

import com.sportrent.shared.model.EquipmentType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class DemoEquipment {
    private DemoEquipment() { }

    static StorageData initialData() {
        List<StoredEquipment> items = new ArrayList<>();
        add(items, EquipmentType.BICYCLE, "Велосипед", "1000.00");
        add(items, EquipmentType.BALL, "Мяч", "500.00");
        add(items, EquipmentType.RACKET, "Ракетка", "700.00");
        return new StorageData(items, List.of());
    }

    private static void add(List<StoredEquipment> items, EquipmentType type, String name, String rate) {
        for (int number = 1; number <= 3; number++) {
            items.add(new StoredEquipment(UUID.randomUUID(), type, name + " №" + number, new BigDecimal(rate)));
        }
    }
}
