package com.sportrent.reporting;

import com.sportrent.shared.contract.RentalQueryService;
import com.sportrent.shared.dto.RentalResponse;
import com.sportrent.shared.dto.ReportSummary;
import com.sportrent.shared.model.RentalStatus;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
public class RentalQueryController {

    private final RentalQueryService queryService;

    public RentalQueryController(RentalQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/api/rentals")
    public List<RentalResponse> list(@RequestParam(required = false) RentalStatus status) {
        return queryService.findRentals(status).stream().map(RentalResponse::from).toList();
    }

    @GetMapping("/api/rentals/{id}")
    public RentalResponse details(@PathVariable UUID id) {
        return RentalResponse.from(queryService.getRental(id));
    }

    @GetMapping("/api/reports/summary")
    public ReportSummary summary() {
        return queryService.getSummary();
    }

    @GetMapping("/api/reports/rentals.txt")
    public ResponseEntity<byte[]> textReport() {
        byte[] body = queryService.buildTextReport().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("sportrent-rentals.txt").build().toString())
                .body(body);
    }
}
