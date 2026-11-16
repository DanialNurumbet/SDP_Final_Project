package com.sportrent.reporting;

import com.sportrent.shared.contract.ReportVisitor;
import com.sportrent.shared.dto.ReportSummary;
import com.sportrent.shared.model.EquipmentSnapshot;
import com.sportrent.shared.model.PriceBreakdown;
import com.sportrent.shared.model.Rental;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextReportExporterTest {

    private final TextReportExporter exporter = new TextReportExporter();

    @Test
    void exportContainsCustomerSnapshotAndExactAmounts() {
        Rental rental = ReportTestData.longTermBicycle();
        ReportSummary summary = new ReportSummary(1, 1, 0, new BigDecimal("8000.00"));

        String text = exporter.export(List.of(rental), summary, Instant.parse("2026-11-15T10:00:00Z"));

        assertTrue(text.contains("Клиент: Алексей, +77001234567"), text);
        assertTrue(text.contains("Предмет на момент оформления: Велосипед №1"), text);
        assertTrue(text.contains("Цена за день: 1000.00 KZT"), text);
        assertTrue(text.contains("Базовая стоимость: 7000.00 KZT"), text);
        assertTrue(text.contains("Скидка: 700.00 KZT"), text);
        assertTrue(text.contains("После скидки: 6300.00 KZT"), text);
        assertTrue(text.contains("Страховка: 1400.00 KZT"), text);
        assertTrue(text.contains("Комплект аксессуаров: 300.00 KZT"), text);
        assertTrue(text.contains("Итого: 8000.00 KZT"), text);
        assertTrue(text.contains("Сумма стоимости всех оформленных аренд: 8000.00 KZT"), text);
        assertTrue(text.contains("не фактическая выручка"), text);
    }

    @Test
    void rentalVisitsRentalThenSnapshotThenBreakdown() {
        List<String> calls = new ArrayList<>();
        ReportVisitor recorder = new ReportVisitor() {
            @Override
            public void visit(Rental rental) {
                calls.add("Rental");
            }

            @Override
            public void visit(EquipmentSnapshot snapshot) {
                calls.add("EquipmentSnapshot");
            }

            @Override
            public void visit(PriceBreakdown breakdown) {
                calls.add("PriceBreakdown");
            }
        };

        ReportTestData.longTermBicycle().accept(recorder);

        assertEquals(List.of("Rental", "EquipmentSnapshot", "PriceBreakdown"), calls);
    }

    @Test
    void completedRentalShowsReturnTimeAndEmptyReportIsReadable() {
        String withRental = exporter.export(List.of(ReportTestData.standardBall()),
                new ReportSummary(1, 0, 1, new BigDecimal("1500.00")), Instant.parse("2026-11-15T10:00:00Z"));
        assertTrue(withRental.contains("Статус: Завершена"), withRental);
        assertTrue(withRental.contains("Возвращена: 2026-11-12T12:00:00Z"), withRental);
        assertTrue(withRental.contains("Доп. услуги: нет"), withRental);

        String empty = exporter.export(List.of(), new ReportSummary(0, 0, 0, new BigDecimal("0.00")),
                Instant.parse("2026-11-15T10:00:00Z"));
        assertTrue(empty.contains("Аренд пока нет."), empty);
    }
}
