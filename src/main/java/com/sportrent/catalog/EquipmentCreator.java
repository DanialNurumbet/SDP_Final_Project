package com.sportrent.catalog;

import com.sportrent.shared.error.ValidationException;
import com.sportrent.shared.model.Equipment;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public abstract class EquipmentCreator {
    public final Equipment create(UUID id, String name, BigDecimal dailyRate) {
        Map<String, String> errors = new LinkedHashMap<>();
        String trimmedName = name == null ? "" : name.strip();
        if (trimmedName.length() < 2 || trimmedName.length() > 100) {
            errors.put("name", "Название должно содержать от 2 до 100 символов.");
        }
        if (dailyRate == null || dailyRate.compareTo(new BigDecimal("0.01")) < 0
                || dailyRate.compareTo(new BigDecimal("1000000.00")) > 0) {
            errors.put("dailyRate", "Цена должна быть от 0.01 до 1 000 000.00 KZT.");
        }
        if (!errors.isEmpty()) throw new ValidationException("Проверьте поля предмета.", errors);
        if (id == null) throw new ValidationException("Не указан идентификатор предмета.");
        return createEquipment(id, trimmedName, dailyRate.setScale(2, RoundingMode.HALF_UP));
    }

    // Factory Method: конкретный создатель выбирает класс продукта.
    protected abstract Equipment createEquipment(UUID id, String name, BigDecimal dailyRate);
}

