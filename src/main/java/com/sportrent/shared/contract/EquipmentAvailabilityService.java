package com.sportrent.shared.contract;

import java.util.UUID;

/**
 * ПРЕДЛОЖЕНИЕ: единственное место,
 * где вычисляется доступность предмета по активным арендам. Реализует Дамир,
 * использует Арыстанали при оформлении, чтобы не дублировать проверку.
 * Если предмета нет, бросает NotFoundException.
 */
public interface EquipmentAvailabilityService {
    boolean isAvailable(UUID equipmentId);
}
