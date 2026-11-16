package com.sportrent.rental;

import java.time.Instant;
import java.util.UUID;

public record LatestRentalNotification(String type, UUID rentalId, UUID equipmentId,
                                       String message, Instant occurredAt) {
    public static LatestRentalNotification from(RentalEvent event) {
        String message = event.type() == RentalEvent.Type.CREATED
                ? "Аренда оформлена."
                : "Инвентарь возвращён.";
        return new LatestRentalNotification(event.type().name(), event.rental().id(),
                event.rental().equipmentId(), message, event.occurredAt());
    }
}
