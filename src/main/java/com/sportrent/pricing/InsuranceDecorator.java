package com.sportrent.pricing;

import com.sportrent.shared.model.ExtraLine;
import com.sportrent.shared.model.ExtraService;

import java.math.BigDecimal;

final class InsuranceDecorator extends ExtraPriceDecorator {
    private static final BigDecimal DAILY_COST = new BigDecimal("200.00");

    InsuranceDecorator(RentalPrice wrapped) {
        super(wrapped);
    }

    @Override
    protected ExtraLine extraLine(int days) {
        return new ExtraLine(ExtraService.INSURANCE, DAILY_COST.multiply(BigDecimal.valueOf(days)));
    }
}
