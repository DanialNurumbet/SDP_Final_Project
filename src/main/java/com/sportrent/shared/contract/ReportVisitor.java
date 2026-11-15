package com.sportrent.shared.contract;

import com.sportrent.shared.model.EquipmentSnapshot;
import com.sportrent.shared.model.PriceBreakdown;
import com.sportrent.shared.model.Rental;

public interface ReportVisitor {
    void visit(Rental rental);

    void visit(EquipmentSnapshot snapshot);

    void visit(PriceBreakdown breakdown);
}
