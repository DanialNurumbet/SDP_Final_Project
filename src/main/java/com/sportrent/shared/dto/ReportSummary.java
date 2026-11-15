package com.sportrent.shared.dto;

import java.math.BigDecimal;

/**
 * totalRentalsAmount: сумма стоимости всех оформленных аренд.
 * Это НЕ полученная выручка: учёта оплаты в проекте нет.
 */
public record ReportSummary(
        int totalCount,
        int activeCount,
        int completedCount,
        BigDecimal totalRentalsAmount) {
}
