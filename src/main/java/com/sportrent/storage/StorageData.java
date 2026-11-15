package com.sportrent.storage;

import com.sportrent.shared.model.Rental;
import java.util.List;

record StorageData(List<StoredEquipment> equipment, List<Rental> rentals) {
    StorageData {
        equipment = List.copyOf(equipment);
        rentals = List.copyOf(rentals);
    }
}
