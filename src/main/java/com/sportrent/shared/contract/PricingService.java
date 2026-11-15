package com.sportrent.shared.contract;

import com.sportrent.shared.model.Equipment;
import com.sportrent.shared.model.PriceBreakdown;
import com.sportrent.shared.model.RentalTerms;

/** Реализует Артем. Единственное место расчёта цены. */
public interface PricingService {

    /** @throws com.sportrent.shared.error.ValidationException если условия некорректны */

    PriceBreakdown calculate(Equipment equipment, RentalTerms terms);
}
