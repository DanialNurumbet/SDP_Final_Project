package com.sportrent.pricing;

import com.sportrent.shared.model.ExtraLine;
import com.sportrent.shared.model.ExtraService;

import java.math.BigDecimal;

final class AccessoryKitDecorator extends ExtraPriceDecorator {
    private static final BigDecimal COST = new BigDecimal("300.00");

    AccessoryKitDecorator(RentalPrice wrapped) {
        super(wrapped);
    }

    @Override
    protected ExtraLine extraLine(int days) {
        return new ExtraLine(ExtraService.ACCESSORY_KIT, COST);
    }
}
