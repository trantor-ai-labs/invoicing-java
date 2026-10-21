package com.acme.invoicing.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.acme.invoicing.io.OrderRepository;
import com.acme.invoicing.model.Invoice;
import com.acme.invoicing.render.MoneyFormatter;

public class CustomerSummary {
    private final OrderRepository repository;
    private final InvoiceService invoices;

    public CustomerSummary(OrderRepository repository, InvoiceService invoices) {
        this.repository = repository;
        this.invoices = invoices;
    }

    public List<String> lines() {
        Map<String, List<Invoice>> grouped = repository.findAll().stream()
                .map(invoices::invoiceFor)
                .collect(Collectors.groupingBy(
                        i -> String.format("%-22s %s", i.getCustomer().getName(), i.getCurrency().getCurrencyCode()),
                        TreeMap::new, Collectors.toList()));
        return grouped.entrySet().stream()
                .map(e -> String.format("%s %2d order(s) %s", e.getKey(), e.getValue().size(), totalOf(e.getValue())))
                .collect(Collectors.toList());
    }

    private static String totalOf(List<Invoice> group) {
        BigDecimal sum = group.stream().map(Invoice::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        Invoice first = group.get(0);
        return MoneyFormatter.format(sum, first.getCurrency(), first.getCountry());
    }
}
