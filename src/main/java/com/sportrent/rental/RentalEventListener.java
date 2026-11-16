package com.sportrent.rental;

@FunctionalInterface
public interface RentalEventListener {
    void onRentalEvent(RentalEvent event);
}
