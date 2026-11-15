package com.sportrent.shared.model;

import com.sportrent.shared.contract.ReportElement;
import com.sportrent.shared.contract.ReportVisitor;

import java.math.BigDecimal;

/** Данные предмета на момент оформления. Изменение каталога их не меняет. */
public record EquipmentSnapshot(String name, EquipmentType type, BigDecimal dailyRate) implements ReportElement {

    @Override
    public void accept(ReportVisitor visitor) {
        visitor.visit(this);
    }
}
