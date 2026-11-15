package com.sportrent.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sportrent.catalog.EquipmentFactory;
import com.sportrent.shared.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class JsonStorageAdapterTest {
    @TempDir Path directory;
    private final EquipmentFactory factory = new EquipmentFactory();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private JsonStorageAdapter open() {
        return new JsonStorageAdapter(new JsonFileStorage(directory.resolve("sportrent.json"), mapper), factory);
    }

    @Test
    void seedsNineItemsOnlyOnceAndReloadsBothRepositories() throws Exception {
        JsonStorageAdapter first = open();
        assertEquals(9, first.findAll().size());
        for (EquipmentType type : EquipmentType.values()) {
            assertEquals(3, first.findAll().stream().filter(item -> item.getType() == type).count());
        }
        Equipment custom = factory.create(UUID.randomUUID(), EquipmentType.RACKET, "Ракетка клиента", new BigDecimal("123.45"));
        first.save(custom);
        Rental rental = new JsonRentalRepository(first).save(RentalFixtures.active(custom));
        JsonStorageAdapter restored = open();
        assertEquals(10, restored.findAll().size());
        assertEquals(custom.getName(), restored.findById(custom.getId()).orElseThrow().getName());
        assertEquals(custom.getDailyRate(), restored.findById(custom.getId()).orElseThrow().getDailyRate());
        assertEquals(rental, new JsonRentalRepository(restored).findById(rental.id()).orElseThrow());
        String json = Files.readString(directory.resolve("sportrent.json"));
        assertTrue(json.contains("RACKET"));
        assertFalse(json.contains("com.sportrent"));
        assertFalse(json.contains("available"));
        assertFalse(json.contains("usageHint"));
    }

    @Test
    void keepsCompletedSnapshotAfterCatalogChangeAndDeletion() {
        JsonStorageAdapter storage = open();
        Equipment equipment = storage.findAll().getFirst();
        JsonRentalRepository rentals = new JsonRentalRepository(storage);
        Rental rental = rentals.save(RentalFixtures.active(equipment));
        Rental completed = rentals.save(rental.completedAt(Instant.parse("2026-11-16T10:00:00Z")));
        storage.save(factory.create(equipment.getId(), equipment.getType(), "Новое название", BigDecimal.TEN));
        storage.deleteById(equipment.getId());
        JsonStorageAdapter restored = open();
        assertTrue(restored.findById(equipment.getId()).isEmpty());
        assertEquals(completed, new JsonRentalRepository(restored).findById(rental.id()).orElseThrow());
        assertEquals(equipment.getName(), completed.equipmentSnapshot().name());
    }

    @Test
    void doesNotReseedIntentionallyEmptyCatalog() {
        JsonStorageAdapter storage = open();
        storage.findAll().forEach(item -> storage.deleteById(item.getId()));
        assertTrue(open().findAll().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "{broken", "null", "{}", "{\"equipment\":[],\"rentals\":null}", "{\"equipment\":[],\"rentals\":[]} trailing"})
    void refusesCorruptStorageWithoutOverwriting(String content) throws Exception {
        Path path = directory.resolve("sportrent.json");
        Files.writeString(path, content);
        assertThrows(StorageException.class, this::open);
        assertEquals(content, Files.readString(path));
    }

    @Test
    void failedWriteLeavesMemoryAndDiskUnchanged() throws Exception {
        JsonStorageAdapter initial = open();
        String previous = Files.readString(directory.resolve("sportrent.json"));
        JsonFileStorage failing = new JsonFileStorage(directory.resolve("sportrent.json"), mapper) {
            @Override void write(StorageData data) { throw new StorageException("Диск недоступен", null); }
        };
        JsonStorageAdapter storage = new JsonStorageAdapter(failing, factory);
        Equipment item = factory.create(UUID.randomUUID(), EquipmentType.BALL, "Новый мяч", BigDecimal.TEN);
        assertThrows(StorageException.class, () -> storage.save(item));
        assertTrue(storage.findById(item.getId()).isEmpty());
        assertEquals(initial.findAll().size(), storage.findAll().size());
        assertEquals(previous, Files.readString(directory.resolve("sportrent.json")));
    }

    @Test
    void fileReplacementFailureKeepsTargetAndCleansTemporaryFile() throws Exception {
        Path target = directory.resolve("occupied");
        Files.createDirectories(target);
        Files.writeString(target.resolve("keep.txt"), "keep");
        JsonFileStorage storage = new JsonFileStorage(target, mapper);
        assertThrows(StorageException.class, () -> storage.write(DemoEquipment.initialData()));
        assertEquals("keep", Files.readString(target.resolve("keep.txt")));
        try (var files = Files.list(directory)) {
            assertFalse(files.anyMatch(path -> path.toString().endsWith(".tmp")));
        }
    }
}
