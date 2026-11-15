package com.sportrent.shared.model;

import com.sportrent.shared.contract.ReportElement;
import com.sportrent.shared.contract.ReportVisitor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Неизменяемая запись аренды. Возврат создаёт новую запись через {@link #completedAt(Instant)}. */
public record Rental(
        UUID id,
        UUID equipmentId,
        Customer customer,
        EquipmentSnapshot equipmentSnapshot,
        int days,
        Tariff tariff,
        List<ExtraService> extras,
        PriceBreakdown priceBreakdown,
        RentalStatus status,
        Instant createdAt,
        Instant returnedAt) implements ReportElement {

    public Rental completedAt(Instant returnTime) {
        return new Rental(id, equipmentId, customer, equipmentSnapshot, days, tariff, extras,
                priceBreakdown, RentalStatus.COMPLETED, createdAt, returnTime);
    }

    /** Посещает саму аренду, затем снимок предмета и разбивку цены. */
    @Override
    public void accept(ReportVisitor visitor) {
        visitor.visit(this);
        equipmentSnapshot.accept(visitor);
        priceBreakdown.accept(visitor);
    }
}
