package com.sportrent.shared.dto;

import com.sportrent.shared.model.ExtraService;
import com.sportrent.shared.model.Tariff;

import java.util.List;
import java.util.UUID;

/** POST /api/pricing/quote. days: Integer, чтобы отсутствие поля давало null */
public record QuoteRequest(UUID equipmentId, Integer days, Tariff tariff, List<ExtraService> extras) {
}
