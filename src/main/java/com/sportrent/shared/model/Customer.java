package com.sportrent.shared.model;

/** Хранится только внутри аренды, отдельного справочника нет. */
public record Customer(String name, String phone) {
}
