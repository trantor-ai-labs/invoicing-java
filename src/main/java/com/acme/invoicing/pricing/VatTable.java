package com.acme.invoicing.pricing;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class VatTable {
    private final Map<String, BigDecimal> rates = new HashMap<>();

    public VatTable() {
        rates.put("FR", new BigDecimal("0.200"));
        rates.put("DE", new BigDecimal("0.190"));
        rates.put("GB", new BigDecimal("0.200"));
        rates.put("CH", new BigDecimal("0.077"));
        rates.put("US", new BigDecimal("0.000"));
    }

    public Optional<BigDecimal> find(String country) {
        if (country == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(rates.get(country.trim().toUpperCase(Locale.ROOT)));
    }
}
