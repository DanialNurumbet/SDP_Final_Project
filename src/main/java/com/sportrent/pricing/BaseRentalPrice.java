package com.sportrent.pricing;

import com.sportrent.shared.model.PriceBreakdown;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

record BaseRentalPrice(PricingStrategy strategy) implements RentalPrice {
    @Override
    public PriceBreakdown calculate(BigDecimal dailyRate, int days) {
        BigDecimal baseCost = dailyRate.multiply(BigDecimal.valueOf(days)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = strategy.discount(baseCost, days).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountedCost = baseCost.subtract(discount);
        return new PriceBreakdown(baseCost, discount, discountedCost, List.of(), discountedCost);
    }
}
