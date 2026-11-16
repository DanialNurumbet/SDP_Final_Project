package com.sportrent.pricing;

import com.sportrent.shared.model.PriceBreakdown;

import java.math.BigDecimal;

interface RentalPrice {
    PriceBreakdown calculate(BigDecimal dailyRate, int days);
}
