package com.sportrent.reporting;

import com.sportrent.shared.contract.ReportVisitor;
import com.sportrent.shared.model.EquipmentSnapshot;
import com.sportrent.shared.model.EquipmentType;
import com.sportrent.shared.model.ExtraService;
import com.sportrent.shared.model.PriceBreakdown;
import com.sportrent.shared.model.Rental;
import com.sportrent.shared.model.RentalStatus;
import com.sportrent.shared.model.Tariff;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

public class TextReportVisitor implements ReportVisitor {

    static final String SEPARATOR = "------------------------------------------------------------";

    private final StringBuilder out = new StringBuilder();

    public String getText() {
        return out.toString();
    }

    @Override
    public void visit(Rental rental) {
        line("Аренда: " + rental.id());
        line("Статус: " + statusLabel(rental.status()));
        line("Клиент: " + rental.customer().name() + ", " + rental.customer().phone());
        line("Оформлена: " + rental.createdAt());
        line("Возвращена: " + (rental.returnedAt() == null ? "—" : rental.returnedAt()));
        line("Срок: " + rental.days() + " дн.");
        line("Тариф: " + tariffLabel(rental.tariff()));
        line("Доп. услуги: " + extrasLabel(rental.extras()));
    }

    @Override
    public void visit(EquipmentSnapshot snapshot) {
        line("Предмет на момент оформления: " + snapshot.name());
        line("  Тип: " + typeLabel(snapshot.type()));
        line("  Цена за день: " + money(snapshot.dailyRate()));
    }

    @Override
    public void visit(PriceBreakdown breakdown) {
        line("Разбивка стоимости:");
        line("  Базовая стоимость: " + money(breakdown.baseCost()));
        line("  Скидка: " + money(breakdown.discount()));
        line("  После скидки: " + money(breakdown.discountedCost()));
        breakdown.extras().forEach(e -> line("  " + extraLabel(e.extra()) + ": " + money(e.cost())));
        line("  Итого: " + money(breakdown.total()));
        line(SEPARATOR);
    }



    //formatting helpers
    private void line(String text) {
        out.append(text).append('\n');
    }

    static String money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString() + " KZT";
    }

    private static String extrasLabel(List<ExtraService> extras) {
        if (extras == null || extras.isEmpty()) {
            return "нет";
        }
        return extras.stream().map(TextReportVisitor::extraLabel).collect(Collectors.joining(", "));
    }

    private static String statusLabel(RentalStatus status) {
        return switch (status) {
            case ACTIVE -> "Активна";
            case COMPLETED -> "Завершена";
        };
    }

    private static String tariffLabel(Tariff tariff) {
        return switch (tariff) {
            case STANDARD -> "Стандартный";
            case LONG_TERM -> "Долгосрочный";
        };
    }

    private static String typeLabel(EquipmentType type) {
        return switch (type) {
            case BICYCLE -> "Велосипед";
            case BALL -> "Мяч";
            case RACKET -> "Ракетка";
        };
    }

    private static String extraLabel(ExtraService extra) {
        return switch (extra) {
            case INSURANCE -> "Страховка";
            case ACCESSORY_KIT -> "Комплект аксессуаров";
        };
    }
}
