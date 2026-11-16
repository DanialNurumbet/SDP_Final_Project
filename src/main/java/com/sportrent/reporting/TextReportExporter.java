package com.sportrent.reporting;

import com.sportrent.shared.dto.ReportSummary;
import com.sportrent.shared.model.Rental;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class TextReportExporter {

    public String export(List<Rental> rentals, ReportSummary summary, Instant generatedAt) {
        StringBuilder text = new StringBuilder();
        text.append("ОТЧЁТ ОБ АРЕНДАХ SPORTRENT\n");
        text.append("Сформирован: ").append(generatedAt).append("\n\n");

        text.append("СВОДКА\n");
        text.append("Всего аренд: ").append(summary.totalCount()).append('\n');
        text.append("Активных: ").append(summary.activeCount()).append('\n');
        text.append("Завершённых: ").append(summary.completedCount()).append('\n');
        text.append("Сумма стоимости всех оформленных аренд: ")
                .append(TextReportVisitor.money(summary.totalRentalsAmount())).append('\n');
        text.append("(Это не фактическая выручка: учёт оплаты не ведётся.)\n\n");

        text.append("АРЕНДЫ (от новых к старым)\n");
        text.append(TextReportVisitor.SEPARATOR).append('\n');
        if (rentals.isEmpty()) {
            text.append("Аренд пока нет.\n");
            return text.toString();
        }

        TextReportVisitor visitor = new TextReportVisitor();
        for (Rental rental : rentals) {
            rental.accept(visitor);
        }
        return text.append(visitor.getText()).toString();
    }
}
