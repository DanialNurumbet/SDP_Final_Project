package com.sportrent.rental;

import com.sportrent.shared.model.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Explicit Builder for immutable rental records. */
public final class RentalBuilder {
    private UUID id;
    private UUID equipmentId;
    private Customer customer;
    private EquipmentSnapshot equipmentSnapshot;
    private Integer days;
    private Tariff tariff;
    private List<ExtraService> extras;
    private PriceBreakdown priceBreakdown;
    private RentalStatus status;
    private Instant createdAt;
    private Instant returnedAt;

    public RentalBuilder id(UUID id) { this.id = id; return this; }
    public RentalBuilder equipmentId(UUID equipmentId) { this.equipmentId = equipmentId; return this; }
    public RentalBuilder customer(Customer customer) { this.customer = customer; return this; }
    public RentalBuilder equipmentSnapshot(EquipmentSnapshot snapshot) { this.equipmentSnapshot = snapshot; return this; }
    public RentalBuilder days(int days) { this.days = days; return this; }
    public RentalBuilder tariff(Tariff tariff) { this.tariff = tariff; return this; }
    public RentalBuilder extras(List<ExtraService> extras) { this.extras = extras; return this; }
    public RentalBuilder priceBreakdown(PriceBreakdown priceBreakdown) { this.priceBreakdown = priceBreakdown; return this; }
    public RentalBuilder status(RentalStatus status) { this.status = status; return this; }
    public RentalBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
    public RentalBuilder returnedAt(Instant returnedAt) { this.returnedAt = returnedAt; return this; }

    public Rental build() {
        if (id == null || equipmentId == null || customer == null || equipmentSnapshot == null
                || days == null || tariff == null || extras == null || priceBreakdown == null
                || status == null || createdAt == null) {
            throw new IllegalStateException("Для аренды не заполнены обязательные поля.");
        }
        if (days < 1 || days > 30 || customer.name() == null || customer.phone() == null) {
            throw new IllegalStateException("Некорректные данные аренды.");
        }
        if (status == RentalStatus.ACTIVE && returnedAt != null
                || status == RentalStatus.COMPLETED && returnedAt == null) {
            throw new IllegalStateException("Дата возврата не соответствует статусу аренды.");
        }
        return new Rental(id, equipmentId, customer, equipmentSnapshot, days, tariff,
                List.copyOf(extras), priceBreakdown, status, createdAt, returnedAt);
    }
}
