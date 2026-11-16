package com.sportrent.rental;

import com.sportrent.catalog.EquipmentFactory;
import com.sportrent.shared.contract.*;
import com.sportrent.shared.dto.CustomerDto;
import com.sportrent.shared.dto.RentalRequest;
import com.sportrent.shared.error.ConflictException;
import com.sportrent.shared.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RentalCommandServiceTest {
    private final Equipment equipment = new EquipmentFactory().create(UUID.randomUUID(), EquipmentType.BICYCLE,
            "Велосипед", new BigDecimal("1000.00"));

    @Test
    void createsRentalWithSnapshotAndPublishesEvent() {
        TestContext context = new TestContext();
        Rental created = context.service.createRental(request());

        assertEquals(RentalStatus.ACTIVE, created.status());
        assertEquals(equipment.getName(), created.equipmentSnapshot().name());
        assertEquals(RentalEvent.Type.CREATED.name(), context.notification.latest().map(LatestRentalNotification::type).orElseThrow());
    }

    @Test
    void rejectsSecondRentalForSameEquipment() {
        TestContext context = new TestContext();
        context.service.createRental(request());

        assertThrows(ConflictException.class, () -> context.service.createRental(request()));
    }

    @Test
    void returnsRentalAndPublishesReturnEvent() {
        TestContext context = new TestContext();
        Rental created = context.service.createRental(request());

        Rental returned = context.service.returnRental(created.id());

        assertEquals(RentalStatus.COMPLETED, returned.status());
        assertNotNull(returned.returnedAt());
        assertEquals(RentalEvent.Type.RETURNED.name(), context.notification.latest().map(LatestRentalNotification::type).orElseThrow());
    }

    private RentalRequest request() {
        return new RentalRequest(equipment.getId(), new CustomerDto("Алексей", "+77001234567"),
                7, Tariff.LONG_TERM, List.of(ExtraService.INSURANCE));
    }

    private final class TestContext {
        private final InMemoryEquipment equipmentRepository = new InMemoryEquipment();
        private final InMemoryRentals rentalRepository = new InMemoryRentals();
        private final RentalEventPublisher publisher = new RentalEventPublisher();
        private final LatestRentalNotificationStore notification = new LatestRentalNotificationStore(publisher);
        private final RentalCommandService service;

        private TestContext() {
            equipmentRepository.item = equipment;
            service = new RentalCommandService(equipmentRepository, equipmentRepository, rentalRepository,
                    (item, terms) -> new PriceBreakdown(new BigDecimal("7000.00"),
                            new BigDecimal("700.00"), new BigDecimal("6300.00"),
                            List.of(new ExtraLine(ExtraService.INSURANCE, new BigDecimal("1400.00"))),
                            new BigDecimal("7700.00")), publisher);
        }
    }

    private static final class InMemoryEquipment implements EquipmentRepository, EquipmentAvailabilityService {
        private Equipment item;
        private Rental rental;

        @Override public List<Equipment> findAll() { return List.of(item); }
        @Override public Optional<Equipment> findById(UUID id) { return item.getId().equals(id) ? Optional.of(item) : Optional.empty(); }
        @Override public Equipment save(Equipment equipment) { item = equipment; return equipment; }
        @Override public void deleteById(UUID id) { item = null; }
        @Override public boolean isAvailable(UUID id) { return rental == null || rental.status() == RentalStatus.COMPLETED; }
    }

    private static final class InMemoryRentals implements RentalRepository {
        private final Map<UUID, Rental> values = new HashMap<>();
        @Override public List<Rental> findAll() { return List.copyOf(values.values()); }
        @Override public Optional<Rental> findById(UUID id) { return Optional.ofNullable(values.get(id)); }
        @Override public Rental save(Rental rental) {
            if (rental.status() == RentalStatus.ACTIVE && values.values().stream()
                    .anyMatch(existing -> existing.equipmentId().equals(rental.equipmentId())
                            && existing.status() == RentalStatus.ACTIVE)) {
                throw new ConflictException("EQUIPMENT_UNAVAILABLE", "Предмет занят.");
            }
            values.put(rental.id(), rental);
            return rental;
        }
    }
}
