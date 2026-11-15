package com.sportrent.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sportrent.catalog.EquipmentFactory;
import com.sportrent.shared.error.ConflictException;
import com.sportrent.shared.error.NotFoundException;
import com.sportrent.shared.error.ValidationException;
import com.sportrent.shared.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class StorageConsistencyTest {
    @TempDir Path directory;
    private final EquipmentFactory factory = new EquipmentFactory();
    private JsonStorageAdapter storage;
    private JsonRentalRepository rentals;

    private JsonStorageAdapter open() {
        return new JsonStorageAdapter(new JsonFileStorage(directory.resolve("sportrent.json"),
                new ObjectMapper().registerModule(new JavaTimeModule())), factory);
    }

    @BeforeEach void setup() { storage = open(); rentals = new JsonRentalRepository(storage); }

    @Test
    void activeRentalPreventsModificationDeletionAndSecondRentalUntilReturn() {
        Equipment item = storage.findAll().getFirst();
        assertTrue(storage.isAvailable(item.getId()));
        Rental active = rentals.save(RentalFixtures.active(item));
        assertFalse(storage.isAvailable(item.getId()));
        assertThrows(ConflictException.class, () -> storage.deleteById(item.getId()));
        assertThrows(ConflictException.class, () -> storage.save(factory.create(item.getId(), item.getType(), "Новое имя", BigDecimal.TEN)));
        assertThrows(ConflictException.class, () -> rentals.save(RentalFixtures.active(item)));
        Rental completed = rentals.save(active.completedAt(Instant.parse("2026-11-16T10:00:00Z")));
        assertTrue(storage.isAvailable(item.getId()));
        assertThrows(ConflictException.class, () -> rentals.save(completed));
        storage.deleteById(item.getId());
        assertEquals(completed, new JsonRentalRepository(open()).findById(active.id()).orElseThrow());
        assertThrows(NotFoundException.class, () -> storage.isAvailable(item.getId()));
    }

    @Test
    void doesNotChangeTypeOrAcceptAnOutdatedSnapshot() {
        Equipment item = storage.findAll().getFirst();
        assertThrows(ValidationException.class, () -> storage.save(factory.create(item.getId(), EquipmentType.RACKET, "Ракетка", BigDecimal.TEN)));
        Rental stale = RentalFixtures.active(item);
        storage.save(factory.create(item.getId(), item.getType(), "Изменённое имя", BigDecimal.TEN));
        assertThrows(ConflictException.class, () -> rentals.save(stale));
        assertTrue(rentals.findAll().isEmpty());
    }

    @Test
    void simultaneousRentalSavesAllowOnlyOneActiveRecord() throws Exception {
        Equipment item = storage.findAll().getFirst();
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var attempts = new ArrayList<Future<Boolean>>();
            for (int i = 0; i < 2; i++) {
                attempts.add(executor.submit(() -> {
                    start.await();
                    try { rentals.save(RentalFixtures.active(item)); return true; }
                    catch (ConflictException expected) { return false; }
                }));
            }
            start.countDown();
            int succeeded = 0;
            for (var result : attempts) if (result.get(5, TimeUnit.SECONDS)) succeeded++;
            assertEquals(1, succeeded);
        }
        assertEquals(1, new JsonRentalRepository(open()).findAll().size());
    }

    @Test
    void concurrentCatalogWritesKeepAllItemsAndRentalData() throws Exception {
        rentals.save(RentalFixtures.active(storage.findAll().getFirst()));
        try (var executor = Executors.newFixedThreadPool(4)) {
            var results = new ArrayList<Future<?>>();
            for (int i = 0; i < 12; i++) {
                results.add(executor.submit(() -> storage.save(factory.create(UUID.randomUUID(), EquipmentType.BALL, "Новый мяч", BigDecimal.TEN))));
            }
            for (var result : results) result.get(5, TimeUnit.SECONDS);
        }
        JsonStorageAdapter restored = open();
        assertEquals(21, restored.findAll().size());
        assertEquals(1, new JsonRentalRepository(restored).findAll().size());
    }
}
