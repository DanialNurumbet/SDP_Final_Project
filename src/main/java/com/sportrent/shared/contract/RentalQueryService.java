package com.sportrent.shared.contract;

import com.sportrent.shared.dto.ReportSummary;
import com.sportrent.shared.model.Rental;
import com.sportrent.shared.model.RentalStatus;

import java.util.List;
import java.util.UUID;

/** Реализует Даниал (через RentalReportFacade). */
public interface RentalQueryService {
    /** status == null: все аренды. Сортировка от новых к старым по createdAt. */
    List<Rental> findRentals(RentalStatus status);

    /** 404 если аренды нет. */
    Rental getRental(UUID id);

    ReportSummary getSummary();

    /** Текст отчёта (UTF-8 при отдаче). */
    String buildTextReport();
}
