package com.sportrent.rental;

import com.sportrent.shared.model.Rental;

import java.time.Instant;

public record RentalEvent(Type type, Rental rental, Instant occurredAt) {
    public enum Type { CREATED, RETURNED }
}
