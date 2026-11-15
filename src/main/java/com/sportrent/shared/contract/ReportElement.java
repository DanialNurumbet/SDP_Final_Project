package com.sportrent.shared.contract;

/** Элемент, который можно обойти, Visitor. Форматирования нет. */
public interface ReportElement {
    void accept(ReportVisitor visitor);
}
