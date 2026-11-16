package com.sportrent.rental;

import com.sportrent.shared.contract.EquipmentAvailabilityService;
import com.sportrent.shared.contract.EquipmentRepository;
import com.sportrent.shared.contract.PricingService;
import com.sportrent.shared.contract.RentalRepository;
import com.sportrent.shared.contract.RentalService;
import com.sportrent.shared.dto.CustomerDto;
import com.sportrent.shared.dto.RentalRequest;
import com.sportrent.shared.error.ConflictException;
import com.sportrent.shared.error.NotFoundException;
import com.sportrent.shared.error.ValidationException;
import com.sportrent.shared.model.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnBean(PricingService.class)
public class RentalCommandService implements RentalService {
    private final EquipmentRepository equipmentRepository;
    private final EquipmentAvailabilityService availability;
    private final RentalRepository rentalRepository;
    private final PricingService pricing;
    private final RentalEventPublisher events;

    public RentalCommandService(EquipmentRepository equipmentRepository,
                                EquipmentAvailabilityService availability,
                                RentalRepository rentalRepository,
                                PricingService pricing,
                                RentalEventPublisher events) {
        this.equipmentRepository = equipmentRepository;
        this.availability = availability;
        this.rentalRepository = rentalRepository;
        this.pricing = pricing;
        this.events = events;
    }

    @Override
    public Rental createRental(RentalRequest request) {
        validateRequest(request);
        Equipment equipment = equipmentRepository.findById(request.equipmentId())
                .orElseThrow(() -> new NotFoundException("EQUIPMENT_NOT_FOUND", "Предмет не найден."));
        if (!availability.isAvailable(equipment.getId())) {
            throw new ConflictException("EQUIPMENT_UNAVAILABLE", "Этот предмет уже находится в аренде.");
        }

        List<ExtraService> extras = List.copyOf(request.extras());
        RentalTerms terms = new RentalTerms(request.days(), request.tariff(), extras);
        PriceBreakdown price = pricing.calculate(equipment, terms);
        Rental rental = new RentalBuilder()
                .id(UUID.randomUUID())
                .equipmentId(equipment.getId())
                .customer(toCustomer(request.customer()))
                .equipmentSnapshot(new EquipmentSnapshot(equipment.getName(), equipment.getType(), equipment.getDailyRate()))
                .days(request.days())
                .tariff(request.tariff())
                .extras(extras)
                .priceBreakdown(price)
                .status(RentalStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        Rental saved = rentalRepository.save(rental);
        events.publish(new RentalEvent(RentalEvent.Type.CREATED, saved, Instant.now()));
        return saved;
    }

    @Override
    public Rental returnRental(UUID rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new NotFoundException("RENTAL_NOT_FOUND", "Аренда не найдена."));
        if (rental.status() == RentalStatus.COMPLETED) {
            throw new ConflictException("RENTAL_ALREADY_COMPLETED", "Эта аренда уже завершена.");
        }
        Rental returned = rentalRepository.save(rental.completedAt(Instant.now()));
        events.publish(new RentalEvent(RentalEvent.Type.RETURNED, returned, Instant.now()));
        return returned;
    }

    private void validateRequest(RentalRequest request) {
        if (request == null || request.equipmentId() == null || request.customer() == null
                || request.days() == null || request.tariff() == null || request.extras() == null) {
            throw new ValidationException("Заполните все обязательные поля аренды.");
        }
        if (request.days() < 1 || request.days() > 30) {
            throw new ValidationException("Срок аренды должен быть от 1 до 30 дней.", java.util.Map.of("days", "Допустимо 1–30"));
        }
        if (new HashSet<>(request.extras()).size() != request.extras().size()
                || request.extras().stream().anyMatch(java.util.Objects::isNull)) {
            throw new ValidationException("Дополнительные услуги нельзя выбирать повторно.");
        }
        validateCustomer(request.customer());
    }

    private void validateCustomer(CustomerDto customer) {
        String name = customer.name() == null ? "" : customer.name().trim();
        if (name.length() < 2 || name.length() > 100) {
            throw new ValidationException("Имя клиента должно содержать от 2 до 100 символов.",
                    java.util.Map.of("customer.name", "Допустимо 2–100 символов"));
        }
        if (customer.phone() == null
                || !customer.phone().replaceAll("[\\s()\\-]", "").matches("\\+?\\d{10,15}")) {
            throw new ValidationException("Телефон должен содержать от 10 до 15 цифр.",
                    java.util.Map.of("customer.phone", "Некорректный телефон"));
        }
    }

    private Customer toCustomer(CustomerDto dto) {
        String phone = dto.phone().replaceAll("[\\s()\\-]", "");
        return new Customer(dto.name().trim(), phone);
    }
}
