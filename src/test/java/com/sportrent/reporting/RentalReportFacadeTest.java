package com.sportrent.reporting;

import com.sportrent.shared.contract.RentalRepository;
import com.sportrent.shared.dto.ReportSummary;
import com.sportrent.shared.error.NotFoundException;
import com.sportrent.shared.model.Rental;
import com.sportrent.shared.model.RentalStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RentalReportFacadeTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), ZoneOffset.UTC);

    private static class InMemoryRentalRepository implements RentalRepository {
        private final List<Rental> rentals = new ArrayList<>();

        InMemoryRentalRepository(Rental... initial) {
            rentals.addAll(List.of(initial));
        }

        @Override
        public List<Rental> findAll() {
            return List.copyOf(rentals);
        }

        @Override
        public Optional<Rental> findById(UUID id) {
            return rentals.stream().filter(r -> r.id().equals(id)).findFirst();
        }

        @Override
        public Rental save(Rental rental) {
            rentals.add(rental);
            return rental;
        }
    }

    private RentalReportFacade facadeWith(Rental... rentals) {
        return new RentalReportFacade(new InMemoryRentalRepository(rentals), new TextReportExporter(), clock);
    }

    @Test
    void summaryCountsActiveAndCompletedAndSumsAllTotals() {
        RentalReportFacade facade = facadeWith(ReportTestData.longTermBicycle(), ReportTestData.standardBall());

        ReportSummary summary = facade.getSummary();

        assertEquals(2, summary.totalCount());
        assertEquals(1, summary.activeCount());
        assertEquals(1, summary.completedCount());
        assertEquals(new BigDecimal("9500.00"), summary.totalRentalsAmount());
    }

    @Test
    void summaryOfEmptyHistoryIsZero() {
        ReportSummary summary = facadeWith().getSummary();

        assertEquals(0, summary.totalCount());
        assertEquals(new BigDecimal("0.00"), summary.totalRentalsAmount());
    }

    @Test
    void findRentalsFiltersByStatusAndSortsNewestFirst() {
        Rental older = ReportTestData.longTermBicycle();   // ACTIVE, created OLD
        Rental newer = ReportTestData.standardBall();      // COMPLETED, created NEW
        RentalReportFacade facade = facadeWith(older, newer);

        assertEquals(List.of(newer, older), facade.findRentals(null));
        assertEquals(List.of(older), facade.findRentals(RentalStatus.ACTIVE));
        assertEquals(List.of(newer), facade.findRentals(RentalStatus.COMPLETED));
    }

    @Test
    void getRentalThrowsNotFoundForUnknownId() {
        RentalReportFacade facade = facadeWith(ReportTestData.longTermBicycle());

        NotFoundException e = assertThrows(NotFoundException.class, () -> facade.getRental(UUID.randomUUID()));
        assertEquals("RENTAL_NOT_FOUND", e.getCode());
    }

    @Test
    void textReportUsesClockAndContainsRentalsNewestFirst() {
        RentalReportFacade facade = facadeWith(ReportTestData.longTermBicycle(), ReportTestData.standardBall());

        String text = facade.buildTextReport();

        assertTrue(text.contains("Сформирован: 2026-11-15T10:00:00Z"), text);
        assertTrue(text.contains("Всего аренд: 2"), text);
        assertTrue(text.indexOf("Мяч футбольный") < text.indexOf("Велосипед №1"), text);
    }
}
