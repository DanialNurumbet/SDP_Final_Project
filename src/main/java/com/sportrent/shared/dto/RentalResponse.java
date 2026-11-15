package com.sportrent.shared.dto;

import com.sportrent.shared.model.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RentalResponse(
        UUID id,
        UUID equipmentId,
        CustomerDto customer,
        EquipmentSnapshot equipmentSnapshot,
        int days,
        Tariff tariff,
        List<ExtraService> extras,
        PriceBreakdown priceBreakdown,
        RentalStatus status,
        Instant createdAt,
        Instant returnedAt) {

    public static RentalResponse from(Rental r) {
        return new RentalResponse(r.id(), r.equipmentId(), CustomerDto.from(r.customer()),
                r.equipmentSnapshot(), r.days(), r.tariff(), r.extras(), r.priceBreakdown(),
                r.status(), r.createdAt(), r.returnedAt());
    }
}
