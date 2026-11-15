package com.sportrent.shared.dto;

import com.sportrent.shared.model.ExtraService;
import com.sportrent.shared.model.Tariff;

import java.util.List;
import java.util.UUID;

/** POST /api/rentals. Цену клиент не присылает: её считает сервер. */
public record RentalRequest(
        UUID equipmentId,
        CustomerDto customer,
        Integer days,
        Tariff tariff,
        List<ExtraService> extras) {
}
