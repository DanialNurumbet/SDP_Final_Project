package com.sportrent.storage;

import com.sportrent.catalog.EquipmentFactory;
import com.sportrent.shared.contract.EquipmentRepository;
import com.sportrent.shared.model.Equipment;
import com.sportrent.shared.model.PriceBreakdown;
import com.sportrent.shared.model.Rental;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JsonStorageAdapter implements EquipmentRepository {
    private final JsonFileStorage file;
    private final EquipmentFactory factory;
    private StorageData state;

    public JsonStorageAdapter(JsonFileStorage file, EquipmentFactory factory) {
        this.file = file;
        this.factory = factory;
        boolean missing = file.isMissing();
        try {
            StorageData loaded = missing ? DemoEquipment.initialData() : file.read();
            validate(loaded);
            state = new StorageData(loaded.equipment(), loaded.rentals().stream().map(this::copyRental).toList());
            if (missing) file.write(state);
        } catch (StorageException error) {
            throw error;
        } catch (RuntimeException error) {
            throw new StorageException("Хранилище содержит некорректные данные. Исправьте файл; начальные данные не записаны.", error);
        }
    }

    @Override
    public synchronized List<Equipment> findAll() {
        return state.equipment().stream().map(item -> item.toEquipment(factory)).toList();
    }

    @Override
    public synchronized Optional<Equipment> findById(UUID id) {
        return state.equipment().stream().filter(item -> item.id().equals(id))
                .findFirst().map(item -> item.toEquipment(factory));
    }

    @Override
    public synchronized Equipment save(Equipment equipment) {
        Equipment valid = factory.create(equipment.getId(), equipment.getType(), equipment.getName(), equipment.getDailyRate());
        List<StoredEquipment> items = new ArrayList<>(state.equipment());
        items.removeIf(item -> item.id().equals(valid.getId()));
        items.add(StoredEquipment.from(valid));
        persist(new StorageData(items, state.rentals()));
        return valid;
    }

    @Override
    public synchronized void deleteById(UUID id) {
        List<StoredEquipment> items = new ArrayList<>(state.equipment());
        items.removeIf(item -> item.id().equals(id));
        persist(new StorageData(items, state.rentals()));
    }

    synchronized List<Rental> findAllRentals() { return state.rentals(); }

    synchronized Optional<Rental> findRentalById(UUID id) {
        return state.rentals().stream().filter(rental -> rental.id().equals(id)).findFirst();
    }

    synchronized Rental saveRental(Rental rental) {
        Rental copy = copyRental(rental);
        List<Rental> rentals = new ArrayList<>(state.rentals());
        rentals.removeIf(item -> item.id().equals(copy.id()));
        rentals.add(copy);
        persist(new StorageData(state.equipment(), rentals));
        return copy;
    }

    private void persist(StorageData updated) {
        validate(updated);
        file.write(updated);
        // Состояние в памяти меняется только после успешной записи обоих списков.
        state = updated;
    }

    private void validate(StorageData data) {
        HashSet<UUID> equipmentIds = new HashSet<>();
        for (StoredEquipment item : data.equipment()) {
            item.toEquipment(factory);
            if (!equipmentIds.add(item.id())) throw new IllegalArgumentException("Duplicate equipment ID");
        }
        HashSet<UUID> rentalIds = new HashSet<>();
        for (Rental rental : data.rentals()) {
            if (rental.id() == null || rental.equipmentId() == null || rental.customer() == null
                    || rental.equipmentSnapshot() == null || rental.priceBreakdown() == null
                    || rental.status() == null || rental.createdAt() == null || rental.tariff() == null
                    || rental.days() < 1 || rental.days() > 30 || !rentalIds.add(rental.id())) {
                throw new IllegalArgumentException("Invalid rental record");
            }
            copyRental(rental);
        }
    }

    private Rental copyRental(Rental rental) {
        PriceBreakdown price = rental.priceBreakdown();
        PriceBreakdown copy = new PriceBreakdown(price.baseCost(), price.discount(), price.discountedCost(),
                List.copyOf(price.extras()), price.total());
        return new Rental(rental.id(), rental.equipmentId(), rental.customer(), rental.equipmentSnapshot(),
                rental.days(), rental.tariff(), List.copyOf(rental.extras()), copy,
                rental.status(), rental.createdAt(), rental.returnedAt());
    }
}
