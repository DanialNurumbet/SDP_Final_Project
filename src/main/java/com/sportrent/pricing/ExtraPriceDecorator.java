package com.sportrent.pricing;

import com.sportrent.shared.model.ExtraLine;
import com.sportrent.shared.model.PriceBreakdown;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

abstract class ExtraPriceDecorator implements RentalPrice {
    private final RentalPrice wrapped;

    ExtraPriceDecorator(RentalPrice wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public PriceBreakdown calculate(BigDecimal dailyRate, int days) {
        PriceBreakdown price = wrapped.calculate(dailyRate, days);
        ExtraLine extra = extraLine(days);
        List<ExtraLine> extras = new ArrayList<>(price.extras());
        extras.add(extra);
        return new PriceBreakdown(price.baseCost(), price.discount(), price.discountedCost(),
                List.copyOf(extras), price.total().add(extra.cost()));
    }

    protected abstract ExtraLine extraLine(int days);
}
