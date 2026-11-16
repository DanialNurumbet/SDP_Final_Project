package com.sportrent.reporting;

import com.sportrent.shared.contract.RentalQueryService;
import com.sportrent.shared.contract.RentalRepository;
import com.sportrent.shared.dto.ReportSummary;
import com.sportrent.shared.error.NotFoundException;
import com.sportrent.shared.model.Rental;
import com.sportrent.shared.model.RentalStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Facade over the reporting subsystem. Controllers talk only to this class (through
 * RentalQueryService) and never see the repository, the sorting rules, the summary
 * arithmetic or the exporter.
 */
@Service
public class RentalReportFacade implements RentalQueryService {

    private final RentalRepository rentalRepository;
    private final TextReportExporter exporter;
    private final Clock clock;

    public RentalReportFacade(RentalRepository rentalRepository, TextReportExporter exporter, Clock clock) {
        this.rentalRepository = rentalRepository;
        this.exporter = exporter;
        this.clock = clock;
    }

    @Override
    public List<Rental> findRentals(RentalStatus status) {
        return rentalRepository.findAll().stream()
                .filter(r -> status == null || r.status() == status)
                .sorted(Comparator.comparing(Rental::createdAt).reversed())
                .toList();
    }

    @Override
    public Rental getRental(UUID id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("RENTAL_NOT_FOUND", "Аренда не найдена"));
    }

    @Override
    public ReportSummary getSummary() {
        return summarize(rentalRepository.findAll());
    }

    @Override
    public String buildTextReport() {
        List<Rental> rentals = findRentals(null);
        return exporter.export(rentals, summarize(rentals), clock.instant());
    }

    private ReportSummary summarize(List<Rental> rentals) {
        int active = 0;
        BigDecimal total = BigDecimal.ZERO;
        for (Rental rental : rentals) {
            if (rental.status() == RentalStatus.ACTIVE) {
                active++;
            }
            total = total.add(rental.priceBreakdown().total());
        }
        return new ReportSummary(rentals.size(), active, rentals.size() - active,
                total.setScale(2, RoundingMode.HALF_UP));
    }
}
