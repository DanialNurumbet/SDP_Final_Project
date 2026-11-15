package com.sportrent.catalog;

import com.sportrent.shared.contract.EquipmentAvailabilityService;
import com.sportrent.shared.contract.EquipmentRepository;
import com.sportrent.shared.dto.EquipmentRequest;
import com.sportrent.shared.dto.EquipmentResponse;
import com.sportrent.shared.error.ConflictException;
import com.sportrent.shared.error.NotFoundException;
import com.sportrent.shared.error.ValidationException;
import com.sportrent.shared.model.Equipment;
import com.sportrent.shared.model.EquipmentType;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class EquipmentService {
    private final EquipmentRepository equipment;
    private final EquipmentAvailabilityService availability;
    private final EquipmentFactory factory;

    public EquipmentService(EquipmentRepository equipment, EquipmentAvailabilityService availability, EquipmentFactory factory) {
        this.equipment = equipment;
        this.availability = availability;
        this.factory = factory;
    }

    public List<EquipmentResponse> findAll(EquipmentType type, Boolean available) {
        return equipment.findAll().stream()
                .filter(item -> type == null || item.getType() == type)
                .map(this::response)
                .filter(item -> available == null || item.available() == available)
                .toList();
    }

    public EquipmentResponse findById(UUID id) { return response(requireEquipment(id)); }

    public EquipmentResponse create(EquipmentRequest request) {
        Equipment item = factory.create(UUID.randomUUID(), request.type(), request.name(), request.dailyRate());
        return response(equipment.save(item));
    }

    public EquipmentResponse update(UUID id, EquipmentRequest request) {
        Equipment existing = requireEquipment(id);
        requireAvailable(id);
        if (request.type() != existing.getType()) {
            throw new ValidationException("Тип существующего предмета менять нельзя.", Map.of("type", "Создайте новый предмет другого типа."));
        }
        Equipment updated = factory.create(id, request.type(), request.name(), request.dailyRate());
        return response(equipment.save(updated));
    }

    public void delete(UUID id) {
        requireEquipment(id);
        requireAvailable(id);
        equipment.deleteById(id);
    }

    private Equipment requireEquipment(UUID id) {
        return equipment.findById(id).orElseThrow(() -> new NotFoundException("EQUIPMENT_NOT_FOUND", "Предмет не найден."));
    }

    private void requireAvailable(UUID id) {
        if (!availability.isAvailable(id)) {
            throw new ConflictException("EQUIPMENT_IN_USE", "Нельзя изменить или удалить предмет с активной арендой.");
        }
    }

    private EquipmentResponse response(Equipment item) {
        return EquipmentResponse.from(item, availability.isAvailable(item.getId()));
    }
}
