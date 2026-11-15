package com.sportrent.shared.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Общая база предметов каталога. Конкретные подклассы (Bicycle, Ball, Racket)
 * и создатели EquipmentCreator реализует Дамир в пакете catalog.
 * Флага available здесь нет: доступность вычисляется по активным арендам.
 */
public abstract class Equipment {

    private final UUID id;
    private final String name;
    private final BigDecimal dailyRate;

    protected Equipment(UUID id, String name, BigDecimal dailyRate) {
        this.id = id;
        this.name = name;
        this.dailyRate = dailyRate;
    }

    public UUID getId() { return id; }

    public String getName() { return name; }

    public BigDecimal getDailyRate() { return dailyRate; }

    /** Тип фиксируется подклассом и не меняется. */
    public abstract EquipmentType getType();

    /** Подсказка по использованию, видна в каталоге. */
    public abstract String getUsageHint();
}
