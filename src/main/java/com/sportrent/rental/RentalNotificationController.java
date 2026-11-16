package com.sportrent.rental;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rental-notifications")
public class RentalNotificationController {
    private final LatestRentalNotificationStore notifications;

    public RentalNotificationController(LatestRentalNotificationStore notifications) {
        this.notifications = notifications;
    }

    @GetMapping("/latest")
    public ResponseEntity<LatestRentalNotification> latest() {
        return notifications.latest().map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
