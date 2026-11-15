package com.sportrent.catalog;

import com.sportrent.shared.dto.EquipmentRequest;
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

    public EquipmentController(EquipmentService service) { this.service = service; }

    @GetMapping
    public List<EquipmentResponse> findAll(@RequestParam(required = false) EquipmentType type,
                                           @RequestParam(required = false) Boolean available) {
        return service.findAll(type, available);
    }

    @GetMapping("/{id}")
    public EquipmentResponse findById(@PathVariable UUID id) { return service.findById(id); }

    @PostMapping
    public ResponseEntity<EquipmentResponse> create(@RequestBody EquipmentRequest request) {
        EquipmentResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/equipment/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public EquipmentResponse update(@PathVariable UUID id, @RequestBody EquipmentRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
