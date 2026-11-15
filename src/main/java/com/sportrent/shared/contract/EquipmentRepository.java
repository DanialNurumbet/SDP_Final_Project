package com.sportrent.shared.contract;

import com.sportrent.shared.model.Equipment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Реализует Дамир (JsonStorageAdapter). */
public interface EquipmentRepository {
    List<Equipment> findAll();

    Optional<Equipment> findById(UUID id);

    Equipment save(Equipment equipment);

    void deleteById(UUID id);
}
