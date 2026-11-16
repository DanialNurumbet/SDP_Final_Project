package com.sportrent.rental;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RentalOperationLogger implements RentalEventListener {
    private static final Logger log = LoggerFactory.getLogger(RentalOperationLogger.class);

    public RentalOperationLogger(RentalEventPublisher publisher) {
        publisher.subscribe(this);
    }

    @Override
    public void onRentalEvent(RentalEvent event) {
        log.info("Rental {}: id={}, equipmentId={}", event.type(), event.rental().id(), event.rental().equipmentId());
    }
}
