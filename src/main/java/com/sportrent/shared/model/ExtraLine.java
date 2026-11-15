package com.sportrent.shared.model;

import java.math.BigDecimal;

/** Одна строка разбивки: дополнительная услуга и её стоимость. */
public record ExtraLine(ExtraService extra, BigDecimal cost) {
}
