package com.sportrent.shared.contract;

import com.sportrent.shared.dto.RentalRequest;
import com.sportrent.shared.model.Rental;

import java.util.UUID;

/** Реализует Арыстанали. */
public interface RentalService {
    /** 400 при неверных данных, 404 если предмета нет, 409 если предмет занят. */
    Rental createRental(RentalRequest request);

    /** 404 если аренды нет, 409 если она уже COMPLETED. */
    Rental returnRental(UUID rentalId);
}
