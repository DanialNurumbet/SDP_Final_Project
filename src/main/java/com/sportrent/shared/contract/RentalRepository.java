package com.sportrent.shared.contract;

import com.sportrent.shared.model.Rental;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Реализует Дамир (JsonStorageAdapter). */
public interface RentalRepository {
    List<Rental> findAll();

    Optional<Rental> findById(UUID id);

    /** Создаёт новую запись или обновляет существующую по id. */
    Rental save(Rental rental);
}
