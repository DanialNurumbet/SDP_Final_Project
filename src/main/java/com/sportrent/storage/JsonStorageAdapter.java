package com.sportrent.storage;

import com.sportrent.catalog.EquipmentFactory;
import com.sportrent.shared.contract.EquipmentRepository;
import com.sportrent.shared.contract.EquipmentAvailabilityService;
import com.sportrent.shared.error.ConflictException;
import com.sportrent.shared.error.NotFoundException;
import com.sportrent.shared.error.ValidationException;
import com.sportrent.shared.model.Equipment;
import com.sportrent.shared.model.EquipmentSnapshot;
import com.sportrent.shared.model.PriceBreakdown;
import com.sportrent.shared.model.Rental;
import com.sportrent.shared.model.RentalStatus;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JsonStorageAdapter implements EquipmentRepository, EquipmentAvailabilityService {
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
        findById(valid.getId()).ifPresent(existing -> {
            requireAvailable(existing.getId());
            if (existing.getType() != valid.getType()) {
                throw new ValidationException("Тип существующего предмета менять нельзя.", java.util.Map.of("type", "Создайте новый предмет другого типа."));
            }
        });
        List<StoredEquipment> items = new ArrayList<>(state.equipment());
        items.removeIf(item -> item.id().equals(valid.getId()));
        items.add(StoredEquipment.from(valid));
        persist(new StorageData(items, state.rentals()));
        return valid;
    }

    @Override
    public synchronized void deleteById(UUID id) {
        requireAvailable(id);
        List<StoredEquipment> items = new ArrayList<>(state.equipment());
        items.removeIf(item -> item.id().equals(id));
        persist(new StorageData(items, state.rentals()));
    }

    synchronized List<Rental> findAllRentals() { return state.rentals(); }

    synchronized Optional<Rental> findRentalById(UUID id) {
        return state.rentals().stream().filter(rental -> rental.id().equals(id)).findFirst();
    }

    synchronized Rental saveRental(Rental rental) {
        validateRental(rental);
        Rental copy = copyRental(rental);
        Optional<Rental> existing = findRentalById(copy.id());
        if (existing.isEmpty()) {
            if (copy.status() != RentalStatus.ACTIVE) throw new ValidationException("Новая аренда должна быть активной.");
            Equipment equipment = requireEquipment(copy.equipmentId());
            if (!isAvailable(equipment.getId())) {
                throw new ConflictException("EQUIPMENT_UNAVAILABLE", "Этот предмет уже находится в аренде.");
            }
            EquipmentSnapshot current = new EquipmentSnapshot(equipment.getName(), equipment.getType(), equipment.getDailyRate());
            if (!current.equals(copy.equipmentSnapshot())) {
                throw new ConflictException("EQUIPMENT_CHANGED", "Предмет изменился. Обновите расчёт и повторите оформление.");
            }
        } else {
            Rental original = existing.orElseThrow();
            if (original.status() == RentalStatus.COMPLETED) {
                throw new ConflictException("RENTAL_ALREADY_COMPLETED", "Эта аренда уже завершена.");
            }
            if (copy.status() != RentalStatus.COMPLETED || !original.completedAt(copy.returnedAt()).equals(copy)) {
                throw new ValidationException("Сохранённые условия аренды изменять нельзя; доступен только возврат.");
            }
        }
        List<Rental> rentals = new ArrayList<>(state.rentals());
        rentals.removeIf(item -> item.id().equals(copy.id()));
        rentals.add(copy);
        persist(new StorageData(state.equipment(), rentals));
        return copy;
    }

    @Override
    public synchronized boolean isAvailable(UUID equipmentId) {
        requireEquipment(equipmentId);
        return state.rentals().stream().noneMatch(rental -> rental.equipmentId().equals(equipmentId)
                && rental.status() == RentalStatus.ACTIVE);
    }

    private Equipment requireEquipment(UUID id) {
        return findById(id).orElseThrow(() -> new NotFoundException("EQUIPMENT_NOT_FOUND", "Предмет не найден."));
    }

    private void requireAvailable(UUID id) {
        if (!isAvailable(id)) throw new ConflictException("EQUIPMENT_IN_USE", "Нельзя изменить или удалить предмет с активной арендой.");
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
        HashSet<UUID> occupiedIds = new HashSet<>();
        for (Rental rental : data.rentals()) {
            validateRental(rental);
            if (!rentalIds.add(rental.id())) throw new IllegalArgumentException("Duplicate rental ID");
            if (rental.status() == RentalStatus.ACTIVE
                    && (!equipmentIds.contains(rental.equipmentId()) || !occupiedIds.add(rental.equipmentId()))) {
                throw new IllegalArgumentException("Missing or multiply rented equipment");
            }
            copyRental(rental);
        }
    }

    private void validateRental(Rental rental) {
        if (rental == null || rental.id() == null || rental.equipmentId() == null || rental.createdAt() == null
                || rental.status() == null || rental.tariff() == null || rental.customer() == null
                || rental.customer().name() == null || rental.customer().phone() == null
                || rental.days() < 1 || rental.days() > 30
                || rental.equipmentSnapshot() == null || rental.priceBreakdown() == null
                || rental.extras() == null || rental.extras().stream().anyMatch(java.util.Objects::isNull)
                || rental.priceBreakdown().extras() == null) {
            throw new ValidationException("Некорректная запись аренды.");
        }
        if (rental.status() == RentalStatus.ACTIVE && rental.returnedAt() != null
                || rental.status() == RentalStatus.COMPLETED && (rental.returnedAt() == null
                || rental.returnedAt().isBefore(rental.createdAt()))) {
            throw new ValidationException("Дата возврата не соответствует статусу аренды.");
        }
        EquipmentSnapshot snapshot = rental.equipmentSnapshot();
        factory.create(rental.equipmentId(), snapshot.type(), snapshot.name(), snapshot.dailyRate());
        PriceBreakdown price = rental.priceBreakdown();
        for (java.math.BigDecimal amount : new java.math.BigDecimal[]{price.baseCost(), price.discount(), price.discountedCost(), price.total()}) {
            if (amount == null || amount.signum() < 0) throw new ValidationException("Некорректная сумма в сохранённой аренде.");
        }
        if (new HashSet<>(rental.extras()).size() != rental.extras().size()) {
            throw new ValidationException("Услуги в сохранённой аренде повторяются.");
        }
        for (var extra : price.extras()) {
            if (extra == null || extra.extra() == null || extra.cost() == null || extra.cost().signum() < 0) {
                throw new ValidationException("Некорректная стоимость услуги.");
            }
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
