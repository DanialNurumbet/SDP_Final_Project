package com.sportrent.pricing;

import java.math.BigDecimal;

final class LongTermPricingStrategy implements PricingStrategy {
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");

    @Override
    public BigDecimal discount(BigDecimal baseCost, int days) {
        return days >= 7 ? baseCost.multiply(DISCOUNT_RATE) : BigDecimal.ZERO;
    }
}
