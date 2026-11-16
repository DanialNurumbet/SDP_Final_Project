package com.sportrent.rental;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class LatestRentalNotificationStore implements RentalEventListener {
    private final AtomicReference<LatestRentalNotification> latest = new AtomicReference<>();

    public LatestRentalNotificationStore(RentalEventPublisher publisher) {
        publisher.subscribe(this);
    }

    @Override
    public void onRentalEvent(RentalEvent event) {
        latest.set(LatestRentalNotification.from(event));
    }

    public Optional<LatestRentalNotification> latest() {
        return Optional.ofNullable(latest.get());
    }
}
