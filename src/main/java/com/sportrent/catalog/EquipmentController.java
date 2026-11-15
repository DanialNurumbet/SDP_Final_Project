package com.sportrent.catalog;

import com.sportrent.shared.dto.EquipmentResponse;
import com.sportrent.shared.model.EquipmentType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {
    private final EquipmentService service;
    private final EquipmentRequestReader requests;

    public EquipmentController(EquipmentService service, EquipmentRequestReader requests) {
        this.service = service;
        this.requests = requests;
    }

    @GetMapping
    public List<EquipmentResponse> findAll(@RequestParam(required = false) EquipmentType type,
                                           @RequestParam(required = false) Boolean available) {
        return service.findAll(type, available);
    }

    @GetMapping("/{id}")
    public EquipmentResponse findById(@PathVariable UUID id) { return service.findById(id); }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<EquipmentResponse> create(@RequestBody String body) {
        EquipmentResponse created = service.create(requests.read(body));
        return ResponseEntity.created(URI.create("/api/equipment/" + created.id())).body(created);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    public EquipmentResponse update(@PathVariable UUID id, @RequestBody String body) {
        return service.update(id, requests.read(body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
