package com.sportrent.rental;

import com.sportrent.shared.dto.RentalRequest;
import com.sportrent.shared.dto.RentalResponse;
import com.sportrent.shared.model.Rental;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/rentals")
public class RentalCommandController {
    private final java.util.Optional<RentalCommandService> service;

    public RentalCommandController(java.util.Optional<RentalCommandService> service) {
        this.service = service;
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<RentalResponse> create(@RequestBody RentalRequest request) {
        RentalResponse response = RentalResponse.from(requireService().createRental(request));
        return ResponseEntity.created(URI.create("/api/rentals/" + response.id())).body(response);
    }

    @PostMapping("/{id}/return")
    public RentalResponse returnRental(@PathVariable UUID id) {
        return RentalResponse.from(requireService().returnRental(id));
    }

    private RentalCommandService requireService() {
        return service.orElseThrow(() -> new IllegalStateException("Модуль расчёта стоимости ещё не подключён."));
    }
}
