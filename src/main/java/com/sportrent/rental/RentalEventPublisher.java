package com.sportrent.rental;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class RentalEventPublisher {
    private final List<RentalEventListener> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(RentalEventListener listener) {
        listeners.add(listener);
    }

    public void publish(RentalEvent event) {
        listeners.forEach(listener -> listener.onRentalEvent(event));
    }
}
