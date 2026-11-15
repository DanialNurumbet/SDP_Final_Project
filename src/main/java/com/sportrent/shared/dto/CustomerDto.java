package com.sportrent.shared.dto;

import com.sportrent.shared.model.Customer;

public record CustomerDto(String name, String phone) {

    public static CustomerDto from(Customer c) {
        return new CustomerDto(c.name(), c.phone());
    }
}
