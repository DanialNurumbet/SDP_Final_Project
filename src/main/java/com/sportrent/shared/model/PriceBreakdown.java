package com.sportrent.shared.model;

import com.sportrent.shared.contract.ReportElement;
import com.sportrent.shared.contract.ReportVisitor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Все суммы в KZT, scale 2, RoundingMode.HALF_UP.
 * discountedCost = baseCost - discount; total = discountedCost + сумма extras.
 */
public record PriceBreakdown(
        BigDecimal baseCost,
        BigDecimal discount,
        BigDecimal discountedCost,
        List<ExtraLine> extras,
        BigDecimal total) implements ReportElement {

    @Override
    public void accept(ReportVisitor visitor) {
        visitor.visit(this);
    }
}
