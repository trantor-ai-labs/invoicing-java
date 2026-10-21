package com.acme.invoicing.io;

import java.util.List;

import com.acme.invoicing.model.Order;

import lombok.Value;

@Value
public class ImportResult {
    List<Order> orders;
    List<String> errors;

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
