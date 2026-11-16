package com.sportrent.pricing;

import java.math.BigDecimal;

final class StandardPricingStrategy implements PricingStrategy {
    @Override
    public BigDecimal discount(BigDecimal baseCost, int days) {
        return BigDecimal.ZERO;
    }
}
