package com.sportrent.storage;

import com.sportrent.shared.contract.RentalRepository;
import com.sportrent.shared.model.Rental;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Второй интерфейс адаптера: его findAll/findById конфликтуют с методами EquipmentRepository.
@Repository
public class JsonRentalRepository implements RentalRepository {
    private final JsonStorageAdapter storage;

    public JsonRentalRepository(JsonStorageAdapter storage) { this.storage = storage; }

    @Override
    public List<Rental> findAll() { return storage.findAllRentals(); }

    @Override
    public Optional<Rental> findById(UUID id) { return storage.findRentalById(id); }

    @Override
    public Rental save(Rental rental) { return storage.saveRental(rental); }
}
