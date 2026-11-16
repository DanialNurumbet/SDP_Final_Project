package com.sportrent.pricing;

import java.math.BigDecimal;

interface PricingStrategy {
    BigDecimal discount(BigDecimal baseCost, int days);
}
